package com.mall.product.service;

import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.ProductAttributeValueItemDTO;
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
import com.mall.product.service.impl.PmsProductAttributeValueServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PmsProductAttributeValueServiceImplTest {

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
    private PmsProductMapper productMapper;

    @Mock
    private PmsProductAttributeMapper attributeMapper;

    @Mock
    private PmsProductAttributeValueMapper attributeValueMapper;

    @Mock
    private PmsProductCategoryAttributeRelationMapper relationMapper;

    @Mock
    private PmsSkuStockMapper skuStockMapper;

    private PmsProductAttributeValueServiceImpl attributeValueService;

    @BeforeEach
    void setUp() {
        attributeValueService = new PmsProductAttributeValueServiceImpl(
                productMapper,
                attributeMapper,
                attributeValueMapper,
                relationMapper,
                skuStockMapper
        );
    }

    @Test
    void listShouldReturnProductAttributeValuesInMapperOrder() {
        when(productMapper.selectById(10L)).thenReturn(product(10L));
        when(attributeValueMapper.selectList(any())).thenReturn(List.of(
                attributeValue(1L, 10L, 2L, "黑色,白色"),
                attributeValue(2L, 10L, 3L, "16GB")
        ));

        var result = attributeValueService.listByProductId(10L);

        assertEquals(2, result.size());
        assertEquals(List.of(2L, 3L), result.stream()
                .map(value -> value.getProductAttributeId())
                .toList());
        assertEquals("黑色,白色", result.getFirst().getValue());
    }

    @Test
    void replaceShouldValidateTrimInsertAndReturnNewValues() {
        mockReplaceValidation(2L, attribute(2L, 0, 0, null, 0));
        when(attributeValueMapper.insert(any(PmsProductAttributeValue.class)))
                .thenAnswer(invocation -> {
                    PmsProductAttributeValue entity = invocation.getArgument(0);
                    entity.setId(100L);
                    return 1;
                });
        when(attributeValueMapper.selectList(any())).thenReturn(List.of(
                attributeValue(100L, 10L, 2L, "黑色,白色")
        ));

        var result = attributeValueService.replace(
                10L,
                List.of(item(2L, "  黑色,白色  "))
        );

        ArgumentCaptor<PmsProductAttributeValue> captor =
                ArgumentCaptor.forClass(PmsProductAttributeValue.class);
        verify(attributeValueMapper).delete(any());
        verify(attributeValueMapper).insert(captor.capture());
        assertEquals(10L, captor.getValue().getProductId());
        assertEquals(2L, captor.getValue().getProductAttributeId());
        assertEquals("黑色,白色", captor.getValue().getValue());
        assertEquals("黑色,白色", result.getFirst().getValue());
    }

    @Test
    void replaceWithEmptyListShouldClearAllValues() {
        when(productMapper.selectById(10L)).thenReturn(product(10L));
        when(skuStockMapper.selectCount(any())).thenReturn(0L);
        when(attributeValueMapper.selectList(any())).thenReturn(List.of());

        var result = attributeValueService.replace(10L, List.of());

        assertTrue(result.isEmpty());
        verify(attributeValueMapper).delete(any());
        verify(attributeValueMapper, never()).insert(
                any(PmsProductAttributeValue.class)
        );
        verifyNoInteractions(attributeMapper, relationMapper);
    }

    @Test
    void replaceShouldRejectDuplicateAttributeIdsBeforeDeleting() {
        when(productMapper.selectById(10L)).thenReturn(product(10L));
        when(skuStockMapper.selectCount(any())).thenReturn(0L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> attributeValueService.replace(
                        10L,
                        List.of(item(2L, "黑色"), item(2L, "白色"))
                )
        );

        assertSame(
                ErrorCode.ATTRIBUTE_SELECTION_INVALID,
                exception.getErrorCode()
        );
        verify(attributeValueMapper, never()).delete(any());
    }

    @Test
    void replaceShouldRejectAttributeNotRelatedToProductCategory() {
        when(productMapper.selectById(10L)).thenReturn(product(10L));
        when(skuStockMapper.selectCount(any())).thenReturn(0L);
        when(relationMapper.selectList(any())).thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> attributeValueService.replace(
                        10L,
                        List.of(item(404L, "未知"))
                )
        );

        assertSame(
                ErrorCode.ATTRIBUTE_SELECTION_INVALID,
                exception.getErrorCode()
        );
        verify(attributeValueMapper, never()).delete(any());
        verifyNoInteractions(attributeMapper);
    }

    @Test
    void replaceShouldRejectValueOutsidePredefinedList() {
        PmsProductAttribute color = attribute(
                2L,
                0,
                1,
                "黑色,白色",
                0
        );
        mockReplaceValidation(2L, color);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> attributeValueService.replace(
                        10L,
                        List.of(item(2L, "金色"))
                )
        );

        assertSame(
                ErrorCode.ATTRIBUTE_SELECTION_INVALID,
                exception.getErrorCode()
        );
        verify(attributeValueMapper, never()).delete(any());
    }

    @Test
    void replaceShouldRejectMissingProductWithoutReadingSkuOrWriting() {
        when(productMapper.selectById(404L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> attributeValueService.replace(
                        404L,
                        List.of(item(2L, "黑色"))
                )
        );

        assertSame(ErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(
                skuStockMapper,
                attributeMapper,
                relationMapper,
                attributeValueMapper
        );
    }

    @Test
    void replaceShouldRollBackTransactionWhenDatabaseWriteFails() {
        PlatformTransactionManager transactionManager =
                mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any()))
                .thenReturn(transactionStatus);

        ProxyFactory proxyFactory = new ProxyFactory(attributeValueService);
        proxyFactory.addAdvice(new TransactionInterceptor(
                transactionManager,
                new AnnotationTransactionAttributeSource()
        ));
        PmsProductAttributeValueService transactionalService =
                (PmsProductAttributeValueService) proxyFactory.getProxy();

        mockReplaceValidation(
                List.of(2L, 3L),
                List.of(
                        attribute(2L, 0, 0, null, 0),
                        attribute(3L, 0, 0, null, 0)
                )
        );
        when(attributeValueMapper.insert(any(PmsProductAttributeValue.class)))
                .thenReturn(1)
                .thenThrow(new DataIntegrityViolationException("write failed"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> transactionalService.replace(
                        10L,
                        List.of(item(2L, "黑色"), item(3L, "16GB"))
                )
        );

        assertSame(ErrorCode.DATA_CONFLICT, exception.getErrorCode());
        verify(attributeValueMapper).delete(any());
        verify(attributeValueMapper, times(2)).insert(
                any(PmsProductAttributeValue.class)
        );
        verify(transactionManager).rollback(transactionStatus);
        verify(transactionManager, never()).commit(any());
    }

    @Test
    void replaceShouldReturnConflictWhenProductAlreadyHasSku() {
        when(productMapper.selectById(10L)).thenReturn(product(10L));
        when(skuStockMapper.selectCount(any())).thenReturn(2L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> attributeValueService.replace(
                        10L,
                        List.of(item(2L, "白色"))
                )
        );

        assertSkuAttributeConflict(exception);
        verify(attributeValueMapper, never()).delete(any());
        verifyNoInteractions(attributeMapper, relationMapper);
    }

    @Test
    void clearShouldReturnConflictWhenProductAlreadyHasSku() {
        when(productMapper.selectById(10L)).thenReturn(product(10L));
        when(skuStockMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> attributeValueService.replace(10L, List.of())
        );

        assertSkuAttributeConflict(exception);
        verify(attributeValueMapper, never()).delete(any());
    }

    private void mockReplaceValidation(
            Long attributeId,
            PmsProductAttribute attribute) {

        mockReplaceValidation(
                List.of(attributeId),
                List.of(attribute)
        );
    }

    private void mockReplaceValidation(
            List<Long> attributeIds,
            List<PmsProductAttribute> attributes) {

        when(productMapper.selectById(10L)).thenReturn(product(10L));
        when(skuStockMapper.selectCount(any())).thenReturn(0L);
        when(relationMapper.selectList(any())).thenReturn(
                attributeIds.stream()
                        .map(id -> relation(20L, id))
                        .toList()
        );
        when(attributeMapper.selectBatchIds(attributeIds))
                .thenReturn(attributes);
    }

    private void assertSkuAttributeConflict(BusinessException exception) {
        assertSame(
                ErrorCode.PRODUCT_HAS_SKU_ATTRIBUTE_UPDATE_FORBIDDEN,
                exception.getErrorCode()
        );
        assertEquals(HttpStatus.CONFLICT, exception.getErrorCode().getHttpStatus());
        assertEquals(40927, exception.getErrorCode().getCode());
        assertTrue(exception.getMessage().contains("先清空SKU"));
    }

    private ProductAttributeValueItemDTO item(Long attributeId, String value) {
        ProductAttributeValueItemDTO item = new ProductAttributeValueItemDTO();
        item.setProductAttributeId(attributeId);
        item.setValue(value);
        return item;
    }

    private PmsProduct product(Long id) {
        PmsProduct product = new PmsProduct();
        product.setId(id);
        product.setProductCategoryId(20L);
        product.setPublishStatus(0);
        return product;
    }

    private PmsProductAttribute attribute(
            Long id,
            int type,
            int inputType,
            String inputList,
            int handAddStatus) {

        PmsProductAttribute attribute = new PmsProductAttribute();
        attribute.setId(id);
        attribute.setName("属性" + id);
        attribute.setType(type);
        attribute.setInputType(inputType);
        attribute.setInputList(inputList);
        attribute.setHandAddStatus(handAddStatus);
        return attribute;
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

    private PmsProductAttributeValue attributeValue(
            Long id,
            Long productId,
            Long attributeId,
            String value) {

        PmsProductAttributeValue attributeValue =
                new PmsProductAttributeValue();
        attributeValue.setId(id);
        attributeValue.setProductId(productId);
        attributeValue.setProductAttributeId(attributeId);
        attributeValue.setValue(value);
        return attributeValue;
    }
}
