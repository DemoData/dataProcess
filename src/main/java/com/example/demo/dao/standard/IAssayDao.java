package com.example.demo.dao.standard;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.TableDao;
import com.example.demo.entity.Assay;

import java.util.List;

/**
 * @author aron
 */
public interface IAssayDao extends TableDao<Assay> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    JSONObject findRecordByIdInHRS(String applyId);

    String findPatientIdByGroupRecordName(String dataSource, String applyId);


}
