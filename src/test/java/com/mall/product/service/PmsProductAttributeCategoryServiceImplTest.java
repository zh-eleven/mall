package com.mall.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.PageResult;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.ProductAttributeCategoryCreateDTO;
import com.mall.product.entity.PmsProductAttributeCategory;
import com.mall.product.mapper.PmsProductAttributeCategoryMapper;
import com.mall.product.service.impl.PmsProductAttributeCategoryServiceImpl;
import com.mall.product.vo.ProductAttributeCategoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PmsProductAttributeCategoryServiceImplTest {

    @Mock
    private PmsProductAttributeCategoryMapper categoryMapper;

    private PmsProductAttributeCategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService =
                new PmsProductAttributeCategoryServiceImpl(categoryMapper);
    }

    @Test
    void createShouldTrimNameAndSetDefaultCounts() {
        ProductAttributeCategoryCreateDTO dto =
                new ProductAttributeCategoryCreateDTO();
        dto.setName("  手机属性  ");

        when(categoryMapper.selectCount(any())).thenReturn(0L);
        when(categoryMapper.insert(
                any(PmsProductAttributeCategory.class)
        ))
                .thenAnswer(invocation -> {
                    PmsProductAttributeCategory category =
                            invocation.getArgument(0);
                    category.setId(1L);
                    return 1;
                });

        PmsProductAttributeCategory saved =
                category(1L, "手机属性", 0, 0);

        when(categoryMapper.selectById(1L)).thenReturn(saved);

        ProductAttributeCategoryVO result =
                categoryService.create(dto);

        ArgumentCaptor<PmsProductAttributeCategory> captor =
                ArgumentCaptor.forClass(
                        PmsProductAttributeCategory.class
                );

        verify(categoryMapper).insert(captor.capture());

        assertEquals("手机属性", captor.getValue().getName());
        assertEquals(0, captor.getValue().getAttributeCount());
        assertEquals(0, captor.getValue().getParamCount());
        assertEquals(1L, result.id());
    }

    @Test
    void createShouldRejectDuplicateName() {
        ProductAttributeCategoryCreateDTO dto =
                new ProductAttributeCategoryCreateDTO();
        dto.setName("手机属性");

        when(categoryMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.create(dto)
        );

        assertSame(
                ErrorCode.ATTRIBUTE_CATEGORY_NAME_ALREADY_EXISTS,
                exception.getErrorCode()
        );

        verify(categoryMapper, never()).insert(
                any(PmsProductAttributeCategory.class)
        );
    }

    @Test
    void pageShouldReturnConvertedResult() {
        Page<PmsProductAttributeCategory> databasePage =
                new Page<>(1, 10, 1);

        databasePage.setRecords(List.of(
                category(1L, "手机属性", 2, 3)
        ));

        when(categoryMapper.selectPage(any(Page.class), any()))
                .thenReturn(databasePage);

        PageResult<ProductAttributeCategoryVO> result =
                categoryService.page(" 手机 ", 1, 10);

        assertEquals(1, result.total());
        assertEquals(1, result.list().size());
        assertEquals("手机属性", result.list().get(0).name());
        assertEquals(2, result.list().get(0).attributeCount());
        assertEquals(3, result.list().get(0).paramCount());
    }

    @Test
    void getByIdShouldFailWhenCategoryDoesNotExist() {
        when(categoryMapper.selectById(404L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.getById(404L)
        );

        assertSame(
                ErrorCode.ATTRIBUTE_CATEGORY_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    private PmsProductAttributeCategory category(
            Long id,
            String name,
            Integer attributeCount,
            Integer paramCount) {

        PmsProductAttributeCategory category =
                new PmsProductAttributeCategory();

        category.setId(id);
        category.setName(name);
        category.setAttributeCount(attributeCount);
        category.setParamCount(paramCount);

        return category;
    }
}