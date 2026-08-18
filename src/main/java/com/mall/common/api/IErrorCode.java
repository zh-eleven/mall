package com.mall.common.api;

import org.springframework.http.HttpStatus;

public interface IErrorCode {

    int getCode();

    String getMessage();

    HttpStatus getHttpStatus();
}