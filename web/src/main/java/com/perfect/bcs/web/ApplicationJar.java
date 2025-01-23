package com.perfect.bcs.web;


import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author liangbo 梁波
 * @since 2016-08-15 16:55
 */
@SpringBootApplication(scanBasePackages = "com.tianda.dds", exclude = DruidDataSourceAutoConfigure.class)
@ServletComponentScan
@EnableCaching
@EnableScheduling
public class ApplicationJar extends SpringBootServletInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationJar.class);

    public static void main(String[] args) {
        // 指定绑定成ipv4
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv4Addresses", "true");

        JacksonTypeHandler.setObjectMapper(new ObjectMapper());

        SpringApplication.run(ApplicationJar.class, args);
    }
}
