package com.nekonade.common.utils;

import org.apache.commons.lang3.time.DateFormatUtils;

public class GameTimeUtils {
    /**
     * 日期格式：yyyy-MM-dd HH:mm:ss
     */
    public final static String NORMAL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static boolean checkTimeIsBetween0(long startTime, long endTime, long now) {
        if(startTime != 0 || endTime != 0){
            if(startTime != 0 && now >= startTime){
                return endTime == 0 || now <= endTime;
            }else if(endTime != 0 && endTime >= now){
                return startTime <= now;
            }
        }else{
            return true;
        }
        return false;
    }

    public static String getStringDate(long millTime) {
        String value = DateFormatUtils.format(millTime, NORMAL_DATE_FORMAT);
        return value;
    }

    /**
     * 0 - 代表无限制
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @return
     */
    public static boolean checkTimeIsBetween(long startTime,long endTime){
        long now = System.currentTimeMillis();
        return checkTimeIsBetween0(startTime, endTime, now);
    }

    public static boolean checkTimeIsBetween(long startTime,long endTime,long targetTime){
        return checkTimeIsBetween0(startTime, endTime, targetTime);
    }
}
