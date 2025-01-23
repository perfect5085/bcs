package com.perfect.bcs.biz;

import cn.hutool.core.util.StrUtil;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liangbo 梁波
 * @date 2025-01-22 22:37
 */
@Service
@Slf4j
public class CacheService {

    @Autowired
    private RedissonClient redissonClient;

    private static final int CACHE_MAX_SECOND = 30 * 24 * 3600;

    /**
     * 添加缓存项
     *
     * @param expiredSeconds 缓存的秒数,最长可缓存30天
     */
    public void set(String key, String value, Integer expiredSeconds) {
        if (!checkKey(key)) {
            throw new RuntimeException("key 不满足要求");
        }

        if (null == value) {
            String msg = StrUtil.format("error_cache_empty_value_for_key={}_value=null", key);
            throw new RuntimeException(msg);
        }

        if (null == expiredSeconds || expiredSeconds <= 0 || expiredSeconds > CACHE_MAX_SECOND) {
            String msg = StrUtil.format("error_cacheWrongExpiredSeconds_key={}_expiredSeconds={}", key,
                                        expiredSeconds);
            throw new RuntimeException(msg);
        }

        RBucket<String> bucket = redissonClient.getBucket(key, new StringCodec());
        bucket.set(value, expiredSeconds, TimeUnit.SECONDS);
    }

    /**
     * 取出缓存项
     */
    public String get(String key) {
        if (!checkKey(key)) {
            return null;
        }

        RBucket<String> bucket = redissonClient.getBucket(key, new StringCodec());
        return bucket.get();
    }

    /**
     * <pre>
     *     检测事项如下:
     *     1, key不能为空
     *     2, key的长度不能超过128字段
     * </pre>
     */
    private boolean checkKey(String key) {
        if (StringUtils.isBlank(key)) {
            log.error("error_cache_key={}", key);
            return false;
        }

        try {
            byte[] keyByte = key.getBytes("UTF-8");
            // key的长度不能超过128字节, 太长了,效率太低
            if (keyByte.length > 128) {
                log.error("error_cache_key={}_key_length > 128", key);
                return false;
            }
        } catch (Throwable e) {
            String msg = MessageFormatter.format("error_getByte_for_key={}", key)
                                         .getMessage();
            log.error(msg, e);
            return false;
        }

        return true;
    }

}
