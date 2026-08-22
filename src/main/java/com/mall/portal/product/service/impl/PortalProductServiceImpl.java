package com.mall.portal.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.PageResult;
import com.mall.common.exception.BusinessException;
import com.mall.portal.product.service.PortalProductService;
import com.mall.portal.product.vo.*;
import com.mall.product.entity.*;
import com.mall.product.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortalProductServiceImpl
        implements PortalProductService {

    private final PmsProductMapper productMapper;
    private final PmsSkuStockMapper skuStockMapper;
    private final PmsProductAttributeMapper attributeMapper;
    private final PmsProductAttributeValueMapper attributeValueMapper;
    private final PmsProductCategoryMapper categoryMapper;

    @Override
    public PageResult<PortalProductSummaryVO> page(
            String keyword,
            Long brandId,
            Long categoryId,
            int pageNum,
            int pageSize) {

        LambdaQueryWrapper<PmsProduct> query =
                new LambdaQueryWrapper<PmsProduct>()
                        .eq(
                                PmsProduct::getPublishStatus,
                                1
                        );

        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();

            query.and(wrapper -> wrapper
                    .like(PmsProduct::getName, value)
                    .or()
                    .like(PmsProduct::getSubTitle, value)
            );
        }

        if (brandId != null) {
            query.eq(PmsProduct::getBrandId, brandId);
        }

        if (categoryId != null) {
            query.eq(
                    PmsProduct::getProductCategoryId,
                    categoryId
            );
        }

        query.orderByDesc(PmsProduct::getSort)
                .orderByDesc(PmsProduct::getId);

        Page<PmsProduct> result = productMapper.selectPage(
                new Page<>(pageNum, pageSize),
                query
        );

        return PageResult.from(
                result,
                PortalProductSummaryVO::from
        );
    }

    @Override
    public PortalProductDetailVO getDetail(
            Long productId) {

        PmsProduct product =
                findPublishedProduct(productId);

        List<PmsProductAttributeValue> attributeValues =
                attributeValueMapper.selectList(
                        new LambdaQueryWrapper<
                                PmsProductAttributeValue>()
                                .eq(
                                        PmsProductAttributeValue
                                                ::getProductId,
                                        productId
                                )
                                .orderByAsc(
                                        PmsProductAttributeValue
                                                ::getProductAttributeId
                                )
                );

        Map<Long, PmsProductAttribute> attributeMap =
                findAttributeMap(attributeValues);

        List<PortalProductAttributeVO> attributes =
                attributeValues.stream()
                        .filter(value -> attributeMap.containsKey(
                                value.getProductAttributeId()
                        ))
                        .map(value ->
                                PortalProductAttributeVO.from(
                                        attributeMap.get(
                                                value.getProductAttributeId()
                                        ),
                                        value
                                )
                        )
                        .toList();

        List<PortalSkuVO> skus =
                skuStockMapper.selectList(
                                new LambdaQueryWrapper<
                                        PmsSkuStock>()
                                        .eq(
                                                PmsSkuStock::getProductId,
                                                productId
                                        )
                                        .orderByAsc(PmsSkuStock::getId)
                        )
                        .stream()
                        .map(PortalSkuVO::from)
                        .toList();

        return PortalProductDetailVO.from(
                product,
                attributes,
                skus
        );
    }

    private PmsProduct findPublishedProduct(
            Long productId) {

        PmsProduct product = productMapper.selectOne(
                new LambdaQueryWrapper<PmsProduct>()
                        .eq(PmsProduct::getId, productId)
                        .eq(PmsProduct::getPublishStatus, 1)
        );

        if (product == null) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_FOUND
            );
        }

        return product;
    }

    private Map<Long, PmsProductAttribute>
    findAttributeMap(
            List<PmsProductAttributeValue> values) {

        if (values.isEmpty()) {
            return Map.of();
        }

        List<Long> attributeIds = values.stream()
                .map(
                        PmsProductAttributeValue
                                ::getProductAttributeId
                )
                .distinct()
                .toList();

        return attributeMapper.selectBatchIds(attributeIds)
                .stream()
                .collect(Collectors.toMap(
                        PmsProductAttribute::getId,
                        Function.identity()
                ));
    }

    @Override
    public List<PortalProductCategoryVO> getCategoryTree() {

        List<PmsProductCategory> categories =
                categoryMapper.selectList(
                        new LambdaQueryWrapper<PmsProductCategory>()
                                .eq(
                                        PmsProductCategory::getShowStatus,
                                        1
                                )
                                .orderByDesc(
                                        PmsProductCategory::getSort
                                )
                                .orderByAsc(
                                        PmsProductCategory::getId
                                )
                );

        Map<Long, List<PmsProductCategory>> childrenByParent =
                categories.stream()
                        .collect(Collectors.groupingBy(
                                PmsProductCategory::getParentId
                        ));

        return childrenByParent
                .getOrDefault(0L, List.of())
                .stream()
                .map(parent ->
                        PortalProductCategoryVO.from(
                                parent,
                                childrenByParent
                                        .getOrDefault(
                                                parent.getId(),
                                                List.of()
                                        )
                                        .stream()
                                        .map(child ->
                                                PortalProductCategoryVO.from(
                                                        child,
                                                        List.of()
                                                )
                                        )
                                        .toList()
                        )
                )
                .toList();
    }
}