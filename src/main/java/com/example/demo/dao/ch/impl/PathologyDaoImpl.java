package com.example.demo.dao.ch.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.ch.BaseDao;
import com.example.demo.dao.ch.IPathologyDao;
import com.example.demo.entity.ch.Pathology;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
public class PathologyDaoImpl extends BaseDao implements IPathologyDao {

    @Override
    protected String generateQuerySql() {
        String sql = "select * from `病理`";//TODO: 待优化成beanPropertyRowMapper的形式
        return sql;
    }

    @Override
    protected <T> RowMapper<T> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new PathologyRowMapper());
        }
        return getRowMapper();
    }

    @Override
    public List<Pathology> findPathologyRecord(String dataSource, int pageNum, int pageSize) {
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

    class PathologyRowMapper implements RowMapper<Pathology> {

        @Override
        public Pathology mapRow(ResultSet rs, int rowNum) throws SQLException {
            Pathology pathology = new Pathology();
            pathology.setId(rs.getInt("id"));
            pathology.setGroupRecordName(rs.getString(Pathology.ColumnMapping.GROUP_RECORD_NAME.columnName()));
            pathology.setCheckTime(rs.getString(Pathology.ColumnMapping.CHECK_TIME.columnName()));
            pathology.setProjectName(rs.getString(Pathology.ColumnMapping.PROJECT_NAME.columnName()));
            pathology.setPatientId(rs.getString(Pathology.ColumnMapping.PATIENT_ID.columnName()));
            pathology.setIsPositive(rs.getString(Pathology.ColumnMapping.IS_POSITIVE.columnName()));
            pathology.setResultDesc(rs.getString(Pathology.ColumnMapping.RESULT_DESC.columnName()));
            pathology.setImpression(rs.getString(Pathology.ColumnMapping.IMPRESSION.columnName()));
            pathology.setPathologyDate(rs.getString(Pathology.ColumnMapping.PATHOLOGY_DATE.columnName()));
            pathology.setRemark(rs.getString(Pathology.ColumnMapping.REMARK.columnName()));
            pathology.setResultTypeName(rs.getString(Pathology.ColumnMapping.RESULT_TYPE_NAME.columnName()));
            pathology.setAdvice(rs.getString(Pathology.ColumnMapping.ADVICE.columnName()));
            return pathology;
        }
    }

}
