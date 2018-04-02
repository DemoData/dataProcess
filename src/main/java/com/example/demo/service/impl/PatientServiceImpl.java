package com.example.demo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.util.TimeUtil;
import com.example.demo.dao.standard.IPatientDao;
import com.example.demo.entity.Patient;
import com.example.demo.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aron
 * @date 2018.02.27
 */
@Slf4j
@Service("patientService")
public class PatientServiceImpl extends BaseService {

    @Autowired
    @Qualifier("patientDao")
    IPatientDao patientDao;

    private Long currentTimeMillis = TimeUtil.getCurrentTimeMillis();

    private String batchNo = "bdsz20180328";
    //北京大学深圳医院﻿57b1e211d897cd373ec76dc6
    private String hospitalId = "57b1e211d897cd373ec76dc6";

    private String patientPrefix = "bdsz_";

    @Override
    public JSONObject bean2Json(Object entity) {
        Patient patient = (Patient) entity;
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Patient.ColumnMapping.ID.value(), patientPrefix + patient.getPatientId());
        jsonObj.put(Patient.ColumnMapping.BATCH_NO.value(), batchNo);
        jsonObj.put(Patient.ColumnMapping.HOSPITAL_ID.value(), hospitalId);
        jsonObj.put(Patient.ColumnMapping.CREATE_TIME.value(), patient.getCreateTime());
        jsonObj.put(Patient.ColumnMapping.SEX.value(), StringUtils.isEmpty(patient.getSex()) ? EMPTY_FLAG : patient.getSex());
        jsonObj.put(Patient.ColumnMapping.AGE.value(), StringUtils.isEmpty(patient.getAge()) ? EMPTY_FLAG : patient.getAge());
        jsonObj.put(Patient.ColumnMapping.BIRTHDAY.value(), StringUtils.isEmpty(patient.getBirthDay()) ? EMPTY_FLAG : patient.getBirthDay());
        jsonObj.put(Patient.ColumnMapping.NAME.value(), StringUtils.isEmpty(patient.getName()) ? EMPTY_FLAG : patient.getName());
        jsonObj.put(Patient.ColumnMapping.ADDRESS.value(), StringUtils.isEmpty(patient.getAddress()) ? EMPTY_FLAG : patient.getAddress());
        jsonObj.put(Patient.ColumnMapping.ORIGIN.value(), StringUtils.isEmpty(patient.getOrigin()) ? EMPTY_FLAG : patient.getOrigin());
        jsonObj.put(Patient.ColumnMapping.MARRIAGE.value(), StringUtils.isEmpty(patient.getMarriage()) ? EMPTY_FLAG : patient.getMarriage());
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
            Map<String, JSONObject> patients = new HashMap<>();
            for (Patient patient : patientList) {
                //对于重复的PID只取一个
                if (patients.containsKey(patient.getPatientId())) {
                    continue;
                }
                //如果patient已近存在于mongodb中则不再插入
                JSONObject result = patientDao.findPatientByIdInHRS(patientPrefix + patient.getPatientId());
                if (result != null) {
                    log.debug("process(): Patient : " + patient.getPatientId() + " already exist in DB");
                    continue;
                }
                patient.setCreateTime(currentTimeMillis);
                JSONObject patientJson = this.bean2Json(patient);

                patients.put(patient.getPatientId(), patientJson);
            }
            //插入到mongodb中
            log.info("inserting available patient count: " + patients.size());
            List<JSONObject> insertList = new ArrayList<>();
            for (Map.Entry<String, JSONObject> entry : patients.entrySet()) {
                if (entry != null) {
                    insertList.add(entry.getValue());
                }
            }
            count += insertList.size();
            patientDao.batchInsert2HRS(insertList);
            pageNum++;
            //清空
            patients.clear();
        }
        log.info(">>>>>>>>>>>total inserted patients: " + count + " from " + dataSource + ",currentTimeMillis:" + currentTimeMillis);
    }

    @Override
    protected Integer getCount(String dataSource) {
        return patientDao.getCount(dataSource);
    }


}
