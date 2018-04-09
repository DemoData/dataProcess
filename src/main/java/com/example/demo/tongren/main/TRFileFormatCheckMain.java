package com.example.demo.tongren.main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 统计文件格式个数
 */
public class TRFileFormatCheckMain {

    public static void main(String[] args) {
        File dirFile = new File(TRConstant.dataDir);
        File[] childDirFileList = dirFile.listFiles();
        Map<String, Integer> formatCountMap = new HashMap<>();
        int sum = 0;
        for(File file : childDirFileList){
            sum++;
            File[] formatList = file.listFiles();
            for(File formatFile : formatList){
                String format = formatFile.getName();
                if(!formatCountMap.containsKey(format)){
                    formatCountMap.put(format, 0);
                }
                formatCountMap.put(format, formatCountMap.get(format) + 1);
            }
        }
        for(String key : formatCountMap.keySet()){
            System.out.println(key + " " + formatCountMap.get(key));
        }
        System.out.println("总数：" + sum);
    }
}
