package com.mall.common.exception;

import com.mall.common.api.ApiResult;
import com.mall.common.api.ErrorCode;
import com.mall.common.api.IErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusinessException(
            BusinessException e) {

        IErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResult.<Void>failed(errorCode, e.getMessage()));
    }

    /**
     * DTO 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidationException(
            MethodArgumentNotValidException e) {

        FieldError fieldError = e.getBindingResult().getFieldError();

        String message = fieldError != null
                ? fieldError.getDefaultMessage()
                : ErrorCode.PARAM_VALIDATION_FAILED.getMessage();

        return ResponseEntity
                .status(ErrorCode.PARAM_VALIDATION_FAILED.getHttpStatus())
                .body(ApiResult.<Void>failed(
                        ErrorCode.PARAM_VALIDATION_FAILED,
                        message
                ));
    }

    /**
     * 数据库唯一约束等数据冲突
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException e) {

        log.warn("数据库数据冲突", e);

        return ResponseEntity
                .status(ErrorCode.DATA_CONFLICT.getHttpStatus())
                .body(ApiResult.<Void>failed(ErrorCode.DATA_CONFLICT));
    }

    /**
     * 未预料异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleException(Exception e) {

        log.error("系统异常", e);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResult.<Void>failed(
                        ErrorCode.INTERNAL_SERVER_ERROR
                ));
    }
}