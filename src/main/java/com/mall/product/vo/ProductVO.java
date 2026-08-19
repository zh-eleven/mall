package com.mall.product.vo;

import com.mall.product.entity.PmsProduct;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductVO(
        Long id,
        Long brandId,
        Long productCategoryId,
        String name,
        String subTitle,
        String productSn,
        BigDecimal price,
        BigDecimal originalPrice,
        Integer stock,
        Integer lowStock,
        String unit,
        BigDecimal weight,
        Integer publishStatus,
        Integer newStatus,
        Integer recommendStatus,
        Integer verifyStatus,
        Integer sort,
        String pic,
        String albumPics,
        String description,
        String detailTitle,
        String detailDesc,
        String detailHtml,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {

    public static ProductVO from(PmsProduct product) {
        return new ProductVO(
                product.getId(),
                product.getBrandId(),
                product.getProductCategoryId(),
                product.getName(),
                product.getSubTitle(),
                product.getProductSn(),
                product.getPrice(),
                product.getOriginalPrice(),
                product.getStock(),
                product.getLowStock(),
                product.getUnit(),
                product.getWeight(),
                product.getPublishStatus(),
                product.getNewStatus(),
                product.getRecommendStatus(),
                product.getVerifyStatus(),
                product.getSort(),
                product.getPic(),
                product.getAlbumPics(),
                product.getDescription(),
                product.getDetailTitle(),
                product.getDetailDesc(),
                product.getDetailHtml(),
                product.getCreateTime(),
                product.getUpdateTime()
        );
    }
}