package com.example.demo.common.support;

import com.alibaba.fastjson.JSONArray;
import com.example.demo.common.util.StringUtil;
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
    public final static String ANCHOR_EXCEL_PATH = "/Users/liulun/Desktop/上海长海医院/技术用-症状&体征-锚点使用.xlsx";

    public static ArrayList<String> anchorList = null;

    static {
        //制定需要获取的列
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i <= 23; i++) {
            if (i != 0 && i != 2 && i != 3) {
                list.add(i);
            }
        }
        //获取Excel中的锚点
        try {
            anchorList = readExcelContent(ANCHOR_EXCEL_PATH, 0, list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String test = ("病史为体检入院。一般可，食欲睡眠正常，大小便正常，近期体重无变化。既往史：发作性胸闷、气促2周，外院胸部CT等检查未发现明显异常。体格检查体温：36.0℃，脉搏：70次/分，呼吸：18次/分，血压：128/70mmHg，体重55kg　　　　　　　　　　体检计划拟安排检查项目：血常规、肝功、肾功电解质、血脂、糖化血红蛋白、三抗、丙肝、梅毒、HIV抗体、AFP、SCC鳞癌相关抗原、CA242、CEA、CA199、NSE、甲状腺功能、心肌损伤标志物、B型钠尿肽（BNP）、吸入性及食物性过敏原特异性检测、尿常规、粪常规、动态心电图、肺功能。预计体检时间3天。入院后各项辅助检查结果:动态心电图（24小时）:见附页肺功能+舒张试验：见附页实验室检查:见附页出院诊断：1.高甘油三酯血症2.焦虑症3.早搏（偶发）建议：1.高甘油三酯血症：低脂低糖饮食，加强运动，控制体重，复查肝功、血脂。2.焦虑症：建议服药：来士普5mg1/早，1周后改为10mg1/早，维持至少半年，神经内科随诊。3.早搏（偶发）：目前不需药物治疗。如有反复心悸，症状明显及时复查动态心电图，根据结果决定是否需进一步治疗。心内科门诊随访。4.保持合理的饮食生活习惯，避免劳累，定期体检。2016-08-04出院小结入院日期：2016-08-02入院诊断：胸闷待查出院日期：2016年08月04日出院诊断：1.焦虑症2.高甘油三酯血症住院天数：2天住院经过：陈美华,女,54岁,汉族，已婚，因“发作性胸闷、气促2周”门诊拟“胸闷待查”于2016-08-02收入院。入院查体：体型偏胖，浅表淋巴结未触及。心肺听诊无明显异常。心肺听诊无明显异常。腹部平坦，全腹未扪及明显包块，无压痛及反跳痛。双下肢无水肿。辅检：肺功能：通气功能正常，弥散功能正常，残气量下降，支气管舒张试验阴性，改善量为-20ml；动态心电图示窦性心律房性早搏（18个单发房早，2次成对房早），时呈房性早搏未下传，三联律，成对房性早搏；室性早搏（1个）。入院后完善相关检查，给予镇静、抗焦虑药物治疗，住院期间病情平稳，无明显胸闷、气促症状发生。目前患者一般情况好，病情平稳，经上级医师同意予以今日出院。出院医嘱：1.出院带药：来士普5mg1/日口服（1周后改10mg1/日）2.低脂低糖饮食，加强运动，控制体重；3.不适随诊。洪惠兰")
                .replaceAll("【【", "").replaceAll("】】","");
        System.out.println(test);
        System.out.println(formatTextByAnchaor(test).replaceAll("【【", "\n【【"));
    }

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
            formattedText = addAnchor(text, anchorList);
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
    public static String addAnchor(String text, List<String> anchors) throws Exception {
        BufferedReader br = new BufferedReader(new StringReader(text));
        String line = br.readLine();
        ArrayList<String> textLines = new ArrayList<String>();
        while (line != null) {
            //line = line.trim();
            line = StringUtil.removeAllBlank(line);
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
        StringBuilder sbu = new StringBuilder();
        for (int k = 0; k < lines.size(); k++) {
            LineItem lineItem = lines.getObject(k, LineItem.class);
            String tempStr = lineItem.getLineWithAnchorBlackBracket(k, null);
            tempStr = tempStr.replaceAll("\n", "").replaceAll("\r", "");
            tempStr = tempStr.replaceAll("【【", "\n【【");
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
            sbu.append(tempStr);
            sbu.append("\n");
        }
        String result = sbu.toString().replaceAll("\\d【【记录时间】】", "【【记录时间】】").replaceAll("\\d\n【【记录时间】】", "【【记录时间】】").replaceAll("【【时间结束标记】】", "");
        /*for (String value : tempAnchor) {
            anchorOriginalMap.put(value, result);
        }*/
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
