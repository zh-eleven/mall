package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.ProductCategoryCreateDTO;
import com.mall.product.dto.ProductCategoryUpdateDTO;
import com.mall.product.entity.PmsProductCategory;
import com.mall.product.mapper.PmsProductCategoryMapper;
import com.mall.product.service.PmsProductCategoryService;
import com.mall.product.vo.ProductCategoryTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.mall.common.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;


@Service
@RequiredArgsConstructor
public class PmsProductCategoryServiceImpl
        implements PmsProductCategoryService {

    private final PmsProductCategoryMapper categoryMapper;

    @Override
    public List<ProductCategoryTreeVO> tree() {
        List<PmsProductCategory> categories =
                categoryMapper.selectList(
                        new LambdaQueryWrapper<PmsProductCategory>()
                                .orderByDesc(PmsProductCategory::getSort)
                                .orderByAsc(PmsProductCategory::getId)
                );

        Map<Long, List<PmsProductCategory>> childrenByParent =
                categories.stream()
                        .collect(Collectors.groupingBy(
                                PmsProductCategory::getParentId
                        ));

        return childrenByParent
                .getOrDefault(0L, List.of())
                .stream()
                .map(parent -> ProductCategoryTreeVO.from(
                        parent,
                        childrenByParent
                                .getOrDefault(parent.getId(), List.of())
                                .stream()
                                .map(child ->
                                        ProductCategoryTreeVO.from(
                                                child,
                                                List.of()
                                        )
                                )
                                .toList()
                ))
                .toList();
    }

    @Override
    @CacheEvict(
            cacheNames = CacheNames.PORTAL_CATEGORY_TREE,
            allEntries = true
    )
    public ProductCategoryTreeVO create(
            ProductCategoryCreateDTO dto) {

        long parentId = dto.getParentId();
        int level;

        if (parentId == 0L) {
            level = 0;
        } else {
            PmsProductCategory parent =
                    categoryMapper.selectById(parentId);

            if (parent == null) {
                throw new BusinessException(
                        ErrorCode.CATEGORY_NOT_FOUND
                );
            }

            if (!Integer.valueOf(0).equals(parent.getLevel())) {
                throw new BusinessException(
                        ErrorCode.CATEGORY_PARENT_INVALID
                );
            }

            level = 1;
        }

        String name = dto.getName().trim();

        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<PmsProductCategory>()
                        .eq(PmsProductCategory::getParentId, parentId)
                        .eq(PmsProductCategory::getName, name)
        );

        if (count > 0) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_NAME_ALREADY_EXISTS
            );
        }

        PmsProductCategory category =
                new PmsProductCategory();

        category.setParentId(parentId);
        category.setName(name);
        category.setLevel(level);
        category.setProductCount(0);
        category.setProductUnit(normalizeText(dto.getProductUnit()));
        category.setNavStatus(
                dto.getNavStatus() == null ? 0 : dto.getNavStatus()
        );
        category.setShowStatus(
                dto.getShowStatus() == null ? 1 : dto.getShowStatus()
        );
        category.setSort(
                dto.getSort() == null ? 0 : dto.getSort()
        );
        category.setIcon(normalizeText(dto.getIcon()));
        category.setKeywords(normalizeText(dto.getKeywords()));
        category.setDescription(normalizeText(dto.getDescription()));

        try {
            categoryMapper.insert(category);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_NAME_ALREADY_EXISTS,
                    exception
            );
        }

        PmsProductCategory saved =
                categoryMapper.selectById(category.getId());

        return ProductCategoryTreeVO.from(saved, List.of());
    }

    @Override
    @CacheEvict(
            cacheNames = CacheNames.PORTAL_CATEGORY_TREE,
            allEntries = true
    )
    public ProductCategoryTreeVO update(
            Long categoryId,
            ProductCategoryUpdateDTO dto) {

        PmsProductCategory category =
                categoryMapper.selectById(categoryId);

        if (category == null) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_NOT_FOUND
            );
        }

        long oldParentId = category.getParentId();
        long targetParentId = dto.getParentId() == null
                ? oldParentId
                : dto.getParentId();

        if (targetParentId == categoryId) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_PARENT_INVALID
            );
        }

        int targetLevel;

        if (targetParentId == 0L) {
            targetLevel = 0;
        } else {
            PmsProductCategory parent =
                    categoryMapper.selectById(targetParentId);

            if (parent == null) {
                throw new BusinessException(
                        ErrorCode.CATEGORY_NOT_FOUND
                );
            }

            if (!Integer.valueOf(0).equals(parent.getLevel())) {
                throw new BusinessException(
                        ErrorCode.CATEGORY_PARENT_INVALID
                );
            }

            targetLevel = 1;
        }

        if (targetParentId != oldParentId && targetParentId != 0L) {
            Long childCount = categoryMapper.selectCount(
                    new LambdaQueryWrapper<PmsProductCategory>()
                            .eq(
                                    PmsProductCategory::getParentId,
                                    categoryId
                            )
            );

            if (childCount > 0) {
                throw new BusinessException(
                        ErrorCode.CATEGORY_MOVE_NOT_ALLOWED
                );
            }
        }

        String targetName = dto.getName() == null
                ? category.getName()
                : dto.getName().trim();

        Long duplicateCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<PmsProductCategory>()
                        .eq(
                                PmsProductCategory::getParentId,
                                targetParentId
                        )
                        .eq(PmsProductCategory::getName, targetName)
                        .ne(PmsProductCategory::getId, categoryId)
        );

        if (duplicateCount > 0) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_NAME_ALREADY_EXISTS
            );
        }

        category.setParentId(targetParentId);
        category.setLevel(targetLevel);
        category.setName(targetName);

        if (dto.getProductUnit() != null) {
            category.setProductUnit(
                    normalizeText(dto.getProductUnit())
            );
        }

        if (dto.getNavStatus() != null) {
            category.setNavStatus(dto.getNavStatus());
        }

        if (dto.getShowStatus() != null) {
            category.setShowStatus(dto.getShowStatus());
        }

        if (dto.getSort() != null) {
            category.setSort(dto.getSort());
        }

        if (dto.getIcon() != null) {
            category.setIcon(normalizeText(dto.getIcon()));
        }

        if (dto.getKeywords() != null) {
            category.setKeywords(
                    normalizeText(dto.getKeywords())
            );
        }

        if (dto.getDescription() != null) {
            category.setDescription(
                    normalizeText(dto.getDescription())
            );
        }

        try {
            categoryMapper.updateById(category);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_NAME_ALREADY_EXISTS,
                    exception
            );
        }

        return ProductCategoryTreeVO.from(
                categoryMapper.selectById(categoryId),
                List.of()
        );
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = CacheNames.PORTAL_CATEGORY_TREE,
            allEntries = true
    )
    public void delete(Long categoryId) {
        Long childCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<PmsProductCategory>()
                        .eq(
                                PmsProductCategory::getParentId,
                                categoryId
                        )
        );

        if (childCount > 0) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_HAS_CHILDREN
            );
        }

        int deleted;

        try {
            deleted = categoryMapper.deleteById(categoryId);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_HAS_PRODUCTS,
                    exception
            );
        }

        if (deleted == 0) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_NOT_FOUND
            );
        }
    }
    private String normalizeText(String value) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }
    @Override
    public ProductCategoryTreeVO getById(Long categoryId) {
        PmsProductCategory category =
                categoryMapper.selectById(categoryId);

        if (category == null) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_NOT_FOUND
            );
        }

        return ProductCategoryTreeVO.from(
                category,
                List.of()
        );
    }
}
