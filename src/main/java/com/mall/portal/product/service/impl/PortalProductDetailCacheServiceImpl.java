package com.mall.portal.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.cache.CacheNames;
import com.mall.common.exception.BusinessException;
import com.mall.portal.product.service.PortalProductDetailCacheService;
import com.mall.portal.product.vo.PortalProductAttributeVO;
import com.mall.portal.product.vo.PortalProductDetailCacheVO;
import com.mall.portal.product.vo.PortalSkuCacheVO;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsProductAttribute;
import com.mall.product.entity.PmsProductAttributeValue;
import com.mall.product.entity.PmsSkuStock;
import com.mall.product.mapper.PmsProductAttributeMapper;
import com.mall.product.mapper.PmsProductAttributeValueMapper;
import com.mall.product.mapper.PmsProductMapper;
import com.mall.product.mapper.PmsSkuStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mall.product.cache.PortalProductNotFoundCache;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortalProductDetailCacheServiceImpl
        implements PortalProductDetailCacheService {

    private final PmsProductMapper productMapper;
    private final PmsProductAttributeMapper attributeMapper;
    private final PmsProductAttributeValueMapper attributeValueMapper;
    private final PmsSkuStockMapper skuStockMapper;
    private final PortalProductNotFoundCache productNotFoundCache;

    @Override
    @Cacheable(
            cacheNames = CacheNames.PORTAL_PRODUCT_DETAIL,
            key = "#productId",
            sync = true
    )
    public PortalProductDetailCacheVO getStaticDetail(Long productId) {

        if (productNotFoundCache.contains(productId)) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_FOUND
            );
        }

        PmsProduct product = productMapper.selectOne(
                new LambdaQueryWrapper<PmsProduct>()
                        .select(
                                PmsProduct::getId,
                                PmsProduct::getBrandId,
                                PmsProduct::getProductCategoryId,
                                PmsProduct::getName,
                                PmsProduct::getSubTitle,
                                PmsProduct::getPrice,
                                PmsProduct::getOriginalPrice,
                                PmsProduct::getUnit,
                                PmsProduct::getPic,
                                PmsProduct::getAlbumPics,
                                PmsProduct::getDescription,
                                PmsProduct::getDetailTitle,
                                PmsProduct::getDetailDesc,
                                PmsProduct::getDetailHtml
                        )
                        .eq(PmsProduct::getId, productId)
                        .eq(PmsProduct::getPublishStatus, 1)
        );

        if (product == null) {
            productNotFoundCache.put(productId);

            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_FOUND
            );
        }

        List<PmsProductAttributeValue> attributeValues =
                attributeValueMapper.selectList(
                        new LambdaQueryWrapper<PmsProductAttributeValue>()
                                .select(
                                        PmsProductAttributeValue::getProductAttributeId,
                                        PmsProductAttributeValue::getValue
                                )
                                .eq(
                                        PmsProductAttributeValue::getProductId,
                                        productId
                                )
                                .orderByAsc(
                                        PmsProductAttributeValue
                                                ::getProductAttributeId
                                )
                );

        Map<Long, PmsProductAttribute> attributeMap =
                findAttributeMap(attributeValues);

        long distinctAttributeCount = attributeValues.stream()
                .map(PmsProductAttributeValue::getProductAttributeId)
                .distinct()
                .count();

        if (attributeMap.size() != distinctAttributeCount) {
            throw new BusinessException(ErrorCode.DATA_CONFLICT);
        }

        List<PortalProductAttributeVO> attributes =
                attributeValues.stream()
                        .map(value -> PortalProductAttributeVO.from(
                                attributeMap.get(
                                        value.getProductAttributeId()
                                ),
                                value
                        ))
                        .toList();

        List<PortalSkuCacheVO> skus =
                skuStockMapper.selectList(
                                new LambdaQueryWrapper<PmsSkuStock>()
                                        .select(
                                                PmsSkuStock::getId,
                                                PmsSkuStock::getPrice,
                                                PmsSkuStock::getPic,
                                                PmsSkuStock::getSpecData
                                        )
                                        .eq(
                                                PmsSkuStock::getProductId,
                                                productId
                                        )
                                        .orderByAsc(PmsSkuStock::getId)
                        )
                        .stream()
                        .map(PortalSkuCacheVO::from)
                        .toList();

        return PortalProductDetailCacheVO.from(
                product,
                attributes,
                skus
        );
    }

    private Map<Long, PmsProductAttribute> findAttributeMap(
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

        return attributeMapper.selectList(
                        new LambdaQueryWrapper<PmsProductAttribute>()
                                .select(
                                        PmsProductAttribute::getId,
                                        PmsProductAttribute::getName
                                )
                                .in(PmsProductAttribute::getId, attributeIds)
                )
                .stream()
                .collect(Collectors.toMap(
                        PmsProductAttribute::getId,
                        Function.identity()
                ));
    }
}
