package com.example.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.config.HRSMongoConfig;
import com.example.demo.config.SDSMongoConfig;
import com.example.demo.util.TimeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@RestController
@Api(tags = "标准化数据")
public class TestController {

    private static Integer count = 0;

    @Autowired
    @Qualifier(HRSMongoConfig.MONGO_TEMPLATE)
    private MongoTemplate primaryMongoTemplate;

    @Autowired
    @Qualifier(SDSMongoConfig.MONGO_TEMPLATE)
    private MongoTemplate secondaryMongoTemplate;

    @GetMapping("/standardData")
    @ApiOperation("标准化数据")
    public void standardData() {
        count = 0;
        Query removeQuery = new Query();
        removeQuery.addCriteria(Criteria.where("batchNo").is("ghyy20180115"));
        removeQuery.addCriteria(Criteria.where("recordType").is("化验记录"));
        removeQuery.addCriteria(Criteria.where("odCategories").is(new String[]{"RA"}));
        secondaryMongoTemplate.remove(removeQuery, "msdata");
        String date = TimeUtil.intToStandardTime(TimeUtil.getCurrentTimeMillis());
        long projectProcessId = TimeUtil.getCurrentTimeMillis();
        int i = 0;
        boolean flag = true;
        while (flag) {
            Query query = new Query();
            query.addCriteria(Criteria.where("batchNo").is("ghyy20180115"));
            query.addCriteria(Criteria.where("recordType").is("化验记录"));
            query.addCriteria(Criteria.where("odCategories").is(new String[]{"RA"}));
            query.with(new PageRequest(i, 2000));
            List<JSONObject> jsonObjects = primaryMongoTemplate.find(query, JSONObject.class, "Record");
            //System.out.println(jsonObjects.size());
            count += jsonObjects.size();
            if (jsonObjects.size() == 0) {
                flag = false;
            } else {
                List<JSONObject> resultList = new ArrayList<JSONObject>();
                for (int m = 0; m < jsonObjects.size(); m++) {
                    JSONObject origin = jsonObjects.get(m);
                    JSONObject result = new JSONObject();
                    result.put("date", date);
                    result.put("batchNo", getValue(origin, "batchNo"));
                    result.put("odCategories", origin.getJSONArray("odCategories"));
                    result.put("patientid", getValue(origin, "patientId"));
                    result.put("recordType", getValue(origin, "recordType"));
                    result.put("SDS_Version", "V1.0.0");
                    Object id = origin.get("_id");
                    if (id instanceof JSONObject) {
                        System.out.println(id);
                        id = new ObjectId(((JSONObject) id).getString("$oid"));
                    }
                    result.put("recordid", id);
                    result.put("hospitalId", getValue(origin, "hospitalId"));
                    result.put("projectProcessId", projectProcessId);
                    result.put("format", getValue(origin, "format"));
                    JSONObject msdata = new JSONObject();
                    result.put("msdata", msdata);
                    JSONArray detailArray = origin.getJSONObject("info").getJSONArray("detailArray");
                    JSONArray ALAArr = new JSONArray();
                    msdata.put("化验", ALAArr);
                    for (int n = 0; n < detailArray.size(); n++) {
                        JSONObject detail = detailArray.getJSONObject(n);
                        JSONObject ALA = new JSONObject();
                        ALAArr.add(ALA);
                        ALA.put("化验结果（定量）", getValue(detail, "定量结果"));
                        ALA.put("化验结果（定性）", getValue(detail, "定性结果"));
                        ALA.put("化验代码", getValue(detail, "化验代码"));
                        ALA.put("化验单位", getValue(detail, "化验单位"));
                        ALA.put("化验名称", getValue(detail, "化验名称"));
                        ALA.put("申请科室", getValue(detail, "申请科室"));
                        ALA.put("仪器编号", getValue(detail, "仪器编号"));
                        ALA.put("化验值状态", getValue(detail, "异常情况"));
                        if (StringUtils.isNotBlank(detail.getString("化验时间"))) {
                            ALA.put("化验时间", detail.getString("化验时间"));
                        } else if (StringUtils.isNotBlank(detail.getString("标本收到日期及时间"))) {
                            ALA.put("化验时间", detail.getString("标本收到日期及时间"));
                        } else if (StringUtils.isNotBlank(detail.getString("标本采样日期及时间"))) {
                            ALA.put("化验时间", detail.getString("标本采样日期及时间"));
                        } else if (StringUtils.isNotBlank(detail.getString("申请日期及时间"))) {
                            ALA.put("化验时间", detail.getString("申请日期及时间"));
                        } else {
                            ALA.put("化验时间", "");
                        }
                    }
                    resultList.add(result);
                }
                secondaryMongoTemplate.insert(resultList, "msdata");
                i++;
            }

        }
        System.out.println("完成");
    }


    @GetMapping("count")
    @ApiOperation("标准化条数")
    public Integer count() {
        return count;
    }

    private String getValue(JSONObject jsonObject, String key) {
        String value = jsonObject.getString(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        } else {
            return "";
        }
    }
}
