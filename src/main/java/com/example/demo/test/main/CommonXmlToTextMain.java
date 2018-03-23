package com.example.demo.test.main;

import com.example.demo.util.FileUtil;
import com.example.demo.util.PatternUtil;
import com.example.demo.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

public class CommonXmlToTextMain {

    private static Map<String, Integer> anchorCountMap = new HashMap<>();
    private static Map<String, Integer> fieldAnchorCountMap = new HashMap<>();
    private static List<String> textAnchorList = new ArrayList<>();
    private static List<String> fieldNotAnchorList = new ArrayList<>();
    private static List<String> commonInfoAnchorList = new ArrayList<>();
    //除基本信息外其它section必须添加的锚点
    private static List<String> anchorList = new ArrayList<>();
    private static int ANCHOR = 0;
    private static int NOT_ANCHOR = 1;

    private static List<String> dirArr = new ArrayList<>();
    static {
        dirArr.add("入院记录");
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

    static {
        anchorList.add("诊断签名");
        anchorList.add("营养会诊");
        anchorList.add("康复会诊");
    }


    public static void addCommonInfoAnchor(){
        commonInfoAnchorList.add("出生地");
        commonInfoAnchorList.add("姓名");
        commonInfoAnchorList.add("性别");
        commonInfoAnchorList.add("民族");
        commonInfoAnchorList.add("婚姻状况");
        commonInfoAnchorList.add("住址");
        commonInfoAnchorList.add("病史叙述者");
        commonInfoAnchorList.add("记录日期");
        commonInfoAnchorList.add("年龄");
        commonInfoAnchorList.add("职业");
        commonInfoAnchorList.add("籍贯");
        commonInfoAnchorList.add("供史者");
        commonInfoAnchorList.add("地址");
        commonInfoAnchorList.add("入院时间");
        commonInfoAnchorList.add("婚姻");
        commonInfoAnchorList.add("记录时间");
    }

    public static void mockData(){
        textAnchorList.add("主  诉：");
        textAnchorList.add("既往史：");
        textAnchorList.add("个人史：");
        textAnchorList.add("体    格    检    查");
        textAnchorList.add("最后诊断");
        textAnchorList.add("初步诊断");
        textAnchorList.add("辅助检查");
        textAnchorList.add("专科检查：");
        textAnchorList.add("家族史：");
        textAnchorList.add("现病史：");
        textAnchorList.add("婚育史：");
        textAnchorList.add("记录时间");
        textAnchorList.add("病史叙述者");
        textAnchorList.add("家住");
        textAnchorList.add("籍贯");
        textAnchorList.add("入院时间");
        textAnchorList.add("入院时情况");
        textAnchorList.add("入院诊断");
        textAnchorList.add("诊疗经过(抢救经过)");
        textAnchorList.add("死亡原因");
        textAnchorList.add("死亡诊断");

        fieldNotAnchorList.add("肢体");
        fieldNotAnchorList.add("缺血症状");
        fieldNotAnchorList.add("时间");
        fieldNotAnchorList.add("起病时间");
        fieldNotAnchorList.add("诱因");
        fieldNotAnchorList.add("治疗过程");
        fieldNotAnchorList.add("精神状态");
        fieldNotAnchorList.add("体力情况");
        fieldNotAnchorList.add("食欲食量");
        fieldNotAnchorList.add("睡眠情况");
        fieldNotAnchorList.add("体重变化");
        fieldNotAnchorList.add("大便");
        fieldNotAnchorList.add("小便");
        fieldNotAnchorList.add("传染病史");
        fieldNotAnchorList.add("心血管病史");
        fieldNotAnchorList.add("手术外伤史");
        fieldNotAnchorList.add("输血史");
        fieldNotAnchorList.add("过敏史");
        fieldNotAnchorList.add("传染病史");
        fieldNotAnchorList.add("预防接种史");
        fieldNotAnchorList.add("出生地");
        fieldNotAnchorList.add("疫区接触");
        fieldNotAnchorList.add("其他接触史");
        fieldNotAnchorList.add("吸烟饮酒史");
        fieldNotAnchorList.add("吸烟饮酒史");
        fieldNotAnchorList.add("父母");
        fieldNotAnchorList.add("父母身体情况");
        fieldNotAnchorList.add("兄弟姐妹");
        fieldNotAnchorList.add("兄弟姐妹身体情况");
        fieldNotAnchorList.add("体温");
        fieldNotAnchorList.add("脉搏");
        fieldNotAnchorList.add("呼吸");
        fieldNotAnchorList.add("收缩压");
        fieldNotAnchorList.add("发育");
        fieldNotAnchorList.add("营养");
        fieldNotAnchorList.add("面容");
        fieldNotAnchorList.add("表情");
        fieldNotAnchorList.add("体位");
        fieldNotAnchorList.add("意识");
        fieldNotAnchorList.add("精神状态");
        fieldNotAnchorList.add("配合检查");
        fieldNotAnchorList.add("色泽");
        fieldNotAnchorList.add("皮肤异常");
        fieldNotAnchorList.add("毛发分布");
        fieldNotAnchorList.add("水肿");
        fieldNotAnchorList.add("肝掌蜘蛛痣");
        fieldNotAnchorList.add("全身浅表淋巴结");
        fieldNotAnchorList.add("头颅异常");
        fieldNotAnchorList.add("眼睑水肿");
        fieldNotAnchorList.add("结膜");
        fieldNotAnchorList.add("眼球外形");
        fieldNotAnchorList.add("巩膜");
        fieldNotAnchorList.add("角膜");
        fieldNotAnchorList.add("瞳孔");
        fieldNotAnchorList.add("瞳孔直径");
        fieldNotAnchorList.add("对光反射");
        fieldNotAnchorList.add("外耳道");
        fieldNotAnchorList.add("乳突压痛");
        fieldNotAnchorList.add("鼻外观");
        fieldNotAnchorList.add("鼻翼");
        fieldNotAnchorList.add("口唇");
        fieldNotAnchorList.add("口腔粘膜");
        fieldNotAnchorList.add("舌苔");
        fieldNotAnchorList.add("伸舌");
        fieldNotAnchorList.add("齿龈");
        fieldNotAnchorList.add("咽部粘膜");
        fieldNotAnchorList.add("扁桃体");
        fieldNotAnchorList.add("颈");
        fieldNotAnchorList.add("颈抵抗");
        fieldNotAnchorList.add("气管");
        fieldNotAnchorList.add("颈动脉");
        fieldNotAnchorList.add("颈静脉");
        fieldNotAnchorList.add("肝颈静脉回流征");
        fieldNotAnchorList.add("甲状腺");
        fieldNotAnchorList.add("甲状腺异常");
        fieldNotAnchorList.add("胸廓");
        fieldNotAnchorList.add("胸骨压痛");
        fieldNotAnchorList.add("乳房");
        fieldNotAnchorList.add("呼吸运动");
        fieldNotAnchorList.add("肋间隙");
        fieldNotAnchorList.add("语颤");
        fieldNotAnchorList.add("叩诊音");
        fieldNotAnchorList.add("呼吸规整");
        fieldNotAnchorList.add("呼吸音");
        fieldNotAnchorList.add("左右");
        fieldNotAnchorList.add("啰音部位");
        fieldNotAnchorList.add("闻及否");
        fieldNotAnchorList.add("罗音");
        fieldNotAnchorList.add("胸膜摩擦音");
        fieldNotAnchorList.add("心前区隆起");
        fieldNotAnchorList.add("心尖搏动");
        fieldNotAnchorList.add("相对浊音界");
        fieldNotAnchorList.add("心率");
        fieldNotAnchorList.add("心律");
        fieldNotAnchorList.add("杂音");
        fieldNotAnchorList.add("心包摩擦音");
        fieldNotAnchorList.add("肛门直肠");
        fieldNotAnchorList.add("脊柱");
        fieldNotAnchorList.add("四肢活动");
        fieldNotAnchorList.add("关节形态");
        fieldNotAnchorList.add("双下肢");
        fieldNotAnchorList.add("肢体");
        fieldNotAnchorList.add("色泽");
        fieldNotAnchorList.add("肢体");
        fieldNotAnchorList.add("最后诊断");
        fieldNotAnchorList.add("配偶");
        fieldNotAnchorList.add("舒张压");
        fieldNotAnchorList.add("鼻窦");
        fieldNotAnchorList.add("初步诊断");
        fieldNotAnchorList.add("其他疾病情况");
        fieldNotAnchorList.add("吸烟时间");
        fieldNotAnchorList.add("吸烟时间单位");
        fieldNotAnchorList.add("吸烟数量");
        fieldNotAnchorList.add("戒烟情况");
        fieldNotAnchorList.add("冶游史");
        fieldNotAnchorList.add("姓名");
        fieldNotAnchorList.add("神志");
        fieldNotAnchorList.add("既往一般健康状况");
        fieldNotAnchorList.add("患病时间");
        fieldNotAnchorList.add("药物名称");
        fieldNotAnchorList.add("胸骨叩痛");
    }
    public static void main(String[] args) throws Exception{
        int sum = 0;
        textAnchorList = readExcelContent("/Users/liulun/Desktop/上海长海医院/锚点text.xlsx", ANCHOR);
        fieldNotAnchorList = readExcelContent("/Users/liulun/Desktop/上海长海医院/锚点field.xlsx", NOT_ANCHOR);
        mockData();
        addCommonInfoAnchor();
        for(String dirName : dirArr){
            String path = "/Users/liulun/Desktop/上海长海医院/血管外科/" + dirName;
            String txtPath = "/Users/liulun/Desktop/上海长海医院/血管外科/txt/" + dirName;
            File txtPathFile = new File(txtPath);
            if(!txtPathFile.exists()){
                System.out.println(txtPath);
                txtPathFile.mkdirs();
            }
            List<File> fileList = FileUtil.listAllFile(path);
            sum += fileList.size();
            boolean commonInfoFlag = false;
            for(int i = 0; i < fileList.size(); i++){
                File file = fileList.get(i);
                if("P02686677_1_716139_入院记录00010010.xml".equals(file.getName())){
                    System.out.println(file.getName());
                }
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                int sectionTop = 0;
                while((line = bufferedReader.readLine()) != null){
                    if(sectionTop < 3){
                        if(line.startsWith("<text")){
                            String text = line.replaceAll("</text>", "").replaceAll("<text>", "").trim();
                            Matcher matcher = PatternUtil.CHINESE_PATTERN.matcher(text);
                            String anchor = "";
                            if(matcher.find()){
                                anchor = matcher.group();
                            }
                            /*if("最后诊断".equals(anchor) || "初步诊断".equals(anchor)){
                                System.out.println(anchor);
                            }*/
                            if((textAnchorList.contains(text) || textAnchorList.contains(anchor))){
                                stringBuilder.append("【【");
                                text = text.replaceAll(":", "：");
                                if(text.startsWith("。") || text.startsWith(",") || text.startsWith("，")){
                                    text = text.substring(1);
                                }
                                if(text.contains("：")){
                                    stringBuilder.append(text.substring(0, text.indexOf("：")));
                                    stringBuilder.append("】】");
                                    stringBuilder.append(text.substring(text.indexOf("：")));
                                }else{
                                    stringBuilder.append(text);
                                    stringBuilder.append("】】：");
                                }
                            }else{
                                stringBuilder.append(text);
                            }
                            stringBuilder.append("\n");
                        }else if(line.startsWith("<fieldelem")){
                            Matcher matcher = PatternUtil.FIELDELEM_PATTERN.matcher(line);
                            if(matcher.find()){
                                String anchor = matcher.group(2);
                                String text = matcher.group(4);
                                text = StringUtil.removeAllBlank(text);
                                /*if(!fieldNotAnchorList.contains(anchor) ||
                                        (commonInfoFlag && commonInfoAnchorList.contains(anchor))) {*/
                                if(commonInfoFlag || anchorList.contains(anchor)){
                                    stringBuilder.append("【【");
                                    stringBuilder.append(anchor);
                                    stringBuilder.append("】】");
                                    stringBuilder.append("：");
                                    if(text.startsWith(anchor) && (text.length() == anchor.length() || text.charAt(anchor.length()) == '：'
                                            || text.charAt(anchor.length()) == ':')){

                                    }else{
                                        stringBuilder.append(text);
                                    }
                                }else{
                                    stringBuilder.append(text);
                                }

                            }
                            stringBuilder.append("\n");
                        }else if(line.startsWith("<section")){
                            Matcher matcher = PatternUtil.SECTION_PATTERN.matcher(line);
                            if(matcher.find()){
                                String name = matcher.group(2);
                                if("一般信息".equals(name) ||
                                        "基本信息".equals(name) || commonInfoAnchorList.contains(name)){
                                    commonInfoFlag = true;
                                }else{
                                    commonInfoFlag = false;
                                }
                            }
                            sectionTop++;
                        }else if(line.endsWith("</section>")){
                            sectionTop--;
                        }
                    }else{
                        if(line.startsWith("<text")) {
                            String text = line.replaceAll("</text>", "").replaceAll("<text>", "").trim();
                            if(StringUtils.isBlank(text)){
                                continue;
                            }
                            Matcher matcher = PatternUtil.CHINESE_PATTERN.matcher(text);
                            String anchor = "";
                            if(matcher.find()){
                                anchor = matcher.group();
                            }
                            if(text.endsWith("：") && commonInfoFlag){
                                stringBuilder.append("【【");
                                stringBuilder.append(anchor);
                                stringBuilder.append("】】");
                                stringBuilder.append("：");
                            }else{
                                stringBuilder.append(text);
                            }
                            stringBuilder.append("\n");
                        }else if(line.startsWith("<fieldelem")){
                            Matcher matcher = PatternUtil.FIELDELEM_PATTERN.matcher(line);
                            if(matcher.find()) {
                                String text = matcher.group(4);
                                text = StringUtil.removeAllBlank(text);
                                stringBuilder.append(text);
                                stringBuilder.append("\n");
                            }
                        }else if(line.startsWith("<section")){
                            Matcher matcher = PatternUtil.SECTION_PATTERN.matcher(line);
                            if(matcher.find()){
                                String name = matcher.group(2);
                                if("一般信息".equals(name) || "基本信息".equals(name) ||
                                         commonInfoAnchorList.contains(name)){
                                    commonInfoFlag = true;
                                }else{
                                    commonInfoFlag = false;
                                }
                            }
                            sectionTop++;
                        }else if(line.endsWith("</section>")){
                            sectionTop--;
                        }
                    }
                }
                bufferedReader.close();
                File txtFile = new File(txtPath + "/" + file.getName().substring(0, file.getName().lastIndexOf(".")) + ".txt");
                if(!txtFile.exists()){
                    txtFile.createNewFile();
                }
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFile), "GBK"));
                bufferedWriter.write(stringBuilder.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        }
        System.out.println(sum);
    }


    /**
     * 读取Excel数据内容
     *
     * @param
     * @return Map 包含单元格数据内容的Map对象
     */
    private static ArrayList<String> readExcelContent(String excelFile,int flag) throws IOException {
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
                String str = getCellFormatValue(row.getCell(3)).trim();
                if(flag == ANCHOR && "Y".equals(str)){
                    content.add(getCellFormatValue(row.getCell(0)));
                }else if(flag == NOT_ANCHOR && "N".equals(str)){
                    content.add(getCellFormatValue(row.getCell(0)));
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
