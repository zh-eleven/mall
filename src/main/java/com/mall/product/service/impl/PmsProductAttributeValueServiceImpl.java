package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.cache.CacheNames;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.ProductAttributeValueItemDTO;
import com.mall.product.entity.PmsProduct;
import com.mall.product.entity.PmsProductAttribute;
import com.mall.product.entity.PmsProductAttributeValue;
import com.mall.product.entity.PmsProductCategoryAttributeRelation;
import com.mall.product.entity.PmsSkuStock;
import com.mall.product.mapper.*;
import com.mall.product.service.PmsProductAttributeValueService;
import com.mall.product.vo.ProductAttributeValueVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PmsProductAttributeValueServiceImpl
        implements PmsProductAttributeValueService {

    private final PmsProductMapper productMapper;
    private final PmsProductAttributeMapper attributeMapper;
    private final PmsProductAttributeValueMapper attributeValueMapper;
    private final PmsProductCategoryAttributeRelationMapper relationMapper;
    private final PmsSkuStockMapper skuStockMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductAttributeValueVO> listByProductId(
            Long productId) {

        findProduct(productId);

        return attributeValueMapper.selectList(
                        new LambdaQueryWrapper<PmsProductAttributeValue>()
                                .eq(
                                        PmsProductAttributeValue::getProductId,
                                        productId
                                )
                                .orderByAsc(
                                        PmsProductAttributeValue
                                                ::getProductAttributeId
                                )
                )
                .stream()
                .map(ProductAttributeValueVO::from)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = CacheNames.PORTAL_PRODUCT_DETAIL,
            key = "#productId"
    )
    public List<ProductAttributeValueVO> replace(
            Long productId,
            List<ProductAttributeValueItemDTO> values) {

        PmsProduct product = findProduct(productId);

        ensureProductHasNoSku(productId);

        List<Long> attributeIds = values.stream()
                .map(ProductAttributeValueItemDTO::getProductAttributeId)
                .toList();

        validateNoDuplicates(attributeIds);
        validateAttributes(
                product.getProductCategoryId(),
                values,
                attributeIds
        );

        attributeValueMapper.delete(
                new LambdaQueryWrapper<PmsProductAttributeValue>()
                        .eq(
                                PmsProductAttributeValue::getProductId,
                                productId
                        )
        );

        try {
            for (ProductAttributeValueItemDTO item : values) {
                PmsProductAttributeValue entity =
                        new PmsProductAttributeValue();

                entity.setProductId(productId);
                entity.setProductAttributeId(
                        item.getProductAttributeId()
                );
                entity.setValue(item.getValue().trim());

                if (attributeValueMapper.insert(entity) != 1) {
                    throw new DataIntegrityViolationException(
                            "商品属性值写入失败"
                    );
                }
            }
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT,
                    exception
            );
        }

        return listByProductId(productId);
    }

    private void ensureProductHasNoSku(Long productId) {
        Long skuCount = skuStockMapper.selectCount(
                new LambdaQueryWrapper<PmsSkuStock>()
                        .eq(
                                PmsSkuStock::getProductId,
                                productId
                        )
        );

        if (skuCount > 0) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_HAS_SKU_ATTRIBUTE_UPDATE_FORBIDDEN
            );
        }
    }

    private PmsProduct findProduct(Long productId) {
        PmsProduct product = productMapper.selectById(productId);

        if (product == null) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_FOUND
            );
        }

        return product;
    }

    private void validateNoDuplicates(List<Long> attributeIds) {
        if (new HashSet<>(attributeIds).size()
                != attributeIds.size()) {

            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_SELECTION_INVALID
            );
        }
    }

    private void validateAttributes(
            Long productCategoryId,
            List<ProductAttributeValueItemDTO> values,
            List<Long> attributeIds) {

        if (attributeIds.isEmpty()) {
            return;
        }

        Set<Long> allowedIds = relationMapper.selectList(
                        new LambdaQueryWrapper<
                                PmsProductCategoryAttributeRelation>()
                                .eq(
                                        PmsProductCategoryAttributeRelation
                                                ::getProductCategoryId,
                                        productCategoryId
                                )
                )
                .stream()
                .map(
                        PmsProductCategoryAttributeRelation
                                ::getProductAttributeId
                )
                .collect(Collectors.toSet());

        if (!allowedIds.containsAll(attributeIds)) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_SELECTION_INVALID
            );
        }

        Map<Long, PmsProductAttribute> attributeMap =
                attributeMapper.selectBatchIds(attributeIds)
                        .stream()
                        .collect(Collectors.toMap(
                                PmsProductAttribute::getId,
                                Function.identity()
                        ));

        if (attributeMap.size() != attributeIds.size()) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_SELECTION_INVALID
            );
        }

        for (ProductAttributeValueItemDTO item : values) {
            validatePredefinedValue(
                    attributeMap.get(item.getProductAttributeId()),
                    item.getValue()
            );
        }
    }

    private void validatePredefinedValue(
            PmsProductAttribute attribute,
            String value) {

        if (!Integer.valueOf(1).equals(attribute.getInputType())
                || Integer.valueOf(1).equals(
                attribute.getHandAddStatus()
        )) {
            return;
        }

        Set<String> allowedValues = Arrays.stream(
                        attribute.getInputList().split(",")
                )
                .map(String::trim)
                .collect(Collectors.toSet());

        boolean invalid = Arrays.stream(value.split(",", -1))
                .map(String::trim)
                .anyMatch(item ->
                        item.isEmpty()
                                || !allowedValues.contains(item)
                );

        if (invalid) {
            throw new BusinessException(
                    ErrorCode.ATTRIBUTE_SELECTION_INVALID
            );
        }
    }
}
