package com.example.demo.test.main;

import com.example.demo.common.constant.CommonConstant;
import com.example.demo.util.FileUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class CalDirFileCountMain {
    private static List<String> dirArr = new ArrayList<>();
    static {
        dirArr.add("手术记录");
        dirArr.add("病程记录");
        dirArr.add("出院小结");
        dirArr.add("麻醉");
        dirArr.add("入院记录");
        dirArr.add("死亡记录");
    }


    public static void main(String[] args) throws Exception{
       for(String dirName : dirArr) {
           System.out.println(dirName);
           String path = "/Users/liulun/Desktop/上海长海医院/血管外科/" + dirName;

           List<File> fileList = FileUtil.listAllFile(path);
           System.out.println(fileList.size());
       }
    }
}
