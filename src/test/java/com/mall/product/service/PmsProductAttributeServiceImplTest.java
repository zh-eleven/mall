package com.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.ProductAttributeCreateDTO;
import com.mall.product.dto.ProductAttributeUpdateDTO;
import com.mall.product.entity.PmsProductAttribute;
import com.mall.product.entity.PmsProductAttributeCategory;
import com.mall.product.mapper.PmsProductAttributeCategoryMapper;
import com.mall.product.mapper.PmsProductAttributeMapper;
import com.mall.product.service.impl.PmsProductAttributeServiceImpl;
import com.mall.product.vo.ProductAttributeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PmsProductAttributeServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MybatisTestSupport.initializeTableInfo(
                PmsProductAttribute.class,
                PmsProductAttributeCategory.class
        );
    }

    @Mock
    private PmsProductAttributeMapper attributeMapper;

    @Mock
    private PmsProductAttributeCategoryMapper categoryMapper;

    private PmsProductAttributeServiceImpl attributeService;

    @BeforeEach
    void setUp() {
        attributeService = new PmsProductAttributeServiceImpl(
                attributeMapper,
                categoryMapper
        );
    }

    @Test
    void createShouldTrimValuesSetDefaultsAndIncreaseAttributeCount() {
        ProductAttributeCreateDTO dto = createDto(10L, "  颜色  ", 0);
        when(categoryMapper.selectById(10L)).thenReturn(category(10L));
        when(attributeMapper.selectCount(any())).thenReturn(0L);
        when(attributeMapper.insert(any(PmsProductAttribute.class)))
                .thenAnswer(invocation -> {
                    PmsProductAttribute value = invocation.getArgument(0);
                    value.setId(100L);
                    return 1;
                });
        when(attributeMapper.selectById(100L))
                .thenAnswer(invocation -> attribute(100L, 10L, "颜色", 0));
        when(categoryMapper.update(isNull(), any())).thenReturn(1);

        ProductAttributeVO result = attributeService.create(dto);

        ArgumentCaptor<PmsProductAttribute> attributeCaptor =
                ArgumentCaptor.forClass(PmsProductAttribute.class);
        verify(attributeMapper).insert(attributeCaptor.capture());
        PmsProductAttribute inserted = attributeCaptor.getValue();
        assertEquals("颜色", inserted.getName());
        assertEquals(0, inserted.getSelectType());
        assertEquals(0, inserted.getInputType());
        assertNull(inserted.getInputList());
        assertEquals(0, inserted.getSort());
        assertEquals(0, inserted.getFilterType());
        assertEquals(0, inserted.getSearchType());
        assertEquals(0, inserted.getRelatedStatus());
        assertEquals(0, inserted.getHandAddStatus());
        assertEquals(100L, result.id());

        assertCountSqlContains("attribute_count = attribute_count + 1");
    }

    @Test
    void createShouldNormalizeCommaSeparatedInputList() {
        ProductAttributeCreateDTO dto = createDto(10L, "容量", 1);
        dto.setInputType(1);
        dto.setInputList(" 64G, 128G ,256G ");
        when(categoryMapper.selectById(10L)).thenReturn(category(10L));
        when(attributeMapper.selectCount(any())).thenReturn(0L);
        when(attributeMapper.insert(any(PmsProductAttribute.class)))
                .thenAnswer(invocation -> {
                    PmsProductAttribute value = invocation.getArgument(0);
                    value.setId(101L);
                    return 1;
                });
        when(attributeMapper.selectById(101L))
                .thenReturn(attribute(101L, 10L, "容量", 1));
        when(categoryMapper.update(isNull(), any())).thenReturn(1);

        attributeService.create(dto);

        ArgumentCaptor<PmsProductAttribute> captor =
                ArgumentCaptor.forClass(PmsProductAttribute.class);
        verify(attributeMapper).insert(captor.capture());
        assertEquals("64G,128G,256G", captor.getValue().getInputList());
    }

    @Test
    void createShouldRejectBlankItemInInputList() {
        ProductAttributeCreateDTO dto = createDto(10L, "容量", 1);
        dto.setInputType(1);
        dto.setInputList("64G, ,256G");
        when(categoryMapper.selectById(10L)).thenReturn(category(10L));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> attributeService.create(dto)
        );

        assertSame(
                ErrorCode.ATTRIBUTE_INPUT_LIST_REQUIRED,
                exception.getErrorCode()
        );
        verify(attributeMapper, never()).insert(any(PmsProductAttribute.class));
    }

    @Test
    void createShouldRejectDuplicateNameInSameCategoryAndType() {
        ProductAttributeCreateDTO dto = createDto(10L, "颜色", 0);
        when(categoryMapper.selectById(10L)).thenReturn(category(10L));
        when(attributeMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> attributeService.create(dto)
        );

        assertSame(
                ErrorCode.ATTRIBUTE_NAME_ALREADY_EXISTS,
                exception.getErrorCode()
        );
        verify(attributeMapper, never()).insert(any(PmsProductAttribute.class));
    }

    @Test
    void createShouldTranslateOnlyDuplicateKeyAsDuplicateName() {
        ProductAttributeCreateDTO dto = createDto(10L, "颜色", 0);
        when(categoryMapper.selectById(10L)).thenReturn(category(10L));
        when(attributeMapper.selectCount(any())).thenReturn(0L);
        when(attributeMapper.insert(any(PmsProductAttribute.class)))
                .thenThrow(new DuplicateKeyException("duplicate key"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> attributeService.create(dto)
        );

        assertSame(
                ErrorCode.ATTRIBUTE_NAME_ALREADY_EXISTS,
                exception.getErrorCode()
        );
    }

    @Test
    void createShouldNotMisreportOtherIntegrityFailureAsDuplicateName() {
        ProductAttributeCreateDTO dto = createDto(10L, "颜色", 0);
        DataIntegrityViolationException databaseFailure =
                new DataIntegrityViolationException("not-null violation");
        when(categoryMapper.selectById(10L)).thenReturn(category(10L));
        when(attributeMapper.selectCount(any())).thenReturn(0L);
        when(attributeMapper.insert(any(PmsProductAttribute.class)))
                .thenThrow(databaseFailure);

        DataIntegrityViolationException actual = assertThrows(
                DataIntegrityViolationException.class,
                () -> attributeService.create(dto)
        );

        assertSame(databaseFailure, actual);
    }

    @Test
    void updateShouldMoveCountBetweenCategories() {
        PmsProductAttribute current = attribute(20L, 10L, "颜色", 0);
        PmsProductAttribute updated = attribute(20L, 11L, "颜色", 0);
        ProductAttributeUpdateDTO dto = new ProductAttributeUpdateDTO();
        dto.setProductAttributeCategoryId(11L);
        when(attributeMapper.selectById(20L)).thenReturn(current, updated);
        when(categoryMapper.selectById(11L)).thenReturn(category(11L));
        when(attributeMapper.selectCount(any())).thenReturn(0L);
        when(attributeMapper.update(isNull(), any())).thenReturn(1);
        when(categoryMapper.update(isNull(), any())).thenReturn(1);

        ProductAttributeVO result = attributeService.update(20L, dto);

        assertEquals(11L, result.productAttributeCategoryId());
        List<String> countSql = capturedCountSql();
        assertTrue(countSql.stream().anyMatch(sql -> sql.contains(
                "GREATEST(attribute_count - 1, 0)"
        )));
        assertTrue(countSql.stream().anyMatch(sql -> sql.contains(
                "attribute_count = attribute_count + 1"
        )));
    }

    @Test
    void updateShouldMoveCountFromAttributeToParameter() {
        PmsProductAttribute current = attribute(20L, 10L, "颜色", 0);
        PmsProductAttribute updated = attribute(20L, 10L, "颜色", 1);
        ProductAttributeUpdateDTO dto = new ProductAttributeUpdateDTO();
        dto.setType(1);
        when(attributeMapper.selectById(20L)).thenReturn(current, updated);
        when(categoryMapper.selectById(10L)).thenReturn(category(10L));
        when(attributeMapper.selectCount(any())).thenReturn(0L);
        when(attributeMapper.update(isNull(), any())).thenReturn(1);
        when(categoryMapper.update(isNull(), any())).thenReturn(1);

        attributeService.update(20L, dto);

        List<String> countSql = capturedCountSql();
        assertTrue(countSql.stream().anyMatch(sql -> sql.contains(
                "GREATEST(attribute_count - 1, 0)"
        )));
        assertTrue(countSql.stream().anyMatch(sql -> sql.contains(
                "param_count = param_count + 1"
        )));
    }

    @Test
    void updateWithoutCategoryOrTypeChangeShouldNotTouchCounts() {
        PmsProductAttribute current = attribute(20L, 10L, "颜色", 0);
        ProductAttributeUpdateDTO dto = new ProductAttributeUpdateDTO();
        dto.setName("  外观颜色  ");
        when(attributeMapper.selectById(20L)).thenReturn(current);
        when(categoryMapper.selectById(10L)).thenReturn(category(10L));
        when(attributeMapper.selectCount(any())).thenReturn(0L);
        when(attributeMapper.update(isNull(), any())).thenReturn(1);

        attributeService.update(20L, dto);

        verify(categoryMapper, never()).update(isNull(), any());
    }

    @Test
    void deleteShouldDecreaseParameterCount() {
        when(attributeMapper.selectById(20L))
                .thenReturn(attribute(20L, 10L, "材质", 1));
        when(attributeMapper.deleteById(20L)).thenReturn(1);
        when(categoryMapper.update(isNull(), any())).thenReturn(1);

        attributeService.delete(20L);

        verify(attributeMapper).deleteById(20L);
        assertCountSqlContains("GREATEST(param_count - 1, 0)");
    }

    @Test
    void deleteShouldNotChangeCountWhenAttributeDoesNotExist() {
        when(attributeMapper.selectById(404L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> attributeService.delete(404L)
        );

        assertSame(ErrorCode.ATTRIBUTE_NOT_FOUND, exception.getErrorCode());
        verify(attributeMapper, never()).deleteById(anyLong());
        verify(categoryMapper, never()).update(isNull(), any());
    }

    @Test
    void deleteShouldNotChangeCountWhenDeleteAffectedNoRows() {
        when(attributeMapper.selectById(20L))
                .thenReturn(attribute(20L, 10L, "材质", 1));
        when(attributeMapper.deleteById(20L)).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> attributeService.delete(20L)
        );

        assertSame(ErrorCode.ATTRIBUTE_NOT_FOUND, exception.getErrorCode());
        verify(categoryMapper, never()).update(isNull(), any());
    }

    @Test
    void createShouldFailTransactionWhenCategoryCountCannotBeUpdated() {
        ProductAttributeCreateDTO dto = createDto(10L, "颜色", 0);
        when(categoryMapper.selectById(10L)).thenReturn(category(10L));
        when(attributeMapper.selectCount(any())).thenReturn(0L);
        when(attributeMapper.insert(any(PmsProductAttribute.class)))
                .thenAnswer(invocation -> {
                    PmsProductAttribute value = invocation.getArgument(0);
                    value.setId(100L);
                    return 1;
                });
        when(categoryMapper.update(isNull(), any())).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> attributeService.create(dto)
        );

        assertSame(
                ErrorCode.ATTRIBUTE_CATEGORY_NOT_FOUND,
                exception.getErrorCode()
        );
        verify(attributeMapper, never()).selectById(100L);
    }

    private ProductAttributeCreateDTO createDto(
            Long categoryId,
            String name,
            int type) {

        ProductAttributeCreateDTO dto = new ProductAttributeCreateDTO();
        dto.setProductAttributeCategoryId(categoryId);
        dto.setName(name);
        dto.setType(type);
        return dto;
    }

    private PmsProductAttribute attribute(
            Long id,
            Long categoryId,
            String name,
            int type) {

        PmsProductAttribute attribute = new PmsProductAttribute();
        attribute.setId(id);
        attribute.setProductAttributeCategoryId(categoryId);
        attribute.setName(name);
        attribute.setSelectType(0);
        attribute.setInputType(0);
        attribute.setSort(0);
        attribute.setFilterType(0);
        attribute.setSearchType(0);
        attribute.setRelatedStatus(0);
        attribute.setHandAddStatus(0);
        attribute.setType(type);
        return attribute;
    }

    private PmsProductAttributeCategory category(Long id) {
        PmsProductAttributeCategory category =
                new PmsProductAttributeCategory();
        category.setId(id);
        category.setName("属性分类" + id);
        category.setAttributeCount(0);
        category.setParamCount(0);
        return category;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<String> capturedCountSql() {
        ArgumentCaptor<LambdaUpdateWrapper> captor =
                ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(categoryMapper, atLeastOnce()).update(isNull(), captor.capture());
        return captor.getAllValues().stream()
                .map(LambdaUpdateWrapper::getSqlSet)
                .toList();
    }

    private void assertCountSqlContains(String expected) {
        assertTrue(capturedCountSql().stream()
                .anyMatch(sql -> sql.contains(expected)));
    }
}
