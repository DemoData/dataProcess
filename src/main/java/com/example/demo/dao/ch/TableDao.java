package com.example.demo.dao.ch;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Assay;

import java.util.List;

/**
 * @author aron
 */
public interface TableDao<T> {
    void batchInsert2HRS(List<JSONObject> records, String collectionName);

    Integer getCount(String dataSource);

    List<Record> findRecord(String dataSource, int pageNum, int pageSize);

    List<T> findArrayListByCondition(String dataSource, String condition);
}
