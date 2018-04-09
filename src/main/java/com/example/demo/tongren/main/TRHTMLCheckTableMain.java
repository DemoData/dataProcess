package com.example.demo.tongren.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

/**
 * 检查入院记录是否都还有TABLE标签
 */
public class TRHTMLCheckTableMain {

    public static void main(String[] args) throws Exception{
        String path = TRConstant.DIR_PREFIX + TRConstant.CLASSFIFY;
        String ruyuanPath = path + "/" + "入院记录";
        File ruyuanFile = new File(ruyuanPath);
        File[] ruyuanChildFileList =  ruyuanFile.listFiles();
        int sum = 0;
        int notContainTableCount = 0;
        for(File ruyuanChildFile : ruyuanChildFileList){
            File[] formatFileList = ruyuanChildFile.listFiles();
            for(File formatFile : formatFileList){
                String formatFileName = formatFile.getName();
                if("Html".equals(formatFileName)){
                    File[] fileList = formatFile.listFiles();
                    for(File file : fileList){
                        sum++;
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Unicode"));
                        String line;
                        boolean tableFlag = false;
                        while((line = bufferedReader.readLine()) != null){
                            if(line.contains("<TABLE")){
                                tableFlag = true;
                                break;
                            }
                        }
                        if(!tableFlag){
                            notContainTableCount++;
                            System.out.println(file.getAbsolutePath());
                        }
                    }
                }
            }
        }
        System.out.println(sum + " " + notContainTableCount);
    }
}
