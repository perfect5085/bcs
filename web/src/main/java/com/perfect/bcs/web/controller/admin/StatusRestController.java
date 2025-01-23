package com.perfect.bcs.web.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liangbo 梁波
 * @since 2016-08-15 17:15
 */
@RestController
@RequestMapping(value = "/open/status/", produces = "application/json")
@Slf4j
public class StatusRestController {

    /**
     * 判断系统是否正常
     *
     * @return
     */
    @GetMapping(value = "ok.json")
    public String ok() {

        return "ok";
    }

}
