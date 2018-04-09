package com.example.demo.tongren.main;

import com.example.demo.common.util.PatternUtil;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * xml转txt以后合并相邻的相同锚点内容
 */
public class TRTxtDeleteCopyAnchorMain {


    private static Map<String, Integer> anchorCountMap = new HashMap<>();

    private static Map<String, String> anchorFileNameMap = new HashMap<>();
    public static void main(String[] args) throws Exception{
        String txtXmlPath = TRConstant.DIR_PREFIX + TRConstant.TXT + TRConstant.xml;
        File txtXmlfile = new File(txtXmlPath);
        File[] txtFileList = txtXmlfile.listFiles();
        for(File txtFile : txtFileList){
            processFile(txtFile);
        }
        //processFile(new File(txtXmlPath + "/2012028987_日常病程_抢救病程录_62c6b64d-d4ed-40fa-b2c4-366e908eab2f.txt"));
        for(String key : anchorCountMap.keySet()){
            System.out.println(key + " " + anchorCountMap.get(key) + " " + anchorFileNameMap.get(key));
        }
    }

    private static void processFile(File txtFile) throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(txtFile), "UTF-8"));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        String lastLine = null;
        while((line = bufferedReader.readLine()) != null){
            if("".equals(line.trim())){
                continue;
            }
            if(lastLine != null){
                Matcher lastMatcher = PatternUtil.ANCHOR_PATTERN.matcher(lastLine);
                String lastAnchor = "";
                if (lastMatcher.find()) {
                    lastAnchor = lastMatcher.group(1);
                }
                Matcher currentMatcher = PatternUtil.ANCHOR_PATTERN.matcher(line);
                String currentAnchor = "";
                if (currentMatcher.find()) {
                    currentAnchor = currentMatcher.group(1);
                }
                if (!"".equals(lastAnchor)) {
                    if (lastAnchor.equals(currentAnchor)) {
                        stringBuilder.append(lastLine);
                        String currentLineContent = line.substring(line.indexOf("】】") + 2);
                        if(currentLineContent.startsWith("：") || currentLineContent.startsWith(":")){
                            currentLineContent = currentLineContent.substring(1);
                        }
                        lastLine = currentLineContent;
                        if(!anchorCountMap.containsKey(lastAnchor)){
                            anchorCountMap.put(lastAnchor, 0);
                            anchorFileNameMap.put(lastAnchor, txtFile.getAbsolutePath());
                        }
                        anchorCountMap.put(lastAnchor, anchorCountMap.get(lastAnchor) + 1);
                    } else {
                        stringBuilder.append(lastLine);
                        stringBuilder.append("\n");
                        lastLine = line;
                    }
                }else{
                    stringBuilder.append(lastLine);
                    stringBuilder.append("\n");
                    lastLine = line;
                }
            }else{
                lastLine = line;
            }
        }
        stringBuilder.append(lastLine);
        bufferedReader.close();
        //System.out.println(stringBuilder.toString());
        /*BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFile), "UTF-8"));
        bufferedWriter.flush();
        bufferedWriter.close();*/
    }
}
