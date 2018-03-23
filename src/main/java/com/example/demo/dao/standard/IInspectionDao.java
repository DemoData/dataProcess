package com.example.demo.dao.standard;

import com.example.demo.dao.TextDao;
import com.example.demo.entity.Inspection;

import java.util.List;

public interface IInspectionDao extends TextDao<Inspection> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);
}
