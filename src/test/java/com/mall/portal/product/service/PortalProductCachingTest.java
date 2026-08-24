package com.mall.portal.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.cache.CacheNames;
import com.mall.common.exception.BusinessException;
import com.mall.config.RedisCacheConfig;
import com.mall.order.mapper.OmsOrderItemMapper;
import com.mall.order.mapper.OmsOrderMapper;
import com.mall.portal.product.service.impl.PortalProductDetailCacheServiceImpl;
import com.mall.portal.product.service.impl.PortalProductServiceImpl;
import com.mall.portal.product.vo.PortalProductAttributeVO;
import com.mall.portal.product.vo.PortalProductDetailCacheVO;
import com.mall.portal.product.vo.PortalSkuCacheVO;
import com.mall.product.dto.ProductAttributeUpdateDTO;
import com.mall.product.dto.ProductUpdateDTO;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsProductAttribute;
import com.mall.product.entity.PmsProductAttributeValue;
import com.mall.product.entity.PmsProductCategory;
import com.mall.product.entity.PmsSkuStock;
import com.mall.product.mapper.PmsProductAttributeMapper;
import com.mall.product.mapper.PmsProductAttributeValueMapper;
import com.mall.product.mapper.PmsProductCategoryMapper;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.mapper.PmsSkuStockMapper;
import com.mall.product.service.MybatisTestSupport;
import com.mall.product.service.impl.PmsProductAttributeServiceImpl;
import com.mall.product.service.impl.PmsProductAttributeValueServiceImpl;
import com.mall.product.service.impl.PmsProductServiceImpl;
import com.mall.product.service.impl.PmsSkuStockServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortalProductCachingTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                PmsProduct.class,
                PmsProductAttribute.class,
                PmsProductAttributeValue.class,
                PmsProductCategory.class,
                PmsSkuStock.class
        );
    }

    @Mock
    private PmsProductMapper productMapper;

    @Mock
    private PmsProductAttributeMapper attributeMapper;

    @Mock
    private PmsProductAttributeValueMapper attributeValueMapper;

    @Mock
    private PmsSkuStockMapper skuStockMapper;

    @Mock
    private PmsProductCategoryMapper categoryMapper;

    @Mock
    private OmsOrderMapper orderMapper;

    @Mock
    private OmsOrderItemMapper orderItemMapper;

    @Test
    void repeatedDetailRequestShouldCacheStaticDataButRefreshStock() {
        when(productMapper.selectOne(any())).thenReturn(product());
        when(attributeValueMapper.selectList(any())).thenReturn(List.of(
                attributeValue()
        ));
        when(attributeMapper.selectList(any())).thenReturn(List.of(
                attribute()
        ));

        List<String> skuSelects = new ArrayList<>();
        List<List<PmsSkuStock>> skuResults = List.of(
                List.of(staticSku()),
                List.of(stockSku(10, 3)),
                List.of(stockSku(10, 6))
        );

        when(skuStockMapper.selectList(any())).thenAnswer(invocation -> {
            LambdaQueryWrapper<PmsSkuStock> query = invocation.getArgument(0);
            skuSelects.add(query.getSqlSelect());
            return skuResults.get(skuSelects.size() - 1);
        });

        PortalProductDetailCacheService cacheService = cachingProxy(
                new PortalProductDetailCacheServiceImpl(
                        productMapper,
                        attributeMapper,
                        attributeValueMapper,
                        skuStockMapper
                )
        );

        PortalProductServiceImpl portalProductService =
                new PortalProductServiceImpl(
                        productMapper,
                        skuStockMapper,
                        categoryMapper,
                        orderMapper,
                        orderItemMapper,
                        cacheService
                );

        var first = portalProductService.getDetail(10L);
        var second = portalProductService.getDetail(10L);

        assertEquals("缓存商品", first.name());
        assertEquals(7, first.skus().getFirst().availableStock());
        assertEquals(4, second.skus().getFirst().availableStock());
        verify(productMapper, times(1)).selectOne(any());
        verify(attributeValueMapper, times(1)).selectList(any());
        verify(attributeMapper, times(1)).selectList(any());
        verify(skuStockMapper, times(3)).selectList(any());

        assertEquals("id,price,pic,spec_data", normalize(skuSelects.get(0)));
        assertEquals(
                "id,stock,locked_stock",
                normalize(skuSelects.get(1))
        );
        assertEquals(
                "id,stock,locked_stock",
                normalize(skuSelects.get(2))
        );
    }

    @Test
    void mismatchedCachedAndCurrentSkuIdsShouldReportDataConflict() {
        PortalProductDetailCacheService cacheService =
                mock(PortalProductDetailCacheService.class);
        when(cacheService.getStaticDetail(10L)).thenReturn(detailCache());
        when(skuStockMapper.selectList(any())).thenReturn(List.of(
                stockSku(999L, 10, 0)
        ));

        PortalProductServiceImpl portalProductService =
                new PortalProductServiceImpl(
                        productMapper,
                        skuStockMapper,
                        categoryMapper,
                        orderMapper,
                        orderItemMapper,
                        cacheService
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> portalProductService.getDetail(10L)
        );

        assertSame(ErrorCode.DATA_CONFLICT, exception.getErrorCode());
    }

    @Test
    void staticDetailShouldRejectMissingOrUnpublishedProduct() {
        when(productMapper.selectOne(any())).thenReturn(null);

        PortalProductDetailCacheServiceImpl cacheService =
                new PortalProductDetailCacheServiceImpl(
                        productMapper,
                        attributeMapper,
                        attributeValueMapper,
                        skuStockMapper
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cacheService.getStaticDetail(10L)
        );

        assertSame(ErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(
                attributeMapper,
                attributeValueMapper,
                skuStockMapper
        );
    }

    @Test
    void cacheVoShouldBeImmutableAndContainNoInventoryFields() {
        List<PortalSkuCacheVO> skus = new ArrayList<>();
        skus.add(cachedSku());

        PortalProductDetailCacheVO detail = new PortalProductDetailCacheVO(
                10L,
                1L,
                2L,
                "缓存商品",
                "副标题",
                new BigDecimal("99.00"),
                new BigDecimal("109.00"),
                "件",
                "product.jpg",
                null,
                null,
                null,
                null,
                null,
                null,
                skus
        );
        skus.clear();

        assertTrue(detail.albumPics().isEmpty());
        assertTrue(detail.attributes().isEmpty());
        assertEquals(1, detail.skus().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> detail.skus().add(cachedSku())
        );

        List<String> componentNames = Arrays.stream(
                        PortalSkuCacheVO.class.getRecordComponents()
                )
                .map(component -> component.getName())
                .toList();

        assertEquals(List.of("id", "price", "pic", "specData"), componentNames);
        assertFalse(componentNames.contains("stock"));
        assertFalse(componentNames.contains("lockedStock"));
        assertFalse(componentNames.contains("availableStock"));
    }

    @Test
    void redisProductDetailCacheShouldUseDedicatedConfiguration() {
        RedisCacheConfig redisConfig = new RedisCacheConfig();
        RedisCacheConfiguration defaults =
                redisConfig.redisCacheConfiguration();
        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.builder(mock(RedisCacheWriter.class))
                        .cacheDefaults(defaults);

        redisConfig.redisCacheManagerBuilderCustomizer(
                defaults,
                new ObjectMapper()
        ).customize(builder);

        RedisCacheConfiguration categoryConfiguration = builder
                .getCacheConfigurationFor(CacheNames.PORTAL_CATEGORY_TREE)
                .orElseThrow();
        RedisCacheConfiguration productConfiguration = builder
                .getCacheConfigurationFor(CacheNames.PORTAL_PRODUCT_DETAIL)
                .orElseThrow();
        RedisCacheManager manager = builder.build();

        assertTrue(manager.isTransactionAware());
        assertEquals(Duration.ofMinutes(30), categoryConfiguration.getTtl());
        assertEquals(Duration.ofMinutes(10), productConfiguration.getTtl());
        assertFalse(productConfiguration.getAllowCacheNullValues());

        Object restored = productConfiguration
                .getValueSerializationPair()
                .read(productConfiguration
                        .getValueSerializationPair()
                        .write(detailCache()));

        assertInstanceOf(PortalProductDetailCacheVO.class, restored);
        assertEquals(10L, ((PortalProductDetailCacheVO) restored).id());
    }

    @Test
    void managementStaticDataWritesShouldEvictProductDetailCache()
            throws NoSuchMethodException {

        assertProductEviction(
                PmsProductServiceImpl.class.getMethod(
                        "update",
                        Long.class,
                        ProductUpdateDTO.class
                )
        );
        assertProductEviction(
                PmsProductServiceImpl.class.getMethod(
                        "updatePublishStatus",
                        Long.class,
                        Integer.class
                )
        );
        assertProductEviction(
                PmsProductServiceImpl.class.getMethod(
                        "delete",
                        Long.class
                )
        );
        assertProductEviction(
                PmsProductAttributeValueServiceImpl.class.getMethod(
                        "replace",
                        Long.class,
                        List.class
                )
        );
        assertProductEviction(
                PmsSkuStockServiceImpl.class.getMethod(
                        "replace",
                        Long.class,
                        List.class
                )
        );

        CacheEvict attributeEviction =
                PmsProductAttributeServiceImpl.class.getMethod(
                                "update",
                                Long.class,
                                ProductAttributeUpdateDTO.class
                        )
                        .getAnnotation(CacheEvict.class);

        assertNotNull(attributeEviction);
        assertArrayEquals(
                new String[]{CacheNames.PORTAL_PRODUCT_DETAIL},
                attributeEviction.cacheNames()
        );
        assertTrue(attributeEviction.allEntries());
    }

    private PortalProductDetailCacheService cachingProxy(
            PortalProductDetailCacheService target) {

        CacheInterceptor interceptor = new CacheInterceptor();
        interceptor.setCacheManager(
                new ConcurrentMapCacheManager(
                        CacheNames.PORTAL_PRODUCT_DETAIL
                )
        );
        interceptor.setCacheOperationSource(
                new AnnotationCacheOperationSource()
        );
        interceptor.afterPropertiesSet();
        interceptor.afterSingletonsInstantiated();

        ProxyFactory factory = new ProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAdvice(interceptor);
        return (PortalProductDetailCacheService) factory.getProxy();
    }

    private void assertProductEviction(java.lang.reflect.Method method) {
        CacheEvict eviction = method.getAnnotation(CacheEvict.class);

        assertNotNull(eviction);
        assertArrayEquals(
                new String[]{CacheNames.PORTAL_PRODUCT_DETAIL},
                eviction.cacheNames()
        );
        assertEquals("#productId", eviction.key());
        assertFalse(eviction.allEntries());
    }

    private String normalize(String sqlSelect) {
        return sqlSelect.replace(" ", "").toLowerCase();
    }

    private PmsProduct product() {
        PmsProduct product = new PmsProduct();
        product.setId(10L);
        product.setBrandId(1L);
        product.setProductCategoryId(2L);
        product.setName("缓存商品");
        product.setSubTitle("副标题");
        product.setPrice(new BigDecimal("99.00"));
        product.setOriginalPrice(new BigDecimal("109.00"));
        product.setUnit("件");
        product.setPic("product.jpg");
        product.setAlbumPics("a.jpg,b.jpg");
        product.setPublishStatus(1);
        return product;
    }

    private PmsProductAttribute attribute() {
        PmsProductAttribute attribute = new PmsProductAttribute();
        attribute.setId(20L);
        attribute.setName("颜色");
        return attribute;
    }

    private PmsProductAttributeValue attributeValue() {
        PmsProductAttributeValue value = new PmsProductAttributeValue();
        value.setProductId(10L);
        value.setProductAttributeId(20L);
        value.setValue("黑色");
        return value;
    }

    private PmsSkuStock staticSku() {
        PmsSkuStock sku = new PmsSkuStock();
        sku.setId(100L);
        sku.setPrice(new BigDecimal("99.00"));
        sku.setPic("sku.jpg");
        sku.setSpecData("[{\"name\":\"颜色\",\"value\":\"黑色\"}]");
        return sku;
    }

    private PmsSkuStock stockSku(int stock, int lockedStock) {
        return stockSku(100L, stock, lockedStock);
    }

    private PmsSkuStock stockSku(
            Long skuId,
            int stock,
            int lockedStock) {

        PmsSkuStock sku = new PmsSkuStock();
        sku.setId(skuId);
        sku.setStock(stock);
        sku.setLockedStock(lockedStock);
        return sku;
    }

    private PortalSkuCacheVO cachedSku() {
        return new PortalSkuCacheVO(
                100L,
                new BigDecimal("99.00"),
                "sku.jpg",
                "[{\"name\":\"颜色\",\"value\":\"黑色\"}]"
        );
    }

    private PortalProductDetailCacheVO detailCache() {
        return new PortalProductDetailCacheVO(
                10L,
                1L,
                2L,
                "缓存商品",
                "副标题",
                new BigDecimal("99.00"),
                new BigDecimal("109.00"),
                "件",
                "product.jpg",
                List.of("a.jpg", "b.jpg"),
                null,
                null,
                null,
                null,
                List.of(new PortalProductAttributeVO(20L, "颜色", "黑色")),
                List.of(cachedSku())
        );
    }
}
