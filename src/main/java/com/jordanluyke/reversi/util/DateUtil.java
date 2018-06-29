package com.jordanluyke.reversi.util;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
public class DateUtil {

    public static Date addTime(Date date, long duration, TimeUnit unit) {
        return new Date(date.getTime() + unit.toMillis(duration));
    }

    public static Timestamp getTimestamp(Date date) {
        return new Timestamp(date.getTime());
    }
}
