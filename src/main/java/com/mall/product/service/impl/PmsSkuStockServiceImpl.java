package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.api.ErrorCode;
import com.mall.common.exception.BusinessException;
import com.mall.product.dto.SkuSpecItemDTO;
import com.mall.product.dto.SkuStockItemDTO;
import com.mall.product.entity.*;
import com.mall.product.mapper.*;
import com.mall.product.service.PmsSkuStockService;
import com.mall.product.vo.SkuStockVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PmsSkuStockServiceImpl
        implements PmsSkuStockService {

    private final PmsSkuStockMapper skuStockMapper;
    private final PmsProductMapper productMapper;
    private final PmsProductAttributeMapper attributeMapper;
    private final PmsProductAttributeValueMapper attributeValueMapper;
    private final PmsProductCategoryAttributeRelationMapper relationMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SkuStockVO> listByProductId(Long productId) {
        findProduct(productId);

        return skuStockMapper.selectList(
                        new LambdaQueryWrapper<PmsSkuStock>()
                                .eq(PmsSkuStock::getProductId, productId)
                                .orderByAsc(PmsSkuStock::getId)
                )
                .stream()
                .map(SkuStockVO::from)
                .toList();
    }

    @Override
    @Transactional
    public List<SkuStockVO> replace(
            Long productId,
            List<SkuStockItemDTO> skus) {

        PmsProduct product = findProduct(productId);

        if (Integer.valueOf(1).equals(
                product.getPublishStatus()
        )) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_PUBLISHED_SKU_UPDATE_FORBIDDEN
            );
        }

        if (skus == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_VALIDATION_FAILED
            );
        }

        List<PmsSkuStock> entities =
                prepareSkus(product, skus);

        ensureSkuCodesAvailable(productId, entities);

        skuStockMapper.delete(
                new LambdaQueryWrapper<PmsSkuStock>()
                        .eq(PmsSkuStock::getProductId, productId)
        );

        try {
            for (PmsSkuStock entity : entities) {
                if (skuStockMapper.insert(entity) != 1) {
                    throw new DuplicateKeyException(
                            "SKU写入失败"
                    );
                }
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT,
                    exception
            );
        }

        synchronizeProduct(product, entities);

        return listByProductId(productId);
    }

    private List<PmsSkuStock> prepareSkus(
            PmsProduct product,
            List<SkuStockItemDTO> skus) {

        Set<Long> requestedAttributeIds = skus.stream()
                .flatMap(item -> item.getSpecs().stream())
                .map(SkuSpecItemDTO::getProductAttributeId)
                .collect(Collectors.toSet());

        Set<Long> allowedAttributeIds =
                findAllowedAttributeIds(
                        product.getProductCategoryId()
                );

        Map<Long, PmsProductAttribute> attributeMap =
                findAttributes(requestedAttributeIds);

        Map<Long, PmsProductAttributeValue> valueMap =
                findProductAttributeValues(
                        product.getId(),
                        requestedAttributeIds
                );

        Set<String> skuCodes = new HashSet<>();
        Set<String> specKeys = new HashSet<>();
        List<PmsSkuStock> result = new ArrayList<>();

        for (SkuStockItemDTO dto : skus) {
            String skuCode = dto.getSkuCode()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (!skuCodes.add(skuCode)) {
                throw new BusinessException(
                        ErrorCode.SKU_CODE_ALREADY_EXISTS
                );
            }

            List<SkuSpecItemDTO> sortedSpecs =
                    dto.getSpecs()
                            .stream()
                            .sorted(Comparator.comparing(
                                    SkuSpecItemDTO
                                            ::getProductAttributeId
                            ))
                            .toList();

            validateSpecs(
                    sortedSpecs,
                    allowedAttributeIds,
                    attributeMap,
                    valueMap
            );

            String specKey = buildSpecKey(sortedSpecs);

            if (!specKeys.add(specKey)) {
                throw new BusinessException(
                        ErrorCode.SKU_SPEC_ALREADY_EXISTS
                );
            }

            PmsSkuStock entity = new PmsSkuStock();

            entity.setProductId(product.getId());
            entity.setSkuCode(skuCode);
            entity.setPrice(dto.getPrice());
            entity.setStock(dto.getStock());
            entity.setLockedStock(0);
            entity.setLowStock(
                    dto.getLowStock() == null
                            ? 0
                            : dto.getLowStock()
            );
            entity.setPic(normalizeText(dto.getPic()));
            entity.setSpecKey(specKey);
            entity.setSpecData(
                    buildSpecData(sortedSpecs, attributeMap)
            );

            result.add(entity);
        }

        return result;
    }

    private void validateSpecs(
            List<SkuSpecItemDTO> specs,
            Set<Long> allowedAttributeIds,
            Map<Long, PmsProductAttribute> attributeMap,
            Map<Long, PmsProductAttributeValue> valueMap) {

        Set<Long> currentIds = new HashSet<>();

        for (SkuSpecItemDTO spec : specs) {
            Long attributeId = spec.getProductAttributeId();

            if (!currentIds.add(attributeId)) {
                throw new BusinessException(
                        ErrorCode.SKU_SPEC_INVALID
                );
            }

            PmsProductAttribute attribute =
                    attributeMap.get(attributeId);

            if (attribute == null
                    || !allowedAttributeIds.contains(attributeId)
                    || !Integer.valueOf(0).equals(
                    attribute.getType()
            )) {
                throw new BusinessException(
                        ErrorCode.SKU_SPEC_INVALID
                );
            }

            PmsProductAttributeValue productValue =
                    valueMap.get(attributeId);

            if (productValue == null
                    || !containsValue(
                    productValue.getValue(),
                    spec.getValue()
            )) {
                throw new BusinessException(
                        ErrorCode.SKU_SPEC_INVALID
                );
            }
        }
    }

    private boolean containsValue(
            String configuredValues,
            String selectedValue) {

        String target = selectedValue.trim();

        return Arrays.stream(configuredValues.split(",", -1))
                .map(String::trim)
                .anyMatch(target::equals);
    }

    private Set<Long> findAllowedAttributeIds(
            Long productCategoryId) {

        return relationMapper.selectList(
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
    }

    private Map<Long, PmsProductAttribute> findAttributes(
            Set<Long> attributeIds) {

        if (attributeIds.isEmpty()) {
            return Map.of();
        }

        return attributeMapper.selectBatchIds(attributeIds)
                .stream()
                .collect(Collectors.toMap(
                        PmsProductAttribute::getId,
                        Function.identity()
                ));
    }

    private Map<Long, PmsProductAttributeValue>
    findProductAttributeValues(
            Long productId,
            Set<Long> attributeIds) {

        if (attributeIds.isEmpty()) {
            return Map.of();
        }

        return attributeValueMapper.selectList(
                        new LambdaQueryWrapper<
                                PmsProductAttributeValue>()
                                .eq(
                                        PmsProductAttributeValue
                                                ::getProductId,
                                        productId
                                )
                                .in(
                                        PmsProductAttributeValue
                                                ::getProductAttributeId,
                                        attributeIds
                                )
                )
                .stream()
                .collect(Collectors.toMap(
                        PmsProductAttributeValue
                                ::getProductAttributeId,
                        Function.identity()
                ));
    }

    private String buildSpecKey(
            List<SkuSpecItemDTO> specs) {

        if (specs.isEmpty()) {
            return "DEFAULT";
        }

        String specKey = specs.stream()
                .map(spec ->
                        spec.getProductAttributeId()
                                + "="
                                + spec.getValue().trim()
                )
                .collect(Collectors.joining("|"));

        if (specKey.length() > 500) {
            throw new BusinessException(
                    ErrorCode.SKU_SPEC_INVALID
            );
        }

        return specKey;
    }

    private String buildSpecData(
            List<SkuSpecItemDTO> specs,
            Map<Long, PmsProductAttribute> attributeMap) {

        List<Map<String, Object>> data = specs.stream()
                .map(spec -> {
                    PmsProductAttribute attribute =
                            attributeMap.get(
                                    spec.getProductAttributeId()
                            );

                    Map<String, Object> item =
                            new LinkedHashMap<>();

                    item.put(
                            "attributeId",
                            spec.getProductAttributeId()
                    );
                    item.put("name", attribute.getName());
                    item.put("value", spec.getValue().trim());

                    return item;
                })
                .toList();

        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    exception
            );
        }
    }

    private void ensureSkuCodesAvailable(
            Long productId,
            List<PmsSkuStock> skus) {

        if (skus.isEmpty()) {
            return;
        }

        List<String> skuCodes = skus.stream()
                .map(PmsSkuStock::getSkuCode)
                .toList();

        Long count = skuStockMapper.selectCount(
                new LambdaQueryWrapper<PmsSkuStock>()
                        .in(PmsSkuStock::getSkuCode, skuCodes)
                        .ne(PmsSkuStock::getProductId, productId)
        );

        if (count > 0) {
            throw new BusinessException(
                    ErrorCode.SKU_CODE_ALREADY_EXISTS
            );
        }
    }

    private void synchronizeProduct(
            PmsProduct product,
            List<PmsSkuStock> skus) {

        long totalStock = skus.stream()
                .mapToLong(PmsSkuStock::getStock)
                .sum();

        if (totalStock > Integer.MAX_VALUE) {
            throw new BusinessException(
                    ErrorCode.PARAM_VALIDATION_FAILED
            );
        }

        LambdaUpdateWrapper<PmsProduct> wrapper =
                new LambdaUpdateWrapper<PmsProduct>()
                        .eq(PmsProduct::getId, product.getId())
                        .set(
                                PmsProduct::getStock,
                                (int) totalStock
                        );

        if (!skus.isEmpty()) {
            BigDecimal minimumPrice = skus.stream()
                    .map(PmsSkuStock::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElseThrow();

            if (product.getOriginalPrice() != null
                    && product.getOriginalPrice()
                    .compareTo(minimumPrice) < 0) {
                throw new BusinessException(
                        ErrorCode.PRODUCT_PRICE_INVALID
                );
            }

            wrapper.set(PmsProduct::getPrice, minimumPrice);
        }

        if (productMapper.update(null, wrapper) != 1) {
            throw new BusinessException(
                    ErrorCode.DATA_CONFLICT
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

    private String normalizeText(String value) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }
}
