package com.example.demo.service.bdsz.fs.mz;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.constant.CommonConstant;
import com.example.demo.common.util.TimeUtil;
import com.example.demo.dao.standard.IAssayDao;
import com.example.demo.dao.standard.IPatientDao;
import com.example.demo.entity.Patient;
import com.example.demo.entity.Record;
import com.example.demo.service.bdsz.fs.BDFSAssayServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("bdmzAssayService")
public class BDMZAssayServiceImpl extends BDFSAssayServiceImpl {

    @Autowired
    @Qualifier("patientDao")
    IPatientDao patientDao;

    private Long currentTimeMillis = TimeUtil.getCurrentTimeMillis();

    private List<JSONObject> newPatients = new ArrayList<>();

    @Override
    protected void initRecordBasicInfo(Record record) {
        record.setHospitalId("57b1e211d897cd373ec76dc6");
        record.setBatchNo("bdsz20180328");
        record.setDepartment("风湿免疫科");
        record.setFormat("table");
        record.setDeleted(false);
        record.setSource("门诊-化验");
        record.setStatus("AMD识别完成");
        record.setRecordType("化验记录");
        record.setSubRecordType("化验");
        record.setPatientId(StringUtils.isEmpty(record.getPatientId()) ? CommonConstant.EMPTY_FLAG : "bdsz_" + record.getPatientId());
        record.setCreateTime(currentTimeMillis);
    }

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        record.setOdCategories(new String[]{"风湿"});

        String groupRecordName = record.getGroupRecordName();

        if (patientCaches.isEmpty() || StringUtils.isEmpty(patientCaches.get(groupRecordName))) {
            String patientId = ((IAssayDao) currentDao()).findPatientIdByGroupRecordName(dataSource, groupRecordName);
            if (!StringUtils.isEmpty(patientId)) {
                patientCaches.put(groupRecordName, "bdsz_" + patientId);
            }
        }
        String pid = patientCaches.get(groupRecordName);
        if (StringUtils.isEmpty(pid)) {
            if (!StringUtils.isEmpty(record.getPatientId())) {
                pid = record.getPatientId();
                JSONObject result = patientDao.findPatientByIdInHRS(pid);
                if (result == null) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put(Patient.ColumnMapping.ID.value(), pid);
                    jsonObj.put(Patient.ColumnMapping.BATCH_NO.value(), "bdsz20180328");
                    jsonObj.put(Patient.ColumnMapping.HOSPITAL_ID.value(), "57b1e211d897cd373ec76dc6");
                    jsonObj.put(Patient.ColumnMapping.CREATE_TIME.value(), currentTimeMillis);
                    jsonObj.put(Patient.ColumnMapping.SEX.value(), EMPTY_FLAG);
                    jsonObj.put(Patient.ColumnMapping.AGE.value(), EMPTY_FLAG);
                    jsonObj.put(Patient.ColumnMapping.BIRTHDAY.value(), EMPTY_FLAG);
                    jsonObj.put(Patient.ColumnMapping.NAME.value(), EMPTY_FLAG);
                    jsonObj.put(Patient.ColumnMapping.ADDRESS.value(), EMPTY_FLAG);
                    jsonObj.put(Patient.ColumnMapping.ORIGIN.value(), EMPTY_FLAG);
                    jsonObj.put(Patient.ColumnMapping.MARRIAGE.value(), EMPTY_FLAG);

                    try {
                        patientDao.save2HRS(jsonObj);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("save patient failed ,pid:" + pid);
                    }
                }
            }
        }
        if (StringUtils.isEmpty(pid)) {
            //无法找到PID的情况下默认给一个假的PID
            pid = "bdsz_forged";
        }
        record.setPatientId(pid);

        //门诊找不到诊断信息，所以orgOdCategory为空
    }

}
