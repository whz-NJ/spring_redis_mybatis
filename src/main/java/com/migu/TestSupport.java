package com.migu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * @author whz
 * @create 2018-07-10 6:53
 * @desc ww
 **/
@ContextConfiguration(locations = ("classpath*:spring/applicationContext.xml"))
@MapperScan("com.winter.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestSupport extends AbstractJUnit4SpringContextTests
{

}