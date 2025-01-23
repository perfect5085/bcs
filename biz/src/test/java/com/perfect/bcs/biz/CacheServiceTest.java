package com.perfect.bcs.biz;

import static org.junit.Assert.assertEquals;

import com.perfect.bcs.biz.config.ServiceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */

@SpringBootTest(classes = { ServiceConfig.class })
@ExtendWith(SpringExtension.class)
//@Import(ServiceConfig.class)
public class CacheServiceTest {

    //@SpringBootApplication(scanBasePackages = "com.perfect.bcs.biz")
    //static class InnerConfig { }

    //@Mock
    //private RedissonClient redissonClient;
    //
    //@Mock
    //private RBucket<String> rBucket;

    @Autowired
    private CacheService cacheService;

    @Test
    public void testSetSuccess() {
        String key = "47329487jofds232";
        String value = "testValue";
        int expiredSeconds = 3600;

        // 测试添加缓存
        cacheService.set(key, value, 10);
        String cachedValue = cacheService.get(key);

        // 验证缓存是否正确存储
        assertEquals("缓存的值应该与设置的值一致", value, cachedValue);
    }

}
