package com.hubilon.google.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "common.invalid.request"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "common.not.found"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "common.unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "common.forbidden"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "common.internal.error"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "user.not.found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "user.already.exists"),
    USER_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "user.invalid.password"),
    USER_INACTIVE(HttpStatus.FORBIDDEN, "user.inactive"),

    // JWT
    JWT_INVALID(HttpStatus.UNAUTHORIZED, "jwt.invalid"),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "jwt.expired");

    private final HttpStatus httpStatus;
    private final String messageKey;
}
