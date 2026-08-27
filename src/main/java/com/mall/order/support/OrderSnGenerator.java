package com.mall.order.support;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderSnGenerator {

    public String generate() {
        return "SO"
                + UUID.randomUUID()
                .toString()
                .replace("-", "");
    }
}