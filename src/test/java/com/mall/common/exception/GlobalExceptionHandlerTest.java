package com.mall.common.exception;

import com.mall.common.api.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void validationErrorShouldReturnStable400Response()
            throws Exception {

        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message")
                        .value("name: 分类名称不能为空"));
    }

    @Test
    void malformedJsonShouldReturn400WithoutInternalDetail()
            throws Exception {

        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message").value("请求体格式错误"));
    }

    @Test
    void typeMismatchShouldReturn400() throws Exception {
        mockMvc.perform(get("/test/type/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message")
                        .value("categoryId: 参数类型错误"));
    }

    @Test
    void missingParameterShouldReturn400() throws Exception {
        mockMvc.perform(get("/test/missing"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message")
                        .value("name: 缺少必填参数"));
    }

    @Test
    void authenticationExceptionShouldReturn401() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100))
                .andExpect(jsonPath("$.message")
                        .value("未登录或登录已过期"));
    }

    @Test
    void accessDeniedShouldReturn403() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(40300))
                .andExpect(jsonPath("$.message").value("没有访问权限"));
    }

    @Test
    void missingResourceShouldReturn404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40407))
                .andExpect(jsonPath("$.message").value("商品分类不存在"));
    }

    @Test
    void duplicateNameShouldReturn409() throws Exception {
        mockMvc.perform(get("/test/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40913))
                .andExpect(jsonPath("$.message")
                        .value("同级分类名称已存在"));
    }

    @Test
    void dataIntegrityViolationShouldReturnSafe409Response()
            throws Exception {

        mockMvc.perform(get("/test/data-conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40900))
                .andExpect(jsonPath("$.message").value("数据冲突"))
                .andExpect(content().string(not(containsString("SQL"))));
    }

    @Test
    void unknownExceptionShouldReturnSafe500Response()
            throws Exception {

        mockMvc.perform(get("/test/unknown"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(50000))
                .andExpect(jsonPath("$.message").value("服务器内部错误"))
                .andExpect(content().string(not(containsString(
                        "InternalImplementationClass"
                ))));
    }

    @Test
    @SuppressWarnings("unchecked")
    void constraintViolationShouldUseLastPathSegment() {
        ConstraintViolation<Object> violation =
                mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn("page.pageNum");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage())
                .thenReturn("页码必须大于等于1");

        ConstraintViolationException exception =
                new ConstraintViolationException(Set.of(violation));

        var response = new GlobalExceptionHandler()
                .handleConstraintViolation(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(40001, response.getBody().getCode());
        assertEquals(
                "pageNum: 页码必须大于等于1",
                response.getBody().getMessage()
        );
    }

    @RestController
    static class TestController {

        @PostMapping("/test/validation")
        void validation(@Valid @RequestBody CategoryRequest request) {
        }

        @GetMapping("/test/type/{categoryId}")
        void type(@PathVariable Long categoryId) {
        }

        @GetMapping("/test/missing")
        void missing(@RequestParam String name) {
        }

        @GetMapping("/test/unauthorized")
        void unauthorized() {
            throw new BadCredentialsException("内部认证详情");
        }

        @GetMapping("/test/forbidden")
        void forbidden() {
            throw new AccessDeniedException("内部权限详情");
        }

        @GetMapping("/test/not-found")
        void notFound() {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        @GetMapping("/test/duplicate")
        void duplicate() {
            throw new BusinessException(
                    ErrorCode.CATEGORY_NAME_ALREADY_EXISTS
            );
        }

        @GetMapping("/test/data-conflict")
        void dataConflict() {
            throw new DataIntegrityViolationException(
                    "SQL duplicate key internal detail"
            );
        }

        @GetMapping("/test/unknown")
        void unknown() {
            throw new IllegalStateException(
                    "InternalImplementationClass stack detail"
            );
        }
    }

    record CategoryRequest(
            @NotBlank(message = "分类名称不能为空")
            String name) {
    }
}
