package com.example.demo.dao.ch;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.entity.ch.MedicalHistory;

import java.util.List;

/**
 * @author aron
 */
public interface IMedicalHistoryDao {

    List<MedicalHistory> findMedicalHistoryRecord(String dataSource, int pageNum, int pageSize);

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    void batchInsert2HRS(List<JSONObject> records, String collectionName);

    void updateStorage(String dataSource);
}
