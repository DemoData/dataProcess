package com.example.demo.dao.temp.standard;

import com.alibaba.fastjson.JSONObject;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * @author aron
 * @date 2018.02.27
 */
public interface IPatientDao {

    List<JSONObject> findByQueryInHRS(Query patientQuery, String collectionName);

    List<JSONObject> findByQueryInSDS(Query patientQuery, String collectionName);

    void batchInsert2HDP(List<JSONObject> records, String collectionName);

    void batchInsert2HDPB(List<JSONObject> records, String collectionName);

}
