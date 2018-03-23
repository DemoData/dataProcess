package com.example.demo.dao.bdsz.zl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.BaseDao;
import com.example.demo.dao.standard.IMedicalHistoryDao;
import com.example.demo.entity.MedicalHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository("bdzlMedicalHistoryDao")
public class BDZLMedicalHistoryDaoImpl extends BaseDao implements IMedicalHistoryDao {

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(id) from Record ", Integer.class);
    }

    @Override
    protected String generateQuerySql() {
        return null;
    }

    @Override
    protected <T> RowMapper<T> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new MedicalHistoryRowMapper());
        }
        return getRowMapper();
    }

    @Override
    public List<MedicalHistory> findRecord(String dataSource, int pageNum, int pageSize) {
        return super.queryForListInSqlServer(getJdbcTemplate(dataSource), pageNum, pageSize, "Record", null, null);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        String sql = "select t.Diagnosis from Diagnosis t where t.groupRecordName= ? group by t.Diagnosis";
        return super.findOrgOdCatByGroupRecordName(sql, dataSource, groupRecordName);
    }

    @Override
    public int batchUpdateContent(String dataSource, List<Object[]> params) {
        synchronized (this) {
            int[] result = getJdbcTemplate(dataSource).batchUpdate("update Record set content=? where id=?", params);
            return result.length;
        }
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, collectionName);
        }
    }

    class MedicalHistoryRowMapper implements RowMapper<MedicalHistory> {

        @Override
        public MedicalHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
            MedicalHistory medicalHistory = new MedicalHistory();
            medicalHistory.setId(rs.getInt("id"));
            medicalHistory.setGroupRecordName(rs.getString("groupRecordName"));
            medicalHistory.setPatientId(rs.getString("PID"));
//            //用于获取所属类型
            medicalHistory.setMedicalHistoryName(rs.getString("RecordType"));
            Blob blobContent = rs.getBlob("BLNR");
//            InputStream is = blobContent.getBinaryStream();
//            ByteArrayInputStream bais = (ByteArrayInputStream)is;
            /*byte[] byte_data = new byte[bais.available()]; //bais.available()返回此输入流的字节数

            bais.read(byte_data, 0,byte_data.length);//将输入流中的内容读到指定的数组
            note = new String(byte_data,"utf-8"); //再转为String，并使用指定的编码方式
            is.close();*/
            byte[] returnBytes = blobContent.getBytes(1, (int) blobContent.length());
            StringBuffer content = new StringBuffer();
            try {
                String utfContent = null;
                String gbkContent = null;
                utfContent = new String(returnBytes, "UTF-8");

                if (utfContent.indexOf("<elements>") > 0) {
                    content.append(utfContent.substring(0, utfContent.indexOf("<elements>")));

                    gbkContent = new String(returnBytes, "GB2312");
                    content.append(gbkContent.substring(gbkContent.indexOf("<elements>"), gbkContent.length()));
                } else {
                    content.append(utfContent);
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            medicalHistory.setMedicalContent(content.toString());
            return medicalHistory;
        }
    }

}
