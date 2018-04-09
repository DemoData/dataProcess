package com.example.demo.test.main;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import sun.jvm.hotspot.debugger.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CHXGUpdateAnchorMain {



    /*static MongoCredential mongoCredential = MongoCredential.createCredential("xh", "HRS-test", "rt0hizu{j9lzJNqi".toCharArray());
    //static ServerAddress serverAddress = new ServerAddress("localhost", 3718);
    static ServerAddress serverAddress = new ServerAddress("localhost", 3718);

    static List<MongoCredential> mongoCredentials = new ArrayList<>();
    static {
        mongoCredentials.add(mongoCredential);
    }
    //static ServerAddress serverAddress = new ServerAddress("localhost", 27017);
    static MongoClient mongo = new MongoClient(serverAddress, mongoCredentials, new MongoClientOptions.Builder().build());
    //static MongoClient mongo = new MongoClient("localhost", 27017);
    static MongoDatabase db = mongo.getDatabase("HRS-test");*/
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

    static List<String> archorsList = new ArrayList<>();
    static {
        archorsList.add("");
    }

    public static void main(String[] args) {
        BasicDBObject docQuery = new BasicDBObject();
        docQuery.append("batchNo", "shch20180316");
        Pattern pattern = Pattern.compile("^.*年龄：.*$", Pattern.CASE_INSENSITIVE);
        docQuery.append("info.text", new BasicDBObject("$regex", pattern));
        System.out.println(dc.count(docQuery));
        FindIterable<Document> iterable = dc.find(docQuery);
        MongoCursor<Document> itor = iterable.iterator();
        int m = 0;
        while(itor.hasNext()){
            Document document = itor.next();
            JSONObject jsonObject = JSONObject.parseObject(document.toJson());
            String text = jsonObject.getJSONObject("info").getString("text");
            text = text.replaceAll("年龄：", "");
            text = text.replaceAll("姓名：", "");
            //String text = jsonObject.getJSONObject("info").getString("text");
            /*String result = TextFormatter.addAnchor(textARS, anchors);
            int lastIndex = 0;
            while(result.indexOf("【【病理结果】】", lastIndex) != -1){
                int index = result.indexOf("【【病理结果】】", lastIndex);
                int nextIndex = result.indexOf("【【", index + 1);
                if(nextIndex == -1){
                    nextIndex = result.length();
                }
                if(nextIndex - index > 58){
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(result.substring(0, index - 1));
                    stringBuilder.append("病理结果");
                    stringBuilder.append(result.substring(index + 8));
                    result = stringBuilder.toString();
                    System.out.println("去掉病理结果:" + jsonObject.get("_id"));
                }
                lastIndex = index + 1;
            }*/
            jsonObject.getJSONObject("info").put("text", text);
            //jsonObject.getJSONObject("info").put("textARS",text);
            document = Document.parse(jsonObject.toJSONString());
            Object _id =  jsonObject.get("_id");
            if(_id instanceof JSONObject){
                dc.updateOne(new Document("_id", new ObjectId(((JSONObject)_id).getString("$oid"))),  new Document("$set", document));
            }else{
                dc.updateOne(new Document("_id", jsonObject.get("_id")),  new Document("$set", document));
            }
            System.out.println(++m);
        }
    }
}
