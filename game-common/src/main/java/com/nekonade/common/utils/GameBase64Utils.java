package com.nekonade.common.utils;

import org.springframework.util.Base64Utils;

public class GameBase64Utils {

    public static byte[] decodeFromString(String value) {
        return Base64Utils.decodeFromString(value);
    }

    public static String encodeToString(byte[] data) {
        return Base64Utils.encodeToString(data);
    }
}
