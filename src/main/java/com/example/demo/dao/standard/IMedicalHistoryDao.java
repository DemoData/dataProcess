package com.example.demo.dao.standard;

import com.example.demo.dao.TextDao;
import com.example.demo.entity.MedicalHistory;

import java.util.List;

/**
 * @author aron
 */
public interface IMedicalHistoryDao extends TextDao<MedicalHistory> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    int batchUpdateContent(String dataSource, List<Object[]> params);
}
