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

        String test = ("2016年05月31日07时50分           出院小结入院日期：2016-05-26入院诊断：1.慢性胰腺炎出院日期：2016-06-02出院诊断：1.慢性胰腺炎住院天数：7 天       入院情况：主要症状及体征：张振力,男,40岁,汉族，已婚，因“腹痛20余天”门诊拟“慢性胰腺炎”于2016-05-26收入院。入院查体：腹平坦，无腹壁静脉曲张，腹部柔软，上腹痛轻压痛、反跳痛，腹部无包块。肝脏肋下未触及，脾脏肋下未触及，Murphy氏征阴性，肾区无叩击痛，无移动性浊音。肠鸣音未见异常，4次/分。腹部超声（2016-05-11 天津铁厂职工医院 2273）：胰腺回声不均匀，主胰管内多发结石，考虑慢性胰腺炎；腹部CT（2016-03-23 天津市北辰区中医院 123592）：1.胰腺多发钙化及胰管结石，胰管轻度不均匀；2.胃窦及十二指肠球部壁较厚；3.肝脏多发小囊性病变；4.心包前缘轻度增厚；彩超（2016-03-23 本院）：胰腺回声不均匀伴主胰管扩张。 　 住院期间主要检查结果：2016-5-26 血：白细胞计数5.64x10^9/L、中性粒细胞65.8%、血小板计数157x10^9/L、血红蛋白141g/L。血：钾4.2mmol/L、氯105mmol/L、葡萄糖5.7mmol/L、尿酸0.30mmol/L。凝血功能（-）。2016-5-27 血：间接胆素8.0umol/L、总胆红素11.6umol/L、直接胆红素3.6umol/L、白蛋白44g/L、丙氨酸氨基转移酶13U/L、碱性磷酸酶76U/L、总胆固醇5.18mmol/L、甘油三酯0.82mmol/L。血：乙型肝炎表面抗原HBsAg阴性 0.02IU/ml、乙型肝炎表面抗体Anti-HBs阴性 0.00mIU/ml。血：丙型肝炎抗体IgG(化学发光法)阴性、HIV抗原抗体(酶联免疫法)阴性、梅毒抗体(化学发光法)阴性。血：甲胎蛋白(AFP)2.66ng/ml、糖链抗原125(CA125)7.10U/ml、糖链抗原15-3(CA15-3)4.00U/ml。血：糖类抗原CA19919.05U/ml、糖类抗原CA7240.93U/ml、癌胚抗原1.14ng/ml。血：急诊淀粉酶100U/L。2016-5-28 血：白细胞计数6.02x10^9/L、中性粒细胞71.0%、血小板计数130x10^9/L。血：淀粉酶96U/L。血：急诊淀粉酶247U/L。尿粪常规未见明显异常。心电图示：正常心电图。腹部B超示：主胰管多发结石伴主胰管扩张，余未见明显异常。胸片示：心肺隔未见明显异常。诊疗经过：治疗情况：入院后完善检查，于2016-05-27日行ESWL术，于2016-05-30日行ERCP术，术后予禁食，抗炎，抑酸，抑酶等对症治疗。病理结果：无。出院情况：目前患者一般情况尚可,病情平稳，经王凯旋副主任医师同意予以出院。出院去向：回家。出院指导：    1、出院带药：胰酶肠溶胶囊(0.3g);3/日;口服    2、用药指导：具体用药指导按说明书。                  特殊用药指导：无。                 食物/药物间相互作用：无。    3、康复指导：生活自理：完全能自理活动指导：在能耐受范围内适当活动    4、饮食指导：无禁忌                 特殊饮食指导：低脂    5、随访及复诊：建议低脂饮食，定期复诊，定期复查血常规，肝肾功能，消化内科随诊，需要及时来院就诊的情况：如有不适，及时就诊。医生/护士已经就出院计划与患方进行了讨论并给予相关指导。    告知患方可根据自己的意愿，前往所在的社区医疗机构或长海医院复诊。    书面出院小结和指导已交于患方一份。王凯旋/朱利芹")
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
