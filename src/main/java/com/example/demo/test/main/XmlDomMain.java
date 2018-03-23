package com.example.demo.test.main;

import com.example.demo.common.constant.CommonConstant;
import com.example.demo.util.FileUtil;
import com.example.demo.common.util.PatternUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

public class XmlDomMain {

    private static Map<String, Integer> anchorCountMap = new HashMap<>();
    private static Map<String, Integer> fieldAnchorCountMap = new HashMap<>();

    private static List<String> dirArr = new ArrayList<>();
    static {
        dirArr.add("病程记录");
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
        //System.out.println("a#&#辅助检查#&#b".split("#&#").length);
        /*int sum = 0;
        for(String dirName : dirArr){
            String path = "/Users/liulun/Desktop/上海长海医院/血管外科/" + dirName;
            List<File> fileList = FileUtil.listAllFile(path);
            sum += fileList.size();
            DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            for(File file : fileList){
                *//*if(!file.getName().equals("00595222_3_457724_入院记录00030001.xml")){
                    continue;
                }
                System.out.println(dirName + " " + file.getName());*//*
                formatXMLFile(file);
                Document xmlDoc = db.parse(file);
                *//*Element root = xmlDoc.getDocumentElement();
                NodeList childNodeList = root.getChildNodes();
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < childNodeList.getLength(); i++){
                    Node node = childNodeList.item(i);
                    String nodeName = node.getNodeName();
                    if("#text".equals(nodeName)){
                        continue;
                    }else if("text".equals(nodeName)){
                        sb.append(node.getTextContent());
                    }else if("section".equals(nodeName)){
                        processSection(sb, node);
                    }
                    sb.append("\n");
                }
                System.out.println(sb.toString());
                break;*//*
            }
        }
        System.out.println(sum);*/
        Set<String> anchorList = new HashSet<>();

        Set<String> fieldAnchorList = new HashSet<>();
        for(String dirName : dirArr){
            String path = "/Users/liulun/Desktop/上海长海医院/血管外科/" + dirName;
            List<File> fileList = FileUtil.listAllFile(path);
            for(int i = 0; i < fileList.size(); i++){
                File file = fileList.get(i);
                formatXMLFile(file);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
                String line;
                String lastLine = "";
                String nextLine = "";
                line = bufferedReader.readLine();
                while(line != null){
                    nextLine = bufferedReader.readLine();
                    if(line.startsWith("<text")){
                        String anchor = line.replaceAll("</text>", "").replaceAll("<text>", "").trim();
                        StringBuilder key = new StringBuilder();
                        if(StringUtils.isNotBlank(lastLine)) {
                            key.append(lastLine);
                        }else {
                            key.append(CommonConstant.ZHAN_WEI_FU);
                        }
                        key.append(CommonConstant.EXCEL_SEPARATOR);
                        key.append(anchor);
                        key.append(CommonConstant.EXCEL_SEPARATOR);
                        if(StringUtils.isNotBlank(nextLine)) {
                            key.append(nextLine);
                        }else {
                            key.append(CommonConstant.ZHAN_WEI_FU);
                        }
                        if(!anchorCountMap.containsKey(key.toString())){
                            anchorCountMap.put(key.toString(), 0);
                        }
                        anchorList.add(key.toString());
                        anchorCountMap.put(key.toString(), anchorCountMap.get(key.toString()) + 1);
                    }else if(line.startsWith("<fieldelem")){
                        Matcher matcher = PatternUtil.FIELDELEM_PATTERN.matcher(line);
                        if(matcher.find()){
                            String anchor = matcher.group(2);
                            String text = matcher.group(4);
                            StringBuilder key = new StringBuilder();
                            if(StringUtils.isNotBlank(lastLine)) {
                                key.append(lastLine);
                            }else {
                                key.append(CommonConstant.ZHAN_WEI_FU);
                            }
                            key.append(CommonConstant.EXCEL_SEPARATOR);
                            key.append(anchor);
                            key.append(CommonConstant.EXCEL_SEPARATOR);
                            if(StringUtils.isNotBlank(nextLine)) {
                                key.append(nextLine);
                            }else {
                                key.append(CommonConstant.ZHAN_WEI_FU);
                            }
                            if(!fieldAnchorCountMap.containsKey(key.toString())){
                                fieldAnchorCountMap.put(key.toString(), 0);
                            }
                            key.append(CommonConstant.EXCEL_SEPARATOR);
                            if(StringUtils.isNotBlank(text)) {
                                key.append(text);
                            }else {
                                key.append(CommonConstant.ZHAN_WEI_FU);
                            }
                            if(!fieldAnchorCountMap.containsKey(key.toString())){
                                fieldAnchorCountMap.put(key.toString(), 0);
                            }
                            fieldAnchorList.add(key.toString());
                            fieldAnchorCountMap.put(key.toString(), fieldAnchorCountMap.get(key.toString()) + 1);
                        }
                    }
                    lastLine = line;
                    line = nextLine;
                }
                bufferedReader.close();
            }
        }
        System.out.println(anchorList.size());
        writer("/Users/liulun/Desktop/上海长海医院","锚点text", "xlsx", anchorList, "text", new String[]{"锚点","上一行","下一行", "类型", "数量"});
        writer("/Users/liulun/Desktop/上海长海医院","锚点field", "xlsx", fieldAnchorList, "field", new String[]{"锚点","上一行","下一行", "类型", "数量"});
    }

    public static void writer(String path, String fileName,String fileType,Set<String> result, String type, String titleRow[]) throws IOException {
        Workbook wb = null;
        String excelPath = path+File.separator+fileName+"."+fileType;
        File file = new File(excelPath);
        Sheet sheet =null;
        //创建工作文档对象
        if (!file.exists()) {
            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook();

            } else if(fileType.equals("xlsx")) {

                wb = new XSSFWorkbook();
            } else {
                throw new RuntimeException("文件格式不正确");
            }
            //创建sheet对象
            sheet = (Sheet) wb.createSheet("锚点原文对应表");
            OutputStream outputStream = new FileOutputStream(excelPath);
            wb.write(outputStream);
            outputStream.flush();
            outputStream.close();

        } else {
            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook();

            } else if(fileType.equals("xlsx")) {
                wb = new XSSFWorkbook();

            } else {
                throw new RuntimeException("文件格式不正确");
            }
        }
        //创建sheet对象
        if (sheet==null) {
            sheet = (Sheet) wb.createSheet("sheet1");
        }

        //添加表头
        Row row = sheet.createRow(0);
        Cell cell;
        for(int i = 0;i < titleRow.length;i++){
            cell = row.createCell(i);
            cell.setCellValue(titleRow[i]);
        }
        int rowIndex = 0;
        for(String value : result){
            row = sheet.createRow(++rowIndex);
            cell = row.createCell(0);
            String[] valueArr = value.split(CommonConstant.EXCEL_SEPARATOR);
            cell.setCellValue(valueArr[1]);
            cell = row.createCell(1);
            cell.setCellValue(valueArr[0]);
            cell = row.createCell(2);
            cell.setCellValue(valueArr[2]);
            cell = row.createCell(3);
            cell.setCellValue(type);
            cell = row.createCell(4);
            if("text".equals(type)){
                cell.setCellValue(anchorCountMap.get(value));
            }else{
                cell.setCellValue(fieldAnchorCountMap.get(value));
                cell = row.createCell(5);
                cell.setCellValue(valueArr[3]);
            }
        }

        //创建文件流
        OutputStream stream = new FileOutputStream(excelPath);
        //写入数据
        wb.write(stream);
        //关闭文件流
        stream.close();
    }

    public static void formatXMLFile(File file) throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while((line = bufferedReader.readLine()) != null){
            if(line.contains("fieldelem")){
                String  codeAttr = "code-system=\"\"";
                int index = line.indexOf(codeAttr);
                if(line.charAt(index + codeAttr.length()) != '>'){
                    line = line.substring(0, index + codeAttr.length()) + ">" + line.substring(index + codeAttr.length());
                }
            }
            line = line.replaceAll("</c&gt;", "");
            if(line.startsWith("<text") && !line.endsWith("</text>")){
                line = line + "</text>";
            }
            if(line.startsWith("<fieldelem") && !line.endsWith("</fieldelem>")){
                line = line + "</fieldelem>";
            }
            stringBuilder.append(line + "\n");
        }
        bufferedReader.close();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "GBK"));
        bufferedWriter.write(stringBuilder.toString());
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public static void processSection(StringBuilder sb, Node section){
        NodeList nodeList = section.getChildNodes();
        if(nodeList.getLength() > 2){
            System.out.println(nodeList.item(1).getNodeName());
            if(!"text".equals(nodeList.item(1).getNodeName())){
                sb.append(((Element)section).getAttribute("name")+ ":");
            }
        }
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            String nodeName = node.getNodeName();
            if("#text".equals(nodeName)){
                continue;
            }else if("text".equals(nodeName)){
                sb.append(node.getTextContent() + "\n");
            }else if("section".equals(nodeName)){
                processSection(sb, node);
            }else if("fieldelem".equals(nodeName)){
                if(node.getNextSibling() != null && node.getNextSibling().getNextSibling() != null
                        && "fieldelem".equals(node.getNextSibling().getNextSibling().getNodeName())){
                    sb.append(node.getTextContent());
                }else if(node.getPreviousSibling() != null && node.getPreviousSibling().getPreviousSibling() != null
                        &&( "fieldelem".equals(node.getPreviousSibling().getPreviousSibling().getNodeName())
                        || ("text".equals(node.getPreviousSibling().getPreviousSibling().getNodeName())
                            && !"，".equals(node.getPreviousSibling().getPreviousSibling().getTextContent().replaceAll(",", "，"))))){
                    sb.append(node.getTextContent());
                }else{
                    sb.append(((Element)node).getAttribute("name")+ ":");
                    sb.append(node.getTextContent());
                }
            }
            sb.append(" ");
        }
        sb.append("\n");
    }
}
