package com.example.demo.dao.ch;

import com.example.demo.entity.ch.Inspection;

import java.util.List;

public interface IInspectionDao extends TextDao<Inspection> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);
}
