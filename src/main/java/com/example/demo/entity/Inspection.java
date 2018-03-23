package com.example.demo.entity;

import com.example.demo.common.constant.CommonConstant;
import lombok.Data;

@Data
public class Inspection {
    private Integer id;
    private String groupRecordName;//一次就诊号
    private String hospitalId;//住院号
    private String inHospitalDate;//入院日期
    private String outHospitalDate;//出院日期
    private String inspectionType;//检查类别
    private String inspectionBodyPart;//检查部位
    private String clinicalDiagnosis;//临床诊断
    private String resultContent;//检查结论正文
    private String reportClinical;//报告诊断/检查所见
    private String resultDesc;//结果描述/报告结论
    private String inspectionState;//检查状态
    private String reportId;//报告号
    private String abnormalFlag;//异常标志/是否阳性
    private String reportFixDate;//报告修正时间
    private String typeName;//分类名称
    private String applyDate;//申请时间
    private String patientId;//病人ID号
    private String birthday;//出生日期
    private String doctorName;//检查医生
    private String hospitalFlag;//住院标识
    private String auditor;//审核者
    private String observeReason;//申请观察原因
    private String inspectionMethod;//检查方法
    private String diagnosis;//诊断
    private String applyNo;//申请号
    private String sex;//性别
    private String reportDate;//报告时间
    private String inspectionDate;//检查时间
    private String auditDate;//审核时间
    private String applyProjectName;//申请项目名称/检查项目名称
    private String advice;//建议
    private String age;//年龄


    public enum ColumnMapping {
        GROUP_RECORD_NAME("groupRecordName", "一次就诊号", false),
        INSPECTION_TYPE("inspectionType", "检查类别", true),
        INSPECTION_BODY_PART("inspectionBodyPart", "检查部位", true),
        CLINICAL_DIAGNOSIS("clinicalDiagnosis", "临床诊断", true),
        RESULT_CONTENT("resultContent", "检查结论正文", true),
        IN_HOSPITAL_DATE("inHospitalDate", "入院日期", true),
        OUT_HOSPITAL_DATE("outHospitalDate", "出院日期", true),
        REPORT_CLINICAL("reportClinical", "检查所见", true),
        INSPECTION_DATE("inspectionDate", "检查时间", true),
        RESULT_DESC("resultDesc", "结果描述", true),//结果描述
        INSPECTION_STATE("inspectionState", "检查状态", true),//检查状态
        REPORT_ID("reportId", "报告号", true),//报告号
        ABNORMAL_FLAG("abnormalFlag", "异常标志", true),//异常标志/是否阳性
        REPORT_FIX_DATE("reportFixDate", "报告修正时间", true),//报告修正时间
        TYPE_NAME("typeName", "分类名称", true),//分类名称
        APPLY_DATE("applyDate", "申请时间", true),//申请时间
        PATIENT_ID("patientId", "病人ID号", true),//病人ID号
        BIRTHDAY("birthday", "出生日期", true),//出生日期
        DOCTOR_NAME("doctorName", "检查医生", true),//检查医生
        HOSPITAL_FLAG("hospitalFlag", "住院标识", true),//住院标识
        AUDITOR("auditor", "审核者", true),//审核者
        OBSERVE_REASON("observeReason", "申请观察原因", true),//申请观察原因
        INSPECTION_METHOD("inspectionMethod", "检查方法", true),//检查方法
        DIAGNOSIS("diagnosis", "诊断", true),//诊断
        APPLY_NO("applyNo", "申请号", true),//申请号
        SEX("sex", "性别", true),//性别
        REPORT_DATE("reportDate", "报告时间", true),//报告时间
        AUDIT_DATE("auditDate", "审核时间", true),//审核时间
        APPLY_PROJECT_NAME("applyProjectName", "检查项目名称", true),//申请项目名称/检查项目名称
        ADVICE("advice", "建议", true),//建议
        AGE("age", "年龄", true);//年龄
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

    public String getAbnormalFlag() {
        if (this.abnormalFlag == null) {
            return CommonConstant.EMPTY_FLAG;
        }
        if ("1".equals(this.abnormalFlag.trim())) {
            return "阳性";
        }
        if ("0".equals(this.abnormalFlag.trim()) || "2".equals(this.abnormalFlag.trim())) {
            return "阴性";
        }
        return abnormalFlag;
    }
}
