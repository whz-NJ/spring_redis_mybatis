package com.migu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.migu.CacheLoadable;
import com.migu.CacheService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author whz
 * @create 2018-07-10 8:51
 * @desc 避免缓存失效模板
 **/
@Component
public class CacheServiceTemplate
{
    private static Logger logger = LogManager.getLogger(CacheService.class);
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *
     * @param key 缓存键值
     * @param expire  缓存失效时间
     * @param timeUnit  缓存失效时间单位
     * @param clazz 缓存的类型
     * @param cacheLoadable 如果缓存失效了，怎么从DB/磁盘获取
     * @param <T>
     * @return
     */
    public <T> T findCache(String key, long expire, TimeUnit timeUnit,
            TypeReference<T> clazz, CacheLoadable<T> cacheLoadable)
    {
        ValueOperations opsForValue = redisTemplate.opsForValue();
        // json 格式数据适合持久化对象，保存到redis
        // 相对jdk序列化/xml，json优势很明显，性能高（相对于protocolBuf有点低），
        // 与平台无关，版本兼容性好（比如增加一个字段，保证可以正常序列化/反序列化），
        // 序列化/反序列化不会抛异常，最多字段值为null
        String json = String.valueOf(opsForValue.get(key));
        if(!StringUtils.isEmpty(json) && (! json.equalsIgnoreCase("null")))
        {
            logger.info("从cache中获取");
            // 从cache里取数据
            return JSON.parseObject(json, clazz);
        }
        synchronized (this)
        {
            json = String.valueOf(opsForValue.get(key));
            if(!StringUtils.isEmpty(json) && (! json.equalsIgnoreCase("null")))
            {
                logger.info("从cache中获取");
                // 从cache里取数据
                return JSON.parseObject(json, clazz);
            }
            logger.info("从db中获取");
            T result = cacheLoadable.load();
            //设置到缓存中（缓存时长10分钟，10分钟后redis就会把它清除）
            opsForValue.set(key, JSON.toJSON(result), expire, timeUnit);
            return result;
        }
    }
}