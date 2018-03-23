package com.example.demo.dao.temp.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.config.MongoDataSourceConfig;
import com.example.demo.dao.temp.standard.PandianDao;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PandianDaoImpl implements PandianDao {

    @Autowired
    @Qualifier(MongoDataSourceConfig.HRS_MONGO_TEMPLATE)
    private MongoTemplate hrsMongoTemplate;

    @Override
    public List<JSONObject> findIdByBatchNo(String batchNo){
        DBObject dbObject = new BasicDBObject();
        dbObject.put("batchNo", batchNo);
        DBObject fieldObject = new BasicDBObject();
        fieldObject.put("_id", true);
        Query query = new BasicQuery(dbObject, fieldObject);
        return hrsMongoTemplate.find(query, JSONObject.class, "Patient");
    }

    @Override
    public List<JSONObject> findCountByQuery(Query query, String collectionName) {
        return hrsMongoTemplate.find(query, JSONObject.class, collectionName);
    }
}
