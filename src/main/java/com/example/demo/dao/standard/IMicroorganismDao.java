package com.example.demo.dao.standard;

import com.example.demo.dao.TableDao;
import com.example.demo.entity.Microorganism;

import java.util.List;

public interface IMicroorganismDao extends TableDao<Microorganism> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    String findPatientIdByGroupRecordName(String dataSource, String applyId);
}
