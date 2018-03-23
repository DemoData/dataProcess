package com.example.demo.util;

import com.alibaba.fastjson.JSONArray;
import com.example.demo.common.support.LineItem;
import com.example.demo.common.support.SplitAnchor;
import com.example.demo.common.support.TextFormatter;
import com.example.demo.common.util.PatternUtil;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

public class ExcelAnchorUtil {

    public static List<String> anchors;
    public final static String ANCHOR_EXCEL_PATH = "/Users/liulun/Desktop/上海长海医院/技术用-症状&体征-锚点使用.xlsx";
    public final static String EXCEL_PATH = "/Users/liulun/Desktop/上海长海医院/长海胰腺检验科入出院随.xlsx";

    public static void main(String[] args) throws Exception{
        imExcel();
    }

    public static int countAnchorCount(String text){
        int count = 0;
        Matcher matcher = PatternUtil.ANCHOR_PATTERN.matcher(text);
        while(matcher.find()){
           count++;
        }
        return count;
    }

    public static void imExcel() throws Exception {
        //制定需要获取的列
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i <= 23; i++) {
            if (i != 0 && i != 2 && i != 3) {
                list.add(i);
            }
        }
        //锚点数据
        anchors = readExcelContent(ANCHOR_EXCEL_PATH, 0, list);
        List<String> textArr = readExcelContent(EXCEL_PATH, 2);


        Iterator<String> itor = textArr.iterator();
        int j = 0;
        int error = 0;
        XSSFWorkbook wb = null;
        XSSFSheet sheet = null;
        XSSFRow row;
        InputStream is = new FileInputStream("/Users/liulun/Desktop/上海长海医院/长海胰腺检验科入出院随.xlsx");
        try {
            wb = new XSSFWorkbook(is);
            sheet = wb.getSheetAt(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        while(itor.hasNext()){
            String textARS = itor.next().trim();
            int first = countAnchorCount(textARS);
            String originText = textARS;
            originText = originText.replaceAll("【【", "").replaceAll("】】", "");
            System.out.println(originText);
            //System.out.println(textARS);
            //textARS = "西医：类风湿关节炎，";
            String result = TextFormatter.addAnchor(originText, anchors);
            result = result.replaceAll("原文记录标题", "【【原文记录标题】】");
            result = result.replaceAll("【【【【原文记录标题】】】】", "【【原文记录标题】】");
            j++;
            row = sheet.getRow(j);
            Cell cell = row.createCell(7);
            cell.setCellValue(result);
            int second = countAnchorCount(result);
            if(!result.equals(textARS)){
                error++;
                cell = row.createCell(8);
                cell.setCellValue("否");
            }else{
                cell = row.createCell(8);
                cell.setCellValue("是");
            }
            cell = row.createCell(9);
            cell.setCellValue(first);
            cell = row.createCell(10);
            cell.setCellValue(second);

        }
        //将文件保存到指定的位置
        try {
            FileOutputStream fos = new FileOutputStream("/Users/liulun/Desktop/上海长海医院/长海胰腺检验科入出院随_2.xlsx");
            wb.write(fos);
            System.out.println("写入成功");
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(error);
    }




    /**
     * 读取Excel数据内容
     *
     * @param
     * @return Map 包含单元格数据内容的Map对象
     */
    private static ArrayList<String> readExcelContent(String excelFile, int sheetNum, List<Integer> readColList) throws IOException {
        POIFSFileSystem fs;
        XSSFWorkbook wb = null;
        XSSFSheet sheet;
        XSSFRow row;
        InputStream is = new FileInputStream(excelFile);
        ArrayList<String> content = new ArrayList<String>();
        try {
            String str = "";
            wb = new XSSFWorkbook(is);
            sheet = wb.getSheetAt(sheetNum);
            // 得到总行数
            int rowNum = sheet.getLastRowNum();
            row = sheet.getRow(0);
            int colNum = row.getPhysicalNumberOfCells();
            // 正文内容应该从第二行开始,第一行为表头的标题
            for (int i = 1; i <= rowNum; i++) {
                row = sheet.getRow(i);
                int j = 0;
                while (j < colNum) {
                    if (readColList.contains(j)) {
                        str = getCellFormatValue(row.getCell((short) j)).trim();
                        if (str.length() > 0) {
                            content.add(str);
                        }
                    }
                    j++;
                }
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 读取Excel数据内容
     *
     * @param
     * @return Map 包含单元格数据内容的Map对象
     */
    private static ArrayList<String> readExcelContent(String excelFile, int readCol) throws IOException {
        POIFSFileSystem fs;
        XSSFWorkbook wb = null;
        XSSFSheet sheet;
        XSSFRow row;
        InputStream is = new FileInputStream(excelFile);
        ArrayList<String> content = new ArrayList<String>();
        try {
            String str = "";
            wb = new XSSFWorkbook(is);
            sheet = wb.getSheetAt(0);
// 得到总行数
            int rowNum = sheet.getLastRowNum();
            row = sheet.getRow(0);
            int colNum = row.getPhysicalNumberOfCells();
// 正文内容应该从第二行开始,第一行为表头的标题
            for (int i = 1; i <= rowNum; i++) {
                row = sheet.getRow(i);
                int j = readCol;
                while (j < colNum) {
                    str += getCellFormatValue(row.getCell((short) j)).trim();
                    break;
                }
                if (str.length() > 0) {
                    content.add(str);
                }
                str = "";
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
