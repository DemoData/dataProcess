package com.example.demo.test.main;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.util.PatternUtil;
import com.example.demo.util.AnchorUtil;
import com.mongodb.*;
import com.mongodb.client.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;

public class AnchorCalMain {

    static MongoCredential mongoCredential = MongoCredential.createCredential("yy", "HRS-live", "rf1)Rauwu3dpsGid".toCharArray());

    static ServerAddress serverAddress = new ServerAddress("localhost", 3718);

    static List<MongoCredential> mongoCredentials = new ArrayList<>();
    static {
        mongoCredentials.add(mongoCredential);
    }
    //static ServerAddress serverAddress = new ServerAddress("localhost", 27017);
    static MongoClient mongo = new MongoClient(serverAddress, mongoCredentials, new MongoClientOptions.Builder().build());
    //static MongoClient mongo = new MongoClient("localhost", 27017);
    static MongoDatabase db = mongo.getDatabase("HRS-live");
    static MongoCollection dc = db.getCollection("Record");
    static List<JSONObject> result = new ArrayList<>();

    public static void main(String[] args) {
        List<Bson> bsons = new ArrayList<>();
        bsons.add(new Document("$match", new Document("batchNo", "shch20180309")));
        List<Document> recordTypeList = new ArrayList<Document>();
        recordTypeList.add(new Document("recordType", "入院记录"));
        recordTypeList.add(new Document("recordType", "出院记录"));
        bsons.add(new Document("$match", new Document("$or", recordTypeList)));
        bsons.add(new Document("$match", new Document("deleted", false)));
        AggregateIterable<Document> iterable = dc.aggregate(bsons).allowDiskUse(true);
        MongoCursor<Document> itor = iterable.iterator();
        Map<String, JSONObject> anchorOriginalMap = new HashMap<>();
        Map<String ,Integer> anchorCountMap = new HashMap<>();
        int i = 0;
        while(itor.hasNext()){
            System.out.println(++i);
            Document document = itor.next();
            JSONObject jsonObject = JSONObject.parseObject(document.toJson());
            String text = jsonObject.getJSONObject("info").getString("text");
            text = text.replaceAll("\n", "").replaceAll("\r", "");
            text = text.replaceAll("【【", "\n【【");
            Matcher matcher = PatternUtil.ANCHOR_PATTERN.matcher(text);
            while(matcher.find()){
                String anchor = matcher.group(1);
                if(!anchorCountMap.containsKey(anchor)){
                    anchorCountMap.put(anchor, 1);
                    JSONObject resultItem = new JSONObject();
                    resultItem.put("原文", text);
                    resultItem.put("锚点数量", AnchorUtil.countAnchorCount(text));
                    resultItem.put("RID", jsonObject.getString("_id"));
                    anchorOriginalMap.put(anchor, resultItem);
                }else{
                    anchorCountMap.put(anchor, anchorCountMap.get(anchor) + 1);
                }
            }
        }
        writer("/Users/liulun/Desktop/上海长海医院", "锚点原文对应表(mongo)", "xlsx", anchorOriginalMap, anchorCountMap, new String[]{"锚点", "原文","锚点数量","RID"});
    }

    public static void writer(String path, String fileName,String fileType,Map<String, JSONObject> anchorOriginalMap,
                       Map<String, Integer> anchorCountMap,String titleRow[]){
        try{

            Workbook wb = null;
            String excelPath = path+ File.separator+fileName+"."+fileType;
            File file = new File(excelPath);
            Sheet sheet =null;
            //创建工作文档对象
            if (!file.exists()) {
                if (fileType.equals("xls")) {
                    wb = new HSSFWorkbook();

                } else if(fileType.equals("xlsx")) {

                    wb = new XSSFWorkbook();
                } else {
                    throw new RuntimeException("文件格式不正确");
                }
                //创建sheet对象
                sheet = (Sheet) wb.createSheet("锚点清单表");
                OutputStream outputStream = new FileOutputStream(excelPath);
                wb.write(outputStream);
                outputStream.flush();
                outputStream.close();

            } else {
                if (fileType.equals("xls")) {
                    wb = new HSSFWorkbook();

                } else if(fileType.equals("xlsx")) {
                    wb = new XSSFWorkbook();

                } else {
                    throw new RuntimeException("文件格式不正确");
                }
            }
            //创建sheet对象
            if (sheet==null) {
                sheet = (Sheet) wb.createSheet("锚点清单表");
            }

            //添加表头
            Row row = sheet.createRow(0);
            Cell cell;
            for(int i = 0;i < titleRow.length;i++){
                cell = row.createCell(i);
                cell.setCellValue(titleRow[i]);
            }
            Iterator<String> keys = anchorOriginalMap.keySet().iterator();
            int rowIndex = 0;
            while(keys.hasNext()){
                String key = keys.next();
                JSONObject jsonObject = anchorOriginalMap.get(key);
                row = sheet.createRow(++rowIndex);
                cell = row.createCell(0);
                cell.setCellValue(key);
                for(int i = 1;i < titleRow.length;i++){
                    cell = row.createCell(i);
                    cell.setCellValue(jsonObject.getString(titleRow[i]));
                }
            }

            //创建文件流
            OutputStream stream = new FileOutputStream(excelPath);
            //写入数据
            wb.write(stream);
            //关闭文件流
            stream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
