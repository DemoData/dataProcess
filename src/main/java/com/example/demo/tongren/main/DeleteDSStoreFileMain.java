package com.example.demo.tongren.main;

import java.io.File;

/**
 * 删除mac生成的.DS_Store文件
 */
public class DeleteDSStoreFileMain {

    private static final String DS_STORE_FILE_NAME = ".DS_Store";
    private static int sum = 0;

    public static void deleteDSStoreFile(String dirPath){
        File dirFile = new File(dirPath);
        File[] childFileList = dirFile.listFiles();
        for(File file : childFileList){
            if(file.isDirectory()){
                deleteDSStoreFile(file.getAbsolutePath());
            }else if(DS_STORE_FILE_NAME.equals(file.getName()) || file.getName().endsWith("txt.baiduyun.uploading.cfg")){
                sum++;
                file.delete();
            }
        }
    }

    public static void main(String[] args) {
        deleteDSStoreFile(TRConstant.DIR_PREFIX + TRConstant.CLASSFIFY);
        System.out.println("删除成功个数" + sum);
    }
}
