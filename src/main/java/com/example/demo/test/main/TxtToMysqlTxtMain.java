package com.example.demo.test.main;

import com.example.demo.common.constant.CommonConstant;
import com.example.demo.util.FileUtil;
import com.example.demo.common.util.PatternUtil;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

public class TxtToMysqlTxtMain {

    private static List<String> dirArr = new ArrayList<>();
    static {
        dirArr.add("麻醉");
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

    private static Map<String, String> mapping = new HashMap<>();
    static {
        mapping.put("入院记录", "入院记录-入院记录");
        mapping.put("出院小结", "出院记录-出院小结");
        mapping.put("死亡记录", "出院记录-死亡记录");
        mapping.put("手术记录", "手术操作记录-手术");
        mapping.put("病程记录", "治疗方案-病程");
        mapping.put("麻醉", "治疗方案-病程");
    }

    public static void main(String[] args) throws Exception{
        Map<String, Integer> anchourCountMap = new TreeMap<>();
        Map<String, String> anchorFileNameMap = new HashMap<>();
        Map<String, List<String>> typeFileNameMap = FileUtil.listTypeFileNameMap("/Users/liulun/Desktop/上海长海医院/血管外科/血管外科下肢动脉相关_20180210_1");
        for(String dirName : dirArr) {
            System.out.println(dirName);
            String path = "/Users/liulun/Desktop/上海长海医院/血管外科/txt/" + dirName;
            String mysqlPath = "/Users/liulun/Desktop/上海长海医院/血管外科/mysql";
            File mysqlFile = new File(mysqlPath);
            if(!mysqlFile.exists()){
                mysqlFile.mkdirs();
            }
            String mysqlFilePath = mysqlPath + "/" + dirName + ".txt";
            mysqlFile = new File(mysqlFilePath);
            if(!mysqlFile.exists()){
                mysqlFile.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mysqlFile), "UTF-8"));
            List<File> fileList = FileUtil.listTxtAllFile(path);
            for (int i = 0; i < fileList.size(); i++) {
                File file = fileList.get(i);
                System.out.println(file.getName());
                String content = FileUtil.readFile(file);
                String[] contentArr = content.split(CommonConstant.EXCEL_SEPARATOR);
                String fileName = file.getName();
                String[] fileNameArr = fileName.split("_");
                for(String value : contentArr){
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("\\N");
                    stringBuilder.append("#$#");
                    stringBuilder.append(fileNameArr[0]);
                    stringBuilder.append("#$#");
                    stringBuilder.append(fileNameArr[2]);
                    stringBuilder.append("#$#");
                    stringBuilder.append(mapping.get(dirName));
                    stringBuilder.append("#$#");
                    stringBuilder.append(value);
                    stringBuilder.append("#$#");
                    StringBuilder orgType = new StringBuilder();
                    for(String key : typeFileNameMap.keySet()){
                        List<String> fileNameList = typeFileNameMap.get(key);
                        if(fileNameList.contains(fileName.substring(0, fileName.lastIndexOf(".")))){
                            orgType.append(key);
                            orgType.append(",");
                        }
                    }
                    if(orgType.length() > 0){
                        stringBuilder.append(orgType.substring(0, orgType.length() - 1));
                    }else{
                        stringBuilder.append("\\N");
                    }
                    stringBuilder.append("\n");
                    bufferedWriter.write(stringBuilder.toString());
                }
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        }
        for(String key : anchourCountMap.keySet()){
            System.out.println(key + " " + anchourCountMap.get(key) + " " + anchorFileNameMap.get(key));

        }
    }
}
