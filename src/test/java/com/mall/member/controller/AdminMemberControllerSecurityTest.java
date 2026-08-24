package com.mall.member.controller;

import com.mall.member.dto.AdminMemberQueryDTO;
import com.mall.member.dto.MemberStatusUpdateDTO;
import com.mall.member.service.AdminMemberService;
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
        AdminMemberControllerSecurityTest.TestConfig.class
)
class AdminMemberControllerSecurityTest {

    @Autowired
    private AdminMemberController controller;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void memberReadShouldAllowQueriesAndDenyStatusUpdate() {
        authenticate("member:read");

        assertDoesNotThrow(() ->
                controller.page(new AdminMemberQueryDTO())
        );
        assertDoesNotThrow(() -> controller.detail(7L));
        assertThrows(
                AccessDeniedException.class,
                () -> controller.updateStatus(7L, status())
        );
    }

    @Test
    void memberWriteShouldAllowStatusUpdateAndDenyQueries() {
        authenticate("member:write");

        assertDoesNotThrow(() ->
                controller.updateStatus(7L, status())
        );
        assertThrows(
                AccessDeniedException.class,
                () -> controller.page(new AdminMemberQueryDTO())
        );
        assertThrows(
                AccessDeniedException.class,
                () -> controller.detail(7L)
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

    private MemberStatusUpdateDTO status() {
        MemberStatusUpdateDTO dto = new MemberStatusUpdateDTO();
        dto.setStatus(0);
        return dto;
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        AdminMemberService adminMemberService() {
            return mock(AdminMemberService.class);
        }

        @Bean
        AdminMemberController adminMemberController(
                AdminMemberService memberService) {
            return new AdminMemberController(memberService);
        }
    }
}
