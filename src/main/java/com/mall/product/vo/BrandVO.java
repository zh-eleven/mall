package com.mall.product.vo;

import com.mall.product.entity.PmsBrand;

import java.time.LocalDateTime;

public record BrandVO(
        Long id,
        String name,
        String firstLetter,
        Integer sort,
        Integer factoryStatus,
        Integer showStatus,
        Integer productCount,
        Integer productCommentCount,
        String logo,
        String bigPic,
        String brandStory,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {

    public static BrandVO from(PmsBrand brand) {
        return new BrandVO(
                brand.getId(),
                brand.getName(),
                brand.getFirstLetter(),
                brand.getSort(),
                brand.getFactoryStatus(),
                brand.getShowStatus(),
                brand.getProductCount(),
                brand.getProductCommentCount(),
                brand.getLogo(),
                brand.getBigPic(),
                brand.getBrandStory(),
                brand.getCreateTime(),
                brand.getUpdateTime()
        );
    }
}