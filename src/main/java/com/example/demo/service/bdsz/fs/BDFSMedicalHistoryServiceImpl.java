package com.example.demo.service.bdsz.fs;

import com.example.demo.entity.MedicalHistory;
import com.example.demo.entity.Record;
import com.example.demo.service.bdsz.zl.BDZLMedicalHistoryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service("bdfsMedicalHistoryService")
public class BDFSMedicalHistoryServiceImpl extends BDZLMedicalHistoryServiceImpl {

    @Override
    protected void customProcess(Record record, MedicalHistory entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        super.customProcess(record, entity, orgOdCatCaches, patientCaches, dataSource);
        record.setOdCategories(new String[]{"风湿"});
    }

    @Override
    protected void initRecordBasicInfo(Record record, MedicalHistory medicalHistory) {
        record.setHospitalId("57b1e211d897cd373ec76dc6");
        record.setBatchNo("bdsz20180328");
        record.setDepartment("风湿免疫科");
        record.setFormat("text");
        record.setDeleted(false);
        record.setSource("病历文书");
        record.setStatus("AMD识别完成");
        record.setPatientId("bdsz_" + medicalHistory.getPatientId());
        record.setGroupRecordName(medicalHistory.getGroupRecordName());
        record.setSourceId(medicalHistory.getId().toString());
        record.setSourceRecordType(medicalHistory.getMedicalHistoryName());
    }

    /**
     * Put MedicalHistory data to Record
     *
     * @param medicalHistory
     * @param record
     */
    protected void putText2Record(MedicalHistory medicalHistory, Record record) {
        super.putText2Record(medicalHistory, record);
    }

}