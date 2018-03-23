package com.example.demo.dao;

import com.alibaba.fastjson.JSONObject;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public interface PandianDao {

    List<JSONObject> findIdByBatchNo(String batchNo);

    List<JSONObject> findListByQuery(Query query, String collectionName);

    Integer findCountByQuery(Query query, String collectionName);
}
