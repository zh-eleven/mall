package com.mall.order.controller;

import com.mall.order.dto.AdminRefundQueryDTO;
import com.mall.order.dto.RefundApproveDTO;
import com.mall.order.dto.RefundRejectDTO;
import com.mall.order.service.AdminOrderRefundService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig(
        AdminRefundControllerSecurityTest.TestConfig.class
)
class AdminRefundControllerSecurityTest {

    @Autowired
    private AdminRefundController controller;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void refundReadShouldAllowQueriesAndDenyReview() {
        authenticate("refund:read");

        assertDoesNotThrow(() ->
                controller.page(new AdminRefundQueryDTO())
        );
        assertDoesNotThrow(() -> controller.detail(900L));
        assertThrows(
                AccessDeniedException.class,
                () -> controller.approve(900L, approveDTO())
        );
        assertThrows(
                AccessDeniedException.class,
                () -> controller.reject(900L, rejectDTO())
        );
    }

    @Test
    void refundWriteShouldAllowReviewAndDenyQueries() {
        authenticate("refund:write");

        assertDoesNotThrow(() ->
                controller.approve(900L, approveDTO())
        );
        assertDoesNotThrow(() ->
                controller.reject(900L, rejectDTO())
        );
        assertThrows(
                AccessDeniedException.class,
                () -> controller.page(new AdminRefundQueryDTO())
        );
        assertThrows(
                AccessDeniedException.class,
                () -> controller.detail(900L)
        );
    }

    private void authenticate(String authority) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        "admin",
                        "n/a",
                        List.of(new SimpleGrantedAuthority(authority))
                )
        );
    }

    private RefundApproveDTO approveDTO() {
        return new RefundApproveDTO();
    }

    private RefundRejectDTO rejectDTO() {
        RefundRejectDTO dto = new RefundRejectDTO();
        dto.setAdminNote("拒绝原因");
        return dto;
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        AdminOrderRefundService refundService() {
            return mock(AdminOrderRefundService.class);
        }

        @Bean
        AdminRefundController adminRefundController(
                AdminOrderRefundService refundService) {
            return new AdminRefundController(refundService);
        }
    }
}
