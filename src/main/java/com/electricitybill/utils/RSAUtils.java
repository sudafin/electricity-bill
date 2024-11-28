package com.electricitybill.utils;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtils {

    // 生成 RSA 密钥对
    public static KeyPair generateKeyPair() {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyPairGenerator.initialize(2048); // 2048位的密钥
        return keyPairGenerator.generateKeyPair();
    }

    // 获取公钥的Base64编码
    public static String getPublicKey(KeyPair keyPair) {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    // 获取私钥的Base64编码
    public static String getPrivateKey(KeyPair keyPair) {
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    // 使用私钥解密
    public static String decrypt(String encryptedData, String privateKeyBase64) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
        RSAPrivateKey privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    // 使用公钥加密
    public static String encrypt(String data, String publicKeyBase64) throws Exception {
        byte[] dataBytes = data.getBytes();
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(dataBytes);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
