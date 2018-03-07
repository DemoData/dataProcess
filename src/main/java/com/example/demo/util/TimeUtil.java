package com.example.demo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author liulun
 */
public class TimeUtil {
    private static final ThreadLocal<SimpleDateFormat> FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> YYYYMMDD = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    private static final int HOUR = 3600;
    private static final int MINUTE = 60;

    public static String intToStandardTime(Long time) {
        Date d = new Date(time);
        return FORMATTER.get().format(d);
    }

    public static String intToYYYYMMDD(Long time) {
        Date d = new Date(time);
        return YYYYMMDD.get().format(d);
    }

    /**
     * 时间字符串去掉中间的符号变为long
     * @param date
     * @return
     */
    public static Long dateStringToLong(String date){
        return Long.valueOf(date.substring(0,19).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", ""));
    }

    public static Long dateStringToMillis(String dateString) {
        Date date;
        try {
            date = FORMATTER.get().parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L;
        }
        return date.getTime();
    }

    public static Long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static String getCurrentTimeString() {
        Date d = new Date(getCurrentTimeMillis());
        return FORMATTER.get().format(d);
    }


    public static String getLastDayStart(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return YYYYMMDD.get().format(cal.getTime()) + " 00:00:00";
    }

    public static String getLastDayEnd(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return YYYYMMDD.get().format(cal.getTime()) + " 23:59:59";
    }

    public static String getLastTwoDayEnd(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        return YYYYMMDD.get().format(cal.getTime()) + " 23:59:59";
    }

    public static String getTodayStart(){
        Calendar cal = Calendar.getInstance();
        return YYYYMMDD.get().format(cal.getTime()) + " 00:00:00";
    }
    /**
     * 获取时间差为XX小时XX分XX秒
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static String formatToHMS(String startTime, String endTime) {
        try {
            Date startDate = FORMATTER.get().parse(startTime);
            Date endDate = FORMATTER.get().parse(endTime);
            long lStartDate = startDate.getTime();
            long lEndDate = endDate.getTime();
            int time = (int) (lEndDate - lStartDate) / 1000;
            StringBuilder stringBuilder = new StringBuilder();
            if (time >= HOUR) {
                stringBuilder.append(time / HOUR + "小时");
                time = time % HOUR;
            }
            if (time >= MINUTE) {
                stringBuilder.append(time / MINUTE + "分");
                time = time % MINUTE;
            }
            if (time > 0) {
                stringBuilder.append(time + "秒");
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return "格式化错误";
        }
    }

    public static String medicalContentTimeFormat(String str){
        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, 10));
        sb.append(" ");
        sb.append(str.substring(10));
        sb.append(":00.0");
        return sb.toString();
    }


    public static void main(String[] args) {
        System.out.println(medicalContentTimeFormat("2016-05-2717:33"));
    }
}
