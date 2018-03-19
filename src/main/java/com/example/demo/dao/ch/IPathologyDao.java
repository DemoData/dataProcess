package com.example.demo.dao.ch;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.entity.ch.Pathology;

import java.util.List;

/**
 * @author aron
 */
public interface IPathologyDao extends TextDao<Pathology> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

}
