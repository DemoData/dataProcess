package com.example.demo.service.ch.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.support.TextFormatter;
import com.example.demo.dao.ch.IPathologyDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Pathology;
import com.example.demo.service.ch.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chyxPathologyService")
public class PathologyServiceImpl extends BaseService {

    @Autowired
    IPathologyDao pathologyDao;

    @Override
    protected void process(String dataSource) throws Exception {
        log.info(">>>>>>>>>Starting process from dataSource: " + dataSource);
        int pageNum = 1;
        boolean isFinish = false;
        Long count = 0L;
        Map<String, List<String>> orgOdCatCaches = new HashMap<>();
        while (!isFinish) {
            //总共的records
            List<Pathology> pathologies = pathologyDao.findPathologyRecord(dataSource, pageNum, getPageSize());
            if (pathologies != null && pathologies.size() < getPageSize()) {
                isFinish = true;
            }
            if (pathologies == null || pathologies.isEmpty()) {
                continue;
            }
            List<JSONObject> jsonList = new ArrayList<>();
            for (Pathology pathology : pathologies) {
                Record record = new Record();
                initRecordBasicInfo(record);

                String groupRecordName = pathology.getGroupRecordName();
                //如果cache中已近存在就不在重复查找
                if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
                    List<String> orgOdCategories = pathologyDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
                    orgOdCatCaches.put(groupRecordName, orgOdCategories);
                }
                record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
                //init odCategories
                record.setOdCategories(new String[]{OD_CATEGORY, this.getOdCategory(dataSource)});
                //put Pathology to Record
                putPathology2Record(pathology, record);
                //不需要id
                JSONObject beanJson = bean2Json(record);
                beanJson.remove("id");
                beanJson.put("_id", new ObjectId().toString());
                jsonList.add(beanJson);
            }
            if(orgOdCatCaches.size()>50000){
                orgOdCatCaches.clear();
            }
            count += jsonList.size();
            log.info("inserting pathology record total count: " + jsonList.size());
            //把找到的record插入到mongodb hrs record中
            pathologyDao.batchInsert2HRS(jsonList, "Record");
            pageNum++;
        }
        log.info(">>>>>>>>>>>total inserted pathology records: " + count + " from " + dataSource);
    }

    /**
     * Put Pathology data to Record
     *
     * @param pathology
     * @param record
     */
    private void putPathology2Record(Pathology pathology, Record record) throws InvocationTargetException, IllegalAccessException, IntrospectionException {
        record.setPatientId("shch_" + pathology.getPatientId());
        record.setGroupRecordName(pathology.getGroupRecordName());
        record.setSourceId(pathology.getId().toString());
        JSONObject info = record.getInfo();
        Map<String, String> stringMap = getFormattedText(pathology);
        info.put(TextFormatter.TEXT, stringMap.get(TextFormatter.TEXT));
        info.put(TextFormatter.TEXT_ARS, stringMap.get(TextFormatter.TEXT_ARS));
    }

    /**
     * Format info.text adn info.textARS
     *
     * @param pathology
     * @return
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private Map<String, String> getFormattedText(Pathology pathology) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        List<Map<String, String>> infoList = new ArrayList<>();
        for (Pathology.ColumnMapping pathologyEnum : Pathology.ColumnMapping.values()) {
            Map<String, String> row = new HashMap<>();
            if (!pathologyEnum.isRequired()) {
                continue;
            }
            row.put(TextFormatter.PROP_NAME, pathologyEnum.propName());
            row.put(TextFormatter.COLUMN_NAME, pathologyEnum.columnName());
            infoList.add(row);
        }
        return TextFormatter.textFormatter(infoList, pathology);
    }

    /**
     * Set Record basic info
     *
     * @param record
     */
    private void initRecordBasicInfo(Record record) {
        record.setHospitalId("57b1e21fd897cd373ec7a14f");
        record.setUserId("5a7c0adcc2f9c4944dd2b070");
        record.setBatchNo("shch20180309");
        record.setTemplateId(EMPTY_FLAG);
        record.setDepartment("检验科");
        record.setRecordType("病理");
        record.setSubRecordType("病理");
        record.setFormat("text");
        record.setDeleted(false);
        record.setSource("采集入库");
        record.setStatus("AMD识别完成");
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

}
