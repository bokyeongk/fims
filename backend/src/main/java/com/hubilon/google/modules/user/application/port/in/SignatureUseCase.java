package com.hubilon.google.modules.user.application.port.in;

public interface SignatureUseCase {

    String saveSignature(Long userId, String signatureData);

    void deleteSignature(Long userId);
}
