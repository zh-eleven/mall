package com.mall.portal.product.service;

import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.portal.product.service.impl.PortalProductDetailCacheServiceImpl;
import com.mall.product.cache.PortalProductNotFoundCache;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsProductAttributeValue;
import com.mall.product.entity.PmsSkuStock;
import com.mall.product.mapper.PmsProductAttributeMapper;
import com.mall.product.mapper.PmsProductAttributeValueMapper;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.mapper.PmsSkuStockMapper;
import com.mall.product.service.MybatisTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortalProductCachingTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                PmsProduct.class,
                PmsProductAttributeValue.class,
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
    private PortalProductNotFoundCache productNotFoundCache;

    private PortalProductDetailCacheServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PortalProductDetailCacheServiceImpl(
                productMapper,
                attributeMapper,
                attributeValueMapper,
                skuStockMapper,
                productNotFoundCache
        );
    }

    @Test
    void knownMissingProductShouldSkipDatabaseLookup() {
        when(productNotFoundCache.contains(10L))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getStaticDetail(10L)
        );

        assertSame(
                ErrorCode.PRODUCT_NOT_FOUND,
                exception.getErrorCode()
        );
        verifyNoInteractions(
                productMapper,
                attributeMapper,
                attributeValueMapper,
                skuStockMapper
        );
    }

    @Test
    void missingPublishedProductShouldStoreNotFoundMarker() {
        when(productNotFoundCache.contains(10L))
                .thenReturn(false);
        when(productMapper.selectOne(any()))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getStaticDetail(10L)
        );

        assertSame(
                ErrorCode.PRODUCT_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(productNotFoundCache).put(10L);
        verifyNoInteractions(
                attributeMapper,
                attributeValueMapper,
                skuStockMapper
        );
    }

    @Test
    void existingPublishedProductShouldNotStoreNotFoundMarker() {
        PmsProduct product = new PmsProduct();
        product.setId(10L);
        product.setBrandId(20L);
        product.setProductCategoryId(30L);
        product.setName("测试手机");
        product.setPrice(new BigDecimal("1999.00"));

        when(productNotFoundCache.contains(10L))
                .thenReturn(false);
        when(productMapper.selectOne(any()))
                .thenReturn(product);
        when(attributeValueMapper.selectList(any()))
                .thenReturn(List.of());
        when(skuStockMapper.selectList(any()))
                .thenReturn(List.of());

        var result = service.getStaticDetail(10L);

        assertEquals(10L, result.id());
        assertEquals("测试手机", result.name());
        verify(productNotFoundCache, never()).put(10L);
        verifyNoInteractions(attributeMapper);
    }
}
