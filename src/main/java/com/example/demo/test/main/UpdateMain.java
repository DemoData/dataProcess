package com.example.demo.test.main;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.support.TextFormatter;
import com.example.demo.util.FileUtil;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
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
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateMain {
    static MongoCredential mongoCredential = MongoCredential.createCredential("yy", "HRS-live", "rf1)Rauwu3dpsGid".toCharArray());

    static ServerAddress serverAddress = new ServerAddress("localhost", 3718);
    //static ServerAddress serverAddress = new ServerAddress("dds-bp1baff8ad4002a42.mongodb.rds.aliyuncs.com", 3717);

    static List<MongoCredential> mongoCredentials = new ArrayList<>();
    static {
        mongoCredentials.add(mongoCredential);
    }
    //static ServerAddress serverAddress = new ServerAddress("localhost", 27017);
    static MongoClient mongo = new MongoClient(serverAddress, mongoCredentials, new MongoClientOptions.Builder().build());
    //static MongoClient mongo = new MongoClient("localhost", 27017);
    static MongoDatabase db = mongo.getDatabase("HRS-live");
    static MongoCollection dc = db.getCollection("Record");
    static ArrayList<String> anchors;
    static Map<String, String> anchorOriginalMap = new HashMap<>();
    static Map<String ,Integer> anchorCountMap = new HashMap<>();
    public final static String ANCHOR_EXCEL_PATH = "/Users/liulun/Desktop/上海长海医院/技术用-症状&体征-锚点使用.xlsx";
    //public final static String ANCHOR_EXCEL_PATH = "./技术用-症状&体征-锚点使用.xlsx";
    public static void main(String[] args) {
        try {

            //制定需要获取的列
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i <= 23; i++) {
                if (i != 0 && i != 2 && i != 3) {
                    list.add(i);
                }
            }
            //锚点数据
            anchors = readExcelContent(ANCHOR_EXCEL_PATH, 0, list);
            imRecord();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void imRecord() throws Exception {
        List<File> fileList = FileUtil.listTxtAllFile("/Users/liulun/Desktop/上海长海医院/血管外科/txt");
        BasicDBObject docQuery = new BasicDBObject();
        docQuery.append("batchNo", "shch20180315");
        docQuery.append("recordType", new BasicDBObject("$in", new String[]{"入院记录","出院记录"}));
        System.out.println(dc.count(docQuery));
        FindIterable<Document> iterable = dc.find(docQuery);
        MongoCursor<Document> itor = iterable.iterator();
        int i = 0;
        Map<String, String> resultMap = new HashMap<>();
        while(itor.hasNext()){
            Document document = itor.next();
            JSONObject jsonObject = JSONObject.parseObject(document.toJson());
            String textARS = jsonObject.getJSONObject("info").getString("textARS");
            String patientId = jsonObject.getString("patientId");
            patientId = patientId.substring(5);
            String startLine = textARS.substring(0, textARS.indexOf("【【"));
            //String text = jsonObject.getJSONObject("info").getString("text");
            //String result = TextFormatter.addAnchor(textARS, anchors);
            for(File file : fileList){
                String fileName = file.getName();
                if(fileName.startsWith(patientId)){
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
                    String line = FileUtil.readFile(file);
                    if(line.equals(textARS)){
                        resultMap.put(fileName, jsonObject.getString("_id"));
                    }
                }
            }
            //jsonObject.getJSONObject("info").put("text", result);

//            if(!jsonObject.getJSONObject("info").containsKey("text_back")){
//                jsonObject.getJSONObject("info").put("text_back", text);
//            }
            /*document = Document.parse(jsonObject.toJSONString());
            Object _id =  jsonObject.get("_id");
            if(_id instanceof JSONObject){
                dc.updateOne(new Document("_id", new ObjectId(((JSONObject)_id).getString("$oid"))),  new Document("$set", document));
            }else{
                dc.updateOne(new Document("_id", jsonObject.get("_id")),  new Document("$set", document));
            }*/
            System.out.println(++i);
        }
        System.out.println(resultMap.size());
    }


    /**
     * 读取Excel数据内容
     *
     * @param
     * @return Map 包含单元格数据内容的Map对象
     */
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
