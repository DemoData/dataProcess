package com.example.demo.service.ch.jyk.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.ch.IPatientDao;
import com.example.demo.entity.ch.Patient;
import com.example.demo.service.ch.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aron
 * @date 2018.02.27
 */
@Slf4j
@Service("chyxPatientService")
public class PatientServiceImpl extends BaseService {

    @Autowired
    @Qualifier("chyxPatientDao")
    IPatientDao patientDao;

    @Override
    public JSONObject bean2Json(Object entity) {
        Patient patient = (Patient) entity;
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Patient.ColumnMapping.ID.value(), patient.getPatientId());
        jsonObj.put(Patient.ColumnMapping.BATCH_NO.value(), "shch20180309");
        jsonObj.put(Patient.ColumnMapping.HOSPITAL_ID.value(), "57b1e21fd897cd373ec7a14f");
        jsonObj.put(Patient.ColumnMapping.SEX.value(), patient.getSex());
        jsonObj.put(Patient.ColumnMapping.AGE.value(), patient.getAge());
        jsonObj.put(Patient.ColumnMapping.BIRTHDAY.value(), patient.getBirthDay());
        jsonObj.put(Patient.ColumnMapping.NAME.value(), StringUtils.isEmpty(patient.getName()) ? EMPTY_FLAG : patient.getName());
        return jsonObj;
    }

    @Override
    protected void runStart(String dataSource, Integer startPage, Integer endPage) {
        log.info(">>>>>>>>>Starting process from dataSource: " + dataSource);
        int pageNum = startPage;
        boolean isFinish = false;
        Long count = 0L;
        while (!isFinish) {
            if (pageNum >= endPage) {
                isFinish = true;
                continue;
            }
            List<Patient> patientList = patientDao.findPatients(dataSource, pageNum, getPageSize());
            if (patientList != null && patientList.size() < getPageSize()) {
                isFinish = true;
            }
            if (patientList == null || patientList.isEmpty()) {
                continue;
            }
            List<JSONObject> patients = new ArrayList<>();
            for (Patient patient : patientList) {
                //如果patient已近存在于mongodb中则不再插入
                JSONObject result = patientDao.findPatientByIdInHRS(patient.getPatientId());
                if (result != null) {
                    log.debug("process(): Patient : " + patient.getPatientId() + " already exist in DB");
                    continue;
                }
                JSONObject patientJson = this.bean2Json(patient);
                patients.add(patientJson);
            }
            count += patients.size();
            //插入到mongodb中
            log.info("inserting available patient count: " + patients.size());
            patientDao.batchInsert2HRS(patients, "Patient");
            pageNum++;
        }
        log.info(">>>>>>>>>>>total inserted patients: " + count + " from " + dataSource);
    }

    @Override
    protected Integer getCount(String dataSource) {
        return patientDao.getCount(dataSource);
    }


}
