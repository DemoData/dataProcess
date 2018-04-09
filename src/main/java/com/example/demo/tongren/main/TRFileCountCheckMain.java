package com.example.demo.tongren.main;

import java.io.File;

/**
 * 检查html,xml,txt文件夹下面是否是相同格式的文件
 */
public class TRFileCountCheckMain {

    public static void main(String[] args) {
        //count结尾的表示后缀名的文件数量，sum结尾表示目录的文件数量
        int xmlCount = 0;
        int htmlCount = 0;
        int txtCount = 0;
        int htmlSum = 0;
        int xmlSum = 0;
        int txtSum = 0;
        File dataFile = new File(TRConstant.dataDir);
        File[] visitNumFileList = dataFile.listFiles();
        for(File visitNumFile : visitNumFileList){
            File[] formatFileList = visitNumFile.listFiles();
            for(File formatFile : formatFileList){
                File[] fileList = formatFile.listFiles();
                String formatName = formatFile.getName();
                int tempCount = 0;
                int tempSum = fileList.length;
                if("Html".equals(formatName)){
                    htmlSum += tempSum;
                    for(File file : fileList){
                        String fileName = file.getName();
                        if(fileName.endsWith("html")){
                            tempCount += 1;
                        }else{
                            System.out.println(fileName);
                        }
                    }
                    htmlCount += tempCount;
                }else if("Xml".equals(formatName)){
                    xmlSum += tempSum;
                    for(File file : fileList){
                        String fileName = file.getName();
                        if(fileName.endsWith("xml")){
                            tempCount += 1;
                        }else{
                            System.out.println(fileName);
                        }
                    }
                    xmlCount += tempCount;
                }else{
                    txtSum += tempSum;
                    for(File file : fileList){
                        String fileName = file.getName();
                        if(fileName.endsWith("txt")){
                            tempCount += 1;
                        }else{
                            System.out.println(fileName);
                        }
                    }
                    txtCount += tempCount;
                }
                if(tempCount != tempSum){
                    System.out.println(formatFile.getAbsolutePath() + " " + tempCount + " " + tempSum);
                }
            }
        }
        System.out.println("xml:" + (xmlCount == xmlSum) + ": " + xmlCount);
        System.out.println("html:" + (htmlCount == htmlSum) + ": " + htmlCount);
        System.out.println("txt:" + (txtCount == txtSum) + ": " + txtCount);
    }
}
