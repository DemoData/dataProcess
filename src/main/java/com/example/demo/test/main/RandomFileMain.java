package com.example.demo.test.main;

import com.example.demo.common.constant.CommonConstant;
import com.example.demo.util.FileUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class RandomFileMain {

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

    static Map<String, List<String>> typeFileNameMap = new HashMap<>();

    public static void main(String[] args) throws Exception{
        int sum = 0;
        String randPath = "/Users/liulun/Desktop/上海长海医院/血管外科/random";
        File randFile = new File(randPath);
        if(!randFile.exists()){
            randFile.mkdirs();
        }else{
            List<File> fileList = FileUtil.listTxtAllFile(randPath);
            for(File file : fileList){
                file.delete();
            }
        }
        for(String dirName : dirArr) {
            System.out.println(dirName);
            String path = "/Users/liulun/Desktop/上海长海医院/血管外科/txt/" + dirName;
            typeFileNameMap.put(dirName, new ArrayList<>());
            List<File> fileList = FileUtil.listTxtAllFile(path);
            List<Integer> getFileOrderList = new ArrayList<>();
            Random random = new Random();
            if(fileList.size() > CommonConstant.RANDOM_SIZE){
                while ((getFileOrderList.size() < CommonConstant.RANDOM_SIZE)){
                    int next = random.nextInt(fileList.size());
                    if(!getFileOrderList.contains(next)){
                        getFileOrderList.add(next);
                        String fileName = fileList.get(next).getName();
                        File newFile = new File(randPath + "/" + fileName);
                        typeFileNameMap.get(dirName).add(fileName);
                        Files.copy(fileList.get(next).toPath(), newFile.toPath());
                    }
                }
            }else{
                for(File file : fileList){
                    typeFileNameMap.get(dirName).add(file.getName());
                    File newFile = new File(randPath + "/" + file.getName());
                    Files.copy(file.toPath(), newFile.toPath());
                }
            }
        }
        writer(randPath,"随机记录", "xlsx", typeFileNameMap, new String[]{"原文","类型"});
        System.out.println(sum);
    }

    public static void writer(String path, String fileName,String fileType,Map<String, List<String>> result,String titleRow[]) throws Exception {
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
        for(String key : result.keySet()){

            List<String> fileList = result.get(key);
            for(String value : fileList){
                row = sheet.createRow(++rowIndex);
                cell = row.createCell(0);
                cell.setCellValue(FileUtil.readFile(new File(path + "/" + value)).replaceAll("【【", "\n【【"));
                cell = row.createCell(1);
                cell.setCellValue(key);
            }

        }

        //创建文件流
        OutputStream stream = new FileOutputStream(excelPath);
        //写入数据
        wb.write(stream);
        //关闭文件流
        stream.close();
    }
}
