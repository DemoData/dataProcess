package com.example.demo.controller;

import com.example.demo.service.IDataService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author aron
 * @date 2018.02.27
 */
@RestController
@Api(tags = "Patient数据处理")
public class PatientProcessController {

    @Autowired
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
