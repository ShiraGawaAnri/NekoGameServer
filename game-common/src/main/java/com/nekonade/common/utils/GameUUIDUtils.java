package com.nekonade.common.utils;

import org.apache.logging.log4j.core.util.UuidUtil;

public class GameUUIDUtils {

    public static String getUId() {
        return UuidUtil.getTimeBasedUuid().toString().replace("-", "");
    }

    public static void main(String[] args) {
        System.out.println(GameUUIDUtils.getUId().length());
    }
}
