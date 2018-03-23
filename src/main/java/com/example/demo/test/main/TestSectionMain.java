package com.example.demo.test.main;

import com.example.demo.util.FileUtil;
import com.example.demo.common.util.PatternUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class TestSectionMain {

    public static void main(String[] args) throws Exception{
        String path = "/Users/liulun/Desktop/上海长海医院/血管外科/入院记录";
        List<File> fileList = FileUtil.listAllFile(path);
        Map<String, Integer> result = new HashMap<>();
        Map<String, String> sectionNameFileNameMap = new HashMap<>();
        for(int i = 0; i < 1; i++){
            StringBuilder stringBuilder = new StringBuilder();
            File file = fileList.get(i);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
            String line;
            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line + "\n");
            }
            Matcher matcher = PatternUtil.SECTION_PATTERN.matcher(stringBuilder.toString());
            int lastIndex = -1;
            while(matcher.find()){
                String sectionName = matcher.group(2);
                String all = matcher.group();
                int currentIndex = stringBuilder.toString().indexOf(all, lastIndex + 1);
                String subStr = "";
                if(lastIndex == -1){
                    subStr = stringBuilder.toString().substring(0, currentIndex);
                }else{
                    subStr = stringBuilder.toString().substring(lastIndex - 1, currentIndex);
                }
                System.out.println(subStr);
                System.out.println(needFieldelemName(subStr));
                lastIndex = currentIndex + 1;
                if(!result.containsKey(sectionName)){
                    result.put(sectionName, 0);
                }
                result.put(sectionName, result.get(sectionName) + 1);
                if(!sectionNameFileNameMap.containsKey(sectionName)){
                    sectionNameFileNameMap.put(sectionName, file.getName());
                }
            }
        }
        for(String key : result.keySet()){
            System.out.println(key + " " + result.get(key) + " " + sectionNameFileNameMap.get(key));
        }

System.out.println(result.size());
    }


    public static String needSectionName(String str){
        Matcher matcher = PatternUtil.SECTION_PATTERN.matcher(str);
        return null;
    }

    public static String needFieldelemName(String src) throws  Exception{
//        while(matcher.find()){
//            String front = matcher.group(1);
//            String fieldName = matcher.group(2);
//            String back = matcher.group(3);
//            System.out.println(front);
//            System.out.println(fieldName);</text>
//            System.out.println(back);
//        }
        src = src.replaceAll("<section ([\\s\\S]+?)>", "").replaceAll("</section>", "");
        src = src.replaceAll("<fieldelem name=\"", "【【").replaceAll("\" code=\"\" code-system=\"\">", "】】");
        src = src.replaceAll("</fieldelem>", "");
        src = src.replaceAll("<text>", "").replaceAll("</text>", "");
        src = src.replaceAll("\n", "");
        return src;
    }

    public static String notNeedFieldName(String src) throws  Exception{
        src = src.replaceAll("<section ([\\s\\S]+?)>", "").replaceAll("</section>", "");
        src = src.replaceAll("<fieldelem ([\\s\\S]+?)>", "").replaceAll("</fieldelem", "");
        src = src.replaceAll("<text>", "【【").replaceAll("</text>", "】】");
        src = src.replaceAll("\n", "");
        return src;
    }
}
