package com.example.demo.dao.ch;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.entity.ch.Inspection;

import java.util.List;

public interface IInspectionDao {

    List<Inspection> findInspectionRecord(String dataSource, int pageNum, int pageSize);

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    void batchInsert2HRS(List<JSONObject> records, String collectionName);
}
