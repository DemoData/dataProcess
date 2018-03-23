package com.example.demo.entity;

import lombok.Data;

/**
 * 医嘱
 */
@Data
public class MedicalOrder {
    private Integer id;
    private String patientId;//病人ID号
    private String hospitalId;//住院号
    private String type;//医嘱类型
    private String timeType;//时间类型 长/临
    private String content;//内容
    private String dosage;//剂量
    private String unit;//单位
    private String approach;//途径
    private String frequency;//频次
    private String startDate;//开始时间
    private String endDate;//停止时间

    public enum ColumnMapping {
        PATIENT_ID("病人ID号"),
        HOSPITAL_ID("住院号"),
        TYPE("类型"),
        TIME_TYPE("长/临"),
        CONTENT("内容"),
        DOSAGE("剂量"),
        UNIT("单位"),
        APPROACH("途径"),
        FREQUENCY("频次"),
        START_DATE("开始时间"),
        END_DATE("停止时间");

        private final String value;

        ColumnMapping(String pValue) {
            this.value = pValue;
        }

        public String value() {
            return this.value;
        }
    }

}
