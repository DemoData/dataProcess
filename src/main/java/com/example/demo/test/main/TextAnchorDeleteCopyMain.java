package com.example.demo.test.main;

import com.example.demo.util.FileUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TextAnchorDeleteCopyMain {

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

    public static void main(String[] args) throws Exception{
        int sum = 0;
        for(String dirName : dirArr) {
            String path = "/Users/liulun/Desktop/上海长海医院/血管外科/txt/" + dirName;
            List<File> fileList = FileUtil.listTxtAllFile(path);
            sum += fileList.size();
            for (int i = 0; i < fileList.size(); i++) {
                File file = fileList.get(i);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                String lastLine = null;
                while((line = bufferedReader.readLine()) != null){
                    if("".equals(line.trim())){
                        continue;
                    }
                    if(lastLine != null){
                        if(!line.startsWith(lastLine)){
                            stringBuilder.append(lastLine);
                            //stringBuilder.append("\n");
                        }
                    }
                    lastLine = line;
                }
                stringBuilder.append(lastLine);
                bufferedReader.close();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "GBK"));
                bufferedWriter.write(stringBuilder.toString().replaceAll("【【", "\n【【"));
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        }
        System.out.println(sum);
    }
}
