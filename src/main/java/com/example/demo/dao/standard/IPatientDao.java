package com.example.demo.dao.standard;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.entity.Patient;

import java.util.List;

/**
 * @author aron
 */
public interface IPatientDao {

    JSONObject findPatientByIdInHRS(String pid);

    List<Patient> findPatients(String dataSource, int pageNum, int pageSize);

    void batchInsert2HRS(List<JSONObject> records);

    void save2HRS(JSONObject patient);

    Integer getCount(String dataSource);

}
