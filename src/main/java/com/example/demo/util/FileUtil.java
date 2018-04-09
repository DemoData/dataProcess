package com.example.demo.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtil {

    public static List<File> listAllFile(String fileDir){
        File file = new File((fileDir));
        List<File> fileList = new ArrayList<>();
        File[] childFileArr = file.listFiles();
        for(int i = 0; i < childFileArr.length; i++){
            if(childFileArr[i].isDirectory()){
                fileList.addAll(listAllFile(childFileArr[i].getAbsolutePath()));
            }else if(childFileArr[i].getName().endsWith(".xml")){
                fileList.add(childFileArr[i]);
            }
        }
        return fileList;
    }

    public static Map<String, List<String>> listTypeFileNameMap(String fileDir){
        Map<String, List<String>> result = new HashMap<>();
        File file = new File((fileDir));
        File[] childFileArr = file.listFiles();
        for(int i = 0; i < childFileArr.length; i++){
            if(childFileArr[i].isDirectory()){
                String dirName = childFileArr[i].getName();
                result.put(dirName, new ArrayList<>());
                List<File> partFileArr = listAllFile(childFileArr[i].getAbsolutePath());
                for(File partFile: partFileArr){
                    if(partFile.length() != 0){
                        String fileName = partFile.getName();
                        result.get(dirName).add(fileName.substring(0, fileName.lastIndexOf(".")));
                    }
                }
            }
        }
        return result;
    }

    public static List<File> listTxtAllFile(String fileDir){
        File file = new File((fileDir));
        List<File> fileList = new ArrayList<>();
        File[] childFileArr = file.listFiles();
        for(int i = 0; i < childFileArr.length; i++){
            if(childFileArr[i].isDirectory()){
                fileList.addAll(listTxtAllFile(childFileArr[i].getAbsolutePath()));
            }else if(childFileArr[i].getName().endsWith(".txt")){
                fileList.add(childFileArr[i]);
            }
        }
        return fileList;
    }

    public static String readFile(File file) throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
            //stringBuilder.append("\n");
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

    public static String readFile(File file, String charSet) throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charSet));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
            //stringBuilder.append("\n");
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        Map<String, List<String>> result = listTypeFileNameMap("/Users/liulun/Desktop/上海长海医院/血管外科/血管外科下肢动脉相关_20180210_1");
        for(String key : result.keySet()){
            System.out.println(key + " " + result.get(key).size());
        }
    }
}
