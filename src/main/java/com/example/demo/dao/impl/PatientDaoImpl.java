package com.example.demo.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.config.HDPBMongoConfig;
import com.example.demo.config.HDPMongoConfig;
import com.example.demo.config.HRSMongoConfig;
import com.example.demo.config.SDSMongoConfig;
import com.example.demo.dao.IPatientDao;
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
@Repository
public class PatientDaoImpl implements IPatientDao {

    @Autowired
    @Qualifier(HRSMongoConfig.MONGO_TEMPLATE)
    private MongoTemplate hrsMongoTemplate;

    @Autowired
    @Qualifier(SDSMongoConfig.MONGO_TEMPLATE)
    private MongoTemplate sdsMongoTemplate;

    @Autowired
    @Qualifier(HDPMongoConfig.MONGO_TEMPLATE)
    private MongoTemplate hdpMongoTemplate;

    @Autowired
    @Qualifier(HDPBMongoConfig.MONGO_TEMPLATE)
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