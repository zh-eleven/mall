package com.mall.portal.product.vo;

import com.mall.product.entity.PmsProduct;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public record PortalProductDetailCacheVO(
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
        List<PortalSkuCacheVO> skus
) {

    public PortalProductDetailCacheVO {
        albumPics = albumPics == null
                ? List.of()
                : List.copyOf(albumPics);
        attributes = attributes == null
                ? List.of()
                : List.copyOf(attributes);
        skus = skus == null
                ? List.of()
                : List.copyOf(skus);
    }

    public static PortalProductDetailCacheVO from(
            PmsProduct product,
            List<PortalProductAttributeVO> attributes,
            List<PortalSkuCacheVO> skus) {

        return new PortalProductDetailCacheVO(
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
                attributes,
                skus
        );
    }

    private static List<String> parseAlbumPics(String albumPics) {
        if (albumPics == null || albumPics.isBlank()) {
            return List.of();
        }

        return Arrays.stream(albumPics.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
