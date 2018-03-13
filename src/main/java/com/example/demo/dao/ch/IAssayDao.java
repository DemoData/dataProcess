package com.example.demo.dao.ch;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Assay;

import java.util.List;

/**
 * @author aron
 */
public interface IAssayDao {
    List<Record> findAssayRecord(String dataSource, int PageNUm, int PageSize);

    List<Assay> findAssaysByApplyId(String dataSource, String applyId);

    void batchInsert2HRS(List<JSONObject> records, String collectionName);

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    JSONObject findRecordByIdInHRS(String applyId);

    String findPatientIdByGroupRecordName(String dataSource, String applyId);
}
