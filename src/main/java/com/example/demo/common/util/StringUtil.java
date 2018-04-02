package com.example.demo.common.util;

public class StringUtil {
    /**
     * 去除字符串中所包含的空格（包括:空格(全角，半角)、制表符、换页符等）
     * @param s
     * @return
     */
    public static String removeAllBlank(String s){
        String result = "";
        if(null!=s && !"".equals(s)){
            result = s.replaceAll("[　*| *| *|\\s*]*", "");
        }
        return result;
    }

    /**
     * 去除字符串中头部和尾部所包含的空格（包括:空格(全角，半角)、制表符、换页符等）
     * @param s
     * @return
     */
    public static String trim(String s){
        String result = "";
        if(null!=s && !"".equals(s)){
            result = s.replaceAll("^[　*| *| *|\\s*]*", "").replaceAll("[　*| *| *|\\s*]*$", "");
        }
        return result;
    }

    /**
     * 替換字符串中所包含的空格、回车、换行符、制表符
     *
     * @param source
     * @return
     */
    public static String replaceBlank(String source, String replaceStr) {
        String result = "";
        if (null != source && !"".equals(source)) {
            result = source.replaceAll("\\s*|\\t|\\r|\\n", replaceStr);
        }
        return result;
    }
}
