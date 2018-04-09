package com.example.demo.tongren.main;

import com.example.demo.common.util.PatternUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TRHtmlToTxtMain {

    public static Pattern anchorPattern = Pattern.compile("^([\\u4e00-\\u9fa5（）]{1,20})[：|:]");
    public static void main(String[] args) throws Exception{
        String path = "/Users/liulun/Desktop/同仁入库/fenlei/入院记录/24小时入院死亡记录/Html/2015035391_入院记录_24小时入院死亡记录_a5e9230e-b362-46b2-a179-081b859b380b.html";
        File file = new File(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Unicode"));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        boolean tableFlag = false;
        boolean bodyFlag = false;
        while((line = bufferedReader.readLine()) != null){
            if(line.contains("<BODY")){
                bodyFlag = true;
            }
            if(!bodyFlag){
                continue;
            }
            if(line.contains("<TABLE")){
                tableFlag = true;
            }else if(line.contains("</TABLE>")){
                tableFlag = false;
            }
            line = line.replaceAll("<B>", "【【").replaceAll("</B>", "】】").trim();
            line = line.replaceAll("<([\\s\\S]+?)>", "");
            if(tableFlag){
                Matcher matcher = anchorPattern.matcher(line);
                if(matcher.find()){
                    String anchor = matcher.group(1);
                    StringBuilder stringBuilder1 = new StringBuilder();
                    stringBuilder1.append("【【");
                    stringBuilder1.append(anchor);
                    stringBuilder1.append("】】");
                    stringBuilder1.append(line.substring(anchor.length()));
                    line = stringBuilder1.toString();
                }
            }
            if(StringUtils.isNotBlank(line)){
                stringBuilder.append(line);
                //stringBuilder.append("\n");
            }

        }
        String result = stringBuilder.toString().replaceAll("【【\t*】】", "");
        result = result.replaceAll("【【", "\n【【");
        StringReader stringReader = new StringReader(result);
        BufferedReader bufferedReader1 = new BufferedReader(stringReader);
        StringBuilder resultStringBuilder = new StringBuilder();
        while((line = bufferedReader1.readLine()) != null){
            resultStringBuilder.append(processAnchorContainColon(line));
            resultStringBuilder.append("\n");
        }
        System.out.println(resultStringBuilder.toString());
    }

    public static String processAnchorContainColon(String line){
        if(!line.contains("】】")){
            return line;
        }
        String anchor = line.substring(0, line.indexOf("】】"));
        if(anchor.contains("：")){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(line.substring(0, line.indexOf("：")));
            stringBuilder.append("】】：");
            stringBuilder.append(line.substring(line.indexOf("】】") + 2));
            return stringBuilder.toString();
        }else{
            return line;
        }


    }
}
