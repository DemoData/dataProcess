package com.example.demo.service.bdsz.zl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.constant.CommonConstant;
import com.example.demo.dao.TableDao;
import com.example.demo.dao.standard.IAssayDao;
import com.example.demo.entity.Assay;
import com.example.demo.entity.Record;
import com.example.demo.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("bdzlAssayService")
public class BDZLAssayServiceImpl extends TableService<Assay> {

    @Autowired
    @Qualifier("bdzlAssayDao")
    private IAssayDao assayDao;

    @Override
    protected String getArrayCondition(Record record) {
        return record.getGroupRecordName();
    }

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        String groupRecordName = record.getGroupRecordName();
        if (StringUtils.isEmpty(groupRecordName)) {
            return;
        }
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            List<String> orgOdCategories = assayDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
            orgOdCatCaches.put(groupRecordName, orgOdCategories);
        }

        record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
        record.setOdCategories(new String[]{"肿瘤"});
    }

    @Override
    protected TableDao<Assay> currentDao() {
        return assayDao;
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

    /**
     * Set Record basic info
     *
     * @param record
     */
    protected void initRecordBasicInfo(Record record) {
        record.setHospitalId("57b1e211d897cd373ec76dc6");
        record.setBatchNo("bdsz20180320");
        record.setDepartment("肿瘤内科");
        record.setFormat("table");
        record.setDeleted(false);
        record.setSource("化验");
        record.setStatus("AMD识别完成");
        record.setRecordType("化验记录");
        record.setSubRecordType("化验");
        record.setPatientId(StringUtils.isEmpty(record.getPatientId()) ? CommonConstant.EMPTY_FLAG : "bdsz_" + record.getPatientId());
    }

    protected void initInfoArray(Record record, List<Assay> assayList) {
        if (assayList == null || assayList.isEmpty()) {
            return;
        }
        //init info
        List<Map<String, String>> detailArray = record.getInfo().getObject("detailArray", List.class);
        //init detail array
        for (Assay assay : assayList) {
            Map<String, String> map = new HashMap<>();
            map.put(Assay.ColumnMapping.ASSAY_TIME.value(), assay.getAssayTime() == null ? EMPTY_FLAG : assay.getAssayTime());
            map.put(Assay.ColumnMapping.ASSAY_NAME.value(), assay.getAssayName() == null ? EMPTY_FLAG : assay.getAssayName());
            map.put(Assay.ColumnMapping.RESULT_FLAG.value(), assay.getResultFlag() == null ? EMPTY_FLAG : assay.getResultFlag());
            map.put(Assay.ColumnMapping.ASSAY_RESULT.value(), assay.getAssayResult() == null ? EMPTY_FLAG : assay.getAssayResult());
            map.put(Assay.ColumnMapping.ASSAY_VALUE.value(), assay.getAssayValue() == null ? EMPTY_FLAG : assay.getAssayValue());
            map.put(Assay.ColumnMapping.ASSAY_UNIT.value(), assay.getAssayUnit() == null ? EMPTY_FLAG : assay.getAssayUnit());
            map.put(Assay.ColumnMapping.ASSAY_SPECIMEN.value(), assay.getAssaySpecimen() == null ? EMPTY_FLAG : assay.getAssaySpecimen());
            map.put(Assay.ColumnMapping.REFERENCE_RANGE.value(), assay.getReferenceRange() == null ? EMPTY_FLAG : assay.getReferenceRange());
            map.put(Assay.ColumnMapping.ASSAY_STATE.value(), assay.getAssayState() == null ? EMPTY_FLAG : assay.getAssayState());
            map.put(Assay.ColumnMapping.ASSAY_METHODNAME.value(), assay.getAssayMethodName() == null ? EMPTY_FLAG : assay.getAssayMethodName());
            detailArray.add(map);
        }
    }

}
