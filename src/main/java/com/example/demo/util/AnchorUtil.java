package com.example.demo.util;

import com.example.demo.common.util.PatternUtil;

import java.util.regex.Matcher;

public class AnchorUtil {

    public static int countAnchorCount(String text){
        int count = 0;
        Matcher matcher = PatternUtil.ANCHOR_PATTERN.matcher(text);
        while(matcher.find()){
            count++;
        }
        return count;
    }
}
