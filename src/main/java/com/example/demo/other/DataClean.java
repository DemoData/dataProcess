package com.example.demo.other;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.support.MappingMatch;
import com.example.demo.config.MongoDataSourceConfig;
import com.example.demo.entity.Mapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class DataClean {

    @Autowired
    @Qualifier(MongoDataSourceConfig.HRS_MONGO_TEMPLATE)
    protected MongoTemplate hrsMongoTemplate;

    public void cleanData() {
        Query query = new Query();
//        query.with(new PageRequest(pageNum, PAGE_SIZE));
        query.addCriteria(Criteria.where("batchNo").is("shch20180309"));
        query.addCriteria(Criteria.where("recordType").in("入院记录", "出院记录"));
        List<JSONObject> jsonObjects = hrsMongoTemplate.find(query, JSONObject.class, "Record");
        log.info("jsonObjects:" + jsonObjects.size());
        List<Mapping> mapping = hrsMongoTemplate.findAll(Mapping.class, "Mapping");

        if (mapping == null || mapping.isEmpty()) {
            MappingMatch.addMappingRule(hrsMongoTemplate);
            mapping = hrsMongoTemplate.findAll(Mapping.class, "Mapping");
        }

        Long count = 0L;

        for (JSONObject item : jsonObjects) {
            JSONObject info = item.getJSONObject("info");
            String dbRecordType = item.getString("recordType");
            if (info == null) {
                log.error("info is null ,_id: " + item.get("_id"));
                continue;
            }
            String textARS = info.getString("textARS");
            if (textARS == null || "".equals(textARS)) {
                log.error("textARS is null ,_id: " + item.get("_id"));
                continue;
            }
            textARS = textARS.replaceAll("[　*| *| *|\\s*]*", "");

            textARS = textARS.length() > 35 ? textARS.substring(0, 35) : textARS;
            String mappedValue = MappingMatch.getMappedValue(mapping, textARS);

            String[] types = mappedValue.split("-");

            if (dbRecordType.equals(types[0])) {
                continue;
            }
            item.put("recordType", types[0]);
            item.put("subRecordType", types[1]);
            hrsMongoTemplate.save(item, "Record");
            log.info("update,_id:" + item.get("_id"));
            count++;
        }
        /*Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where("_id").in());
        hrsMongoTemplate.updateMulti(updateQuery, , JSONObject.class, "Record");*/
        log.info("Done," + count);

    }

    public void updateRecordTypeByAnchor() {
        Query query = new Query();
        query.addCriteria(Criteria.where("batchNo").is("bdsz20180320"));
        query.addCriteria(Criteria.where("recordType").in("入院记录"));
        List<JSONObject> jsonObjects = hrsMongoTemplate.find(query, JSONObject.class, "Record");
        log.info("jsonObjects:" + jsonObjects.size());

        Long count = 0L;
        for (JSONObject item : jsonObjects) {
            JSONObject info = item.getJSONObject("info");
            if (info == null) {
                log.error("info is null ,_id: " + item.get("_id"));
                continue;
            }
            String text = info.getString("text");
            if (text == null || "".equals(text)) {
                log.error("textARS is null ,_id: " + item.get("_id"));
                continue;
            }

            count = anchorMatch(text, item, count);
        }
        log.info("Done," + count);

    }

    /**
     * 锚点匹配操作，目前只对入院做处理
     *
     * @param anchorContent
     * @param item
     */
    private Long anchorMatch(String anchorContent, JSONObject item, Long count) {
        /*if (!"入院记录".equals(item.getString("recordType"))) {
            return count;
        }*/
        String[] inHospital = {"现病史", "个人史", "婚育史", "月经史", "家族史"};
        //String[] outHospital = {"治疗经过", "诊疗经过", "出院指导", "出院医嘱", "出院诊断"};

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
        int machedCount = 0;
        for (String in : inHospital) {
            if (anchors.containsKey(in)) {
                machedCount++;
            }
        }
        if (machedCount < 2) {
            String sourceRecordType = item.getString("sourceRecordType");
            log.info("匹配锚点个数为：" + machedCount + "，修改为出院记录,sourceRecordType:" + sourceRecordType + ",_id:" + item.getString("_id"));
            item.put("recordType", "出院记录");
            if (anchorContent.contains("死亡时间")) {
                item.put("subRecordType", "死亡记录");
            } else {
                item.put("subRecordType", "出院记录");
            }
            hrsMongoTemplate.save(item, "Record");
            count++;
        }
        return count;
    }

}
