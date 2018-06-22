package com.ut.netty.server.product.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Description: Redis缓存配置
 * @author: Superman
 * @Company: www.km1930.com
 * @Create 2017/7/26 17:16
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

    //Redis服务器IP
    @Value("${spring.redis.host}")
    private String IP;
    //Redis的端口号
    @Value("${spring.redis.port}")
    private int PORT;
    //访问密码
//    @Value("${spring.redis.password}")
//    private String AUTH;
    //可用连接实例的最大数目，默认值为8；
    //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
    @Value("${spring.redis.pool.max-active:8}")
    private int MAX_ACTIVE;
    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    @Value("${spring.redis.pool.max-idle:8}")
    private int MAX_IDLE;
    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    @Value("${spring.redis.pool.max-wait:-1}")
    private int MAX_WAIT;
    @Value("${spring.redis.timeout:0}")
    private int TIMEOUT;
    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    @Value("${spring.redis.pool.test-on-borrow:true}")
    private boolean TEST_ON_BORROW;

    private Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    /**
     * 初始化Redis连接池
     */
    @Bean(name = "jedisPool", autowire = Autowire.BY_NAME)
    public JedisPool redisPoolFactory() {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(MAX_IDLE);
            config.setMaxTotal(MAX_ACTIVE);
            config.setMaxWaitMillis(MAX_WAIT);
            config.setTestOnBorrow(TEST_ON_BORROW);
            JedisPool jedisPool = new JedisPool(config, IP, PORT, TIMEOUT);
//            JedisPool jedisPool = new JedisPool(config, IP, PORT, TIMEOUT, AUTH);
            return jedisPool;
        } catch (Exception e) {
            logger.error("连接池创建失败", e);
        }
        return null;
    }
}
