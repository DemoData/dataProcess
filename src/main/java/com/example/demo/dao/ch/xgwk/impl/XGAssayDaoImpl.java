package com.example.demo.dao.ch.xgwk.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.ch.BaseDao;
import com.example.demo.dao.ch.IAssayDao;
import com.example.demo.dao.ch.IMedicalOrderDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Assay;
import com.example.demo.entity.ch.MedicalOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository("xgAssayDao")
public class XGAssayDaoImpl extends BaseDao implements IAssayDao {

    @Override
    protected String generateQuerySql() {
        String sql = "select id,`病人ID号`,`住院号` from `检验结果` GROUP BY `病人ID号`";
        return sql;
    }

    @Override
    protected <T> RowMapper<T> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new AssayRowMapper());
        }
        return getRowMapper();
    }

    @Override
    public List<Record> findRecord(String dataSource, int pageNum, int pageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), pageNum, pageSize);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        return super.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
    }

    @Override
    public JSONObject findRecordByIdInHRS(String applyId) {
        return null;
    }

    @Override
    public String findPatientIdByGroupRecordName(String dataSource, String applyId) {
        return null;
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(t.`病人ID号`) from (select `病人ID号` from `检验结果` GROUP BY `病人ID号`) t", Integer.class);
    }

    @Override
    public List<Assay> findArrayListByCondition(String dataSource, String condition) {
        log.debug("findArrayListByCondition(): condition: " + condition);
        String sql = "select `标本` AS 'assaySpecimen' , `检验项目` AS 'assayName',`结果` AS 'assayResult',`单位` AS 'assayUnit',`异常值` AS 'resultFlag',`报告日期` AS 'assayTime' from `检验结果` where 病人ID号 =?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper(Assay.class), condition);
    }

    class AssayRowMapper implements RowMapper<Record> {

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            Record record = new Record();
            record.setPatientId(rs.getString("病人ID号"));
            record.setSourceId(rs.getString("id"));
            record.setGroupRecordName(rs.getString("住院号"));
            return record;
        }
    }

}
