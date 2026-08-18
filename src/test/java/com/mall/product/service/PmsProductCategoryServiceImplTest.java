package com.mall.product.service;

import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.ProductCategoryCreateDTO;
import com.mall.product.dto.ProductCategoryUpdateDTO;
import com.mall.product.entity.PmsProductCategory;
import com.mall.product.mapper.PmsProductCategoryMapper;
import com.mall.product.service.impl.PmsProductCategoryServiceImpl;
import com.mall.product.vo.ProductCategoryTreeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PmsProductCategoryServiceImplTest {

    @Mock
    private PmsProductCategoryMapper categoryMapper;

    private PmsProductCategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService =
                new PmsProductCategoryServiceImpl(categoryMapper);
    }

    @Test
    void treeShouldAssembleFirstAndSecondLevelCategories() {
        PmsProductCategory phones = category(
                1L, 0L, 0, "手机"
        );
        PmsProductCategory android = category(
                11L, 1L, 1, "安卓手机"
        );
        PmsProductCategory ios = category(
                12L, 1L, 1, "苹果手机"
        );
        PmsProductCategory computers = category(
                2L, 0L, 0, "电脑"
        );
        PmsProductCategory laptops = category(
                21L, 2L, 1, "笔记本"
        );

        when(categoryMapper.selectList(any()))
                .thenReturn(List.of(
                        phones,
                        android,
                        ios,
                        computers,
                        laptops
                ));

        List<ProductCategoryTreeVO> result = categoryService.tree();

        assertEquals(2, result.size());
        assertEquals("手机", result.get(0).name());
        assertEquals(
                List.of("安卓手机", "苹果手机"),
                result.get(0).children().stream()
                        .map(ProductCategoryTreeVO::name)
                        .toList()
        );
        assertTrue(result.get(0).children().stream()
                .allMatch(child -> child.children().isEmpty()));
        assertEquals("电脑", result.get(1).name());
        assertEquals(1, result.get(1).children().size());
        assertEquals("笔记本", result.get(1).children().get(0).name());
    }

    @Test
    void createFirstLevelShouldSetLevelAndDefaults() {
        ProductCategoryCreateDTO dto = createDto(0L, "  手机  ");
        dto.setProductUnit("   ");
        dto.setIcon("  phone.png  ");
        dto.setKeywords("  通讯  ");
        dto.setDescription("  手机分类  ");

        when(categoryMapper.selectCount(any())).thenReturn(0L);
        when(categoryMapper.insert(any(PmsProductCategory.class)))
                .thenAnswer(invocation -> {
                    PmsProductCategory inserted =
                            invocation.getArgument(0);
                    inserted.setId(100L);
                    return 1;
                });
        when(categoryMapper.selectById(100L))
                .thenAnswer(invocation -> category(
                        100L, 0L, 0, "手机"
                ));

        ProductCategoryTreeVO result = categoryService.create(dto);

        ArgumentCaptor<PmsProductCategory> captor =
                ArgumentCaptor.forClass(PmsProductCategory.class);
        verify(categoryMapper).insert(captor.capture());
        PmsProductCategory inserted = captor.getValue();

        assertEquals(0L, inserted.getParentId());
        assertEquals(0, inserted.getLevel());
        assertEquals("手机", inserted.getName());
        assertEquals(0, inserted.getProductCount());
        assertNull(inserted.getProductUnit());
        assertEquals(0, inserted.getNavStatus());
        assertEquals(1, inserted.getShowStatus());
        assertEquals(0, inserted.getSort());
        assertEquals("phone.png", inserted.getIcon());
        assertEquals("通讯", inserted.getKeywords());
        assertEquals("手机分类", inserted.getDescription());
        assertEquals(100L, result.id());
        assertEquals(0, result.level());
        assertTrue(result.children().isEmpty());
    }

    @Test
    void createSecondLevelShouldSetLevelOne() {
        ProductCategoryCreateDTO dto = createDto(10L, "智能手机");
        PmsProductCategory parent = category(
                10L, 0L, 0, "手机"
        );
        PmsProductCategory saved = category(
                101L, 10L, 1, "智能手机"
        );

        when(categoryMapper.selectById(10L)).thenReturn(parent);
        when(categoryMapper.selectCount(any())).thenReturn(0L);
        when(categoryMapper.insert(any(PmsProductCategory.class)))
                .thenAnswer(invocation -> {
                    PmsProductCategory inserted =
                            invocation.getArgument(0);
                    inserted.setId(101L);
                    return 1;
                });
        when(categoryMapper.selectById(101L)).thenReturn(saved);

        ProductCategoryTreeVO result = categoryService.create(dto);

        ArgumentCaptor<PmsProductCategory> captor =
                ArgumentCaptor.forClass(PmsProductCategory.class);
        verify(categoryMapper).insert(captor.capture());

        assertEquals(10L, captor.getValue().getParentId());
        assertEquals(1, captor.getValue().getLevel());
        assertEquals(1, result.level());
        assertEquals(10L, result.parentId());
    }

    @Test
    void createShouldFailWhenParentDoesNotExist() {
        ProductCategoryCreateDTO dto = createDto(99L, "智能手机");
        when(categoryMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.create(dto)
        );

        assertSame(ErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
        verify(categoryMapper, never()).selectCount(any());
        verify(categoryMapper, never())
                .insert(any(PmsProductCategory.class));
    }

    @Test
    void createShouldRejectThirdLevelCategory() {
        ProductCategoryCreateDTO dto = createDto(20L, "游戏手机");
        when(categoryMapper.selectById(20L))
                .thenReturn(category(20L, 10L, 1, "智能手机"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.create(dto)
        );

        assertSame(
                ErrorCode.CATEGORY_PARENT_INVALID,
                exception.getErrorCode()
        );
        verify(categoryMapper, never()).selectCount(any());
        verify(categoryMapper, never())
                .insert(any(PmsProductCategory.class));
    }

    @Test
    void createShouldRejectDuplicateNameUnderSameParent() {
        ProductCategoryCreateDTO dto = createDto(0L, "手机");
        when(categoryMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.create(dto)
        );

        assertSame(
                ErrorCode.CATEGORY_NAME_ALREADY_EXISTS,
                exception.getErrorCode()
        );
        verify(categoryMapper, never())
                .insert(any(PmsProductCategory.class));
    }

    @Test
    void createShouldAllowSameNameUnderDifferentParent() {
        ProductCategoryCreateDTO dto = createDto(2L, "配件");
        PmsProductCategory targetParent = category(
                2L, 0L, 0, "电脑"
        );
        PmsProductCategory saved = category(
                102L, 2L, 1, "配件"
        );

        when(categoryMapper.selectById(2L)).thenReturn(targetParent);
        when(categoryMapper.selectCount(any())).thenReturn(0L);
        when(categoryMapper.insert(any(PmsProductCategory.class)))
                .thenAnswer(invocation -> {
                    PmsProductCategory inserted =
                            invocation.getArgument(0);
                    inserted.setId(102L);
                    return 1;
                });
        when(categoryMapper.selectById(102L)).thenReturn(saved);

        ProductCategoryTreeVO result = categoryService.create(dto);

        ArgumentCaptor<PmsProductCategory> captor =
                ArgumentCaptor.forClass(PmsProductCategory.class);
        verify(categoryMapper).insert(captor.capture());

        assertEquals(2L, captor.getValue().getParentId());
        assertEquals("配件", captor.getValue().getName());
        assertEquals(2L, result.parentId());
        assertEquals("配件", result.name());
        verify(categoryMapper).selectCount(any());
    }

    @Test
    void getByIdShouldFailWhenCategoryDoesNotExist() {
        when(categoryMapper.selectById(404L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.getById(404L)
        );

        assertSame(ErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateShouldApplyOnlyProvidedFields() {
        PmsProductCategory existing = category(
                11L, 1L, 1, "旧名称"
        );
        existing.setProductUnit("件");
        existing.setNavStatus(0);
        existing.setShowStatus(1);
        existing.setSort(5);
        existing.setIcon("old.png");
        existing.setKeywords("旧关键词");
        existing.setDescription("旧描述");

        ProductCategoryUpdateDTO dto = new ProductCategoryUpdateDTO();
        dto.setName("  新名称  ");
        dto.setSort(20);
        dto.setDescription("   ");

        when(categoryMapper.selectById(11L)).thenReturn(existing);
        when(categoryMapper.selectById(1L))
                .thenReturn(category(1L, 0L, 0, "手机"));
        when(categoryMapper.selectCount(any())).thenReturn(0L);

        ProductCategoryTreeVO result =
                categoryService.update(11L, dto);

        ArgumentCaptor<PmsProductCategory> captor =
                ArgumentCaptor.forClass(PmsProductCategory.class);
        verify(categoryMapper).updateById(captor.capture());
        PmsProductCategory updated = captor.getValue();

        assertEquals(11L, updated.getId());
        assertEquals(1L, updated.getParentId());
        assertEquals(1, updated.getLevel());
        assertEquals("新名称", updated.getName());
        assertEquals(20, updated.getSort());
        assertNull(updated.getDescription());
        assertEquals("件", updated.getProductUnit());
        assertEquals(0, updated.getNavStatus());
        assertEquals(1, updated.getShowStatus());
        assertEquals("old.png", updated.getIcon());
        assertEquals("旧关键词", updated.getKeywords());
        assertEquals("新名称", result.name());
        assertEquals(20, result.sort());
    }

    @Test
    void updateShouldRejectCategoryAsItsOwnParent() {
        PmsProductCategory existing = category(
                10L, 0L, 0, "手机"
        );
        ProductCategoryUpdateDTO dto = new ProductCategoryUpdateDTO();
        dto.setParentId(10L);

        when(categoryMapper.selectById(10L)).thenReturn(existing);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.update(10L, dto)
        );

        assertSame(
                ErrorCode.CATEGORY_PARENT_INVALID,
                exception.getErrorCode()
        );
        verify(categoryMapper, never()).selectCount(any());
        verify(categoryMapper, never())
                .updateById(any(PmsProductCategory.class));
    }

    @Test
    void updateShouldRejectMoveUnderSecondLevelCategory() {
        PmsProductCategory existing = category(
                10L, 0L, 0, "手机"
        );
        ProductCategoryUpdateDTO dto = new ProductCategoryUpdateDTO();
        dto.setParentId(20L);

        when(categoryMapper.selectById(10L)).thenReturn(existing);
        when(categoryMapper.selectById(20L))
                .thenReturn(category(20L, 2L, 1, "笔记本"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.update(10L, dto)
        );

        assertSame(
                ErrorCode.CATEGORY_PARENT_INVALID,
                exception.getErrorCode()
        );
        verify(categoryMapper, never()).selectCount(any());
        verify(categoryMapper, never())
                .updateById(any(PmsProductCategory.class));
    }

    @Test
    void updateShouldRejectMovingFirstLevelCategoryWithChildren() {
        PmsProductCategory existing = category(
                10L, 0L, 0, "手机"
        );
        ProductCategoryUpdateDTO dto = new ProductCategoryUpdateDTO();
        dto.setParentId(2L);

        when(categoryMapper.selectById(10L)).thenReturn(existing);
        when(categoryMapper.selectById(2L))
                .thenReturn(category(2L, 0L, 0, "电脑"));
        when(categoryMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.update(10L, dto)
        );

        assertSame(
                ErrorCode.CATEGORY_MOVE_NOT_ALLOWED,
                exception.getErrorCode()
        );
        verify(categoryMapper, never())
                .updateById(any(PmsProductCategory.class));
    }

    @Test
    void updateShouldRejectDuplicateNameUnderTargetParent() {
        PmsProductCategory existing = category(
                11L, 1L, 1, "旧名称"
        );
        ProductCategoryUpdateDTO dto = new ProductCategoryUpdateDTO();
        dto.setName("重复名称");

        when(categoryMapper.selectById(11L)).thenReturn(existing);
        when(categoryMapper.selectById(1L))
                .thenReturn(category(1L, 0L, 0, "手机"));
        when(categoryMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.update(11L, dto)
        );

        assertSame(
                ErrorCode.CATEGORY_NAME_ALREADY_EXISTS,
                exception.getErrorCode()
        );
        verify(categoryMapper, never())
                .updateById(any(PmsProductCategory.class));
    }

    @Test
    void deleteShouldSucceedWhenCategoryHasNoChildren() {
        when(categoryMapper.selectCount(any())).thenReturn(0L);
        when(categoryMapper.deleteById(10L)).thenReturn(1);

        assertDoesNotThrow(() -> categoryService.delete(10L));

        verify(categoryMapper).deleteById(10L);
    }

    @Test
    void deleteShouldRejectCategoryWithChildren() {
        when(categoryMapper.selectCount(any())).thenReturn(2L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.delete(10L)
        );

        assertSame(
                ErrorCode.CATEGORY_HAS_CHILDREN,
                exception.getErrorCode()
        );
        verify(categoryMapper, never()).deleteById(anyLong());
    }

    @Test
    void deleteShouldFailWhenCategoryDoesNotExist() {
        when(categoryMapper.selectCount(any())).thenReturn(0L);
        when(categoryMapper.deleteById(404L)).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.delete(404L)
        );

        assertSame(ErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void createShouldTranslateUniqueConstraintConflict() {
        ProductCategoryCreateDTO dto = createDto(0L, "手机");
        when(categoryMapper.selectCount(any())).thenReturn(0L);
        when(categoryMapper.insert(any(PmsProductCategory.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate key SQL detail"
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.create(dto)
        );

        assertSame(
                ErrorCode.CATEGORY_NAME_ALREADY_EXISTS,
                exception.getErrorCode()
        );
        assertNotNull(exception.getCause());
    }

    @Test
    void deleteShouldTranslateRelatedProductConflict() {
        when(categoryMapper.selectCount(any())).thenReturn(0L);
        when(categoryMapper.deleteById(10L))
                .thenThrow(new DataIntegrityViolationException(
                        "foreign key SQL detail"
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.delete(10L)
        );

        assertSame(
                ErrorCode.CATEGORY_HAS_PRODUCTS,
                exception.getErrorCode()
        );
        assertNotNull(exception.getCause());
    }

    private ProductCategoryCreateDTO createDto(
            Long parentId,
            String name) {

        ProductCategoryCreateDTO dto =
                new ProductCategoryCreateDTO();
        dto.setParentId(parentId);
        dto.setName(name);
        return dto;
    }

    private PmsProductCategory category(
            Long id,
            Long parentId,
            Integer level,
            String name) {

        PmsProductCategory category =
                new PmsProductCategory();
        category.setId(id);
        category.setParentId(parentId);
        category.setLevel(level);
        category.setName(name);
        category.setProductCount(0);
        category.setNavStatus(0);
        category.setShowStatus(1);
        category.setSort(0);
        return category;
    }
}
