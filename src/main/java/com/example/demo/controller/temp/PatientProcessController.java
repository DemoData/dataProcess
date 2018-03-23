package com.example.demo.controller.temp;

import com.example.demo.service.standard.IDataService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author aron
 * @date 2018.02.27
 */
@Slf4j
@RestController
@Api(tags = "Patient数据处理")
public class PatientProcessController {

    @Autowired
    @Qualifier("tempPatientService")
    private IDataService patientService;

    @GetMapping("/processPatientData")
    public boolean processPatientData() {
        try {
            return patientService.processData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
