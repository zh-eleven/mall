package com.mall.portal.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.PageResult;
import com.mall.common.cache.CacheNames;
import com.mall.common.exception.BusinessException;
import com.mall.order.mapper.OmsOrderItemMapper;
import com.mall.order.mapper.OmsOrderMapper;
import org.springframework.cache.annotation.Cacheable;
import com.mall.portal.product.service.PortalProductDetailCacheService;
import com.mall.portal.product.service.PortalProductService;
import com.mall.portal.product.vo.*;
import com.mall.product.entity.*;
import com.mall.product.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortalProductServiceImpl
        implements PortalProductService {

    private final PmsProductMapper productMapper;
    private final PmsSkuStockMapper skuStockMapper;
    private final PmsProductCategoryMapper categoryMapper;
    private final OmsOrderMapper orderMapper;
    private final OmsOrderItemMapper orderItemMapper;
    private final PortalProductDetailCacheService detailCacheService;

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

        PortalProductDetailCacheVO detail =
                detailCacheService.getStaticDetail(productId);

        List<PmsSkuStock> stockRows = skuStockMapper.selectList(
                new LambdaQueryWrapper<PmsSkuStock>()
                        .select(
                                PmsSkuStock::getId,
                                PmsSkuStock::getStock,
                                PmsSkuStock::getLockedStock
                        )
                        .eq(PmsSkuStock::getProductId, productId)
                        .orderByAsc(PmsSkuStock::getId)
        );

        List<PortalSkuVO> skus = mergeCurrentStock(
                detail.skus(),
                stockRows
        );

        return PortalProductDetailVO.from(
                detail,
                skus
        );
    }

    private List<PortalSkuVO> mergeCurrentStock(
            List<PortalSkuCacheVO> cachedSkus,
            List<PmsSkuStock> stockRows) {

        Map<Long, PmsSkuStock> stockBySkuId = new HashMap<>();

        for (PmsSkuStock stockRow : stockRows) {
            if (stockRow.getId() == null
                    || stockRow.getStock() == null
                    || stockRow.getLockedStock() == null
                    || stockBySkuId.put(
                    stockRow.getId(),
                    stockRow
            ) != null) {
                throw new BusinessException(
                        ErrorCode.DATA_CONFLICT
                );
            }
        }

        if (stockBySkuId.size() != cachedSkus.size()) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }

        List<PortalSkuVO> result = new ArrayList<>(
                cachedSkus.size()
        );

        for (PortalSkuCacheVO cachedSku : cachedSkus) {
            PmsSkuStock stockRow = cachedSku.id() == null
                    ? null
                    : stockBySkuId.remove(cachedSku.id());

            if (stockRow == null) {
                throw new BusinessException(
                        ErrorCode.DATA_CONFLICT
                );
            }

            result.add(PortalSkuVO.from(
                    cachedSku,
                    stockRow.getStock(),
                    stockRow.getLockedStock()
            ));
        }

        if (!stockBySkuId.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
            );
        }

        return List.copyOf(result);
    }

    @Override
    @Cacheable(
            cacheNames = CacheNames.PORTAL_CATEGORY_TREE,
            key = "'all'",
            sync = true
    )
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
