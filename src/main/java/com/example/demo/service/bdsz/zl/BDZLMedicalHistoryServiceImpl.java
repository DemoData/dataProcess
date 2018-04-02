package com.example.demo.service.bdsz.zl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.constant.CommonConstant;
import com.example.demo.common.support.MappingMatch;
import com.example.demo.common.support.TextFormatter;
import com.example.demo.common.util.StringUtil;
import com.example.demo.config.MongoDataSourceConfig;
import com.example.demo.dao.TextDao;
import com.example.demo.dao.standard.IMedicalHistoryDao;
import com.example.demo.entity.Mapping;
import com.example.demo.entity.MedicalHistory;
import com.example.demo.entity.Record;
import com.example.demo.service.TextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service("bdzlMedicalHistoryService")
public class BDZLMedicalHistoryServiceImpl extends TextService<MedicalHistory> {

    @Autowired
    @Qualifier("bdzlMedicalHistoryDao")
    IMedicalHistoryDao medicalHistoryDao;

    @Autowired
    @Qualifier(MongoDataSourceConfig.HRS_MONGO_TEMPLATE)
    protected MongoTemplate hrsMongoTemplate;

    @Override
    protected TextDao<MedicalHistory> currentDao() {
        return medicalHistoryDao;
    }

    @Override
    protected void customProcess(Record record, MedicalHistory entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {
        //通過mapping set对应的类型
        setRecordType(record, entity);

        String groupRecordName = entity.getGroupRecordName();
        //如果cache中已近存在就不在重复查找
        if (orgOdCatCaches.isEmpty() || StringUtils.isEmpty(orgOdCatCaches.get(groupRecordName))) {
            List<String> orgOdCategories = medicalHistoryDao.findOrgOdCatByGroupRecordName(dataSource, groupRecordName);
            orgOdCatCaches.put(groupRecordName, orgOdCategories);
        }
        record.setOrgOdCategories(orgOdCatCaches.get(groupRecordName).toArray(new String[0]));
        record.setOdCategories(new String[]{"肿瘤"});
    }

    @Override
    protected Map<String, String> getFormattedText(MedicalHistory entity) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        return null;
    }

    @Override
    protected void initRecordBasicInfo(Record record, MedicalHistory medicalHistory) {
        record.setHospitalId("57b1e211d897cd373ec76dc6");
        record.setBatchNo("bdsz20180320");
        record.setDepartment("肿瘤内科");
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
        JSONObject info = record.getInfo();
        //调用提供的方法得到锚点文本
        String medicalContent = medicalHistory.getMedicalContent();
        if (StringUtils.isEmpty(medicalContent)) {
            log.error("!!!! 病历内容为空 , id : " + medicalHistory.getId() + "!!!!");
        }
        // 只获取元素section的内容
        int xmlIndex = medicalContent.indexOf("<section");
        int endIndex = medicalContent.lastIndexOf("</section>");
        if (xmlIndex < 0 || endIndex < 0) {
            log.error("putText2Record(): can not found specific character, medicalHistory id:" + medicalHistory.getId());
        } else {
            medicalContent = medicalContent.substring(xmlIndex, endIndex + 10);
        }
        //移除xml中的元素标签
        medicalContent = medicalContent.replaceAll("<([\\s\\S]+?)>", CommonConstant.EMPTY_FLAG);
        //去除文本中的空白字符
        BufferedReader br = new BufferedReader(new StringReader(medicalContent));
        String line = null;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                //line = line.trim();
                line = StringUtil.replaceBlank(line, CommonConstant.EMPTY_FLAG);
                if (StringUtils.isEmpty(line) || line.contains("注：") || line.contains("温馨提示：")) {
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

    @Override
    protected boolean validateRecord(Record record) {
        Object testARS = record.getInfo().get(TextFormatter.TEXT_ARS);
        //如果文本字符少于20则不入库
        if (StringUtils.isEmpty(testARS.toString()) || testARS.toString().length() < 20) {
            log.info("字符少于20,不做入库处理,id:" + record.getSourceId());
            return false;
        }
        String recordType = record.getRecordType();
        //对于入出院记录，如果字符小于300，则属于其他类型
        if (("入院记录".equals(recordType) || "出院记录".equals(recordType)) && testARS.toString().length() < 300) {
            log.info("字符过小修改为其他类型,id:" + record.getSourceId());
            record.setRecordType("其他记录");
            record.setSubRecordType("其他");
        }
        return true;
    }

    /**
     * 锚点匹配操作，目前只对出入院做处理
     *
     * @param anchorContent
     * @param record
     */
    protected void anchorMatch(String anchorContent, Record record) {
        if (!("入院记录".equals(record.getRecordType()) || "出院记录".equals(record.getRecordType()))) {
            return;
        }
        String[] inHospital = {"现病史", "个人史", "婚育史", "月经史", "家族史"};
//        String[] outHospital = {"治疗经过", "诊疗经过", "出院指导", "出院医嘱", "出院诊断"};

        Pattern pattern = Pattern.compile("【【(.*?)】】");
        Matcher matcher = pattern.matcher(anchorContent);

        Map<String, String> anchors = new HashMap<>();
        while (matcher.find()) {
            String group = matcher.group(1);
            if (group == null) {
                continue;
            }
            group.trim();
            if ("".equals(group)) {
                continue;
            }
            anchors.put(matcher.group(1), null);
        }
        int inCount = 0;
        for (String in : inHospital) {
            if (anchors.containsKey(in)) {
                inCount++;
            }
        }
        if ("入院记录".equals(record.getRecordType())) {
            if (inCount < 2) {
                log.info("匹配锚点个数为：" + inCount + "，修改为出院记录,sourceRecordType:" + record.getSourceRecordType() + ",id:" + record.getSourceId());
                record.setRecordType("出院记录");
                if (anchorContent.contains("死亡时间")) {
                    record.setSubRecordType("死亡记录");
                } else {
                    record.setSubRecordType("出院记录");
                }

            }
        } else {
            if (inCount >= 2) {
                log.info("匹配锚点个数为：" + inCount + "，修改为入院记录,sourceRecordType:" + record.getSourceRecordType() + ",id:" + record.getSourceId());
                record.setRecordType("入院记录");
                record.setSubRecordType("入院记录");
            }
        }

    }

    protected void setRecordType(Record record, MedicalHistory medicalHistory) {
        String recordType = medicalHistory.getMedicalHistoryName();

        if (StringUtils.isEmpty(recordType)) {
            log.error("!!!!!!!!!!!! mapping is empty , id : " + medicalHistory.getId() + "!!!!!!!!!!");
            return;
        }
        List<Mapping> mapping = hrsMongoTemplate.findAll(Mapping.class, "Mapping");

        if (mapping == null || mapping.isEmpty()) {
            MappingMatch.addMappingRule(hrsMongoTemplate);
            mapping = hrsMongoTemplate.findAll(Mapping.class, "Mapping");
        }

        recordType = MappingMatch.getMappedValue(mapping, recordType);
        String[] types = recordType.split("-");
        if (types == null || types.length < 2) {
            log.error("!!!!!!!!!!!! mapping value is invalid , id : " + medicalHistory.getId() + "!!!!!!!!!!");
            return;
        }
        record.setRecordType(types[0]);
        record.setSubRecordType(types[1]);
    }

    @Override
    protected JSONObject bean2Json(Object entity) {
        return (JSONObject) JSONObject.toJSON(entity);
    }

}
