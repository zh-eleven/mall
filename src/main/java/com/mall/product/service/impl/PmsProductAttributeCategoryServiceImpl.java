package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.PageResult;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.ProductAttributeCategoryCreateDTO;
import com.mall.product.dto.ProductAttributeCategoryUpdateDTO;
import com.mall.product.entity.PmsProductAttributeCategory;
import com.mall.product.mapper.PmsProductAttributeCategoryMapper;
import com.mall.product.service.PmsProductAttributeCategoryService;
import com.mall.product.vo.ProductAttributeCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PmsProductAttributeCategoryServiceImpl
        implements PmsProductAttributeCategoryService {

    private final PmsProductAttributeCategoryMapper categoryMapper;

    @Override
    public ProductAttributeCategoryVO create(
            ProductAttributeCategoryCreateDTO dto) {

        String name = dto.getName().trim();

        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<PmsProductAttributeCategory>()
                        .eq(PmsProductAttributeCategory::getName, name)
        );

        if (count > 0) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_CATEGORY_NAME_ALREADY_EXISTS
            );
        }

        PmsProductAttributeCategory category =
                new PmsProductAttributeCategory();

        category.setName(name);
        category.setAttributeCount(0);
        category.setParamCount(0);

        try {
            categoryMapper.insert(category);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_CATEGORY_NAME_ALREADY_EXISTS,
                    exception
            );
        }

        return ProductAttributeCategoryVO.from(
                categoryMapper.selectById(category.getId())
        );
    }

    @Override
    public PageResult<ProductAttributeCategoryVO> page(
            String keyword,
            int pageNum,
            int pageSize) {

        LambdaQueryWrapper<PmsProductAttributeCategory> query =
                new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            query.like(
                    PmsProductAttributeCategory::getName,
                    keyword.trim()
            );
        }

        query.orderByAsc(PmsProductAttributeCategory::getId);

        Page<PmsProductAttributeCategory> result =
                categoryMapper.selectPage(
                        new Page<>(pageNum, pageSize),
                        query
                );

        return PageResult.from(
                result,
                ProductAttributeCategoryVO::from
        );
    }

    @Override
    public ProductAttributeCategoryVO getById(Long categoryId) {
        return ProductAttributeCategoryVO.from(
                findById(categoryId)
        );
    }

    @Override
    public ProductAttributeCategoryVO update(
            Long categoryId,
            ProductAttributeCategoryUpdateDTO dto) {

        PmsProductAttributeCategory category =
                findById(categoryId);

        if (dto.getName() != null) {
            String name = dto.getName().trim();

            Long count = categoryMapper.selectCount(
                    new LambdaQueryWrapper<PmsProductAttributeCategory>()
                            .eq(
                                    PmsProductAttributeCategory::getName,
                                    name
                            )
                            .ne(
                                    PmsProductAttributeCategory::getId,
                                    categoryId
                            )
            );

            if (count > 0) {
                throw new BusinessException(
                        ErrorCode
                                .ATTRIBUTE_CATEGORY_NAME_ALREADY_EXISTS
                );
            }

            category.setName(name);

            try {
                categoryMapper.updateById(category);
            } catch (DuplicateKeyException exception) {
                throw new BusinessException(
                        ErrorCode
                                .ATTRIBUTE_CATEGORY_NAME_ALREADY_EXISTS,
                        exception
                );
            }
        }

        return ProductAttributeCategoryVO.from(
                findById(categoryId)
        );
    }

    @Override
    public void delete(Long categoryId) {
        int deleted;

        try {
            deleted = categoryMapper.deleteById(categoryId);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_CATEGORY_HAS_ATTRIBUTES,
                    exception
            );
        }

        if (deleted == 0) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_CATEGORY_NOT_FOUND
            );
        }
    }

    private PmsProductAttributeCategory findById(Long categoryId) {
        PmsProductAttributeCategory category =
                categoryMapper.selectById(categoryId);

        if (category == null) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_CATEGORY_NOT_FOUND
            );
        }

        return category;
    }
}
