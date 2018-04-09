package com.example.demo.tongren.main;

import com.example.demo.common.constant.CommonConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public class TRJYKTxtToMysqlTxtMain {

    public static void main(String[] args) throws Exception{
        String patientFilePath = TRConstant.DIR_PREFIX + "/病人列表_20180301_1.xlsx";
        Map<String, String> visitNumPatientIdMap = readExcelContentMap(patientFilePath, 0,2, 1);
        String parentPath = TRConstant.DIR_PREFIX + TRConstant.DIR_PATH;
        String mysqlPath = TRConstant.DIR_PREFIX + "/mysql";
        File mysqlFile = new File(mysqlPath);
        if(!mysqlFile.exists()){
            mysqlFile.mkdirs();
        }
        File JYKFile = new File(mysqlFile + "/jyk.txt");
        if(!JYKFile.exists()){
            JYKFile.createNewFile();
        }
        int sum = 0;
        BufferedWriter jylBufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(JYKFile), "UTF-8"));
        File parentPathFile = new File(parentPath);
        File[] visitNumFileList = parentPathFile.listFiles();
        for(File visitNumFile : visitNumFileList){
            String visitNumFileName = visitNumFile.getName();
            File[] formatFileList = visitNumFile.listFiles();
            for(File formatFile : formatFileList){
                String formatFileName = formatFile.getName();
                if("JYK".equals(formatFileName)){
                    File[] fileList = formatFile.listFiles();
                    for(File file: fileList){
                        sum++;
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Unicode"));
                        String line;
                        String fileName = file.getName();
                        String[] fileNameArr = fileName.split("_");
                        //是否细菌报告
                        boolean xijunFlag = false;
                        if(fileNameArr[4].startsWith("98")){
                            xijunFlag = true;
                        }
                        while((line = bufferedReader.readLine()) != null){
                            if(!line.contains("】【")){
                                //System.out.println("空：" + visitNumFile + fileName);
                                continue;
                            }
                            String[] lineArr = initArr(xijunFlag);
                            line = line.substring(1, line.length() - 1);
                            String[] tempArr = line.split("】【");
                            for(int i = 0; i < tempArr.length; i++){
                                if(xijunFlag){
                                    //System.out.println("空：" + visitNumFile + fileName);
                                    //System.out.println(tempArr[i]);
                                }
                                lineArr[i] = tempArr[i];
                            }
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("\\N");
                            stringBuilder.append(CommonConstant.MYSQL_SEPARATOR);
                            stringBuilder.append(visitNumFileName);
                            stringBuilder.append(CommonConstant.MYSQL_SEPARATOR);
                            String patientId = visitNumPatientIdMap.get(visitNumFileName);
                            if(StringUtils.isNotBlank(patientId)){
                                stringBuilder.append(patientId);
                            }else{
                                stringBuilder.append("\\N");
                            }stringBuilder.append(CommonConstant.MYSQL_SEPARATOR);
                            stringBuilder.append(TRTimeFormatUtil.formatTimeBySimpleDateFormat(fileNameArr[0]));
                            stringBuilder.append(CommonConstant.MYSQL_SEPARATOR);
                            stringBuilder.append(lineArr[0]);
                            stringBuilder.append(CommonConstant.MYSQL_SEPARATOR);
                            if(xijunFlag){
                                stringBuilder.append("\\N");
                            }else{
                                stringBuilder.append(lineArr[3]);
                            }
                            stringBuilder.append(CommonConstant.MYSQL_SEPARATOR);
                            if(xijunFlag){
                                stringBuilder.append(lineArr[2]);
                            }else{
                                stringBuilder.append("\\N");
                            }
                            stringBuilder.append(CommonConstant.MYSQL_SEPARATOR);
                            if(xijunFlag){
                                stringBuilder.append("\\N");
                            }else{
                                if(lineArr[2].contains(" ")){
                                    stringBuilder.append(lineArr[2].substring(0, lineArr[2].lastIndexOf(" ")));
                                }else{
                                    stringBuilder.append(lineArr[2]);
                                }
                                /*String[] result = lineArr[2].split(" ");
                                if(result.length == 0){
                                    stringBuilder.append("\\N");
                                }else if(result.length == 1 || result.length == 2){
                                    stringBuilder.append(result[0]);
                                }else if(result.length == 3){
                                    stringBuilder.append(result[0] + " " + result[1]);
                                }else{
                                    System.out.println(fileName);
                                    System.out.println(line);
                                    throw new Exception("化验结果单位拆分异常" + visitNumFileName);
                                }*/
                            }
                            stringBuilder.append(CommonConstant.MYSQL_SEPARATOR);
                            if(xijunFlag){
                                stringBuilder.append("\\N");
                            }else{
                                if(lineArr[2].contains(" ")){
                                    stringBuilder.append(lineArr[2].substring(lineArr[2].lastIndexOf(" ") + 1));
                                }else{
                                    stringBuilder.append("\\N");
                                }
                                /*String[] result = lineArr[2].split(" ");
                                if(result.length == 2) {
                                    stringBuilder.append(result[1]);
                                }else if(result.length == 1 || result.length == 0){
                                    stringBuilder.append("\\N");
                                }else if(result.length == 3){
                                    stringBuilder.append(result[2]);
                                }*/
                            }
                            stringBuilder.append(CommonConstant.MYSQL_SEPARATOR);
                            stringBuilder.append("\\N");
                            stringBuilder.append(CommonConstant.MYSQL_SEPARATOR);
                            if(xijunFlag){
                                stringBuilder.append("\\N");
                            }else{
                                stringBuilder.append(lineArr[1]);
                            }
                            stringBuilder.append(CommonConstant.MYSQL_SEPARATOR);
                            stringBuilder.append("\\N");
                            stringBuilder.append(CommonConstant.MYSQL_SEPARATOR);
                            if(xijunFlag){
                                stringBuilder.append(lineArr[1]);
                                if(line.length() < 5){
                                    System.out.println(visitNumFile + "/" + fileName);
                                    System.out.println(line);
                                }

                            }else{
                                stringBuilder.append("\\N");
                            }
                            jylBufferedWriter.write(stringBuilder.toString());
                            jylBufferedWriter.write("\n");
                        }
                        bufferedReader.close();
                    }

                }
            }
        }
        jylBufferedWriter.flush();
        jylBufferedWriter.close();
        System.out.println("文件总数:" + sum);
    }

    /**
     * 读取Excel数据内容
     *
     * @param
     * @return Map 包含单元格数据内容的Map对象
     */
    private static Map<String, String> readExcelContentMap(String excelFile, int sheetNum, int keyCol, int valueCol) throws IOException {
        POIFSFileSystem fs;
        XSSFWorkbook wb = null;
        XSSFSheet sheet;
        XSSFRow row;
        InputStream is = new FileInputStream(excelFile);
        Map<String, String> content = new HashMap<>();
        try {
            String str = "";
            wb = new XSSFWorkbook(is);
            sheet = wb.getSheetAt(sheetNum);
            // 得到总行数
            int rowNum = sheet.getLastRowNum();
            // 正文内容应该从第二行开始,第一行为表头的标题
            for (int i = 1; i <= rowNum; i++) {
                row = sheet.getRow(i);
                String key = getCellFormatValue(row.getCell(keyCol));
                String value = getCellFormatValue(row.getCell(valueCol));
                content.put(key, value);
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
                        cellvalue = String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue());
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

    public static String[] initArr(boolean xijunFlag){
        String[] lineArr;
        if(xijunFlag){
            lineArr = new String[3];
        }else{
            lineArr = new String[4];
        }
        for(int i = 0; i < lineArr.length; i++){
            lineArr[i] = " ";
        }
        return lineArr;
    }
}
