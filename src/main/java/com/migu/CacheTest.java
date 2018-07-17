package com.migu;

import com.migu.dao.Order;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author whz
 * @create 2018-07-10 6:55
 * @desc ww
 **/
public class CacheTest extends TestSupport
{
    private static final Logger logger = LogManager.getLogger(CacheTest.class.getName());
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RedisTemplate redisTemplate;
    private static final String CACHE_KEY = "zhangfei";
    private CountDownLatch latch = new CountDownLatch(10);

    @Before
    public void init()
    {
        redisTemplate.delete(CACHE_KEY);
    }

    @Test
    public void testCache()
    {
        List<Order> query = cacheService.query();
        logger.info(query.toString());

        List<Order> query2 = cacheService.query();
        logger.info(query2.toString());
    }
    private class QueryTask implements Runnable
    {

        @Override public void run()
        {
            try
            {
                latch.await();  //等待10个线程都创建好了
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            List<Order> query = cacheService.query();
            logger.info(query.toString());
        }
    }

    @Test
    public void testCache2() throws InterruptedException
    {
        for(int i = 1; i<=10; i++)
        {
            new Thread(new QueryTask()).start();
            latch.countDown();
        }
        List<Order> query = cacheService.query();
        logger.info(query.toString());
        Thread.currentThread().sleep(5000);
    }
}