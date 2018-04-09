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

        String test = ("2016年12月30日10时01分出院小结\n" +
                "【【入院日期】】：2016-12-26\n" +
                "【【入院诊断】】：1.下肢动脉硬化闭塞症（间歇性跛行期）2.高血压Ⅲ级3.糖尿病\n" +
                "【【出院日期】】：2016-12-31\n" +
                "【【出院诊断】】：1.下肢动脉硬化闭塞症（间歇性跛行期）2.高血压Ⅲ级3.糖尿病\n" +
                "【【住院天数】】：5天\n" +
                "【【主要症状及体征】】：吴双娣,女,83岁,汉族，已婚，因“左下肢间歇性跛行一月余”门诊拟“左下肢动脉硬化闭塞”于2016-12-26收入院。\n" +
                "【【入院查体】】：双下肢无畸形，左皮肤色泽稍苍白，右侧皮肤色泽正常。左下肢皮温低。左侧足背动脉未触及，左侧胫后动脉未触及，右侧足背动脉、胫后动脉搏动正常。双侧股动脉搏正常。双下肢感觉功能未见明显异常，活动可。双侧桡动脉、颈动脉搏动无明显异常。\n" +
                "【【住院期间主要检查结果】】：2016/12/27 10:05:04  血  血浆D-二聚体2.49μg/ml、活化部分凝血活酶时间30.2s、凝血酶时间17.4s、纤维蛋白降解产物7.11μg/ml、凝血酶时间对照17.0s、凝血酶原时间14.1s、凝血酶原时间对照13.2s、纤维蛋白原2.90g/L、凝血酶原时间国际标准化比值1.1、活化部分凝血活酶时间对照33.0s2016/12/27 9:45:02  血  白细胞计数4.87x10^9/L、中性粒细胞计数3.48x10^9/L、血小板计数265x10^9/L、血小板压积0.25%、中性粒细胞71.3%、血红蛋白92g/L2016/12/27 8:57:03  血  B型钠尿肽(BNP)41.00pg/ml、肌酸磷酸激酶(CKMB质量法)0.50μg/L、肌红蛋白(Myo)17.200ng/ml、高敏肌钙蛋白I(cTnI)0.02μg/L、心型脂肪酸结合蛋白&lt;2.5ng/ml      　\n" +
                "【【诊疗经过】】：患者入院后积极完善相关检查。\n" +
                "【【治疗情况】】：入院后于2016-12-28日予左下肢PTA+斑块旋切。术后予抗凝、活血治疗，现患者恢复可，予出院。病理结果：无。\n" +
                "【【出院情况】】：目前患者一般情况尚可，诉患者症状较前好转，病情平稳，今裴轶飞主治医师同意予以出院。\n" +
                "【【出院去向】】：回家\n" +
                "【【出院指导】】：1、出院带药：硫酸氢氯吡格雷普通片 75.0000mg 1/日 口服己酮可可碱缓释片0.4g 3/日 口服阿托伐他汀钙片20mg  1/日口服贝前列素钠片 40ug  3/日口服奥美拉唑肠溶胶囊20mg  1/日口服2、用药指导：具体用药指导按说明书。：无。食物/药物间相互作用：无。3、康复指导：：部分自理：在能耐受范围内适当活动4、饮食指导：有禁忌荤：无5、随访及复诊：裴轶飞主治医师周二上午门诊，包俊教授周一上午门诊，周二下午特需门诊需要及时来院就诊的情况：不明原因的出血，下肢疼痛加重，以及其他自认为需要来院的情况。医生/护士已经就出院计划与患方进行了讨论并给予相关指导。告知患方可根据自己的意愿，前往所在的社区医疗机构或长海医院复诊。书面出院小结和指导已交于患方一份。\n" +
                "【【病程签名】】：裴轶飞/陈茂春")
                .replaceAll("【【", "").replaceAll("】】","").replaceAll("\n", "");
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
            //line = StringUtil.removeAllBlank(line);
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
            //tempStr = tempStr.replaceAll("\n", "").replaceAll("\r", "");
            //tempStr = tempStr.replaceAll("【【", "\n【【");
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
