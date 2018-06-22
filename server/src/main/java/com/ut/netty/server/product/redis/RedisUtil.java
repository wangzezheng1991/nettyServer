package com.ut.netty.server.product.redis;

import com.ut.netty.server.product.common.Constants;
import com.ut.netty.server.product.entity.PUser;
import com.ut.netty.server.product.entity.Proom;
import com.ut.netty.server.product.serializer.SerializerHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

/**
 * @Description:
 * @author: Superman
 * @Company: www.km1930.com
 * @Create 2017/7/27 9:26
 */

public class RedisUtil<T> {

    private static Logger logger = LoggerFactory.getLogger(RedisUtil.class);
    private JedisPool jedisPool;
    private Jedis jedis;
    //键值过期时间(1小时)
    private int TIMEOUT = 60 * 60;

    public RedisUtil() {

    }

    public RedisUtil(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.jedis = this.jedisPool.getResource();
//        this.jedis.auth("N/A");
    }

    public void closeJedis() {
        if (this.jedis != null)
            this.jedis.close();
    }

    public synchronized byte[] getObject(Object key) {
        try {
            //redis 操作逻辑
            //采用自定义序列化
            String redisKey = String.valueOf(key);
            return jedis.get(redisKey.getBytes());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
        return null;
    }

    public synchronized void putObject(Object key, byte[] bytes, int timeout) {
        try {
            String redisKey = String.valueOf(key);
            String result = jedis.setex(redisKey.getBytes(), timeout, bytes);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
    }

    public synchronized void putObject(Object key, byte[] bytes) {
        try {
            String redisKey = String.valueOf(key);
            String result = jedis.set(redisKey.getBytes(), bytes);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
    }

    /**
     * 添加字符串
     *
     * @param redisKey
     * @param object
     */
    public synchronized void putString(String redisKey, String object, int timeout) {
        try {
            jedis.setex(redisKey, timeout, object);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
    }

    public synchronized void putString(String redisKey, String object) {
        try {
            jedis.set(redisKey, object);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
    }

    /**
     * 拼接字符串
     *
     * @param key
     * @param object
     */
    public synchronized void appendString(String key, String object) {
        try {
            jedis.append(key, object);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
    }

    /**
     * 删除对象
     *
     * @param key
     */
    public synchronized void delObject(String key) {
        try {
            jedis.del(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 获取字符串
     *
     * @param key
     */
    public synchronized String getString(String key) {
        try {
            return jedis.get(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
        return null;
    }

    /**
     * 添加map
     *
     * @param redisKey
     * @param map
     */
    public synchronized void putMap(String redisKey, Map map) {
        try {
            jedis.hmset(redisKey, map);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
    }

    /**
     * 直接删除map中的key
     *
     * @param redisKey
     * @param mapKey
     */
    public synchronized void delKeyFromMap(String redisKey, String mapKey) {
        try {
            jedis.hdel(redisKey, mapKey);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
    }

    /**
     * 根据key获取map中的value
     *
     * @param redisKey
     * @param mapKey
     * @return value
     */
    public synchronized Object getValueFromMap(String redisKey, String mapKey) {
        try {
            Object object = jedis.hmget(redisKey, mapKey).get(0);
            return object;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
        return null;
    }

    /**
     * 获取map中所有的key
     *
     * @param redisKey
     * @return Set
     */
    public synchronized Set getKeysFromMap(String redisKey) {
        try {
            Set object = jedis.hkeys(redisKey);
            return object;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
        return null;
    }

    /**
     * 获取map中所有的value
     *
     * @param redisKey
     * @return List
     */
    public synchronized List getValuessFromMap(String redisKey) {
        try {
            List object = jedis.hvals(redisKey);
            return object;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
        return null;
    }

    /**
     * 根据Key获取map
     *
     * @param redisKey
     * @return Map
     */
    public synchronized Map<String, String> getMap(String redisKey) {
        try {
            Map<String, String> map = jedis.hgetAll(redisKey);
            return map;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
        return null;
    }

    /**
     * 生成房间号
     *
     * @return
     */
    public synchronized long generatedRoomId() {
        try {
            if (jedis.get("roomId") == null) {
                jedis.set("roomId", "1000");
                return 1000;
            }
            return jedis.incr("roomId");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
        return 0;
    }

    /**
     * 重新命名Key
     *
     * @return
     */
    public synchronized void keyRename(String oldName, String newName) {
        try {
            jedis.rename(oldName, newName);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
    }

    /**
     * 获取所有等待的房间
     *
     * @return
     */
    public synchronized Set<String> getWaitRoom(String patern) {
        try {
            Set<String> keySet = jedis.keys(patern + "*");
            return keySet;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
        return null;
    }

    /**
     * key模糊匹配
     * *: 通配任意多个字符 ?: 通配单个字符 []: 通配括号内的某1个字符
     *
     * @return
     */
    public synchronized Set<String> keys(String patern) {
        try {
            Set<String> keySet = jedis.keys(patern);
            return keySet;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jedis.close();
        }
        return null;
    }

    public synchronized List<PUser> players(LinkedHashSet<Integer> userIds, int rno) {
        List<PUser> players = new LinkedList<>();
        for (int uid : userIds) {
            //采用自定义序列化
            String redisKey = String.valueOf(Constants.playerKey(rno,uid));
            byte[] bytes = jedis.get(redisKey.getBytes());
            PUser pUser = SerializerHolder.serializerImpl().readObject(bytes, PUser.class);
            if (null !=pUser)
                players.add(pUser);
        }
        return players;
    }

    public synchronized int getrnoByUserId(int userId) {
        int rno =0;
        String no = jedis.get(Constants.KEY_OF_USER_ROOM + userId);
        if (StringUtils.isNotBlank(no)) {
            rno = Integer.parseInt(no);
        }
        return rno;
    }

    public synchronized Proom getRoomByuserId(int userId) {
        String no = jedis.get(Constants.KEY_OF_USER_ROOM + userId);
        if (StringUtils.isBlank(no))
            return null;
        int rno = Integer.parseInt(no);
        byte[] bytes = jedis.get((Constants.KEY_OF_ROOM+rno).getBytes());
        Proom proom = SerializerHolder.serializerImpl().readObject(bytes, Proom.class);
        return proom;
    }

}
