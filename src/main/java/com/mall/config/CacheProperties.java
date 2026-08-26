package com.mall.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "mall.cache")
public class CacheProperties {

    @NotBlank
    private String keyPrefix = "mall:v1:";

    @NotNull
    private Duration categoryTreeTtl = Duration.ofMinutes(30);

    @NotNull
    private Duration productDetailTtl = Duration.ofMinutes(10);

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Duration getCategoryTreeTtl() {
        return categoryTreeTtl;
    }

    public void setCategoryTreeTtl(Duration categoryTreeTtl) {
        this.categoryTreeTtl = categoryTreeTtl;
    }

    public Duration getProductDetailTtl() {
        return productDetailTtl;
    }

    public void setProductDetailTtl(Duration productDetailTtl) {
        this.productDetailTtl = productDetailTtl;
    }
}