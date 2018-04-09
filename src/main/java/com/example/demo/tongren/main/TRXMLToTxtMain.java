package com.example.demo.tongren.main;

import com.example.demo.common.util.PatternUtil;
import com.example.demo.util.FileUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TRXMLToTxtMain {

    private static Pattern newCtrlLabelPattern = Pattern.compile("<NewCtrl[\\s\\S]+? 元素名称=\"([\\s\\S]+?)\">([\\s\\S]+?)</NewCtrl>");
    private static Pattern newCtrlLableItemNamePattern = Pattern.compile("元素名称=\"([\\s\\S]+?)\"");
    private static Pattern sectionItemNamePattern = Pattern.compile("ItemName=\"([\\s\\S]+?)\"");

    public static void main(String[] args) throws Exception{
        String parentPath = TRConstant.DIR_PREFIX + TRConstant.CLASSFIFY;
        int sum = 0;
        int count = 0;
        String txtPath = TRConstant.DIR_PREFIX + TRConstant.TXT + "/xml";
        File txtFile = new File(txtPath);
        if(!txtFile.exists()){
            txtFile.mkdirs();
        }
        File parentPathFile = new File(parentPath);
        File[] medicalHistoryFileList = parentPathFile.listFiles();
        Set<String> labelSet = new HashSet<>();
        for(File medicalHistoryFile : medicalHistoryFileList) {
            String medicalHistoryFileName = medicalHistoryFile.getName();
            File[] childMedicalHistoryFileList = medicalHistoryFile.listFiles();
            for (File childMedicalHistoryFile : childMedicalHistoryFileList) {
                String childMedicalHistoryFileName = childMedicalHistoryFile.getName();
                File[] formatFileList = childMedicalHistoryFile.listFiles();
                for (File formatFile : formatFileList) {
                    String formatFileName = formatFile.getName();
                    if("Xml".equals(formatFileName)){
                        File[] fileList = formatFile.listFiles();
                        count += fileList.length;
                        System.out.println("count:" + count);
                        for(File file : fileList){
                            sum++;
                            String fileName = file.getName();
                            String newFileName = txtPath + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + ".txt";
                            File newFile = new File(newFileName);
                            if(!newFile.exists()){
                                newFile.createNewFile();
                            }
                            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile), "UTF-8"));
                            bufferedWriter.write(processFileContent(file));
                            bufferedWriter.flush();
                            bufferedWriter.close();
                        }
                    }
                }
            }
        }
        for(String label : labelSet){
            System.out.println(label);
        }
        System.out.println("文件总数: " + sum);
        System.out.println("count:" + count);
        //System.out.println(processFileContent(new File("/Users/liulun/Desktop/同仁入库/fenlei/出院小结/出院小结1.1/Xml/2015026014_出院小结_出院小结1.1_e0061bf9-df7c-4f32-a36e-484f8de2a0a5.xml")));
    }

    private static String processFileContent(File file) throws Exception{
        String content = FileUtil.readFile(file, "Unicode");
        content = content.replaceAll("><", ">\n<");
        BufferedReader bufferedReader = new BufferedReader(new StringReader(content));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        int sectionFlag = 0;
        boolean sectionNewCtrlFlag = false;
        while((line = bufferedReader.readLine()) != null){
            if(line.startsWith("<NewCtrl")){
                if(sectionFlag == 0){
                    Matcher matcher = newCtrlLableItemNamePattern.matcher(line);
                    if(matcher.find()){
                        String anchor = matcher.group(1);
                        if(StringUtils.isNotBlank(anchor)){
                            if(anchor.endsWith(":") || anchor.endsWith("：")){
                                stringBuilder.append(anchor);
                            }else{
                                stringBuilder.append("【【");
                                stringBuilder.append(anchor);
                                stringBuilder.append("】】：");
                            }
                        }
                    }
                }else{
                    sectionNewCtrlFlag = true;
                }
            }else if(line.startsWith("<Section")){
                sectionFlag++;
                if(sectionFlag == 1){
                    Matcher matcher = sectionItemNamePattern.matcher(line);
                    if(matcher.find()){
                        String anchor = matcher.group(1);
                        if(StringUtils.isNotBlank(anchor)){
                            if(anchor.endsWith(":") || anchor.endsWith("：")){
                                stringBuilder.append(anchor);
                            }else{
                                stringBuilder.append("【【");
                                stringBuilder.append(anchor);
                                stringBuilder.append("】】：");
                            }
                        }
                    }
                }
            }else if(line.startsWith("<Content_Text")){
                if((!sectionNewCtrlFlag && sectionFlag == 1) || sectionFlag == 0){
                    line = line.replaceAll("<Content_Text>", "")
                            .replaceAll("</Content_Text>", "").replaceAll("<Content_Text/>","")
                            .replaceAll("<Content_Text />", "").trim();
                    if("已婚".equals(line) || "未婚".equals(line)){
                        if(!stringBuilder.toString().endsWith("【【婚姻状况】】：")){
                            stringBuilder.append("【【婚姻状况】】：");
                        }
                    }
                    if(StringUtils.isNotEmpty(line)){
                        stringBuilder.append(line);
                    }
                    stringBuilder.append("\n");
                }
            }else if(line.startsWith("</NewCtrl>")){
                if(sectionFlag > 0){
                    sectionNewCtrlFlag = false;
                }
            }else if(line.startsWith("</Section>")){
                sectionFlag--;
            }
        }
        /*Matcher matcher = newCtrlLabelPattern.matcher(content);
        while(matcher.find()){
            String anchorContent = matcher.group(2);
            anchorContent = anchorContent.replaceAll("<Content_Text>", "").replaceAll("</Content_Text>", "").replaceAll("<Content_Text/>","");
            String anchor = matcher.group(1);
            stringBuilder.append("【【");
            stringBuilder.append(anchor);
            stringBuilder.append("】】：");
            stringBuilder.append(anchorContent);
            stringBuilder.append("\n");
        }*/
        return stringBuilder.toString();
    }
}
