package com.example.demo.test.main;

import com.example.demo.common.support.TextFormatter;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExcelCompareMain {

    static List<String> partList;
    public static void main(String[] args) throws Exception{
        partList = readExcelContent("/Users/liulun/Desktop/上海长海医院/词典_v29.0_功能.xlsx");
        List<String> contentList = readExcelContent("/Users/liulun/Desktop/上海长海医院/锚点text.xlsx");
        int j = 0;
        XSSFWorkbook wb = null;
        XSSFSheet sheet = null;
        XSSFRow row;
        InputStream is = new FileInputStream("/Users/liulun/Desktop/上海长海医院/锚点text.xlsx");
        try {
            wb = new XSSFWorkbook(is);
            sheet = wb.getSheetAt(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int sum = 0;
        for(String content : contentList){
            if(content.startsWith("：") || content.startsWith(":")){
                content = content.substring(1);
            }
            if(content.endsWith("：") || content.endsWith(":")){
                content = content.substring(0, content.length() - 1);
            }
            j++;
            if(partList.contains(content)){
                row = sheet.getRow(j);
                Cell cell = row.createCell(3);
                cell.setCellValue("N");
                System.out.println(content);
                sum++;
            }
        }
        //将文件保存到指定的位置
        try {
            FileOutputStream fos = new FileOutputStream("/Users/liulun/Desktop/上海长海医院/锚点text.xlsx");
            wb.write(fos);
            System.out.println("写入成功");
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(sum);
    }




    /**
     * 读取Excel数据内容
     *
     * @param
     * @return Map 包含单元格数据内容的Map对象
     */
    private static ArrayList<String> readExcelContent(String excelFile) throws IOException {
        POIFSFileSystem fs;
        XSSFWorkbook wb = null;
        XSSFSheet sheet;
        XSSFRow row;
        InputStream is = new FileInputStream(excelFile);
        ArrayList<String> content = new ArrayList<String>();
        try {
            wb = new XSSFWorkbook(is);
            sheet = wb.getSheetAt(0);
            // 得到总行数
            int rowNum = sheet.getLastRowNum();
            row = sheet.getRow(0);
            // 正文内容应该从第二行开始,第一行为表头的标题
            for (int i = 1; i <= rowNum; i++) {
                row = sheet.getRow(i);
                String str = getCellFormatValue(row.getCell(0)).trim();
                content.add(str);
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 根据HSSFCell类型设置数据
     *
     * @param cell
     * @return
     */
    private static String getCellFormatValue(XSSFCell cell) {
        String cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                // 如果当前Cell的Type为NUMERIC
                case HSSFCell.CELL_TYPE_NUMERIC:
                case HSSFCell.CELL_TYPE_FORMULA: {
                    // 判断当前的cell是否为Date
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        // 如果是Date类型则，转化为Data格式

                        //方法1：这样子的data格式是带时分秒的：2011-10-12 0:00:00
                        //cellvalue = cell.getDateCellValue().toLocaleString();

                        //方法2：这样子的data格式是不带带时分秒的：2011-10-12
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        cellvalue = sdf.format(date);
                    }
                    // 如果是纯数字
                    else {
                        // 取得当前Cell的数值
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                // 如果当前Cell的Type为STRIN
                case HSSFCell.CELL_TYPE_STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                // 默认的Cell值
                default:
                    cellvalue = " ";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;

    }
}
