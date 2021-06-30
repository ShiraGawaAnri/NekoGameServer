package com.nekonade.common.utils;

import org.apache.commons.lang.RandomStringUtils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AESUtils {

    public static String createSecret(long userId, String zoneId) {
        return RandomStringUtils.randomAscii(16);
    }

    private static SecretKey generateKey(String secret) throws NoSuchAlgorithmException {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        //linux下可能出现问题
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(secret.getBytes());
        keygen.init(128, secureRandom);
        SecretKey original_key = keygen.generateKey();
        byte[] raw = original_key.getEncoded();
        SecretKey key = new SecretKeySpec(raw, "AES");
        return key;
    }

    public static byte[] encode(String secret, byte[] content) {
        try {
            SecretKey key = generateKey(secret);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decode(String secret, byte[] content) {
        try {
            SecretKey key = generateKey(secret);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String value = "aaaaafff";
        byte[] content = value.getBytes();
        String secret = createSecret(23, "01");
        byte[] result = encode(secret, content);
        byte[] dec = decode(secret, result);
        System.out.println(new String(dec));

        System.out.println(RandomStringUtils.randomAscii(16));
    }
}
