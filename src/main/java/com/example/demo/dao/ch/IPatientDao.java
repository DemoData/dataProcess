package com.example.demo.dao.ch;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.entity.ch.Patient;

import java.util.List;

/**
 * @author aron
 */
public interface IPatientDao {
    List<Patient> findAllPatients(String dataSource, int pageNum, int pageSize);

    JSONObject findPatientByIdInHRS(String pid);

    void batchInsert2HRS(List<JSONObject> records, String collectionName);

}
