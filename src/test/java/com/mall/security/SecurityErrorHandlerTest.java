package com.mall.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityErrorHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void authenticationEntryPointShouldReturnApiResult401()
            throws Exception {

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        new RestAuthenticationEntryPoint(objectMapper).commence(
                new MockHttpServletRequest(),
                response,
                new BadCredentialsException("敏感认证详情")
        );

        JsonNode body = objectMapper.readTree(
                response.getContentAsByteArray()
        );

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals(40100, body.get("code").asInt());
        assertEquals("未登录或登录已过期", body.get("message").asText());
        assertFalse(response.getContentAsString().contains("敏感认证详情"));
    }

    @Test
    void accessDeniedHandlerShouldReturnApiResult403()
            throws Exception {

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        new RestAccessDeniedHandler(objectMapper).handle(
                new MockHttpServletRequest(),
                response,
                new AccessDeniedException("敏感权限详情")
        );

        JsonNode body = objectMapper.readTree(
                response.getContentAsByteArray()
        );

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals(40300, body.get("code").asInt());
        assertEquals("没有访问权限", body.get("message").asText());
        assertFalse(response.getContentAsString().contains("敏感权限详情"));
    }
}
