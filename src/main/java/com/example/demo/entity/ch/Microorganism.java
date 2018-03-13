package com.example.demo.entity.ch;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
public class Microorganism {
    private Integer id;
    private String groupRecordName;
    private String validateMethodCode;//检验方法编码
    private String checkDate;//检验时间
    private String checkApplyNo;//检验申请号
    private String microorganismCode;//微生物代码
    private String microorganismGrowResult;//微生物培养结果
    private String checkValue;//检验值
    private String checkResult;//检验结果
    private String antibioticName;//抗生素名称
    private String microorganismName;//微生物名称
    private String projectName;//项目名称
    private String remark;//备注

    public enum ColumnMapping {
        /*GROUP_RECORD_NAME("groupRecordName", "一次就诊号", false),
        validate_Method_Code("validateMethodCode", "检验方法编码", true),
        check_Date("checkDate", "检验时间", true),//检验时间
        check_Apply_No("checkApplyNo", "检验申请号", true),//检验申请号
        microorganism_Code("microorganismCode", "微生物代码", true),//微生物代码
        microorganism_Grow_Result("microorganismGrowResult", "微生物培养结果", true),//微生物培养结果
        check_Value("checkValue", "检验值", true),//检验值
        check_Result("checkResult", "检验结果", true),//检验结果
        antibiotic_Name("antibioticName", "抗生素名称", true),//抗生素名称
        microorganism_Name("microorganismName", "微生物名称", true),//微生物名称
        project_Name("projectName", "项目名称", true),//项目名称
        remark("remark", "备注", true);//备注*/
        GROUP_RECORD_NAME("一次就诊号"),
        VALIDATE_METHOD_CODE("检验方法编码"),
        CHECK_DATE("检验时间"),//检验时间
        CHECK_APPLY_NO("检验申请号"),//检验申请号
        MICROORGANISM_CODE("微生物代码"),//微生物代码
        MICROORGANISM_GROW_RESULT("微生物培养结果"),//微生物培养结果
        CHECK_VALUE("检验值"),//检验值
        CHECK_RESULT("检验结果"),//检验结果
        ANTIBIOTIC_NAME("抗生素名称"),//抗生素名称
        MICROORGANISM_NAME("微生物名称"),//微生物名称
        PROJECT_NAME("项目名称"),//项目名称
        REMARK("备注");

        private final String value;

        ColumnMapping(String pValue) {
            this.value = pValue;
        }

        public String value() {
            return this.value;
        }
    }
}
