package com.example.demo.service.ch.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.config.MysqlDataSourceConfig;
import com.example.demo.dao.ch.IAssayDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Assay;
import com.example.demo.service.IDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chyxAssayService")
public class AssayServiceImpl implements IDataService {
    private static int PAGE_SIZE = 1000;
    private static String EMPTY_FLAG = "";

    @Autowired
    IAssayDao assayDao;

    @Override
    public boolean processData() {
        try {
            process(MysqlDataSourceConfig.MYSQL_JKCT_DATASOURCE, false);
            process(MysqlDataSourceConfig.MYSQL_TNB_DATASOURCE, true);
            process(MysqlDataSourceConfig.MYSQL_YX_DATASOURCE, true);
            process(MysqlDataSourceConfig.MYSQL_YXZW_DATASOURCE, true);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void process(String dataSource, boolean dulplicateCheck) {
        int pageNum = 1;
        boolean isFinish = false;
        Long count = 0L;
        Map<String, List<String>> orgOdCatCache = new HashMap<>();
        while (!isFinish) {
            List<Record> recordList = assayDao.findAssayRecord(dataSource, pageNum, PAGE_SIZE);
            if (recordList != null && recordList.size() < PAGE_SIZE) {
                isFinish = true;
            }
            if (recordList == null || recordList.isEmpty()) {
                continue;
            }
            List<JSONObject> jsonList = new ArrayList<>();
            //遍历record
            for (Record record : recordList) {
                String applyId = record.getId();
                if (dulplicateCheck) {
                    JSONObject jsonItem = assayDao.findRecordByIdInHRS(applyId);
                    if (jsonItem != null) {
                        log.debug("process(): record : " + applyId + " already exist in DB");
                        continue;
                    }
                }
                String groupRecordName = record.getGroupRecordName();
                if (StringUtils.isEmpty(applyId)) {
                    continue;
                }
                //init odCategories
                String odCategorie = EMPTY_FLAG;
                if (MysqlDataSourceConfig.MYSQL_JKCT_DATASOURCE.equals(dataSource)) {
                    odCategorie = "健康查体";
                }
                if (MysqlDataSourceConfig.MYSQL_YX_DATASOURCE.equals(dataSource)) {
                    odCategorie = "胰腺相关";
                }
                if (MysqlDataSourceConfig.MYSQL_YXZW_DATASOURCE.equals(dataSource)) {
                    odCategorie = "胰腺占位";
                }
                if (MysqlDataSourceConfig.MYSQL_TNB_DATASOURCE.equals(dataSource)) {
                    odCategorie = "糖尿病相关";
                }
                record.setOdCategories(new String[]{"糖尿病", odCategorie});

                //如果cache中已近存在就不在重复查找
                if (orgOdCatCache.isEmpty() || StringUtils.isEmpty(orgOdCatCache.get(groupRecordName))) {
                    List<String> orgOdCategories = assayDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
                    orgOdCatCache.put(groupRecordName, orgOdCategories);
                }
                record.setOrgOdCategories(orgOdCatCache.get(groupRecordName).toArray(new String[0]));
                //查找化验报告通过检验申请号
                List<Assay> assayList = assayDao.findAssaysByApplyId(dataSource, applyId);
                JSONObject jsonObject = generateJson(record, assayList);
                jsonList.add(jsonObject);
            }
            orgOdCatCache.clear();
            count += jsonList.size();
            log.info("inserting assay record total count: " + jsonList.size());
            //把找到的record插入到mongodb hrs record中
            assayDao.batchInsert2HRS(jsonList, "Record");
            pageNum++;
        }
        log.info(">>>>>>>>>>>total inserted assay records: " + count + " from " + dataSource);
    }

    /**
     * 转换为需要入库的json类型
     *
     * @param record
     * @param assayList
     * @return
     */
    private JSONObject generateJson(Record record, List<Assay> assayList) {
        JSONObject jsonItem = new JSONObject();
        jsonItem.put("_id", record.getId());
        jsonItem.put("userId", "5a7c0adcc2f9c4944dd2b070");
        jsonItem.put("groupRecordName", record.getGroupRecordName());
        jsonItem.put("patientId", record.getPatientId());//TODO:
        jsonItem.put("templateId", EMPTY_FLAG);
        jsonItem.put("department", "检验科");
        //init info
        JSONObject info = record.getInfo();
        List<Map<String, String>> detailArray = new ArrayList<>();
        info.put("basicInfo", new JSONObject());
        info.put("detailArray", detailArray);
        jsonItem.put("info", info);
        //init detail array
        for (Assay assay : assayList) {
            Map<String, String> map = new HashMap<>();
            map.put(Assay.MongoColumn.ASSAY_TIME.value(), assay.getAssayTime());
            map.put(Assay.MongoColumn.ASSAY_NAME.value(), assay.getAssayName());
            map.put(Assay.MongoColumn.RESULT_FLAG.value(), assay.getResultFlag());
            map.put(Assay.MongoColumn.ASSAY_RESULT.value(), assay.getAssayResult());
            map.put(Assay.MongoColumn.ASSAY_VALUE.value(), assay.getAssayValue());
            map.put(Assay.MongoColumn.ASSAY_UNIT.value(), assay.getAssayUnit());
            map.put(Assay.MongoColumn.ASSAY_SPECIMEN.value(), assay.getAssaySpecimen());
            map.put(Assay.MongoColumn.REFERENCE_RANGE.value(), assay.getReferenceRange());
            map.put(Assay.MongoColumn.ASSAY_STATE.value(), assay.getAssayState());
            map.put(Assay.MongoColumn.ASSAY_METHODNAME.value(), assay.getAssayMethodName());
            detailArray.add(map);
        }
        jsonItem.put("recordType", "化验记录");
        jsonItem.put("subRecordType", "化验");
        jsonItem.put("odCategories", record.getOdCategories());
        jsonItem.put("orgOdCategories", record.getOrgOdCategories());
        jsonItem.put("sourceId", record.getSourceId());
        jsonItem.put("format", "table");
        return jsonItem;
    }
}
