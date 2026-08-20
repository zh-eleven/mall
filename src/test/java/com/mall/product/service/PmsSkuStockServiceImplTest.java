package com.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.SkuSpecItemDTO;
import com.mall.product.dto.SkuStockItemDTO;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsProductAttribute;
import com.mall.product.entity.PmsProductAttributeValue;
import com.mall.product.entity.PmsProductCategoryAttributeRelation;
import com.mall.product.entity.PmsSkuStock;
import com.mall.product.mapper.PmsProductAttributeMapper;
import com.mall.product.mapper.PmsProductAttributeValueMapper;
import com.mall.product.mapper.PmsProductCategoryAttributeRelationMapper;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.mapper.PmsSkuStockMapper;
import com.mall.product.service.impl.PmsSkuStockServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PmsSkuStockServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                PmsProduct.class,
                PmsProductAttribute.class,
                PmsProductAttributeValue.class,
                PmsProductCategoryAttributeRelation.class,
                PmsSkuStock.class
        );
    }

    @Mock
    private PmsSkuStockMapper skuStockMapper;

    @Mock
    private PmsProductMapper productMapper;

    @Mock
    private PmsProductAttributeMapper attributeMapper;

    @Mock
    private PmsProductAttributeValueMapper attributeValueMapper;

    @Mock
    private PmsProductCategoryAttributeRelationMapper relationMapper;

    private PmsSkuStockServiceImpl skuStockService;

    @BeforeEach
    void setUp() {
        skuStockService = new PmsSkuStockServiceImpl(
                skuStockMapper,
                productMapper,
                attributeMapper,
                attributeValueMapper,
                relationMapper,
                new ObjectMapper()
        );
    }

    @Test
    void listShouldReturnSkuRowsForExistingProduct() {
        when(productMapper.selectById(10L)).thenReturn(product(10L, 0));
        when(skuStockMapper.selectList(any())).thenReturn(List.of(
                skuStock(1L, "SKU-BLACK", "2=黑色", 5)
        ));

        var result = skuStockService.listByProductId(10L);

        assertEquals(1, result.size());
        assertEquals("SKU-BLACK", result.getFirst().getSkuCode());
        assertEquals(5, result.getFirst().getAvailableStock());
    }

    @Test
    void replaceShouldCreateSkuNormalizeCodeAndReturnCreatedRows() {
        PmsProductAttribute color = attribute(2L, "颜色", 0);
        PmsProductAttributeValue selected =
                attributeValue(2L, "黑色,白色");
        PmsSkuStock stored = skuStock(100L, "SKU-BLACK", "2=黑色", 5);
        mockSuccessfulReplace(List.of(color), List.of(selected), List.of(stored));

        var result = skuStockService.replace(
                10L,
                List.of(sku("  sku-black  ", "99.00", 5, spec(2L, "黑色")))
        );

        ArgumentCaptor<PmsSkuStock> captor =
                ArgumentCaptor.forClass(PmsSkuStock.class);
        verify(skuStockMapper).delete(any());
        verify(skuStockMapper).insert(captor.capture());
        PmsSkuStock inserted = captor.getValue();
        assertEquals("SKU-BLACK", inserted.getSkuCode());
        assertEquals("2=黑色", inserted.getSpecKey());
        assertTrue(inserted.getSpecData().contains("\"name\":\"颜色\""));
        assertEquals(0, inserted.getLockedStock());
        assertEquals("SKU-BLACK", result.getFirst().getSkuCode());
    }

    @Test
    void replaceShouldRejectDuplicateSkuCodesAfterNormalization() {
        mockSpecContext(
                product(10L, 0),
                List.of(attribute(2L, "颜色", 0)),
                List.of(attributeValue(2L, "黑色,白色"))
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> skuStockService.replace(10L, List.of(
                        sku("sku-1", "99.00", 1, spec(2L, "黑色")),
                        sku("SKU-1", "109.00", 2, spec(2L, "白色"))
                ))
        );

        assertSame(
                ErrorCode.SKU_CODE_ALREADY_EXISTS,
                exception.getErrorCode()
        );
        verify(skuStockMapper, never()).delete(any());
    }

    @Test
    void replaceShouldRejectSkuCodeUsedByAnotherProduct() {
        mockSpecContext(
                product(10L, 0),
                List.of(attribute(2L, "颜色", 0)),
                List.of(attributeValue(2L, "黑色"))
        );
        when(skuStockMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> skuStockService.replace(
                        10L,
                        List.of(sku("sku-1", "99.00", 1, spec(2L, "黑色")))
                )
        );

        assertSame(
                ErrorCode.SKU_CODE_ALREADY_EXISTS,
                exception.getErrorCode()
        );
        verify(skuStockMapper, never()).delete(any());
    }

    @Test
    void replaceShouldRejectDuplicateSpecCombinationRegardlessOfOrder() {
        mockSpecContext(
                product(10L, 0),
                List.of(
                        attribute(2L, "颜色", 0),
                        attribute(3L, "容量", 0)
                ),
                List.of(
                        attributeValue(2L, "黑色"),
                        attributeValue(3L, "16GB")
                )
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> skuStockService.replace(10L, List.of(
                        sku(
                                "SKU-1",
                                "99.00",
                                1,
                                spec(2L, "黑色"),
                                spec(3L, "16GB")
                        ),
                        sku(
                                "SKU-2",
                                "109.00",
                                1,
                                spec(3L, "16GB"),
                                spec(2L, "黑色")
                        )
                ))
        );

        assertSame(
                ErrorCode.SKU_SPEC_ALREADY_EXISTS,
                exception.getErrorCode()
        );
        verify(skuStockMapper, never()).delete(any());
    }

    @Test
    void replaceShouldRejectParameterAttributeAsSkuSpec() {
        mockSpecContext(
                product(10L, 0),
                List.of(attribute(4L, "材质", 1)),
                List.of(attributeValue(4L, "金属"))
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> skuStockService.replace(
                        10L,
                        List.of(sku("SKU-1", "99.00", 1, spec(4L, "金属")))
                )
        );

        assertSame(ErrorCode.SKU_SPEC_INVALID, exception.getErrorCode());
        verify(skuStockMapper, never()).delete(any());
    }

    @Test
    void replaceShouldRejectSpecValueNotSelectedForProduct() {
        mockSpecContext(
                product(10L, 0),
                List.of(attribute(2L, "颜色", 0)),
                List.of(attributeValue(2L, "黑色"))
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> skuStockService.replace(
                        10L,
                        List.of(sku("SKU-1", "99.00", 1, spec(2L, "白色")))
                )
        );

        assertSame(ErrorCode.SKU_SPEC_INVALID, exception.getErrorCode());
        verify(skuStockMapper, never()).delete(any());
    }

    @Test
    void replaceShouldRejectPublishedProductBeforeReadingSpecs() {
        when(productMapper.selectById(10L)).thenReturn(product(10L, 1));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> skuStockService.replace(
                        10L,
                        List.of(sku("SKU-1", "99.00", 1, spec(2L, "黑色")))
                )
        );

        assertSame(
                ErrorCode.PRODUCT_PUBLISHED_SKU_UPDATE_FORBIDDEN,
                exception.getErrorCode()
        );
        verifyNoInteractions(
                attributeMapper,
                attributeValueMapper,
                relationMapper,
                skuStockMapper
        );
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void replaceShouldSynchronizeMinimumPriceAndTotalStock() {
        List<PmsProductAttribute> attributes = List.of(
                attribute(2L, "颜色", 0)
        );
        List<PmsProductAttributeValue> values = List.of(
                attributeValue(2L, "黑色,白色")
        );
        mockSuccessfulReplace(attributes, values, List.of());

        skuStockService.replace(10L, List.of(
                sku("SKU-BLACK", "99.00", 5, spec(2L, "黑色")),
                sku("SKU-WHITE", "89.00", 7, spec(2L, "白色"))
        ));

        ArgumentCaptor<LambdaUpdateWrapper> captor =
                ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(productMapper).update(isNull(), captor.capture());
        LambdaUpdateWrapper<PmsProduct> wrapper = captor.getValue();
        assertTrue(wrapper.getSqlSet().contains("stock"));
        assertTrue(wrapper.getSqlSet().contains("price"));
        Map<String, Object> parameters = wrapper.getParamNameValuePairs();
        assertTrue(parameters.containsValue(12));
        assertTrue(parameters.values().stream().anyMatch(
                value -> value instanceof BigDecimal price
                        && price.compareTo(new BigDecimal("89.00")) == 0
        ));
    }

    @Test
    void replaceShouldRollBackSkusWhenProductSummaryUpdateFails() {
        PlatformTransactionManager transactionManager =
                mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any()))
                .thenReturn(transactionStatus);

        ProxyFactory proxyFactory = new ProxyFactory(skuStockService);
        proxyFactory.addAdvice(new TransactionInterceptor(
                transactionManager,
                new AnnotationTransactionAttributeSource()
        ));
        PmsSkuStockService transactionalService =
                (PmsSkuStockService) proxyFactory.getProxy();

        mockSpecContext(
                product(10L, 0),
                List.of(attribute(2L, "颜色", 0)),
                List.of(attributeValue(2L, "黑色,白色"))
        );
        when(skuStockMapper.selectCount(any())).thenReturn(0L);
        when(skuStockMapper.insert(any(PmsSkuStock.class))).thenReturn(1);
        when(productMapper.update(isNull(), any())).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> transactionalService.replace(10L, List.of(
                        sku("SKU-BLACK", "99.00", 5, spec(2L, "黑色")),
                        sku("SKU-WHITE", "89.00", 7, spec(2L, "白色"))
                ))
        );

        assertSame(ErrorCode.DATA_CONFLICT, exception.getErrorCode());
        verify(skuStockMapper).delete(any());
        verify(skuStockMapper, times(2)).insert(any(PmsSkuStock.class));
        verify(productMapper).update(isNull(), any());
        verify(transactionManager).rollback(transactionStatus);
        verify(transactionManager, never()).commit(any());
    }

    private void mockSuccessfulReplace(
            List<PmsProductAttribute> attributes,
            List<PmsProductAttributeValue> values,
            List<PmsSkuStock> storedSkus) {

        mockSpecContext(product(10L, 0), attributes, values);
        when(skuStockMapper.selectCount(any())).thenReturn(0L);
        when(skuStockMapper.insert(any(PmsSkuStock.class))).thenReturn(1);
        when(productMapper.update(isNull(), any())).thenReturn(1);
        when(skuStockMapper.selectList(any())).thenReturn(storedSkus);
    }

    private void mockSpecContext(
            PmsProduct product,
            List<PmsProductAttribute> attributes,
            List<PmsProductAttributeValue> values) {

        when(productMapper.selectById(10L)).thenReturn(product);
        when(relationMapper.selectList(any())).thenReturn(
                attributes.stream()
                        .map(attribute -> relation(
                                product.getProductCategoryId(),
                                attribute.getId()
                        ))
                        .toList()
        );
        when(attributeMapper.selectBatchIds(anyCollection()))
                .thenReturn(attributes);
        when(attributeValueMapper.selectList(any())).thenReturn(values);
    }

    private PmsProduct product(Long id, int publishStatus) {
        PmsProduct product = new PmsProduct();
        product.setId(id);
        product.setProductCategoryId(20L);
        product.setPublishStatus(publishStatus);
        product.setPrice(new BigDecimal("100.00"));
        product.setOriginalPrice(new BigDecimal("500.00"));
        product.setStock(0);
        return product;
    }

    private PmsProductAttribute attribute(Long id, String name, int type) {
        PmsProductAttribute attribute = new PmsProductAttribute();
        attribute.setId(id);
        attribute.setName(name);
        attribute.setType(type);
        return attribute;
    }

    private PmsProductAttributeValue attributeValue(
            Long attributeId,
            String value) {

        PmsProductAttributeValue attributeValue =
                new PmsProductAttributeValue();
        attributeValue.setProductId(10L);
        attributeValue.setProductAttributeId(attributeId);
        attributeValue.setValue(value);
        return attributeValue;
    }

    private PmsProductCategoryAttributeRelation relation(
            Long categoryId,
            Long attributeId) {

        PmsProductCategoryAttributeRelation relation =
                new PmsProductCategoryAttributeRelation();
        relation.setProductCategoryId(categoryId);
        relation.setProductAttributeId(attributeId);
        return relation;
    }

    private SkuStockItemDTO sku(
            String skuCode,
            String price,
            int stock,
            SkuSpecItemDTO... specs) {

        SkuStockItemDTO sku = new SkuStockItemDTO();
        sku.setSkuCode(skuCode);
        sku.setPrice(new BigDecimal(price));
        sku.setStock(stock);
        sku.setSpecs(List.of(specs));
        return sku;
    }

    private SkuSpecItemDTO spec(Long attributeId, String value) {
        SkuSpecItemDTO spec = new SkuSpecItemDTO();
        spec.setProductAttributeId(attributeId);
        spec.setValue(value);
        return spec;
    }

    private PmsSkuStock skuStock(
            Long id,
            String skuCode,
            String specKey,
            int stock) {

        PmsSkuStock sku = new PmsSkuStock();
        sku.setId(id);
        sku.setProductId(10L);
        sku.setSkuCode(skuCode);
        sku.setPrice(new BigDecimal("99.00"));
        sku.setStock(stock);
        sku.setLockedStock(0);
        sku.setLowStock(0);
        sku.setSpecKey(specKey);
        sku.setSpecData("[]");
        return sku;
    }
}
