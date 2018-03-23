package com.example.demo.test.main;

import com.example.demo.util.FileUtil;
import com.example.demo.common.util.PatternUtil;

import java.io.*;
import java.util.*;

public class DeleteNotValidAnchorMain {

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
        int sum = 0;
        Map<String, Integer> deleteCountMap = new TreeMap<>();
        Map<String, String> deleteNameLineMap = new HashMap<>();
        for(String dirName : dirArr) {
            System.out.println(dirName);
            String path = "/Users/liulun/Desktop/上海长海医院/血管外科/txt/" + dirName;
            List<File> fileList = FileUtil.listTxtAllFile(path);
            sum += fileList.size();
            for (int i = 0; i < fileList.size(); i++) {
                File file = fileList.get(i);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    if(!PatternUtil.STANDARD_ANCHOR_WITH_SYMBOL_PATTERN.matcher(line).find()) {
                        stringBuilder.append(line);
                    }else{
                        if(!deleteCountMap.containsKey(line)){
                            deleteCountMap.put(line, 0);
                            deleteNameLineMap.put(line, file.getName());
                        }
                        deleteCountMap.put(line, deleteCountMap.get(line) + 1);

                    }
                }
                bufferedReader.close();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "GBK"));
                bufferedWriter.write(stringBuilder.toString().replaceAll("【【", "\n【【"));
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        }
        for(String line : deleteNameLineMap.keySet()){
            System.out.println(line + " " + deleteNameLineMap.get(line) + " " + deleteCountMap.get(line));
        }
        System.out.println(sum);
    }
}
