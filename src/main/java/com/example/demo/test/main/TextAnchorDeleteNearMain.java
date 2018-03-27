package com.example.demo.test.main;

import com.example.demo.util.FileUtil;
import com.example.demo.common.util.PatternUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class TextAnchorDeleteNearMain {

    private static List<String> dirArr = new ArrayList<>();
    static {
        dirArr.add("病程记录");
        //dirArr.add("入院记录");
        /*dirArr.add("病程记录");
        dirArr.add("出院小结");
        dirArr.add("单页护理文书");
        dirArr.add("护理模板");
        dirArr.add("会诊记录");
        dirArr.add("检查申请");
        dirArr.add("静脉血栓");
        dirArr.add("旧版生命体征");
        dirArr.add("旧版一般护理记录");
        dirArr.add("科研病历");
        dirArr.add("临时打印文件");
        dirArr.add("麻醉");
        dirArr.add("手术报告单");
        dirArr.add("手术记录");
        dirArr.add("死亡记录");
        dirArr.add("特殊科室护理记录");
        dirArr.add("知情文件");*/
    }

    private static Map<String, String> nearMap = new HashMap<>();
    static {
        nearMap.put("出生地", "籍贯");
        nearMap.put("病史叙述者", "供史者");
        nearMap.put("婚姻状况", "婚姻");
        nearMap.put("现居住地址", "地址");
        nearMap.put("住址", "地址");
        nearMap.put("家住", "住址");
        nearMap.put("记录时间", "记录日期");
        //nearMap.put("主要症状及体征", "入院情况");
        nearMap.put("记录者", "病程签名");
    }

    public static void main(String[] args) throws Exception{
        int sum = 0;
        for(String dirName : dirArr) {
            String path = "/Users/liulun/Desktop/上海长海医院/血管外科/txt/" + dirName;
            List<File> fileList = FileUtil.listTxtAllFile(path);
            sum += fileList.size();
            for (int i = 0; i < fileList.size(); i++) {
                File file = fileList.get(i);
                //if(file.getName().equals("00322382_2_400451_入院记录00020006.txt")) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    String lastLine = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        if ("".equals(line.trim())) {
                            continue;
                        }
                        if (lastLine != null) {
                            Matcher lastMatcher = PatternUtil.ANCHOR_PATTERN.matcher(lastLine);
                            String lastAnchor = "";
                            if (lastMatcher.find()) {
                                lastAnchor = lastMatcher.group(1);
                            }
                            Matcher currentMatcher = PatternUtil.ANCHOR_PATTERN.matcher(line);
                            String currentAnchor = "";
                            if (currentMatcher.find()) {
                                currentAnchor = currentMatcher.group(1);
                            }
                            if (!"".equals(lastAnchor)) {
                                if (lastAnchor.equals(currentAnchor) || lastAnchor.equals(nearMap.get(currentAnchor))
                                        ||currentAnchor.equals(nearMap.get(lastAnchor))) {
                                    if(!lastLine.endsWith("】】：") && (lastLine.endsWith(":") || lastLine.endsWith("："))){
                                        stringBuilder.append("【【");
                                        stringBuilder.append(lastLine.substring(lastLine.indexOf("：") + 1, lastLine.length() - 1));
                                        stringBuilder.append("】】：");
                                        line = line.substring(line.indexOf("：") + 1);
                                    }else{
                                        stringBuilder.append(lastLine);
                                        line = line.substring(line.indexOf("：") + 1);
                                    }
                                } else {
                                    stringBuilder.append(lastLine);
                                }
                            }else{
                                stringBuilder.append(lastLine);
                            }
                        }
                        lastLine = line;
                    }
                    stringBuilder.append(lastLine);
                    bufferedReader.close();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "GBK"));
                    bufferedWriter.write(stringBuilder.toString().replaceAll("【【", "\n【【"));
                    bufferedWriter.flush();
                    bufferedWriter.close();
                //}
            }
        }
        System.out.println(sum);
    }
}
