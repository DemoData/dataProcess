package com.example.demo.dao.ch;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author aron
 */
public interface TextDao<T> {
    void batchInsert2HRS(List<JSONObject> records, String collectionName);

    Integer getCount(String dataSource);

    List<T> findRecord(String dataSource, int pageNum, int pageSize);

}