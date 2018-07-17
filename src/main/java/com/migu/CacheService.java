package com.migu;

import com.alibaba.fastjson.TypeReference;
import com.migu.dao.Order;
import com.migu.dao.OrderExample;
import com.migu.dao.OrderMapper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author whz
 * @create 2018-07-10 6:30
 * @desc ww
 **/
@Component
public class CacheService
{
    private static Logger logger = LogManager.getLogger(CacheService.class);
    private static final String CACHE_KEY = "zhangfei";
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired CacheServiceTemplate cacheServiceTemplate;

    public /* synchronized */ List<Order> query() {  // synchronized效率差
        ValueOperations opsForValue = redisTemplate.opsForValue();
        // json 格式数据适合持久化对象，保存到redis
        // 相对jdk序列化/xml，json优势很明显，性能高（相对于protocolBuf有点低），
        // 与平台无关，版本兼容性好（比如增加一个字段，保证可以正常序列化/反序列化），
        // 序列化/反序列化不会抛异常，最多字段值为null
        String json = String.valueOf(opsForValue.get(CACHE_KEY));
        if(!StringUtils.isEmpty(json) && (! json.equalsIgnoreCase("null")))
        {
            logger.info("从cache中获取");
            // 从cache里取数据
            return JSON.parseArray(json, Order.class);
        }
        synchronized (this)
        {
            json = String.valueOf(opsForValue.get(CACHE_KEY));
            if(!StringUtils.isEmpty(json) && (! json.equalsIgnoreCase("null")))
            {
                logger.info("从cache中获取");
                // 从cache里取数据
                return JSON.parseArray(json, Order.class);
            }
            logger.info("从db中获取");
            List<Order> list = orderMapper.selectByExample(new OrderExample());  //执行耗时2秒钟（全表扫描，求订单总额/总数，无法优化）
            //设置到缓存中（缓存时长10分钟，10分钟后redis就会把它清除）
            opsForValue.set(CACHE_KEY, JSON.toJSON(list), 10, TimeUnit.MINUTES);
            return list;
        }
    }
    public /* synchronized */ List<Order> query2() {
        return cacheServiceTemplate.findCache(CACHE_KEY, 10, TimeUnit.MINUTES,
                new TypeReference<List<Order>>(){},
                new CacheLoadable<List<Order>>()
                {
                    @Override public List<Order> load()
                    {
                        return orderMapper.selectByExample(new OrderExample());  //耗时2秒
                    }
                });
    }
}
