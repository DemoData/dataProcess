package com.example.demo.common.util;

import com.example.demo.constant.CommonConstant;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternUtil {

    public static Pattern MEDICAL_CONTENT_SPLIT_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{4}:\\d{2}[^::、)）,，。]");
    public static Pattern MEDICAL_CONTENT_TXT_SPLIT_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}[^:、)）,，。]");
    public static Pattern EMPTY_PATTERN = Pattern.compile("\\s*|\t|\r|\n");
    public static Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    public static Pattern ANCHOR_PATTERN = Pattern.compile("【【([\\s\\S]+?)】】");
    public static Pattern HTML_PATTERN = Pattern.compile("<([\\s\\S]+?)>");
    public static Pattern CHINESE_PATTERN = Pattern.compile("[\u4e00-\u9fa5]+");
    public static Pattern STANDARD_ANCHOR_PATTERN = Pattern.compile("^([\u4e00-\u9fa5]+)：$");
    public static Pattern STANDARD_ANCHOR_WITH_SYMBOL_PATTERN = Pattern.compile("^【【[\u4e00-\u9fa5]+】】：$");
    public static Pattern DIGEST_PATTERN = Pattern.compile("\\d");
    public static Pattern SECTION_PATTERN = Pattern.compile("<section ([\\s\\S]+?)([\u4e00-\u9fa5]+)([\\s\\S]+?)>");
    public static Pattern FIELDELEM_PATTERN = Pattern.compile("(<fieldelem [\\s\\S]+?)([\u4e00-\u9fa5]+)([\\s\\S]+?>)([\\s\\S]+)<");

    public static Long medicalContentSplitPatternToInt(String str){
        return Long.valueOf(str.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", ""));
    }


    public static String getMd5ByFile(File file) throws FileNotFoundException {
        String value = null;
        FileInputStream in = new FileInputStream(file);
        try {
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    public static void main(String[] args) {
        try{
            System.out.println(STANDARD_ANCHOR_PATTERN.matcher("治疗经过：").find());
            int exsitFileCount = 0;
            Map<String, Integer> result = new HashMap<>();
            String chongming = "/Users/liulun/Desktop/上海长海医院/血管外科/重名";
            File chongmingFile = new File(chongming);
            if(!chongmingFile.exists()){
                chongmingFile.mkdir();
            }
            List<File> fileList = FileUtil.listAllFile("/Users/liulun/Desktop/上海长海医院/血管外科/血管外科下肢动脉相关_20180210_1");
            for(int i = 0; i < fileList.size(); i++){
                if(fileList.get(i).length() == 0){
                    System.out.println("内容为空:" + fileList.get(i).getAbsolutePath());
                    continue;
                }
                String fileName = fileList.get(i).getName();
                Matcher matcher = PatternUtil.CHINESE_PATTERN.matcher(fileName);
                if(matcher.find()){
                    String type = matcher.group();
                    if(!result.containsKey(type)){
                        result.put(type, 0);
                    }
                    File file = new File("/Users/liulun/Desktop/上海长海医院/血管外科/" + type);
                    if(!file.exists()){
                        file.mkdir();
                    }
                    File newFile = new File("/Users/liulun/Desktop/上海长海医院/血管外科/" + type + "/" + fileName);
                    if(!newFile.exists()){
                        //newFile.createNewFile();
                    }else{
                        String newFileMD5 = getMd5ByFile(newFile);
                        String originalMD5 = getMd5ByFile(fileList.get(i));
                        if(newFileMD5.equals(originalMD5)){
                            continue;
                        }
                        int order = 1;
                        newFile = new File(chongming + "/" + fileName.substring(0, fileName.lastIndexOf("."))+ "_" + order + ".xml");
                        while(newFile.exists()){
                            order++;
                            newFile = new File(chongming + "/" + fileName.substring(0, fileName.lastIndexOf("."))+ "_" + order + ".xml");
                        }
                        //System.out.println(fileName);
                        //System.out.println(fileName);
                        exsitFileCount++;
                    }
                    Files.copy(fileList.get(i).toPath(), newFile.toPath());
                    result.put(type, result.get(type) + 1);
                }
                /**String txtFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".txt";
                File txtFile = new File("/Users/liulun/Desktop/上海长海医院/血管外科/txt/" + txtFileName);
                if(!txtFile.exists()){
                    txtFile.createNewFile();
                }
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(txtFile));
                InputStreamReader isr = new InputStreamReader(new FileInputStream(fileList.get(i)), "GBK");
                BufferedReader bufferedReader = new BufferedReader(isr);
                String line;
                while((line = bufferedReader.readLine()) != null){
                    line = StringUtil.trim(line.replaceAll("<([\\s\\S]+?)>", ""));
                    if(!"".equals(line)) {
                        bufferedWriter.write(line + "\n");
                    }
                }
                bufferedWriter.flush();
                bufferedWriter.close();
                bufferedReader.close();**/
            }
            System.out.println("同名文件:" + exsitFileCount);
            int sum = 0;
            for(String key : result.keySet()){
                sum += result.get(key);
                File file = new File("/Users/liulun/Desktop/上海长海医院/血管外科/" + key);
                System.out.println(key + " " + result.get(key)  + " " +  file.listFiles().length);
            }
            System.out.println("总文件：" + sum);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
