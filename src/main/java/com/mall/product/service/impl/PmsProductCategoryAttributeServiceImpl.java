package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.product.entity.PmsProductAttribute;
import com.mall.product.entity.PmsProductCategory;
import com.mall.product.entity.PmsProductCategoryAttributeRelation;
import com.mall.product.mapper.PmsProductAttributeMapper;
import com.mall.product.mapper.PmsProductCategoryAttributeRelationMapper;
import com.mall.product.mapper.PmsProductCategoryMapper;
import com.mall.product.service.PmsProductCategoryAttributeService;
import com.mall.product.vo.ProductAttributeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PmsProductCategoryAttributeServiceImpl
        implements PmsProductCategoryAttributeService {

    private final PmsProductCategoryMapper categoryMapper;
    private final PmsProductAttributeMapper attributeMapper;
    private final PmsProductCategoryAttributeRelationMapper relationMapper;

    @Override
    public List<ProductAttributeVO> listByCategoryId(
            Long productCategoryId) {

        validateCategory(productCategoryId);

        List<Long> attributeIds = relationMapper.selectList(
                        new LambdaQueryWrapper<
                                PmsProductCategoryAttributeRelation>()
                                .eq(
                                        PmsProductCategoryAttributeRelation
                                                ::getProductCategoryId,
                                        productCategoryId
                                )
                                .orderByAsc(
                                        PmsProductCategoryAttributeRelation
                                                ::getId
                                )
                )
                .stream()
                .map(
                        PmsProductCategoryAttributeRelation
                                ::getProductAttributeId
                )
                .toList();

        if (attributeIds.isEmpty()) {
            return List.of();
        }

        return attributeMapper.selectList(
                        new LambdaQueryWrapper<PmsProductAttribute>()
                                .in(
                                        PmsProductAttribute::getId,
                                        attributeIds
                                )
                                .orderByDesc(
                                        PmsProductAttribute::getSort
                                )
                                .orderByAsc(
                                        PmsProductAttribute::getId
                                )
                )
                .stream()
                .map(ProductAttributeVO::from)
                .toList();
    }

    @Override
    @Transactional
    public List<ProductAttributeVO> replace(
            Long productCategoryId,
            List<Long> attributeIds) {

        validateCategory(productCategoryId);

        List<Long> distinctIds = attributeIds
                .stream()
                .distinct()
                .toList();

        if (!distinctIds.isEmpty()) {
            Long existingCount = attributeMapper.selectCount(
                    new LambdaQueryWrapper<PmsProductAttribute>()
                            .in(
                                    PmsProductAttribute::getId,
                                    distinctIds
                            )
            );

            if (existingCount != distinctIds.size()) {
                throw new BusinessException(
                        ErrorCode.ATTRIBUTE_SELECTION_INVALID
                );
            }
        }

        relationMapper.delete(
                new LambdaQueryWrapper<
                        PmsProductCategoryAttributeRelation>()
                        .eq(
                                PmsProductCategoryAttributeRelation
                                        ::getProductCategoryId,
                                productCategoryId
                        )
        );

        if (!distinctIds.isEmpty()) {
            List<PmsProductCategoryAttributeRelation> relations =
                    distinctIds.stream()
                            .map(attributeId -> {
                                PmsProductCategoryAttributeRelation relation =
                                        new PmsProductCategoryAttributeRelation();

                                relation.setProductCategoryId(
                                        productCategoryId
                                );
                                relation.setProductAttributeId(attributeId);

                                return relation;
                            })
                            .toList();

            try {
                for (PmsProductCategoryAttributeRelation relation
                        : relations) {

                    if (relationMapper.insert(relation) != 1) {
                        throw new DataIntegrityViolationException(
                                "商品分类属性关联写入失败"
                        );
                    }
                }
            } catch (DataIntegrityViolationException exception) {
                throw new BusinessException(
                        ErrorCode.DATA_CONFLICT,
                        exception
                );
            }
        }

        return listByCategoryId(productCategoryId);
    }

    private PmsProductCategory validateCategory(
            Long productCategoryId) {

        PmsProductCategory category =
                categoryMapper.selectById(productCategoryId);

        if (category == null) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_NOT_FOUND
            );
        }

        if (!Integer.valueOf(1).equals(category.getLevel())) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_ATTRIBUTE_ONLY_LEAF_ALLOWED
            );
        }

        return category;
    }
}
