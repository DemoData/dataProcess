package com.example.demo.tongren.main;

import java.io.File;
import java.lang.reflect.Field;

public class TRDirCalMain {



    public static void main(String[] args) {
        File file = new File(TRConstant.dataDir);
        System.out.println(file.listFiles().length);
    }
}
