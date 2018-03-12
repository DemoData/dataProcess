package com.example.demo.dao.ch;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.entity.ch.Pathology;

import java.util.List;

/**
 * @author aron
 */
public interface IPathologyDao {

    List<Pathology> findPathologyRecord(String dataSource, int pageNum, int pageSize);

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    void batchInsert2HRS(List<JSONObject> records, String collectionName);
}
