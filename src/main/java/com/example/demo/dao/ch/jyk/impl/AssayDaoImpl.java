package com.example.demo.dao.ch.jyk.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.constant.CommonConstant;
import com.example.demo.dao.BaseDao;
import com.example.demo.dao.standard.IAssayDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.Assay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository("jyAssayDao")
public class AssayDaoImpl extends BaseDao implements IAssayDao {

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(t.`检验申请号`) from (select `检验申请号` from `检验报告明细` GROUP BY `检验申请号`) t", Integer.class);
    }

    @Override
    public List<Record> findRecord(String dataSource, int PageNum, int PageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), PageNum, PageSize);
    }

    @Override
    public List<Assay> findArrayListByCondition(String dataSource, String applyId) {
        log.debug("findAssaysByApplyId(): 查找化验报告通过检验申请号: " + applyId);
        String sql = "select t.`检验时间` AS 'assayTime',t.`项目名称` AS 'assayName',t.`结果正常标志` AS 'resultFlag',t.`检验结果` AS 'assayResult',t.`检验值` AS 'assayValue',t.`单位` AS 'assayUnit',t.`标本` AS 'assaySpecimen',t.`参考范围` AS 'referenceRange',t.`检验状态` AS 'assayState',t.`检验方法名称` AS 'assayMethodName' from `检验报告明细` t where t.`检验申请号`=?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        List<Assay> assays = jdbcTemplate.query(sql, new BeanPropertyRowMapper(Assay.class), applyId);
        return assays;
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, collectionName);
        }
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        String sql = "select t.`诊断名称` from `诊断信息` t where t.`一次就诊号`= ? group by t.`诊断名称`";
        return super.findOrgOdCatByGroupRecordName(sql,dataSource, groupRecordName);
    }

    @Override
    public JSONObject findRecordByIdInHRS(String applyId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(applyId));
        JSONObject record = hrsMongoTemplate.findOne(query, JSONObject.class, "Record");
        return record;
    }

    @Override
    public String findPatientIdByGroupRecordName(String dataSource, String groupRecordName) {
        log.debug("findPatientIdByGroupRecordName(): 查找PatientId通过一次就诊号: " + groupRecordName);
        String sql = "select t.`病人ID号` from `患者基本信息` t where t.`一次就诊号`= ? group by t.`一次就诊号`";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        StringBuffer patientId = new StringBuffer("shch_");
        String id;
        try {
            id = jdbcTemplate.queryForObject(sql, String.class, groupRecordName);
            patientId.append(id);
        } catch (EmptyResultDataAccessException e) {
            log.error("Can not found patientId via groupRecordName:" + groupRecordName);
            e.printStackTrace();
            return CommonConstant.EMPTY_FLAG;
        }
        return patientId.toString();
    }

    @Override
    protected String generateQuerySql() {
        String sql = "select t.`一次就诊号` AS 'groupRecordName',t.`检验申请号` AS 'applyId' from `检验报告明细` t GROUP BY t.`检验申请号` ";
        return sql;
    }

    @Override
    protected RowMapper<Record> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new AssayRowMapper());
        }
        return getRowMapper();
    }

    class AssayRowMapper implements RowMapper<Record> {

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            Record record = new Record();
            record.setGroupRecordName(rs.getString("groupRecordName"));
            record.setId(rs.getString("applyId"));
            record.setSourceId(rs.getString("applyId"));
            return record;
        }
    }
}
