package com.example.demo.entity;

import lombok.Data;

@Data
public class MedicalHistory {

    private Integer id;//id
    private String groupRecordName;//一次就诊号
    private String bedNo;//床号
    private String documentType;//文档类型名称
    private String createDate;//创建日期
    private String medicalContent;//病历内容
    private String recordDate;//记录日期
    private String patientId;//病人ID号
    private String medicalStatus;//病历状态
    private String hospitalizedFlag;//住院标识
    private String medicalHistoryName;//病历名称
    private String hospitalizedMode;//入院方式
    private String orgOdCategories;//病种
    private String mapping;//病历名称类型mapping

    public enum ColumnMapping {
        GROUP_RECORD_NAME("一次就诊号"),
        BED_NO("床号"),
        DOCUMENT_TYPE("文档类型名称"),
        CREATE_DATE("创建日期"),
        MEDICAL_CONTENT("更新内容"),
        RECORD_DATE("记录日期"),
        PATIENT_ID("病人ID号"),
        MEDICAL_STATUS("病历状态"),
        HOSPITALIZED_FLAG("住院标识"),
        MEDICAL_HISTORY_NAME("病历名称"),
        HOSPITALIZED_MODE("入院方式");

        private final String value;

        ColumnMapping(String pValue) {
            this.value = pValue;
        }

        public String value() {
            return this.value;
        }
    }
}
