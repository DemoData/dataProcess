package com.example.demo.service.ch.xgwk.impl;

import com.example.demo.dao.ch.IAssayDao;
import com.example.demo.dao.ch.TableDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.Assay;
import com.example.demo.service.ch.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chxgAssayService")
public class XGAssayServiceImpl extends TableService<Assay> {

    @Autowired
    @Qualifier("xgAssayDao")
    private IAssayDao assayDao;


    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {

    }

    @Override
    protected TableDao<Assay> currentDao() {
        return assayDao;
    }

    /**
     * Set Record basic info
     *
     * @param record
     */
    protected void initRecordBasicInfo(Record record) {
        record.setHospitalId("57b1e21fd897cd373ec7a14f");
        record.setBatchNo("shch20180315");
        record.setDepartment("血管外科");
        record.setFormat("table");
        record.setDeleted(false);
        record.setSource("采集入库");
        record.setStatus("AMD识别完成");
        record.setRecordType("化验记录");
        record.setSubRecordType("化验");
        record.setPatientId("shxg_" + record.getPatientId());
        record.setOrgOdCategories(new String[]{});
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
            map.put(Assay.ColumnMapping.ASSAY_SPECIMEN.value(), assay.getAssaySpecimen() == null ? EMPTY_FLAG : assay.getAssaySpecimen());
            map.put(Assay.ColumnMapping.ASSAY_NAME.value(), assay.getAssayName() == null ? EMPTY_FLAG : assay.getAssayName());
            map.put(Assay.ColumnMapping.ASSAY_RESULT.value(), assay.getAssayResult() == null ? EMPTY_FLAG : assay.getAssayResult());
            map.put(Assay.ColumnMapping.ASSAY_UNIT.value(), assay.getAssayUnit() == null ? EMPTY_FLAG : assay.getAssayUnit());
            map.put(Assay.ColumnMapping.RESULT_FLAG.value(), assay.getResultFlag() == null ? EMPTY_FLAG : assay.getResultFlag());
            map.put(Assay.ColumnMapping.ASSAY_TIME.value(), assay.getAssayTime() == null ? EMPTY_FLAG : assay.getAssayTime());
            detailArray.add(map);
        }
    }


}