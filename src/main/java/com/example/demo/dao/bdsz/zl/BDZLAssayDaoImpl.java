package com.example.demo.dao.bdsz.zl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.BaseDao;
import com.example.demo.dao.standard.IAssayDao;
import com.example.demo.entity.Assay;
import com.example.demo.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
        //由于sqlserver 分组分页问题，暂时不分页
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
        return getJdbcTemplate(dataSource).queryForObject("select count(t.GROUPRECORDNAME) from (select GROUPRECORDNAME from TM_LAB_ROUTINE_RESULT group by GROUPRECORDNAME) t", Integer.class);
    }

    @Override
    public List<Assay> findArrayListByCondition(String dataSource, String condition) {
        log.debug("findArrayListByCondition(): condition: " + condition);
        String sql = "select ITEM_CH_NAME AS 'assayName', ITEM_TIME AS 'assayTime',ITEM_RESULT_DES_CODE AS 'resultFlag',ITEM_RESULT_DES_NAME AS 'assayResult',ITEM_RESULT_NUM AS 'assayValue',ITEM_RESULT_UNIT AS 'assayUnit',RESULT_REFERENCE AS 'referenceRange' from TM_LAB_ROUTINE_RESULT where GROUPRECORDNAME =?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper(Assay.class), condition);
    }

    @Override
    public JSONObject findRecordByIdInHRS(String applyId) {
        return null;
    }

    @Override
    public String findPatientIdByGroupRecordName(String dataSource, String applyId) {
        return null;
    }

    class AssayRowMapper implements RowMapper<Record> {

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            Record record = new Record();
            record.setPatientId(rs.getString("PID"));
            record.setSourceId(rs.getString("GROUPRECORDNAME"));
            record.setGroupRecordName(rs.getString("GROUPRECORDNAME"));
            return record;
        }
    }

}
