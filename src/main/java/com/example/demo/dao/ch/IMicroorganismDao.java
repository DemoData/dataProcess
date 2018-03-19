package com.example.demo.dao.ch;

import com.example.demo.entity.ch.Microorganism;

import java.util.List;

public interface IMicroorganismDao extends TableDao<Microorganism> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    String findPatientIdByGroupRecordName(String dataSource, String applyId);
}
