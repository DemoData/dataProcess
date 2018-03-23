package com.example.demo.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
public class Pathology {
    private Integer id;
    private String groupRecordName;//一次就诊号
    private String patientId;
    private String checkTime;//检查时间
    private String projectName;//项目名称
    private String isPositive;//是否阳性
    private String resultDesc;//结果描述
    private String impression;//印象
    private String pathologyDate;//病理报告时间
    private String remark;//备注
    private String resultTypeName;//结果类型名称
    private String advice;//建议

    public enum ColumnMapping {
        GROUP_RECORD_NAME("groupRecordName", "一次就诊号", false),
        PATIENT_ID("patientId", "病人ID号", false),
        CHECK_TIME("checkTime", "检查时间", true),
        PROJECT_NAME("projectName", "项目名称", true),
        IS_POSITIVE("isPositive", "是否阳性", true),
        RESULT_DESC("resultDesc", "结果描述", true),
        IMPRESSION("impression", "印象", true),
        PATHOLOGY_DATE("pathologyDate", "病理报告时间", true),
        REMARK("remark", "备注", true),
        RESULT_TYPE_NAME("resultTypeName", "结果类型名称", true),
        ADVICE("advice", "建议", true);

        private final String propName;
        private final String columnName;
        private final boolean flag;

        ColumnMapping(String pPropName, String pColumnName, boolean pFlag) {
            this.propName = pPropName;
            this.columnName = pColumnName;
            this.flag = pFlag;
        }

        public String propName() {
            return this.propName;
        }

        public String columnName() {
            return this.columnName;
        }

        public boolean isRequired() {
            return this.flag;
        }
    }
}
