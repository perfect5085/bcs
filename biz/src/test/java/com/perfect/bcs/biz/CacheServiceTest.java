package com.perfect.bcs.biz;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */

//@SpringBootTest(classes = { ServiceConfig.class })
//@ExtendWith(SpringExtension.class)
//@Import(ServiceConfig.class)
public class CacheServiceTest {

    //@SpringBootApplication(scanBasePackages = "com.perfect.bcs.biz")
    //static class InnerConfig { }

    //@Mock
    //private RedissonClient redissonClient;
    //
    //@Mock
    //private RBucket<String> rBucket;

    @Mock
    private CacheService    cacheService;
    @Mock
    private RedissonClient  redissonClient;
    @Mock
    private RBucket<String> rBucket;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // 初始化 Mockito 注解
    }

    @Test
    public void testSetSuccess() {
        String key = "47329487jofds232";
        String value = "testValue";
        int expiredSeconds = 3600;

        // 模拟 RedissonClient.getBucket 方法返回 Mock 的 RBucket
        when(redissonClient.<String> getBucket(eq(key), any(StringCodec.class))).thenReturn(rBucket);


        // 测试添加缓存
        cacheService.set(key, value, 10);

        when(rBucket.get()).thenReturn(value);
        String cachedValue = cacheService.get(key);

        // 验证缓存是否正确存储
        Assertions.assertEquals(value, cachedValue, "缓存的值应该与设置的值一致");
    }

}
