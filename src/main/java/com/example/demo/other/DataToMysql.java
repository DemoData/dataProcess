package com.example.demo.other;

import com.example.demo.dao.standard.IMedicalHistoryDao;
import com.example.demo.entity.MedicalHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

@Slf4j
@Service
public class DataToMysql {

    @Autowired
    @Qualifier("bdzlMedicalHistoryDao")
    IMedicalHistoryDao medicalHistoryDao;

    public void process() {
        int pageNum = 1;
        int pageSize = 1000;

        boolean isFinished = false;

        OutputStreamWriter resultWriter = null;
        String RESULT_FILE_PATH = "/Users/aron/record_bdsz.txt";

        try {
            resultWriter = new OutputStreamWriter(new FileOutputStream(RESULT_FILE_PATH), "UTF-8");

            while (!isFinished) {

                List<MedicalHistory> record = medicalHistoryDao.findRecord("blDataSource", pageNum, pageSize);

                if (record.size() < pageSize) {
                    isFinished = true;
                }

                for (MedicalHistory medicalHistory : record) {
                    Integer id = medicalHistory.getId();
                    String pid = medicalHistory.getPatientId();
                    String rid = medicalHistory.getBedNo();
                    String groupRecordName = medicalHistory.getGroupRecordName();
                    String recordType = medicalHistory.getMedicalHistoryName();
                    String userId = medicalHistory.getHospitalizedMode();
                    String content = medicalHistory.getMedicalContent();

//                    resultWriter.write(id + "|" + pid + "|" + rid + "|" + groupRecordName + "|" + recordType + "|" + userId + "|" + content + "||");
                }
                pageNum++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            /*if (resultWriter != null) {
                try {
                    resultWriter.flush();
                    resultWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
        }

    }
}