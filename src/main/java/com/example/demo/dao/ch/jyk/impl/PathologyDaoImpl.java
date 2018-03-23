package com.example.demo.dao.ch.jyk.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.BaseDao;
import com.example.demo.dao.standard.IPathologyDao;
import com.example.demo.entity.Pathology;
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
        String sql = "select * from `病理`";
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
    public List<Pathology> findRecord(String dataSource, int pageNum, int pageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), pageNum, pageSize);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        String sql = "select t.`诊断名称` from `诊断信息` t where t.`一次就诊号`= ? group by t.`诊断名称`";
        return super.findOrgOdCatByGroupRecordName(sql,dataSource, groupRecordName);
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(id) from `病理`", Integer.class);
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
