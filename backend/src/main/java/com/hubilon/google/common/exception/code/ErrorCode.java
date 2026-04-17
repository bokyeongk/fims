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
    USER_SOCIAL_LOGIN_ONLY(HttpStatus.BAD_REQUEST, "user.social.login.only"),

    // JWT
    JWT_INVALID(HttpStatus.UNAUTHORIZED, "jwt.invalid"),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "jwt.expired"),

    // Profile
    SIGNATURE_TOO_LARGE(HttpStatus.BAD_REQUEST, "signature.too.large"),
    EMAIL_VERIFY_INVALID(HttpStatus.BAD_REQUEST, "email.verify.invalid"),
    EMAIL_VERIFY_EXPIRED(HttpStatus.BAD_REQUEST, "email.verify.expired"),
    EMAIL_VERIFY_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "email.verify.exceeded"),
    PHONE_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "phone.format.invalid");

    private final HttpStatus httpStatus;
    private final String messageKey;
}
