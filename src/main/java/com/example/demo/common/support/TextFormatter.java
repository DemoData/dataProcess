package com.example.demo.common.support;

import com.alibaba.fastjson.JSONArray;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 用于处理文本信息的锚点格式化
 */
public class TextFormatter {

    public final static String PROP_NAME = "propName";

    public final static String COLUMN_NAME = "columnName";
    public final static String TEXT = "text";
    public final static String TEXT_ARS = "textARS";
    public final static String ANCHOR_EXCEL_PATH = "/data/hitales/SHCHRK/技术用-症状&体征-锚点使用.xlsx";


    private TextFormatter() {
    }

    /**
     * Format info.text adn info.textARS
     *
     * @param properties
     * @param bean
     * @return
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Map<String, String> textFormatter(List<Map<String, String>> properties, Object bean) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        List<Map<String, Object>> infoList = new ArrayList<>();

        for (Map<String, String> property : properties) {
            String propName = property.get(PROP_NAME);
            String columnName = property.get(COLUMN_NAME);
            Map<String, Object> row = new HashMap<>();
            PropertyDescriptor pd = new PropertyDescriptor(propName, bean.getClass());
            Object value = pd.getReadMethod().invoke(bean);
            if (value == null) {
                continue;
            }
            if (value instanceof String && StringUtils.isEmpty(((String) value).trim())) {
                continue;
            }
            row.put(columnName, value);
            infoList.add(row);
        }
        StringBuffer text = new StringBuffer();
        StringBuffer textARS = new StringBuffer();
        String prefixSymbol = "【【";
        String suffixSymbol = "】】";
        String colonSymbol = "：";
        for (Map<String, Object> row : infoList) {
            for (Map.Entry entry : row.entrySet()) {
                text.append(prefixSymbol).append(entry.getKey()).append(suffixSymbol).append(colonSymbol).append(entry.getValue()).append("\n");
                textARS.append(entry.getKey()).append(colonSymbol).append(entry.getValue()).append("\n");
            }
        }
        Map<String, String> result = new HashMap<>();
        result.put(TEXT, text.toString());
        result.put(TEXT_ARS, textARS.toString());
        return result;
    }

    public static String formatTextByAnchaor(String text) {
        String formattedText = null;
        try {
            //制定需要获取的列
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i <= 23; i++) {
                if (i != 0 && i != 2 && i != 3) {
                    list.add(i);
                }
            }
            //获取Excel中的锚点
            ArrayList<String> anchorList = readExcelContent(ANCHOR_EXCEL_PATH, 0, list);
            formattedText = addAnchor(text, anchorList);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formattedText;
    }

    /**
     * @param text
     * @return
     * @throws Exception
     */
    public static String addAnchor(String text, ArrayList<String> anchors) throws Exception {
        BufferedReader br = new BufferedReader(new StringReader(text));
        String line = br.readLine();
        ArrayList<String> textLines = new ArrayList<String>();
        while (line != null) {
            line = line.trim();
            textLines.add(line);
            line = br.readLine();
        }
        JSONArray lines = new JSONArray();
        for (int i = 0; i < textLines.size(); i++) {
            lines.add(new LineItem(textLines.get(i), null, false));
        }
        for (int k = 0; k < lines.size(); k++) {
            SplitAnchor splitAnchor = new SplitAnchor(k == 0 ? null : lines.getObject(k - 1, LineItem.class),
                    lines.getObject(k, LineItem.class), k < lines.size() - 1 ? lines.getObject(k + 1, LineItem.class) : null, anchors);
            splitAnchor.tagAnchor();
        }
//        Set<String> tempAnchor = new HashSet<>();
        StringBuilder sbu = new StringBuilder();
        for (int k = 0; k < lines.size(); k++) {
            LineItem lineItem = lines.getObject(k, LineItem.class);
            String tempStr = lineItem.getLineWithAnchorBlackBracket(k, null);
            /*ArrayList<AnchorInfo> anchorInfos = lineItem.anchorInfos;
            for (AnchorInfo anchorInfo : anchorInfos) {
                if (!anchorOriginalMap.containsKey(anchorInfo.anchor)) {
                    anchorOriginalMap.put(anchorInfo.anchor, tempStr);
                    anchorCountMap.put(anchorInfo.anchor, 1);
                    tempAnchor.add(anchorInfo.anchor);
                } else {
                    anchorCountMap.put(anchorInfo.anchor, anchorCountMap.get(anchorInfo.anchor) + 1);
                }
            }*/
            sbu.append(tempStr).append("\n");
            //sbu.append(tempStr);
        }
        String result = sbu.toString().replaceAll("\\d【【记录时间】】", "【【记录时间】】").replaceAll("【【时间结束标记】】", "");
        /*for (String value : tempAnchor) {
            anchorOriginalMap.put(value, result);
        }*/
        //System.out.println(sbu.toString());
        return result;
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
