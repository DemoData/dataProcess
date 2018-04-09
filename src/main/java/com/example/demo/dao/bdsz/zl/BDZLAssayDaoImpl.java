package com.example.demo.dao.bdsz.zl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.constant.CommonConstant;
import com.example.demo.dao.BaseDao;
import com.example.demo.dao.standard.IAssayDao;
import com.example.demo.entity.Assay;
import com.example.demo.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository("bdzlAssayDao")
public class BDZLAssayDaoImpl extends BaseDao implements IAssayDao {

    @Override
    protected String generateQuerySql() {
        return null;
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
//        return super.queryForListInSqlServer(getJdbcTemplate(dataSource), pageNum, pageSize, "TM_LAB_ROUTINE_RESULT", "GROUPRECORDNAME", "group by GROUPRECORDNAME");
        //TODO: sqlserver 分组无法通过id分页问题
        return getJdbcTemplate(dataSource).query("select PID,GROUPRECORDNAME from TM_LAB_ROUTINE_RESULT group by GROUPRECORDNAME,PID", this.generateRowMapper());
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        String sql = "select t.Diagnosis from Diagnosis t where t.groupRecordName= ? group by t.Diagnosis";
        return super.findOrgOdCatByGroupRecordName(sql, dataSource, groupRecordName);
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        //北大深圳的化验不分页
        return 1000;
//        return getJdbcTemplate(dataSource).queryForObject("select count(t.GROUPRECORDNAME) from (select GROUPRECORDNAME from TM_LAB_ROUTINE_RESULT group by GROUPRECORDNAME) t", Integer.class);
    }

    @Override
    public List<Assay> findArrayListByCondition(String dataSource, String condition) {
        log.debug("findArrayListByCondition(): condition: " + condition);
        String sql = "select PID AS 'patientId', ITEM_CH_NAME AS 'assayName', ITEM_TIME AS 'assayTime',ITEM_RESULT_DES_CODE AS 'resultFlag',ITEM_RESULT_DES_NAME AS 'assayResult',ITEM_RESULT_NUM AS 'assayValue',ITEM_RESULT_UNIT AS 'assayUnit',RESULT_REFERENCE AS 'referenceRange' from TM_LAB_ROUTINE_RESULT where GROUPRECORDNAME =?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper(Assay.class), condition);
    }

    @Override
    public JSONObject findRecordByIdInHRS(String applyId) {
        return null;
    }

    @Override
    public String findPatientIdByGroupRecordName(String dataSource, String applyId) {
        log.debug("findPatientIdByGroupRecordName(): 查找PatientId通过一次就诊号: " + applyId);
        String sql = "select PID from Record where groupRecordName=? group by groupRecordName,PID";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        Map<String, Object> result = null;
        try {
            result = jdbcTemplate.queryForMap(sql, applyId);
        } catch (EmptyResultDataAccessException e) {
            log.info("findPatientIdByGroupRecordName(): can not found PatientId by GroupRecordName");
            return null;
        }
        return result.get("PID").toString();
    }

    class AssayRowMapper implements RowMapper<Record> {

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            Record record = new Record();
            Object pid = rs.getObject("PID");

            if (pid != null && !(pid instanceof String) && pid.toString().indexOf(".") > 0 && pid.toString().indexOf("E") > 0) {
                pid = new BigDecimal(pid.toString()).toPlainString();
            } else {
                pid = pid != null ? pid.toString().substring(0, pid.toString().indexOf(".")) : CommonConstant.EMPTY_FLAG;
            }

            Object groupRecordName = rs.getObject("GROUPRECORDNAME");
            if (groupRecordName != null && !(groupRecordName instanceof String) && groupRecordName.toString().indexOf(".") > 0 && groupRecordName.toString().indexOf("E") > 0) {
                groupRecordName = new BigDecimal(groupRecordName.toString()).toPlainString();
            } else {
                groupRecordName = groupRecordName != null ? groupRecordName.toString().substring(0, groupRecordName.toString().indexOf(".")) : CommonConstant.EMPTY_FLAG;
            }
            record.setPatientId(pid.toString());
            record.setSourceId(groupRecordName.toString());
            record.setGroupRecordName(groupRecordName.toString());
            return record;
        }
    }

}
