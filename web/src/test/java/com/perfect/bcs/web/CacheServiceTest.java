package com.perfect.bcs.web;

import cn.hutool.core.util.RandomUtil;
import com.perfect.bcs.biz.CacheService;
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

@SpringBootTest(classes = { ApplicationJar.class })
@ExtendWith(SpringExtension.class)
public class CacheServiceTest {

    @Autowired
    private CacheService cacheService;

    @Test
    public void testSetGet() {
        String key = "test-47329487jofds232";
        String value = "testValue";

        // 测试添加缓存
        cacheService.set(key, value, 3);
        String cachedValue = cacheService.get(key);

        // 验证缓存是否正确存储
        Assertions.assertEquals("缓存的值应该与设置的值一致", value, cachedValue);
    }

    @Test
    public void testSetExpire() throws Throwable {
        String key = "test-47329487jofds232";
        String value = "testValue";

        // 测试添加缓存
        cacheService.set(key, value, 3);
        String cachedValue = cacheService.get(key);
        Assertions.assertNotNull(cachedValue, "缓存的值应该不是空");

        Thread.sleep(5000);
        cachedValue = cacheService.get(key);

        Assertions.assertNull(cachedValue, "缓存的值应该是空");

    }

    @Test
    public void testSetKey() throws Throwable {
        String key = RandomUtil.randomStringUpper(130);
        String value = "testValue";

        // 测试添加缓存
        try {
            cacheService.set(key, value, 3);
        } catch (Throwable e) {
            Assertions.assertTrue(true, "缓存Key太长，无法存入，符合要求。");
            return;
        }

        Assertions.assertTrue(false, "缓存Key太长，但是还是放进了缓存！！！");
    }

}
