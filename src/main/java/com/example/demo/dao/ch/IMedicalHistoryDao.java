package com.example.demo.dao.ch;

import com.example.demo.entity.ch.MedicalHistory;

import java.util.List;

/**
 * @author aron
 */
public interface IMedicalHistoryDao extends TextDao<MedicalHistory> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    void updateStorage(String dataSource);

}
