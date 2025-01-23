package com.perfect.bcs.web.controller;

import com.perfect.bcs.biz.DataGeneratorService;
import com.perfect.bcs.web.controller.common.CommonController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/open/data-generator/", produces = "application/json")
@Slf4j
public class DataGeneratorRestController extends CommonController {

    @Autowired
    private DataGeneratorService dataGeneratorService;

    @GetMapping(value = "mock-account.json")
    public void get(int count) throws Throwable {
        dataGeneratorService.generateAccounts(count);
    }

}