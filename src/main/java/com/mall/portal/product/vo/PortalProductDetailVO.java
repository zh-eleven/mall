package com.mall.portal.product.vo;

import com.mall.product.entity.PmsProduct;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public record PortalProductDetailVO(
        Long id,
        Long brandId,
        Long productCategoryId,
        String name,
        String subTitle,
        BigDecimal price,
        BigDecimal originalPrice,
        String unit,
        String pic,
        List<String> albumPics,
        String description,
        String detailTitle,
        String detailDesc,
        String detailHtml,
        List<PortalProductAttributeVO> attributes,
        List<PortalSkuVO> skus
) {

    public static PortalProductDetailVO from(
            PmsProduct product,
            List<PortalProductAttributeVO> attributes,
            List<PortalSkuVO> skus) {

        return new PortalProductDetailVO(
                product.getId(),
                product.getBrandId(),
                product.getProductCategoryId(),
                product.getName(),
                product.getSubTitle(),
                product.getPrice(),
                product.getOriginalPrice(),
                product.getUnit(),
                product.getPic(),
                parseAlbumPics(product.getAlbumPics()),
                product.getDescription(),
                product.getDetailTitle(),
                product.getDetailDesc(),
                product.getDetailHtml(),
                List.copyOf(attributes),
                List.copyOf(skus)
        );
    }

    private static List<String> parseAlbumPics(
            String albumPics) {

        if (albumPics == null || albumPics.isBlank()) {
            return List.of();
        }

        return Arrays.stream(albumPics.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}