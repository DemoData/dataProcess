package com.example.demo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.dao.IPatientDao;
import com.example.demo.service.IPatientService;
import com.example.demo.util.ExcelUtil;
import com.example.demo.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author aron
 * @date 2018.02.27
 */
@Slf4j
@Service
public class PatientServiceImpl implements IPatientService {

    private static int PAGE_SIZE = 1000;
    private static String EMPTY_FLAG = "未提及";

    @Autowired
    private IPatientDao patientDao;

    @Override
    public boolean processPatientData() {
        int pageNum = 0;
        int count = 0;
        log.info(">>>>>>>>>>starting<<<<<<<<<<<<");
        long startTime = System.currentTimeMillis();
        Long currentTimeMillis = TimeUtil.getCurrentTimeMillis();
        currentTimeMillis = 1518189131863L;
        log.info(">>>>>>>>>>>>>>>currentTimeMillis is " + currentTimeMillis + " sec<<<<<<<<<<<<<<<<");
        String currentDate = TimeUtil.intToStandardTime(currentTimeMillis);
        ArrayList<JSONObject> addressList = ExcelUtil.getProvinceCityList("/data/hitales/标准映射_省市区到省.xlsx");
        boolean isFinish = false;
        try {
            while (!isFinish) {
                Query patientQuery = new Query();
                patientQuery.with(new PageRequest(pageNum, PAGE_SIZE));
                patientQuery.addCriteria(Criteria.where("batchNo").is("ghyy20180115"));
                List<JSONObject> patients = patientDao.findByQueryInHRS(patientQuery, "Patient");
                log.info(">>>>>>>>>>> Found patients count : " + patients.size());
                count += patients.size();
                if (patients.size() < PAGE_SIZE) {
                    isFinish = true;
                }
                List<JSONObject> entities = new ArrayList<JSONObject>();
                for (JSONObject patient : patients) {
                    JSONObject entity = new JSONObject();
                    entity.put("oldCategoried", new String[]{"RA"});
                    entity.put("SDS_version", "V1.0.0");
                    entity.put("Tab_Version", "VB1.0.1");
                    Object _id = patient.get("_id");
                    if (_id instanceof JSONObject) {
                        entity.put("PID", ((JSONObject) _id).getString("$oid"));
                    } else {
                        entity.put("PID", patient.getString("_id"));
                    }
                    //entity.put("PID", patient.getString("_id"));
                    entity.put("hospitalId", patient.getString("hospitalId"));
                    entity.put("projectProcessId", currentTimeMillis);
                    entity.put("入库时间", currentDate);
                    entity.put("性别", getValue(patient, "性别"));
                    String jiguan = getValue(patient, "籍贯");
                    entity.put("籍贯-原文", jiguan);
                    if (EMPTY_FLAG.equals(jiguan)) {
                        entity.put("籍贯", EMPTY_FLAG);
                    } else {
                        for (JSONObject jsonObject : addressList) {
                            if (jiguan.contains(jsonObject.getString("省市区"))) {
                                entity.put("籍贯", jsonObject.getString("所属省"));
                                break;
                            }
                        }
                        if (!entity.containsKey("籍贯")) {
                            entity.put("籍贯", EMPTY_FLAG);
                        }
                    }
                    entity.put("出生地", getValue(patient, "出生地"));
                    String address = getLiveAddress(patient, "居住地址");
                    entity.put("现住址-原文", address);
                    if (EMPTY_FLAG.equals(address)) {
                        entity.put("现住址", EMPTY_FLAG);
                    } else {
                        for (JSONObject jsonObject : addressList) {
                            if (address.contains(jsonObject.getString("省市区"))) {
                                entity.put("现住址", jsonObject.getString("所属省"));
                                break;
                            }
                        }
                        if (!entity.containsKey("现住址")) {
                            entity.put("现住址", EMPTY_FLAG);
                        }
                    }
                    entity.put("出生年", getBornDate(patient, "出生日期"));
                    entity.put("婚姻状况", getValue(patient, "婚姻状况"));
                    //log.info("processing..." + entity.toString());
                    entities.add(entity);
                }
                patientDao.batchInsert2HDP(entities, "ADO");
                patientDao.batchInsert2HDPB(entities, "ADO");
                pageNum++;
            }
        } catch (Exception e) {
            log.info("!!! Get ERROR !!! " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        long endTime = System.currentTimeMillis();
        log.info(">>>>>>>>>>>>>>>Cost " + (endTime - startTime) / 1000 + " sec<<<<<<<<<<<<<<<<");
        log.info(">>>>>>>>>>>>>>>inserted " + count + " records , Process Done<<<<<<<<<<<<<<<<");
        return isFinish;
    }

    private String getValueInPatient(JSONObject patient, String key) {
        String value = patient.getString(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return null;
    }

    private String getValue(JSONObject patient, String key) {
        String value = getValueInPatient(patient, key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        String sdsValue = findInSDS(patient, key);
        if (StringUtils.isNotBlank(sdsValue)) {
            return sdsValue;
        }
        return EMPTY_FLAG;
    }

    private String getLiveAddress(JSONObject patient, String key) {
        String value = getValueInPatient(patient, key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        if (StringUtils.isNotBlank(getValueInPatient(patient, "户口地址"))) {
            return getValueInPatient(patient, "户口地址");
        }
        String sdsValue = findInSDS(patient, key);
        if (StringUtils.isNotBlank(sdsValue)) {
            if ("null".equals(sdsValue)) {
                log.info("为null数据:" + patient);
            }
            return sdsValue;
        }
        return EMPTY_FLAG;
    }

    private String getBornDate(JSONObject patient, String key) {
        String value = getValueInPatient(patient, key);
        if (StringUtils.isNotBlank(value)) {
            //精确到年
            return value.substring(0, 4).toString();
        }
        String sdsValue = findInSDS(patient, key);
        if (StringUtils.isNotBlank(sdsValue)) {
            return sdsValue;
        }
        String admissionOrLeaveDate = findInSDS(patient, "入院日期");
        if (StringUtils.isBlank(admissionOrLeaveDate)) {
            admissionOrLeaveDate = findInSDS(patient, "出院日期");
            if (StringUtils.isBlank(admissionOrLeaveDate)) {
                return EMPTY_FLAG;
            }
        }
        if (StringUtils.isNotBlank(admissionOrLeaveDate) && StringUtils.isBlank(admissionOrLeaveDate.replaceAll("[^0-9]", ""))) {
            return EMPTY_FLAG;
        }
        String age = findInSDS(patient, "年龄");
        if (StringUtils.isNotBlank(age)) {
            String dateStr = admissionOrLeaveDate.replaceAll("[^0-9]", "");
            String ageStr = age.replaceAll("[^0-9]", "");
            if (StringUtils.isBlank(dateStr) || StringUtils.isBlank(ageStr) || dateStr.length() < 4) {
                return EMPTY_FLAG;
            }
            int dateInt = Integer.valueOf(dateStr.substring(0, 4).toString());
            int ageInt = Integer.valueOf(ageStr);
            return String.valueOf(dateInt - ageInt);
        }
        return EMPTY_FLAG;
    }

    private String findInSDS(JSONObject patient, String key) {
        String pid = patient.getString("_id");
        Query query = new Query();
        query.addCriteria(Criteria.where("patientid").is(pid));
        List<JSONObject> msdatas = patientDao.findByQueryInSDS(query, "msdata");
        if (msdatas.size() == 0) {
            return "未提及";
        }
        for (JSONObject msdata : msdatas) {
            Object msdataItem = msdata.get("msdata");
            if (msdataItem == null) {
                continue;
            }
            if (msdataItem instanceof LinkedHashMap) {
                Object baseInfoItems = ((LinkedHashMap<String, Object>) msdataItem).get("标准基本信息");
                if (baseInfoItems == null) {
                    continue;
                }
                if (baseInfoItems instanceof List) {
                    List<LinkedHashMap<String, String>> baseinfos = ((List<LinkedHashMap<String, String>>) baseInfoItems);
                    for (LinkedHashMap<String, String> item : baseinfos) {
                        String title = item.get("段落标题");
                        String info = item.get("标准基本信息");
                        if ((StringUtils.isNotBlank(title) && title.contains(key)) || (StringUtils.isNotBlank(info) && info.contains(key))) {
                            return item.get("标准基本信息内容");
                        }
                    }
                }
            }
        }
        return null;
    }
}
