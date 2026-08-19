package com.mall.common.exception;

import com.mall.common.api.ApiResult;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.IErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleNoResourceFound(
            NoResourceFoundException exception) {

        log.debug("接口不存在: {}", exception.getResourcePath());

        return failed(ErrorCode.ENDPOINT_NOT_FOUND);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleNoHandlerFound(
            NoHandlerFoundException exception) {

        log.debug("接口不存在: {}", exception.getRequestURL());

        return failed(ErrorCode.ENDPOINT_NOT_FOUND);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidationException(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .sorted(Comparator
                        .comparing(FieldError::getField)
                        .thenComparing(error -> Objects.toString(
                                error.getDefaultMessage(),
                                "参数值无效"
                        )))
                .map(error -> formatParameterMessage(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = ErrorCode.PARAM_VALIDATION_FAILED.getMessage();
        }

        return failed(ErrorCode.PARAM_VALIDATION_FAILED, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraintViolation(
            ConstraintViolationException exception) {

        String message = exception.getConstraintViolations()
                .stream()
                .sorted(Comparator.comparing(violation ->
                        violation.getPropertyPath().toString()))
                .map(this::formatConstraintViolation)
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = ErrorCode.PARAM_VALIDATION_FAILED.getMessage();
        }

        return failed(ErrorCode.PARAM_VALIDATION_FAILED, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResult<Void>> handleMessageNotReadable(
            HttpMessageNotReadableException exception) {

        return failed(
                ErrorCode.PARAM_VALIDATION_FAILED,
                "请求体格式错误"
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResult<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {

        return failed(
                ErrorCode.PARAM_VALIDATION_FAILED,
                formatParameterMessage(
                        exception.getName(),
                        "参数类型错误"
                )
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResult<Void>> handleMissingParameter(
            MissingServletRequestParameterException exception) {

        return failed(
                ErrorCode.PARAM_VALIDATION_FAILED,
                formatParameterMessage(
                        exception.getParameterName(),
                        "缺少必填参数"
                )
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusinessException(
            BusinessException exception) {

        IErrorCode errorCode = exception.getErrorCode();
        log.warn(
                "业务异常: code={}, message={}",
                errorCode.getCode(),
                exception.getMessage(),
                exception
        );

        return failed(errorCode, exception.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResult<Void>> handleAuthenticationException(
            AuthenticationException exception) {

        log.debug("认证失败", exception);
        return failed(ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResult<Void>> handleAccessDeniedException(
            AccessDeniedException exception) {

        log.debug("权限不足", exception);
        return failed(ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException exception) {

        log.warn("数据库数据冲突", exception);
        return failed(ErrorCode.DATA_CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleException(
            Exception exception) {

        log.error("未预期的系统异常", exception);
        return failed(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String formatConstraintViolation(
            ConstraintViolation<?> violation) {

        String propertyPath = violation.getPropertyPath().toString();
        int lastDot = propertyPath.lastIndexOf('.');
        String parameterName = lastDot >= 0
                ? propertyPath.substring(lastDot + 1)
                : propertyPath;

        return formatParameterMessage(
                parameterName,
                violation.getMessage()
        );
    }

    private String formatParameterMessage(
            String parameterName,
            String validationMessage) {

        String message = Objects.requireNonNullElse(
                validationMessage,
                "参数值无效"
        );
        return parameterName + ": " + message;
    }

    private ResponseEntity<ApiResult<Void>> failed(
            IErrorCode errorCode) {
        return failed(errorCode, errorCode.getMessage());
    }

    private ResponseEntity<ApiResult<Void>> failed(
            IErrorCode errorCode,
            String message) {

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResult.<Void>failed(errorCode, message));
    }
}
