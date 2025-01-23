package com.perfect.bcs.web.controller;

import com.perfect.bcs.biz.DataGeneratorService;
import com.perfect.bcs.web.controller.common.CommonController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
@Api(tags = "数据生成器")
@RestController
@RequestMapping(value = "/open/data-generator/", produces = "application/json")
@Slf4j
public class DataGeneratorRestController extends CommonController {

    @Autowired
    private DataGeneratorService dataGeneratorService;

    @ApiOperation(value = "批量随机生成账户及余额数据")
    @ApiImplicitParams({ @ApiImplicitParam(name = "count", value = "每1000条一批，指定多少批", required = true), })
    @GetMapping(value = "mock-account.json")
    public void get(int count) throws Throwable {
        dataGeneratorService.generateAccounts(count);
    }

}