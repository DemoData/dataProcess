package com.example.demo.dao.ch;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Assay;

import java.util.List;

/**
 * @author aron
 */
public interface IAssayDao extends TableDao<Assay> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    JSONObject findRecordByIdInHRS(String applyId);

    String findPatientIdByGroupRecordName(String dataSource, String applyId);


}
