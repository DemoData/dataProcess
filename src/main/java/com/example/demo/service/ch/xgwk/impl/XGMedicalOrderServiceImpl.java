package com.example.demo.service.ch.xgwk.impl;

import com.example.demo.dao.ch.IMedicalOrderDao;
import com.example.demo.dao.ch.TableDao;
import com.example.demo.entity.Record;
import com.example.demo.entity.ch.MedicalOrder;
import com.example.demo.service.ch.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("chxgMedicalOrderService")
public class XGMedicalOrderServiceImpl extends TableService<MedicalOrder> {

    @Autowired
    private IMedicalOrderDao medicalOrderDao;

    @Override
    protected void customProcess(Record record, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {

    }

    @Override
    protected TableDao<MedicalOrder> currentDao() {
        return medicalOrderDao;
    }

    @Override
    protected void initRecordBasicInfo(Record record) {
        record.setHospitalId("57b1e21fd897cd373ec7a14f");
        record.setBatchNo("shch20180315");
        record.setDepartment("血管外科");
        record.setFormat("table");
        record.setDeleted(false);
        record.setSource("采集入库");
        record.setStatus("AMD识别完成");
        record.setRecordType("治疗方案");
        record.setSubRecordType("医嘱");
        record.setPatientId("shxg_" + record.getPatientId());
        record.setOrgOdCategories(new String[]{});
    }

    @Override
    protected void initInfoArray(Record record, List<MedicalOrder> assayList) {
        if (assayList == null || assayList.isEmpty()) {
            return;
        }
        //init info
        List<Map<String, String>> detailArray = record.getInfo().getObject("detailArray", List.class);
        //init detail array
        for (MedicalOrder medicalOrder : assayList) {
            Map<String, String> map = new HashMap<>();
            map.put(MedicalOrder.ColumnMapping.TYPE.value(), medicalOrder.getType() == null ? EMPTY_FLAG : medicalOrder.getType());
            map.put(MedicalOrder.ColumnMapping.TIME_TYPE.value(), medicalOrder.getTimeType() == null ? EMPTY_FLAG : medicalOrder.getTimeType());
            map.put(MedicalOrder.ColumnMapping.CONTENT.value(), medicalOrder.getContent() == null ? EMPTY_FLAG : medicalOrder.getContent());
            map.put(MedicalOrder.ColumnMapping.DOSAGE.value(), medicalOrder.getDosage() == null ? EMPTY_FLAG : medicalOrder.getDosage());
            map.put(MedicalOrder.ColumnMapping.UNIT.value(), medicalOrder.getUnit() == null ? EMPTY_FLAG : medicalOrder.getUnit());
            map.put(MedicalOrder.ColumnMapping.APPROACH.value(), medicalOrder.getApproach() == null ? EMPTY_FLAG : medicalOrder.getApproach());
            map.put(MedicalOrder.ColumnMapping.FREQUENCY.value(), medicalOrder.getFrequency() == null ? EMPTY_FLAG : medicalOrder.getFrequency());
            //日期格式化处理
            /*
             SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MM dd HH mm ss");
            Date date = null;
            date = dateFormat.parse(string);
             */
            map.put(MedicalOrder.ColumnMapping.START_DATE.value(), medicalOrder.getStartDate() == null ? EMPTY_FLAG : medicalOrder.getStartDate());
            map.put(MedicalOrder.ColumnMapping.END_DATE.value(), medicalOrder.getEndDate() == null ? EMPTY_FLAG : medicalOrder.getEndDate());
            detailArray.add(map);
        }
    }


}