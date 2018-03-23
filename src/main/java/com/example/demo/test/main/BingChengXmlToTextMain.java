package com.example.demo.test.main;

import com.example.demo.common.constant.CommonConstant;
import com.example.demo.common.util.PatternUtil;
import com.example.demo.common.util.StringUtil;
import com.example.demo.util.FileUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class BingChengXmlToTextMain {

    //除基本信息外其它section必须添加的锚点
    private static List<String> anchorList = new ArrayList<>();

    private static List<String> dirArr = new ArrayList<>();
    static {
        dirArr.add("麻醉");
    }

    static {
        anchorList.add("病程签名");
        anchorList.add("姓名");
        anchorList.add("性别");
        anchorList.add("年龄");
        anchorList.add("婚姻状况");
        anchorList.add("民族");
        anchorList.add("出生地");
    }

    private static List<String> sectionList = new ArrayList<>();
    static {
        sectionList.add("病程签名");
        sectionList.add("基本情况");
        sectionList.add("简要病情");
        //sectionList.add("标题");
    }



    public static void main(String[] args) throws Exception{
        int sum = 0;
        for(String dirName : dirArr){
            String path = "/Users/liulun/Desktop/上海长海医院/血管外科/" + dirName;
            String txtPath = "/Users/liulun/Desktop/上海长海医院/血管外科/txt/" + dirName;
            File txtPathFile = new File(txtPath);
            if(!txtPathFile.exists()){
                System.out.println(txtPath);
                txtPathFile.mkdirs();
            }
            List<File> fileList = FileUtil.listAllFile(path);
            sum += fileList.size();
            for(int i = 0; i < fileList.size(); i++){
                File file = fileList.get(i);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                boolean bingchengqianming = false;
                while((line = bufferedReader.readLine()) != null){
                    String text = "";
                    String anchor = "";
                    if(line.startsWith("<text")){
                        text = line.replaceAll("</text>", "").replaceAll("<text>", "").trim();
                        text = StringUtil.trim(text);
                    }else if(line.startsWith("<fieldelem")){
                        Matcher matcher = PatternUtil.FIELDELEM_PATTERN.matcher(line);
                        if(matcher.find()){
                            anchor = matcher.group(2);
                            text = matcher.group(4);
                            text = StringUtil.removeAllBlank(text);
                        }
                    }else if(line.startsWith("<section")){
                        Matcher matcher = PatternUtil.SECTION_PATTERN.matcher(line);
                        if(matcher.find()){
                            anchor = matcher.group(2);
                            if(sectionList.contains(anchor)){
                                bingchengqianming = true;
                            }else {
                                bingchengqianming = false;
                            }
                            if("病程记录".equals(anchor)){
                                if(stringBuilder.length() != 0){
                                    stringBuilder.append(CommonConstant.EXCEL_SEPARATOR);
                                }

                            }
                        }
                    }else if(line.startsWith("</section>") && bingchengqianming){
                        bingchengqianming = false;
                    }
                    if(StringUtils.isNotEmpty(text) || bingchengqianming){
                        if(anchorList.contains(anchor) && bingchengqianming) {
                            stringBuilder.append("【【");
                            stringBuilder.append(anchor);
                            stringBuilder.append("】】：");
                        }
                        stringBuilder.append(text);
                        stringBuilder.append("\n");
                    }

                }
                bufferedReader.close();
                File txtFile = new File(txtPath + "/" + file.getName().substring(0, file.getName().lastIndexOf(".")) + ".txt");
                if(!txtFile.exists()){
                    txtFile.createNewFile();
                }
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFile), "GBK"));
                bufferedWriter.write(stringBuilder.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        }
        System.out.println(sum);
    }





}
