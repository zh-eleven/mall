package com.mall.seckill.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties
        .ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "seckill")
public class SeckillProperties {

    @Min(1000)
    private long compensationScanDelay = 60000;

    @Min(1)
    @Max(1000)
    private int compensationBatchSize = 100;

    @Min(10000)
    private long reservationStaleMillis = 120000;

    @Min(1)
    @Max(5000)
    private int reservationSkuBatchSize = 1000;

    @Min(1)
    @Max(1000)
    private int reservationRequestBatchSize = 100;

    @Min(0)
    private long activityCloseGraceMillis = 300000;

    @Min(1)
    @Max(1000)
    private int activityCloseBatchSize = 100;
}
