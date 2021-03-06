package com.example.demo.service;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.common.constant.CommonConstant;
import com.example.demo.common.support.TextFormatter;
import com.example.demo.dao.TextDao;
import com.example.demo.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class TextService<T> extends BaseService {


    @Override
    protected void runStart(String dataSource, Integer startPage, Integer endPage) {
        log.info(">>>>>>>>>Starting process from dataSource: " + dataSource);
        int pageNum = startPage;
        boolean isFinish = false;
        Long count = 0L;
        Map<String, List<String>> orgOdCatCaches = new HashMap<>();
        Map<String, String> patientCaches = new HashMap<>();
        while (!isFinish) {
            if (pageNum >= endPage) {
                isFinish = true;
                continue;
            }
            List<T> resultList = currentDao().findRecord(dataSource, startPage, getPageSize());
            if (resultList == null || resultList.isEmpty()) {
                log.info("runStart(): can not found any record");
                break;
            }
            if (resultList != null && resultList.size() < getPageSize()) {
                isFinish = true;
            }

            List<JSONObject> jsonList = new ArrayList<>();
            //遍历record
            for (T entity : resultList) {
                Record record = new Record();
                record.setOdCategories(new String[]{getOdCategory(dataSource)});
                record.setOrgOdCategories(new String[]{CommonConstant.EMPTY_FLAG});

                initRecordBasicInfo(record, entity);

                customProcess(record, entity, orgOdCatCaches, patientCaches, dataSource);

                putText2Record(entity, record);
                //校验Record,不满足则跳过
                if (!validateRecord(record)) {
                    continue;
                }
                //移除id,添加string类型_id
                JSONObject jsonObject = bean2Json(record);
                jsonObject.remove("id");
                jsonObject.put("_id", new ObjectId().toString());
                jsonList.add(jsonObject);
            }
            if (orgOdCatCaches.size() > 50000) {
                orgOdCatCaches.clear();
            }
            if (patientCaches.size() > 50000) {
                patientCaches.clear();
            }
            count += jsonList.size();
            log.info("inserting record total count: " + jsonList.size());
            //把找到的record插入到mongodb hrs record中
            currentDao().batchInsert2HRS(jsonList, "Record");
            pageNum++;
        }
        log.info(">>>>>>>>>>>total inserted records: " + count + " from " + dataSource);
    }

    /**
     * 相关校验规则,用于子类去重写
     *
     * @param record
     * @return
     */
    protected boolean validateRecord(Record record) {
        return true;
    }

    protected abstract TextDao<T> currentDao();

    protected abstract void customProcess(Record record, T entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource);

    @Override
    protected Integer getCount(String dataSource) {
        return currentDao().getCount(dataSource);
    }

    protected void putText2Record(T entity, Record record) {
        JSONObject info = record.getInfo();
        //调用提供的方法得到锚点文本
        Map<String, String> stringMap = null;
        try {
            stringMap = getFormattedText(entity);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        info.put(TextFormatter.TEXT, stringMap.get(TextFormatter.TEXT));
        info.put(TextFormatter.TEXT_ARS, stringMap.get(TextFormatter.TEXT_ARS));
    }

    protected abstract Map<String, String> getFormattedText(T entity) throws IntrospectionException, InvocationTargetException, IllegalAccessException;


    /**
     * Set Record basic info
     *
     * @param record
     */
    protected abstract void initRecordBasicInfo(Record record, T entity);


}