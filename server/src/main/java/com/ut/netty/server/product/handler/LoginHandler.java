package com.ut.netty.server.product.handler;

import com.alibaba.fastjson.JSON;
import com.ut.netty.server.product.common.CommonMemory;
import com.ut.netty.server.product.common.Constants;
import com.ut.netty.server.product.common.message.Message;
import com.ut.netty.server.product.common.message.MessageStorehouse;
import com.ut.netty.server.product.entity.User;
import com.ut.netty.server.product.mapper.UserMapper;
import com.ut.netty.server.product.redis.RedisUtil;
import com.ut.netty.server.product.serializer.SerializerHolder;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/16 11:35
 */
@Component
public class LoginHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);

    @Resource
    private UserMapper userMapper;
    @Autowired
    private RedisUtil redisUtil;

    public void login(Channel channel, Message message) {
        try {
            //result: -1:用户不存在, 0:登录成功,且不需要重连,  1:登录成功,可以重连进入房间
            User user = JSON.parseObject(message.getData().toString(), User.class);
            //第一步:根据用户id查询用户基本信息
            user = userMapper.getUserById(user.getId());
            //用户为空登录失败, 不为空判断是否需要重连
            if (null != user) {
                String userId = user.getId() + "";
                //第二步:判断用户是否已经在其他设备登录
                Channel oldchannel = CommonMemory.getChannel(userId);
                if (oldchannel != null) {
                    CommonMemory.updateChannelUser(userId, channel, oldchannel);
                    message.data(MessageStorehouse.M103());
                    user.sendMsg(message);
                    return;
                } else {
                    //第三步:把用户信息添加到缓存中
                    redisUtil.putObject(Constants.KEY_OF_USER + userId, SerializerHolder.serializerImpl().writeObject(user));
                    //通信通道添加到缓存中去
                    String channelId = channel.id().asShortText();
                    CommonMemory.addChannel(channelId, userId, channel);
                    //第四步:从缓存中判断这个用户是否在某个房间内
                    int rno = redisUtil.getrnoByUserId(user.getId());
                    if (rno != 0) {
                        message.data(MessageStorehouse.M102());
                        user.sendMsg(message);
                    } else {
                        message.data(MessageStorehouse.M100());
                        user.sendMsg(message);
                    }
                    return;
                }
            }
            message.data(MessageStorehouse.M201());
            channel.writeAndFlush(message).syncUninterruptibly();
            return;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
