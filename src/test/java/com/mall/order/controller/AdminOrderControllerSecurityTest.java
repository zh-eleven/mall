package com.mall.order.controller;

import com.mall.order.dto.AdminOrderQueryDTO;
import com.mall.order.dto.OrderShipDTO;
import com.mall.order.service.AdminOrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig(
        AdminOrderControllerSecurityTest.TestConfig.class
)
class AdminOrderControllerSecurityTest {

    @Autowired
    private AdminOrderController controller;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void orderReadShouldAllowQueriesAndDenyShipping() {

        authenticate("order:read");

        assertDoesNotThrow(() ->
                controller.page(new AdminOrderQueryDTO())
        );

        assertDoesNotThrow(() ->
                controller.detail(500L)
        );

        assertThrows(
                AccessDeniedException.class,
                () -> controller.ship(
                        500L,
                        shipDTO()
                )
        );
    }

    @Test
    void orderWriteShouldAllowShippingAndDenyQueries() {

        authenticate("order:write");

        assertDoesNotThrow(() ->
                controller.ship(500L, shipDTO())
        );

        assertThrows(
                AccessDeniedException.class,
                () -> controller.page(
                        new AdminOrderQueryDTO()
                )
        );

        assertThrows(
                AccessDeniedException.class,
                () -> controller.detail(500L)
        );
    }

    private void authenticate(String authority) {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken
                                .authenticated(
                                        "admin",
                                        "n/a",
                                        List.of(
                                                new SimpleGrantedAuthority(
                                                        authority
                                                )
                                        )
                                )
                );
    }

    private OrderShipDTO shipDTO() {
        OrderShipDTO dto = new OrderShipDTO();
        dto.setDeliveryCompany("顺丰速运");
        dto.setDeliverySn("SF123456789");
        return dto;
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        AdminOrderService adminOrderService() {
            return mock(AdminOrderService.class);
        }

        @Bean
        AdminOrderController adminOrderController(
                AdminOrderService orderService) {
            return new AdminOrderController(orderService);
        }
    }
}
