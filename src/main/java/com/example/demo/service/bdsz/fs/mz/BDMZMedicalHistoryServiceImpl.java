package com.example.demo.service.bdsz.fs.mz;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.constant.CommonConstant;
import com.example.demo.common.support.TextFormatter;
import com.example.demo.common.util.StringUtil;
import com.example.demo.common.util.TimeUtil;
import com.example.demo.entity.MedicalHistory;
import com.example.demo.entity.Record;
import com.example.demo.service.bdsz.fs.BDFSMedicalHistoryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("bdmzMedicalHistoryService")
public class BDMZMedicalHistoryServiceImpl extends BDFSMedicalHistoryServiceImpl {

    private Long currentTimeMillis = TimeUtil.getCurrentTimeMillis();

    @Override
    protected void initRecordBasicInfo(Record record, MedicalHistory medicalHistory) {
        record.setHospitalId("57b1e211d897cd373ec76dc6");
        record.setBatchNo("bdsz20180328");
        record.setDepartment("风湿免疫科");
        record.setFormat("text");
        record.setDeleted(false);
        record.setSource("门诊-病历文书");
        record.setStatus("AMD识别完成");
        record.setPatientId("bdsz_" + medicalHistory.getPatientId());
        record.setGroupRecordName(medicalHistory.getGroupRecordName());
        record.setSourceId(medicalHistory.getId().toString());
        record.setSourceRecordType(medicalHistory.getMedicalHistoryName());
        record.setCreateTime(currentTimeMillis);
    }

    @Override
    protected void customProcess(Record record, MedicalHistory entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        setRecordType(record, entity);
        record.setOdCategories(new String[]{"风湿"});
    }

    /**
     * Put MedicalHistory data to Record
     *
     * @param medicalHistory
     * @param record
     */
    protected void putText2Record(MedicalHistory medicalHistory, Record record) {
        JSONObject info = record.getInfo();
        //调用提供的方法得到锚点文本
        String medicalContent = medicalHistory.getMedicalContent();
        if (StringUtils.isEmpty(medicalContent)) {
            medicalContent = CommonConstant.EMPTY_FLAG;
            log.error("!!!! 病历内容为空 , id : " + medicalHistory.getId() + "!!!!");
        }

        //去除文本中的空白字符
        BufferedReader br = new BufferedReader(new StringReader(medicalContent));
        String line = null;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                //line = line.trim();
                line = StringUtil.replaceBlank(line, CommonConstant.EMPTY_FLAG);
                if (StringUtils.isEmpty(line) || line.contains("温馨提示")||line.contains("提醒：") || line.contains("就诊时请携带本病历记录") ||
                        line.contains("在综合门诊化验") || line.contains("请您在我院就诊时") || line.contains("祝您早日康复")) {
                    continue;
                }
                stringBuilder.append(line).append("\n");
            }
            medicalContent = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] split = medicalContent.split("\\{");
        //如果字符中包含'{'多余30个则属于模板描述
        if (split != null && split.length > 30) {
            record.setRecordType("其他记录");
            record.setSubRecordType("模板");
        }
        //不打锚点的文本
        info.put(TextFormatter.TEXT_ARS, medicalContent);
        //打锚点
        medicalContent = TextFormatter.formatTextByAnchaor(medicalContent);

        //对入院记录做锚点匹配
        anchorMatch(medicalContent, record);

        //打锚点后文本
        info.put(TextFormatter.TEXT, medicalContent);
    }

    protected void setRecordType(Record record, MedicalHistory medicalHistory) {
        record.setRecordType("门诊记录");
        record.setSubRecordType("门诊");
    }

}