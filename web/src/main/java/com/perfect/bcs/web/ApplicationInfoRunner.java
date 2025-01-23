package com.perfect.bcs.web;

import ch.qos.logback.classic.Logger;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author liangbo 梁波
 * @date 2020-11-04 18:06
 */
@Slf4j
@Component
@Order(value = 2)
public class ApplicationInfoRunner implements ApplicationRunner, Ordered {

    @Autowired
    Environment environment;

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String serverPort = environment.getProperty("local.server.port");

        BuildProperties buildProperties = applicationContext.getBean(BuildProperties.class);
        Instant instant = buildProperties.getTime();
        String buildTimeStr;
        if (instant != null) {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.ofHours(8)); // 切换到GMT8区时间
            buildTimeStr = LocalDateTimeUtil.format(localDateTime, "yyyy-MM-dd HH:mm:ss");
        } else {
            buildTimeStr = "无法获取Jar包的编译打包时间！！！";
        }

        log.info("================================");
        log.info("================================");
        log.info(StrUtil.format("===  应用启动环境   === {}", ObjectUtil.toString(environment.getActiveProfiles())));
        log.info(StrUtil.format("===  应用启动端口   === {}", serverPort));
        log.info(StrUtil.format("===  Jar编译打包时间=== {}", buildTimeStr));
        log.info("================================");
        log.info("================================");
        log.info("===  Spring_Boot_Success!!!  ===");

        // 判断是否在 jvm 启动参数中明确关闭控制台日志输出
        String closeFlag = System.getProperty("close-console-log-flag");
        if (BooleanUtil.toBoolean(closeFlag)) {
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.detachAppender("console");
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 10000;
    }

}

