package com.example.demo.dao.ch.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.ch.BaseDao;
import com.example.demo.dao.ch.IMedicalHistoryDao;
import com.example.demo.dao.ch.IPathologyDao;
import com.example.demo.entity.ch.MedicalHistory;
import com.example.demo.entity.ch.Pathology;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
public class MedicalHistoryDaoImpl extends BaseDao implements IMedicalHistoryDao {

    @Override
    protected String generateQuerySql() {
        String sql = "select id,`一次就诊号`,`病人ID号`,mapping,`更新内容`,`病历名称` from `病历文书` where status = 0  AND isStorage = 0 AND (mapping like '%入院记录%' OR mapping like '%出院记录%') ";
        return sql;
    }

    @Override
    protected <T> RowMapper<T> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new MedicalHistoryRowMapper());
        }
        return getRowMapper();
    }

    @Override
    public List<MedicalHistory> findMedicalHistoryRecord(String dataSource, int pageNum, int pageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), pageNum, pageSize);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        return super.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, collectionName);
        }
    }

    @Override
    public void updateStorage(String dataSource) {
        String sql = "update `病历文书` set isStorage=1 where status = 0 AND (mapping like '%入院记录%' OR mapping like '%出院记录%')";
        int rows = getJdbcTemplate(dataSource).update(sql);
        log.info("updateStorage(): There are " + rows + " updated");
    }

    class MedicalHistoryRowMapper implements RowMapper<MedicalHistory> {

        @Override
        public MedicalHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
            MedicalHistory medicalHistory = new MedicalHistory();
            medicalHistory.setId(rs.getInt("id"));
            medicalHistory.setGroupRecordName(rs.getString(MedicalHistory.ColumnMapping.GROUP_RECORD_NAME.value()));
            medicalHistory.setPatientId(rs.getString(MedicalHistory.ColumnMapping.PATIENT_ID.value()));
            //用于获取所属类型
            medicalHistory.setMapping(rs.getString("mapping"));
            medicalHistory.setMedicalHistoryName(rs.getString(MedicalHistory.ColumnMapping.MEDICAL_HISTORY_NAME.value()));
            medicalHistory.setMedicalContent(rs.getString(MedicalHistory.ColumnMapping.MEDICAL_CONTENT.value()));
            return medicalHistory;
        }
    }

}
