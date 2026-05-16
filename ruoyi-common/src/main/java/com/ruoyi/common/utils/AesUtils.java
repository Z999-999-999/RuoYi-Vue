package com.ruoyi.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES 加解密工具（用于 API Key 等敏感数据加密存储）
 * 算法: AES/CBC/PKCS5Padding，128位密钥
 */
public class AesUtils
{
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String AES = "AES";

    /**
     * 加密
     * @param plaintext 明文
     * @param key 密钥（至少16字符，取前16字节）
     * @return Base64 编码的密文（IV + 密文）
     */
    public static String encrypt(String plaintext, String key)
    {
        try
        {
            byte[] keyBytes = normalizeKey(key);
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, AES);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            // 拼接: IV(16字节) + 密文
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        }
        catch (Exception e)
        {
            throw new RuntimeException("AES加密失败", e);
        }
    }

    /**
     * 解密
     * @param ciphertext Base64 编码的密文（IV + 密文）
     * @param key 密钥（至少16字符，取前16字节）
     * @return 明文
     */
    public static String decrypt(String ciphertext, String key)
    {
        try
        {
            byte[] keyBytes = normalizeKey(key);
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            byte[] iv = new byte[16];
            byte[] encrypted = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encrypted, 0, encrypted.length);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, AES);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new RuntimeException("AES解密失败", e);
        }
    }

    /**
     * 判断字符串是否为 AES 加密后的格式（Base64 且长度 > 24）
     */
    public static boolean isEncrypted(String value)
    {
        if (value == null || value.isEmpty()) return false;
        // API Key 原始格式以 "ak_" 开头，加密后是 Base64 不以此开头
        if (value.startsWith("ak_")) return false;
        try
        {
            byte[] decoded = Base64.getDecoder().decode(value);
            return decoded.length > 16; // IV(16) + 至少1块密文
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private static byte[] normalizeKey(String key)
    {
        byte[] keyBytes = new byte[16];
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(raw, 0, keyBytes, 0, Math.min(raw.length, 16));
        return keyBytes;
    }
}
