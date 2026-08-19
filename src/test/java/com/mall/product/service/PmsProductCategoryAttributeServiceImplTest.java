package com.mall.product.service;

import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.product.entity.PmsProductAttribute;
import com.mall.product.entity.PmsProductCategory;
import com.mall.product.entity.PmsProductCategoryAttributeRelation;
import com.mall.product.mapper.PmsProductAttributeMapper;
import com.mall.product.mapper.PmsProductCategoryAttributeRelationMapper;
import com.mall.product.mapper.PmsProductCategoryMapper;
import com.mall.product.service.impl.PmsProductCategoryAttributeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PmsProductCategoryAttributeServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                PmsProductAttribute.class,
                PmsProductCategory.class,
                PmsProductCategoryAttributeRelation.class
        );
    }

    @Mock
    private PmsProductCategoryMapper categoryMapper;

    @Mock
    private PmsProductAttributeMapper attributeMapper;

    @Mock
    private PmsProductCategoryAttributeRelationMapper relationMapper;

    private PmsProductCategoryAttributeServiceImpl relationService;

    @BeforeEach
    void setUp() {
        relationService = new PmsProductCategoryAttributeServiceImpl(
                categoryMapper,
                attributeMapper,
                relationMapper
        );
    }

    @Test
    void replaceShouldDeduplicateAndInsertEachRelationCompatibly() {
        when(categoryMapper.selectById(20L)).thenReturn(category(20L, 1));
        when(attributeMapper.selectCount(any())).thenReturn(2L);
        when(relationMapper.insert(
                any(PmsProductCategoryAttributeRelation.class)
        )).thenReturn(1);
        when(relationMapper.selectList(any())).thenReturn(List.of(
                relation(1L, 20L, 2L),
                relation(2L, 20L, 3L)
        ));
        when(attributeMapper.selectList(any())).thenReturn(List.of(
                attribute(2L, "颜色"),
                attribute(3L, "容量")
        ));

        var result = relationService.replace(20L, List.of(2L, 2L, 3L));

        assertEquals(List.of("颜色", "容量"), result.stream()
                .map(value -> value.name())
                .toList());
        ArgumentCaptor<PmsProductCategoryAttributeRelation> captor =
                ArgumentCaptor.forClass(
                        PmsProductCategoryAttributeRelation.class
                );
        verify(relationMapper, times(2)).insert(captor.capture());
        assertEquals(List.of(2L, 3L), captor.getAllValues().stream()
                .map(PmsProductCategoryAttributeRelation::getProductAttributeId)
                .toList());
        assertTrue(captor.getAllValues().stream().allMatch(
                relation -> relation.getProductCategoryId().equals(20L)
        ));
    }

    @Test
    void replaceWithEmptyListShouldClearAllRelations() {
        when(categoryMapper.selectById(20L)).thenReturn(category(20L, 1));
        when(relationMapper.selectList(any())).thenReturn(List.of());

        var result = relationService.replace(20L, List.of());

        assertTrue(result.isEmpty());
        verify(relationMapper).delete(any());
        verify(attributeMapper, never()).selectCount(any());
        verify(relationMapper, never()).insert(
                any(PmsProductCategoryAttributeRelation.class)
        );
    }

    @Test
    void replaceShouldValidateAllAttributesBeforeDeletingOldRelations() {
        when(categoryMapper.selectById(20L)).thenReturn(category(20L, 1));
        when(attributeMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> relationService.replace(20L, List.of(2L, 404L))
        );

        assertSame(
                ErrorCode.ATTRIBUTE_SELECTION_INVALID,
                exception.getErrorCode()
        );
        verify(relationMapper, never()).delete(any());
        verify(relationMapper, never()).insert(
                any(PmsProductCategoryAttributeRelation.class)
        );
    }

    @Test
    void replaceShouldRejectFirstLevelCategoryWithoutChangingRelations() {
        when(categoryMapper.selectById(10L)).thenReturn(category(10L, 0));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> relationService.replace(10L, List.of(2L))
        );

        assertSame(
                ErrorCode.CATEGORY_ATTRIBUTE_ONLY_LEAF_ALLOWED,
                exception.getErrorCode()
        );
        verifyNoInteractions(attributeMapper, relationMapper);
    }

    @Test
    void replaceShouldRollBackTransactionOnInsertConflict() {
        PlatformTransactionManager transactionManager =
                mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any()))
                .thenReturn(transactionStatus);

        ProxyFactory proxyFactory = new ProxyFactory(relationService);
        proxyFactory.addAdvice(new TransactionInterceptor(
                transactionManager,
                new AnnotationTransactionAttributeSource()
        ));
        PmsProductCategoryAttributeService transactionalService =
                (PmsProductCategoryAttributeService) proxyFactory.getProxy();

        when(categoryMapper.selectById(20L)).thenReturn(category(20L, 1));
        when(attributeMapper.selectCount(any())).thenReturn(2L);
        when(relationMapper.insert(
                any(PmsProductCategoryAttributeRelation.class)
        )).thenReturn(1)
                .thenThrow(new DuplicateKeyException("duplicate relation"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> transactionalService.replace(20L, List.of(2L, 3L))
        );

        assertSame(ErrorCode.DATA_CONFLICT, exception.getErrorCode());
        assertNotNull(exception.getCause());
        verify(relationMapper).delete(any());
        verify(relationMapper, times(2)).insert(
                any(PmsProductCategoryAttributeRelation.class)
        );
        verify(transactionManager).rollback(transactionStatus);
        verify(transactionManager, never()).commit(any());
    }

    @Test
    void listShouldRejectMissingCategory() {
        when(categoryMapper.selectById(404L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> relationService.listByCategoryId(404L)
        );

        assertSame(ErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(attributeMapper, relationMapper);
    }

    private PmsProductCategory category(Long id, int level) {
        PmsProductCategory category = new PmsProductCategory();
        category.setId(id);
        category.setParentId(level == 0 ? 0L : 10L);
        category.setName("分类" + id);
        category.setLevel(level);
        return category;
    }

    private PmsProductAttribute attribute(Long id, String name) {
        PmsProductAttribute attribute = new PmsProductAttribute();
        attribute.setId(id);
        attribute.setProductAttributeCategoryId(1L);
        attribute.setName(name);
        attribute.setSelectType(0);
        attribute.setInputType(0);
        attribute.setSort(0);
        attribute.setFilterType(0);
        attribute.setSearchType(0);
        attribute.setRelatedStatus(0);
        attribute.setHandAddStatus(0);
        attribute.setType(0);
        return attribute;
    }

    private PmsProductCategoryAttributeRelation relation(
            Long id,
            Long categoryId,
            Long attributeId) {

        PmsProductCategoryAttributeRelation relation =
                new PmsProductCategoryAttributeRelation();
        relation.setId(id);
        relation.setProductCategoryId(categoryId);
        relation.setProductAttributeId(attributeId);
        return relation;
    }
}
