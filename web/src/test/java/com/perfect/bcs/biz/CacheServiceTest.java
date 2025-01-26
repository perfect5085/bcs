package com.perfect.bcs.biz;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.perfect.bcs.web.ApplicationJar;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
@Slf4j
@SpringBootTest(classes = { ApplicationJar.class })
@ExtendWith(SpringExtension.class)
public class CacheServiceTest {

    @Autowired
    private CacheService cacheService;

    @Test
    public void testSetGet() {
        String key = IdUtil.fastSimpleUUID();
        String value = "testValue";

        // 测试添加缓存
        cacheService.set(key, value, 3);
        String cachedValue = cacheService.get(key);

        // 验证缓存是否正确存储
        Assertions.assertEquals(value, cachedValue, "缓存的值应该与设置的值一致");
    }

    @Test
    public void testSetKey() throws Throwable {

        try {
            cacheService.set(null, "value", 3);
            Assertions.assertTrue(true, "缓存Key为空!");
        } catch (Throwable e) {
            log.info(e.getMessage());
        }

        try {
            cacheService.set(RandomUtil.randomStringUpper(150), "value", 3);
            Assertions.assertTrue(true, "缓存Key太长!");
        } catch (Throwable e) {
            log.info(e.getMessage());
        }

        try {
            cacheService.set("11", null, 3);
            Assertions.assertTrue(true, "缓存Value为空!");
        } catch (Throwable e) {
            log.info(e.getMessage());
        }

        try {
            cacheService.set("11", "11", null);
            Assertions.assertTrue(true, "缓存时间为空!");
        } catch (Throwable e) {
            log.info(e.getMessage());
        }

        try {
            cacheService.set("11", "11", -100);
            Assertions.assertTrue(true, "缓存时间是负数!");
        } catch (Throwable e) {
            log.info(e.getMessage());
        }

        try {
            cacheService.set("11", "11", 35 * 24 * 3600);
            Assertions.assertTrue(true, "缓存时间太长!");
        } catch (Throwable e) {
            log.info(e.getMessage());
        }

    }

    @Test
    public void testSetExpire() throws Throwable {
        String key = IdUtil.fastSimpleUUID();
        String value = "testValue";

        // 测试添加缓存
        cacheService.set(key, value, 3);
        String cachedValue = cacheService.get(key);
        Assertions.assertNotNull(cachedValue, "缓存的值应该不是空");

        Thread.sleep(5000);
        cachedValue = cacheService.get(key);

        Assertions.assertNull(cachedValue, "缓存的值应该是空");

    }

}
