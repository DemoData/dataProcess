package com.example.demo.other;

import com.example.demo.dao.TextDao;
import com.example.demo.dao.standard.IMedicalHistoryDao;
import com.example.demo.entity.MedicalHistory;
import com.example.demo.entity.Record;
import com.example.demo.service.TextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BlobToContent extends TextService<MedicalHistory> {
    @Autowired
    @Qualifier("bdzlMedicalHistoryDao")
    IMedicalHistoryDao medicalHistoryDao;

    @Override
    protected void runStart(String dataSource, Integer startPage, Integer endPage) {
        int pageSize = 5000;
        int pageNum = startPage;
        boolean isFinish = false;
        Long count = 0L;
        while (!isFinish) {
            if (pageNum >= endPage) {
                isFinish = true;
                continue;
            }
            List<MedicalHistory> resultList = medicalHistoryDao.findRecord(dataSource, pageNum, pageSize);
            if (resultList != null && resultList.size() < pageSize) {
                isFinish = true;
            }
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            List<Object[]> params = new ArrayList<>();

            //遍历record
            for (MedicalHistory entity : resultList) {
                Integer id = entity.getId();
                String medicalContent = entity.getMedicalContent();
                Object[] param = {medicalContent, id};
                params.add(param);
            }

            count += params.size();
            log.info("updating record count: " + params.size());
            //把找到的record插入到mongodb hrs record中
            int result = medicalHistoryDao.batchUpdateContent(dataSource, params);
            log.info(">>>>>updated count: " + result);
            pageNum++;
        }
        log.info(">>>>>>>>>>>total updated records: " + count);
    }

    @Override
    protected TextDao<MedicalHistory> currentDao() {
        return medicalHistoryDao;
    }

    @Override
    protected void customProcess(Record record, MedicalHistory entity, Map<String, List<String>> orgOdCatCaches, Map<String, String> patientCaches, String dataSource) {

    }

    @Override
    protected Map<String, String> getFormattedText(MedicalHistory entity) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        return null;
    }

    @Override
    protected void initRecordBasicInfo(Record record, MedicalHistory entity) {

    }
}
