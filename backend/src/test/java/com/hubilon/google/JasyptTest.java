package com.hubilon.google;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.jupiter.api.Test;

class JasyptTest {

    private static final String SECRET_KEY = "local-dev-secret";

    private PooledPBEStringEncryptor createEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(SECRET_KEY);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }

    @Test
    void encryptValues() {
        PooledPBEStringEncryptor encryptor = createEncryptor();

        String dbUsername = "postgres";
        String dbPassword = "postgres";
        String jwtSecret = "bXlTdXBlclNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvbjEyMzQ1Njc4OTA=";

        System.out.println("===== Jasypt Encrypted Values =====");
        System.out.println("DB Username: ENC(" + encryptor.encrypt(dbUsername) + ")");
        System.out.println("DB Password: ENC(" + encryptor.encrypt(dbPassword) + ")");
        System.out.println("JWT Secret:  ENC(" + encryptor.encrypt(jwtSecret) + ")");
        System.out.println("===================================");
    }

    @Test
    void decryptValues() {
        PooledPBEStringEncryptor encryptor = createEncryptor();

        // Replace these with your actual ENC(...) values from application.yaml
        // String encryptedValue = "your_encrypted_value_here";
        // System.out.println("Decrypted: " + encryptor.decrypt(encryptedValue));

        System.out.println("Replace encryptedValue with your ENC(...) content to decrypt.");
    }
}
