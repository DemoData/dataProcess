package com.example.demo.test.main;

import com.example.demo.util.FileUtil;
import com.example.demo.common.util.PatternUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class SpecialAnchorMain {

    private static List<String> dirArr = new ArrayList<>();
    static {
        dirArr.add("手术记录");
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

    private static List<String> specialAnchorList = new ArrayList<>();
    static {
        //出院小结
        /*specialAnchorList.add("入院查体");
        specialAnchorList.add("入院辅检");*/
        //死亡记录
        /*specialAnchorList.add("入院诊断");
        specialAnchorList.add("出院诊断");
        specialAnchorList.add("病理诊断");
        specialAnchorList.add("死亡原因");
        specialAnchorList.add("住院经过");
        specialAnchorList.add("死者姓名");
        specialAnchorList.add("性别");
        specialAnchorList.add("出生日期");
        specialAnchorList.add("年龄");
        specialAnchorList.add("婚否");
        specialAnchorList.add("身份证编号");
        specialAnchorList.add("入院时间");
        specialAnchorList.add("科别");
        specialAnchorList.add("病区");
        specialAnchorList.add("主治医师");
        specialAnchorList.add("住院医师");
        specialAnchorList.add("科 主诊医师");
        specialAnchorList.add("科主诊医师");
        specialAnchorList.add("讨论地点");
        specialAnchorList.add("主持人");
        specialAnchorList.add("参加讨论人员");
        specialAnchorList.add("经验教训");
        specialAnchorList.add("住院天数");
        specialAnchorList.add("生前工作单位");
        specialAnchorList.add("常住户口地址");*/
        //手术记录
        specialAnchorList.add("术前诊断");
        specialAnchorList.add("术后诊断");
        specialAnchorList.add("手术人员");
        specialAnchorList.add("麻醉方式");
        specialAnchorList.add("麻醉人员");
        specialAnchorList.add("报告者");
        specialAnchorList.add("手术者");
        specialAnchorList.add("记录者");
        specialAnchorList.add("手术经过");
    }

    public static void main(String[] args) throws Exception{
        int sum = 0;
        for(String dirName : dirArr) {
            System.out.println(dirName);
            String path = "/Users/liulun/Desktop/上海长海医院/血管外科/txt/" + dirName;
            List<File> fileList = FileUtil.listTxtAllFile(path);
            sum += fileList.size();
            for (int i = 0; i < fileList.size(); i++) {
                File file = fileList.get(i);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    for(String anchor : specialAnchorList){
                        //line = line.replaceAll("。" + anchor + "：", "。【【" + anchor + "】】：");//出院小结
                        //line = line.replaceAll(anchor + "：", "【【" + anchor + "】】：");//死亡记录
                        line = line.replaceAll(anchor + " ", "【【" + anchor + "】】：");//手术记录
                    }
                    stringBuilder.append(line);
                }
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
