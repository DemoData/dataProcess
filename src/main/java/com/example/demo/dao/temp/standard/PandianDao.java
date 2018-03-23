package com.example.demo.dao.temp.standard;

import com.alibaba.fastjson.JSONObject;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public interface PandianDao {

    List<JSONObject> findIdByBatchNo(String batchNo);

    List<JSONObject> findCountByQuery(Query query, String collectionName);
}
