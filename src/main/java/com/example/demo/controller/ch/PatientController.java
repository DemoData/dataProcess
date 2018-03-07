package com.example.demo.controller.ch;

import com.example.demo.service.IDataService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author aron
 * @date 2018.02.27
 */
@RestController
@Api(tags = "长海医院Patient数据处理")
@RequestMapping("/chyx")
public class PatientController {

    @Autowired
    @Qualifier("chyxPatientService")
    private IDataService patientService;

    @GetMapping("/processPatient")
    public boolean processPatient() {
        try {
            return patientService.processData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
