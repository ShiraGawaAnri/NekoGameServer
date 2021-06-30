package com.nekonade.common.utils;

import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class HmacUtils {

    private static SecretKey generateKey(String type,String secret) throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance(type);
        //SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        //linux下可能出现问题，替换为以下暂代
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(secret.getBytes());
        keygen.init(128, secureRandom);
        SecretKey key = keygen.generateKey();
        return key;
    }

    // 获取 HmacMD5 Key
    public static byte[] getHmacMd5Key(String secureKey) {
        return getHmacKey("HmacMD5",secureKey);
    }

    // 获取 HmacSHA256
    public static byte[] getHmacSha256Key(String secureKey) {
        return getHmacKey("HmacSHA256",secureKey);
    }

    // 获取 HmacSHA512
    public static byte[] getHmacSha512Key(String secureKey) {
        return getHmacKey("HmacSHA512",secureKey);
    }

    // 获取 HMAC Key
    public static byte[] getHmacKey(String type,String secureKey) {
        try {
            // 1、创建密钥生成器
            //KeyGenerator keyGenerator = KeyGenerator.getInstance(type);
            // 2、产生密钥
            SecretKey secretKey = generateKey(type,secureKey);
            // 3、获取密钥
            byte[] key = secretKey.getEncoded();
            return key;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // HMAC MD5 加密
    public static String encryptHmacMD5(byte[] data, byte[] key) {
        HMac hmac = new HMac(new MD5Digest());
        hmac.update(data, 0, data.length);
        byte[] rsData = new byte[hmac.getMacSize()];
        hmac.doFinal(rsData, 0);
        return Hex.toHexString(rsData);
    }

    // HMAC SHA256 加密
    public static String encryptHmacSHA256(byte[] data, byte[] key) {
        HMac hmac = new HMac(new SHA256Digest());
        hmac.update(data, 0, data.length);
        byte[] rsData = new byte[hmac.getMacSize()];
        hmac.doFinal(rsData, 0);
        return Hex.toHexString(rsData);
    }

    // HMAC SHA521 加密
    public static String encryptHmacSHA512(byte[] data, byte[] key) {
        HMac hmac = new HMac(new SHA512Digest());
        hmac.update(data, 0, data.length);
        byte[] rsData = new byte[hmac.getMacSize()];
        hmac.doFinal(rsData, 0);
        return Hex.toHexString(rsData);
    }

}
