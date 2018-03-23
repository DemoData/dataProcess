package com.example.demo.entity;

import lombok.Data;

@Data
public class Assay {

    private String id;//id
    private String onceClinicId;//一次就诊号
    private String patientId;//病人ID号
    private String groupRecordName;//住院ID号
    private String hospitalId;//住院号
    private String assayId;//检验申请号
    private String assayTime;//检验时间
    private String assayName;//项目名称
    private String resultFlag;//结果正常标识/异常情况
    private String assayResult;//检验结果/定性结果
    private String assayValue;//检验值/定量结果
    private String assayUnit;//检验单位
    private String assaySpecimen;//检验标本
    private String referenceRange;//参考范围
    private String assayState;//检验状态
    private String assayMethodName;//检验方法名称

    public enum ColumnMapping {
        ASSAY_TIME("化验时间"),
        ASSAY_NAME("化验名称"),
        RESULT_FLAG("异常情况"),
        ASSAY_RESULT("定性结果"),//化验结果 包含中文
        ASSAY_VALUE("定量结果"),//化验值 一般只有数字
        ASSAY_UNIT("化验单位"),
        ASSAY_SPECIMEN("标本"),
        REFERENCE_RANGE("参考值"),//一个参考范围
        ASSAY_STATE("化验状态"),
        ASSAY_METHODNAME("化验方法名称");

        private final String value;

        ColumnMapping(String pValue) {
            this.value = pValue;
        }

        public String value() {
            return this.value;
        }
    }
}