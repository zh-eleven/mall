package com.mall.order.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "order")
public class OrderProperties {

    @Min(1)
    private long timeoutMinutes = 30;

    @Min(1000)
    private long timeoutScanDelay = 60000;

    @Min(1)
    @Max(1000)
    private int timeoutBatchSize = 100;
}
