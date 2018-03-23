package com.example.demo.test.main;

import com.example.demo.util.FileUtil;
import com.example.demo.common.util.PatternUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;

public class CountAnchorMain {

    private static List<String> dirArr = new ArrayList<>();
    static {
        dirArr.add("手术记录");
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

    public static void main(String[] args) throws Exception{
        Map<String, Integer> anchourCountMap = new TreeMap<>();
        Map<String, String> anchorFileNameMap = new HashMap<>();
        for(String dirName : dirArr) {
            System.out.println(dirName);
            String path = "/Users/liulun/Desktop/上海长海医院/血管外科/txt/" + dirName;
            List<File> fileList = FileUtil.listTxtAllFile(path);
            for (int i = 0; i < fileList.size(); i++) {
                File file = fileList.get(i);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Matcher matcher = PatternUtil.ANCHOR_PATTERN.matcher(line);
                    if(matcher.find()){
                        String anchor = matcher.group(1);
                        if(!anchourCountMap.containsKey(anchor)){
                            anchourCountMap.put(anchor, 0);
                            anchorFileNameMap.put(anchor, file.getName());
                        }
                        anchourCountMap.put(anchor, anchourCountMap.get(anchor) + 1);
                    }
                }
            }

        }
        for(String key : anchourCountMap.keySet()){
            System.out.println(key + " " + anchourCountMap.get(key) + " " + anchorFileNameMap.get(key));

        }
    }
}
