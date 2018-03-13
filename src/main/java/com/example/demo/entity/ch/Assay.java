package com.example.demo.entity.ch;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
public class Assay {

    private String id;//id
    private String onceClinicId;//一次就诊号
    private String assayId;//检验申请号
    private String assayTime;//检验时间
    private String assayName;//项目名称
    private String resultFlag;//结果正常标识
    private String assayResult;//检验结果
    private String assayValue;//检验值
    private String assayUnit;//检验单位
    private String assaySpecimen;//检验标本
    private String referenceRange;//参考范围
    private String assayState;//检验状态
    private String assayMethodName;//检验方法名称

    public enum ColumnMapping {
        ASSAY_TIME("化验时间"),
        ASSAY_NAME("化验名称"),
        RESULT_FLAG("异常情况"),
        ASSAY_RESULT("化验结果"),
        ASSAY_VALUE("化验值"),
        ASSAY_UNIT("单位"),
        ASSAY_SPECIMEN("标本"),
        REFERENCE_RANGE("参考范围"),
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