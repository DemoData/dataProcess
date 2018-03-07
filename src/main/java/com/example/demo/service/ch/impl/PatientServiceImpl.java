package com.example.demo.service.ch.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.config.MysqlDataSourceConfig;
import com.example.demo.dao.ch.IPatientDao;
import com.example.demo.entity.ch.Patient;
import com.example.demo.service.IDataService;
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
public class PatientServiceImpl implements IDataService {

    private static int PAGE_SIZE = 1000;
    private static String EMPTY_FLAG = "";

    @Autowired
    @Qualifier("chyxPatientDao")
    IPatientDao patientDao;


    @Override
    public boolean processData() {
        try {
            process(MysqlDataSourceConfig.MYSQL_JKCT_DATASOURCE);
            process(MysqlDataSourceConfig.MYSQL_TNB_DATASOURCE);
            process(MysqlDataSourceConfig.MYSQL_YX_DATASOURCE);
            process(MysqlDataSourceConfig.MYSQL_YXZW_DATASOURCE);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void process(String dataSource) throws Exception {
        log.info(">>>>>>>>>Starting process from dataSource: " + dataSource);
        int pageNum = 1;
        boolean isFinish = false;
        Long count = 0L;
        while (!isFinish) {
            List<Patient> patientList = patientDao.findAllPatients(dataSource, pageNum, PAGE_SIZE);
            if (patientList != null && patientList.size() < PAGE_SIZE) {
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
                JSONObject patientJson = patient2Json(patient);
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

    public JSONObject patient2Json(Patient patient) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Patient.MongoColumn.ID.value(), patient.getPatientId());
        jsonObj.put(Patient.MongoColumn.BATCH_NO.value(), "shch20180208");
        jsonObj.put(Patient.MongoColumn.HOSPITAL_ID.value(), "57b1e21fd897cd373ec7a14f");
        jsonObj.put(Patient.MongoColumn.SOURCE.value(), "mysql上传系统");
        jsonObj.put(Patient.MongoColumn.SEX.value(), patient.getSex());
        jsonObj.put(Patient.MongoColumn.AGE.value(), patient.getAge());
        jsonObj.put(Patient.MongoColumn.BIRTHDAY.value(), patient.getBirthDay());
        jsonObj.put(Patient.MongoColumn.NAME.value(), StringUtils.isEmpty(patient.getName()) ? EMPTY_FLAG : patient.getName());
        return jsonObj;
    }


}
