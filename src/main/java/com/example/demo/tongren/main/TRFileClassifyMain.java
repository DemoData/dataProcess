package com.example.demo.tongren.main;

import java.io.File;
import java.nio.file.Files;

/**
 * 文件分类
 */
public class TRFileClassifyMain {

    public static void main(String[] args) throws Exception {
        String classifyPath = TRConstant.DIR_PREFIX + TRConstant.CLASSFIFY;
        File classifyFile = new File(classifyPath);
        if (!classifyFile.exists()) {
            classifyFile.mkdirs();
        }
        int sum = 0;
        File dataFile = new File(TRConstant.dataDir);
        File[] visitNumFileList = dataFile.listFiles();
        for (File visitNumFile : visitNumFileList) {
            String visitNumFileName = visitNumFile.getName();
            File[] formatFileList = visitNumFile.listFiles();
            for (File formatFile : formatFileList) {
                String formatFileName = formatFile.getName();
                if ("Html".equals(formatFileName) || "Xml".equals(formatFileName)) {
                    File[] fileList = formatFile.listFiles();
                    for (File file : fileList) {
                        String fileName = file.getName();
                        String[] fileNameArr = fileName.split("_");
                        String path = classifyPath  + "/" + fileNameArr[0] + "/" + fileNameArr[1]+ "/" + formatFileName;
                        File pathFile = new File(path);
                        if (!pathFile.exists()) {
                            pathFile.mkdirs();
                        }
                        String newFileName = visitNumFileName + "_" + fileName;
                        File newFile = new File(path + "/" + newFileName);
                        Files.copy(file.toPath(), newFile.toPath());
                        sum++;
                    }
                }
            }
        }
        System.out.println("分类文件数量：" + sum);
    }
}
