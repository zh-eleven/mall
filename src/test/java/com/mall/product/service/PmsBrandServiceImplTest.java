package com.mall.product.service;

import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.BrandCreateDTO;
import com.mall.product.entity.PmsBrand;
import com.mall.product.mapper.PmsBrandMapper;
import com.mall.product.service.impl.PmsBrandServiceImpl;
import com.mall.product.vo.BrandVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PmsBrandServiceImplTest {

    @Mock
    private PmsBrandMapper brandMapper;

    private PmsBrandServiceImpl brandService;

    @BeforeEach
    void setUp() {
        brandService =
                new PmsBrandServiceImpl(brandMapper);
    }

    @Test
    void createShouldReturnCreatedBrand() {
        BrandCreateDTO dto = new BrandCreateDTO();
        dto.setName("测试品牌");
        dto.setFirstLetter("t");

        when(brandMapper.selectCount(any()))
                .thenReturn(0L);

        when(brandMapper.insert(any(PmsBrand.class)))
                .thenAnswer(invocation -> {
                    PmsBrand brand =
                            invocation.getArgument(0);
                    brand.setId(100L);
                    return 1;
                });

        PmsBrand saved = new PmsBrand();
        saved.setId(100L);
        saved.setName("测试品牌");
        saved.setFirstLetter("T");
        saved.setSort(0);
        saved.setFactoryStatus(0);
        saved.setShowStatus(1);
        saved.setProductCount(0);
        saved.setProductCommentCount(0);

        when(brandMapper.selectById(100L))
                .thenReturn(saved);

        BrandVO result = brandService.create(dto);

        assertEquals(100L, result.id());
        assertEquals("测试品牌", result.name());
        assertEquals("T", result.firstLetter());

        verify(brandMapper).insert(any(PmsBrand.class));
    }

    @Test
    void createShouldRejectDuplicateName() {
        BrandCreateDTO dto = new BrandCreateDTO();
        dto.setName("小米");

        when(brandMapper.selectCount(any()))
                .thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> brandService.create(dto)
        );

        assertSame(
                ErrorCode.BRAND_NAME_ALREADY_EXISTS,
                exception.getErrorCode()
        );

        verify(
                brandMapper,
                never()
        ).insert(any(PmsBrand.class));
    }

    @Test
    void deleteShouldRejectMissingBrand() {
        when(brandMapper.deleteById(999L))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> brandService.delete(999L)
        );

        assertSame(
                ErrorCode.BRAND_NOT_FOUND,
                exception.getErrorCode()
        );
    }

    @Test
    void createShouldTranslateUniqueConstraintConflict() {
        BrandCreateDTO dto = new BrandCreateDTO();
        dto.setName("小米");

        when(brandMapper.selectCount(any()))
                .thenReturn(0L);
        when(brandMapper.insert(any(PmsBrand.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate key SQL detail"
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> brandService.create(dto)
        );

        assertSame(
                ErrorCode.BRAND_NAME_ALREADY_EXISTS,
                exception.getErrorCode()
        );
        assertNotNull(exception.getCause());
    }

    @Test
    void deleteShouldTranslateRelatedProductConflict() {
        when(brandMapper.deleteById(10L))
                .thenThrow(new DataIntegrityViolationException(
                        "foreign key SQL detail"
                ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> brandService.delete(10L)
        );

        assertSame(
                ErrorCode.BRAND_HAS_PRODUCTS,
                exception.getErrorCode()
        );
        assertNotNull(exception.getCause());
    }
}
