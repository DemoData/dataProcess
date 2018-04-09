package com.example.demo.tongren.main;

import com.example.demo.util.MapOrderUtil;

import java.io.File;
import java.util.*;

/**
 * 统计病史大类和病史小类数量
 */
public class TRCalBingShiMain {

    public static void main(String[] args) {
        Map<String, Set<String>> bingshiMap = new HashMap<>();
        Map<String, Integer> bingshiCountMap = new HashMap<>();
        Map<String, String> bingshiFileMap = new HashMap<>();
        File dirFile = new File(TRConstant.dataDir);
        File[] childFileList = dirFile.listFiles();
        for(File file : childFileList){
            File[] formatList =  file.listFiles();
            for(File formatFile : formatList){
                String format = formatFile.getName();
                if("Html".equals(format) || "Xml".equals(format)){
                    File[] xmlFileList = formatFile.listFiles();
                    for(File xmlFile : xmlFileList){
                        String xmlFileName = xmlFile.getName();
                        String[] xmlFileNameArr = xmlFileName.split("_");
                        if(!bingshiMap.containsKey(xmlFileNameArr[0])){
                            bingshiMap.put(xmlFileNameArr[0], new HashSet<>());
                        }
                        bingshiMap.get(xmlFileNameArr[0]).add(xmlFileNameArr[1]);
                        String key = xmlFileNameArr[0] + "_" + xmlFileNameArr[1];
                        if(!bingshiCountMap.containsKey(key)){
                            bingshiCountMap.put(key, 0);
                            bingshiFileMap.put(key, xmlFile.getAbsolutePath());
                        }
                        bingshiCountMap.put(key, bingshiCountMap.get(key) + 1);
                    }
                }
            }
        }
        for(String key : bingshiMap.keySet()){
            System.out.println("病史大类:" + key);
            System.out.println("病史小类:" + bingshiMap.get(key));
        }
        bingshiCountMap = MapOrderUtil.sortMapByIntegerInValue(bingshiCountMap);
        System.out.println("------------------华丽的分隔符-------------------");
        for(String key : bingshiCountMap.keySet()){
            if(key.startsWith("入院记录")){
                System.out.println(key + " " + bingshiCountMap.get(key) + " " + bingshiFileMap.get(key));
            }
        }
    }
}
