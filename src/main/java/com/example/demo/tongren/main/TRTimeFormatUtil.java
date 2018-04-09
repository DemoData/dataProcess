package com.example.demo.tongren.main;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TRTimeFormatUtil {

    private static SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss");
    private static SimpleDateFormat standardFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String formatTime(String time){
        int length = time.length();
        if(length < 12){
            System.out.println(time);
        }
        for(int i = length; i < 14; i++){
            time += "0";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(time.substring(0, 4));
        stringBuilder.append("/");
        stringBuilder.append(time.substring(4,6));
        stringBuilder.append("/");
        stringBuilder.append(time.substring(6, 8));
        stringBuilder.append(" ");
        stringBuilder.append(time.substring(8, 10));
        stringBuilder.append(":");
        stringBuilder.append(time.substring(10,12));
        stringBuilder.append(":");
        stringBuilder.append(time.substring(12));
        return stringBuilder.toString();
    }

    public static String formatTimeBySimpleDateFormat(String time) throws Exception{
        int length = time.length();
        if(length < 12){
            System.out.println(time);
        }
        for(int i = length; i < 14; i++){
            time += "0";
        }
        Date date = yyyyMMddHHmmss.parse(time);
        return standardFormat.format(date);
    }

    public static void main(String[] args) throws Exception{

    }
}
