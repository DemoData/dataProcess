package com.example.demo.dao.ch;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Microorganism;

import java.util.List;

public interface IMicroorganismDao {
    List<Record> findMicroorganismRecord(String dataSource, int PageNUm, int PageSize);

    List<Microorganism> findMicroorganismByApplyId(String dataSource, String applyId);

    void batchInsert2HRS(List<JSONObject> records, String collectionName);

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    String findPatientIdByGroupRecordName(String dataSource, String applyId);
}
