package com.ut.netty.server.product.common;

import com.ut.netty.server.product.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/4/17 13:23
 */
@Component
public class Comcomponent {

    @Autowired
    private JedisPool jedisPool;

    /**
     * 获取redisUtil
     */
    @Bean(name = "redisUtil", autowire = Autowire.BY_NAME)
    public RedisUtil redisPoolFactory() {
        try {
            return new RedisUtil(jedisPool);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
