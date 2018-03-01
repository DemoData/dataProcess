package com.example.demo.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class ExcelUtil {

    public static ArrayList<JSONObject> getProvinceCityList(String filePath) {
        ArrayList<JSONObject> firstExcel = new ArrayList<JSONObject>();
        try {
            File file = new File(filePath);
            FileInputStream inp = new FileInputStream(file);
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(0); // 获得第三个工作薄(2008工作薄)
            // 填充上面的表格,数据需要从数据库查询
            Row row5 = sheet.getRow(0); // 获得工作薄的第五行
            Cell cell54 = row5.getCell(0);// 获得第五行的第四个单元格

            int coloumNum = sheet.getRow(0).getPhysicalNumberOfCells();//获得总列数
            int rowNum = sheet.getLastRowNum();//获得总行数
            System.out.print(cell54.getStringCellValue() + ":" + coloumNum + ":" + rowNum);

            String coloum0 = sheet.getRow(0).getCell(0).getStringCellValue();
            String coloum1 = sheet.getRow(0).getCell(1).getStringCellValue();
            String coloum2 = sheet.getRow(0).getCell(2).getStringCellValue();
            String coloum3 = sheet.getRow(0).getCell(3).getStringCellValue();
            for (int i = 1; i <= rowNum; i++) {
                JSONObject jsonObject = new JSONObject();
                Row row = sheet.getRow(i);
                jsonObject.put(coloum0, row.getCell(0).getStringCellValue());
                jsonObject.put(coloum1, row.getCell(1).getStringCellValue());
                if (row.getCell(2) == null) {
                    jsonObject.put(coloum2, "");
                } else {
                    jsonObject.put(coloum2, row.getCell(2).getStringCellValue());
                }

                jsonObject.put(coloum3, row.getCell(3).getStringCellValue());
                firstExcel.add(jsonObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return firstExcel;
    }

    public static void main(String[] args) {
        ArrayList<JSONObject> result = getProvinceCityList("/Users/liulun/Desktop/标准映射_省市区到省.xlsx");
        for(JSONObject key : result){
            System.out.println(key);
        }

    }


}
