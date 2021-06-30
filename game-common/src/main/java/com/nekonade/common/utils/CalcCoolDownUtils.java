package com.nekonade.common.utils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class CalcCoolDownUtils {


    //凌晨5点为基准
    private static final int RESETHOUR = 5;

    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("+8");

    //计算距离下个刷新日还需要多长毫秒
    //0 - 明天
    //1 - 周1
    //缺省 明天

    public static long calcCoolDownTimestamp() {
        return calcCoolDownTimestamp(0);
    }

    public static long calcCoolDownTimestamp(Integer weekDay) {
        LocalDateTime now = LocalDateTime.now(ZONE_OFFSET);
        LocalDateTime resetDateTime;
        switch (weekDay) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                resetDateTime = now.with(DayOfWeek.of(weekDay)).withHour(RESETHOUR).withMinute(0).withSecond(0).withNano(0);
                if (resetDateTime.isBefore(now)) {
                    resetDateTime = resetDateTime.plusWeeks(1);
                }
                break;
            case 0:
            default:
                resetDateTime = now.withHour(RESETHOUR).withMinute(0).withSecond(0).withNano(0);
                if (now.getHour() > RESETHOUR) {
                    resetDateTime = resetDateTime.plusDays(1);
                }
                break;
        }
        return Duration.between(now, resetDateTime).toMillis();
    }
}
