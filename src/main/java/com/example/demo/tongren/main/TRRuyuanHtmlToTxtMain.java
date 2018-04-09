package com.example.demo.tongren.main;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 同仁入院记录html转txt处理
 */
public class TRRuyuanHtmlToTxtMain {

    public static Pattern anchorPattern = Pattern.compile("^([\\u4e00-\\u9fa5（）]{1,20})[：|:]");

    public static void main(String[] args) throws Exception{
        String classfifyPath = TRConstant.DIR_PREFIX + TRConstant.CLASSFIFY;
        String ruyuanPath = classfifyPath + TRConstant.RUYUAN;
        String ruyuanHtmlTxtPath = ruyuanPath + TRConstant.TXT;
        File ruyuanHtmlTxtFile = new File(ruyuanHtmlTxtPath);
        if(!ruyuanHtmlTxtFile.exists()){
            ruyuanHtmlTxtFile.mkdirs();
        }
        File ruyuanFile = new File(ruyuanPath);
        File[] ruyuanChildFileList = ruyuanFile.listFiles();
        for(File ruyuanChildFile : ruyuanChildFileList){
            File[] formatFileList = ruyuanChildFile.listFiles();
            for(File formatFile : formatFileList){
                String formatFileName = formatFile.getName();
                if(TRConstant.HTML.equals(formatFileName)){
                    File[] fileList = formatFile.listFiles();
                    for(File file : fileList){
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Unicode"));
                        String line;
                        StringBuilder stringBuilder = new StringBuilder();
                        boolean tableFlag = false;
                        boolean bodyFlag = false;
                        while((line = bufferedReader.readLine()) != null){
                            if(line.contains("<BODY")){
                                bodyFlag = true;
                            }
                            if(!bodyFlag){
                                continue;
                            }
                            if(line.contains("<TABLE")){
                                tableFlag = true;
                            }else if(line.contains("</TABLE>")){
                                tableFlag = false;
                            }
                            line = line.replaceAll("<B>", "【【").replaceAll("</B>", "】】").trim();
                            line = line.replaceAll("<([\\s\\S]+?)>", "");
                            if(tableFlag){
                                Matcher matcher = anchorPattern.matcher(line);
                                if(matcher.find()){
                                    String anchor = matcher.group(1);
                                    StringBuilder stringBuilder1 = new StringBuilder();
                                    stringBuilder1.append("【【");
                                    stringBuilder1.append(anchor);
                                    stringBuilder1.append("】】");
                                    stringBuilder1.append(line.substring(anchor.length()));
                                    line = stringBuilder1.toString();
                                }
                            }
                            if(StringUtils.isNotBlank(line)){
                                stringBuilder.append(line);
                                //stringBuilder.append("\n");
                            }

                        }
                        String result = stringBuilder.toString().replaceAll("【【\t*】】", "");
                        result = result.replaceAll("【【", "\n【【");
                        StringReader stringReader = new StringReader(result);
                        BufferedReader stringBufferReader = new BufferedReader(stringReader);
                        StringBuilder resultStringBuilder = new StringBuilder();
                        while((line = stringBufferReader.readLine()) != null){
                            resultStringBuilder.append(processAnchorContainColon(line));
                            //resultStringBuilder.append("\n");
                        }
                        //result = resultStringBuilder.toString().replaceAll("】】【【","");
                        result = result.replaceAll("【【", "\n【【");
                        String fileName = file.getName();
                        String txtFilePath = ruyuanHtmlTxtPath + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + ".txt";
                        File txtFile = new File(txtFilePath);
                        if(!txtFile.exists()){
                            txtFile.createNewFile();
                        }
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFile), "UTF-8"));
                        bufferedWriter.write(result);
                        bufferedWriter.flush();
                        bufferedReader.close();
                        stringBufferReader.close();
                        bufferedWriter.close();
                    }
                }
            }
        }
    }

    public static String processAnchorContainColon(String line){
        if(!line.contains("】】")){
            return line;
        }
        String anchor = line.substring(0, line.indexOf("】】"));
        if(anchor.contains("：")){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(line.substring(0, line.indexOf("：")));
            stringBuilder.append("】】：");
            stringBuilder.append(line.substring(line.indexOf("】】") + 2));
            return stringBuilder.toString();
        }else{
            return line;
        }


    }
}
