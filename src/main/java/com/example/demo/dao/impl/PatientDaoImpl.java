package com.example.demo.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.config.MongoDataSourceConfig;
import com.example.demo.dao.IPatientDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author aron
 * @date 2018.02.27
 */
@Slf4j
@Repository
public class PatientDaoImpl implements IPatientDao {

    @Autowired
    @Qualifier(MongoDataSourceConfig.HRS_MONGO_TEMPLATE)
    private MongoTemplate hrsMongoTemplate;

    @Autowired
    @Qualifier(MongoDataSourceConfig.SDS_MONGO_TEMPLATE)
    private MongoTemplate sdsMongoTemplate;

    @Autowired
    @Qualifier(MongoDataSourceConfig.HDP_MONGO_TEMPLATE)
    private MongoTemplate hdpMongoTemplate;

    @Autowired
    @Qualifier(MongoDataSourceConfig.HDPB_MONGO_TEMPLATE)
    private MongoTemplate hdpbMongoTemplate;

    @Override
    public List<JSONObject> findByQueryInHRS(Query patientQuery, String collectionName) {
        return hrsMongoTemplate.find(patientQuery, JSONObject.class, collectionName);
    }

    @Override
    public List<JSONObject> findByQueryInSDS(Query patientQuery, String collectionName) {
        return sdsMongoTemplate.find(patientQuery, JSONObject.class, collectionName);
    }

    @Override
    public void batchInsert2HDP(List<JSONObject> records, String collectionName) {
        hdpMongoTemplate.insert(records, collectionName);
    }

    @Override
    public void batchInsert2HDPB(List<JSONObject> records, String collectionName) {
        hdpbMongoTemplate.insert(records, collectionName);
    }
}