package com.ut.netty.server.product.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ut.netty.server.product.acceptor.MessageListener;
import com.ut.netty.server.product.common.CommonMemory;
import com.ut.netty.server.product.common.Constants;
import com.ut.netty.server.product.common.message.Message;
import com.ut.netty.server.product.common.message.MessageStorehouse;
import com.ut.netty.server.product.entity.PUser;
import com.ut.netty.server.product.entity.Proom;
import com.ut.netty.server.product.entity.User;
import com.ut.netty.server.product.mapper.UserMapper;
import com.ut.netty.server.product.redis.RedisUtil;
import com.ut.netty.server.product.serializer.SerializerHolder;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ut.netty.server.product.common.MessageConfig.INVITEED_FRIENDS;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/20 13:34
 */
@Component
public class CommonHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommonHandler.class);

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserMapper userMapper;

    /**
     * @param channel reconnect 0:不重连,  1:重连进入房间
     */
    public void reconnect(Channel channel, Message message) {
        try {
            Map map = JSON.parseObject(message.getData().toString(), HashMap.class);
            int reconnect = Integer.parseInt(map.get("reconnect").toString());
            String channelId = channel.id().asShortText();
            int userId = CommonMemory.getUserIdByChannelId(channelId);
            //根据房间号获取房间
            Proom proom = redisUtil.getRoomByuserId(userId);
            //根据用户id获取用户
            User user = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_USER + userId), User.class);
            if (null == proom) {
                message.data(MessageStorehouse.M201());
                user.sendMsg(message);
                return;
            }
            synchronized (proom) {
                //0:不重连
                if (reconnect == 0) {
                    //如果房间只有一个人,删除房间
                    if (proom.players().size() == 1)
                        redisUtil.delObject(Constants.KEY_OF_ROOM + proom.getRno());
                    redisUtil.delObject(Constants.KEY_OF_USER_ROOM + user.getId());
                    return;
                } else { //1:重连进入房间
                    int state = 2;
                    int status = 0;
                    if (proom.state() != Constants.ROOM_STATE_READY) {
                        status = 2;
                        state = 3;
                    }
                    user.setState(state);
                    redisUtil.putObject(Constants.KEY_OF_USER + userId, SerializerHolder.serializerImpl().writeObject(user));
                    //更新玩家状态
                    PUser pUser = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.playerKey(proom.getRno(),userId)),
                            PUser.class);
                    pUser.setStatus(status);
                    redisUtil.putObject(Constants.playerKey(proom.getRno(),user.getId()), SerializerHolder.serializerImpl().writeObject(pUser));
                }
            }
            message.data(MessageStorehouse.M200());
            user.sendMsg(message);
            return;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void findFriends(Channel channel, Message message) {
        try {
            //1.校验用户有没有登录
            String channelId = channel.id().asShortText();
            int userId = CommonMemory.getUserIdByChannelId(channelId);
            User user = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_USER + userId), User.class);
            if (null == user) {
                message.setData(MessageStorehouse.M500());
                channel.writeAndFlush(message).syncUninterruptibly().addListener(MessageListener.getInstance());
                return;
            } else {
                List<Map> friends = userMapper.findFriends(userId);
                JSONArray jsonArray = null;
                if (null != friends && friends.size() > 0) {
                    //状态(0:离线 1:在线不在房间内; 2:在线在等待房间内; 3:在线正在骑行)
                    for (int i = 0; i < friends.size(); i++) {
                        Map map = friends.get(i);
                        Integer id = Integer.parseInt(map.get("id").toString());
                        int status = 0;
                        User friend = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_USER + id), User.class);
                        if (null != friend)
                            status = friend.getState();
                        map.put("status", status);
                    }
                    jsonArray = (JSONArray) JSONArray.toJSON(friends);
                }
                message.data(MessageStorehouse.it.M000(jsonArray));
                user.sendMsg(message);
                return;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void inviteFriend(Channel channel, Message message) {
        try {
            String channelId = channel.id().asShortText();
            int userId = CommonMemory.getUserIdByChannelId(channelId);
            User user = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_USER + userId), User.class);
            //1.校验用户有没有登录
            if (null == user) {
                message.setData(MessageStorehouse.M500());
                user.sendMsg(message);
                return;
            }
            //2.校验邀请人是否在房间内
            Proom proom = redisUtil.getRoomByuserId(userId);
            if (null == proom) {
                //根据房间号获取房间
                message.setData(MessageStorehouse.M506());
                user.sendMsg(message);
                return;
            }
            //3.校验必要参数
            JSONObject jsonObject = JSONObject.parseObject(message.data().toString());
            if (null == jsonObject.get("id")) {
                message.setData(MessageStorehouse.M502());
                user.sendMsg(message);
                return;
            }
            //4.校验被邀请人是否在线
            int invitedId = Integer.parseInt(jsonObject.get("id").toString());
            User invitedUser = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_USER + invitedId), User.class);
            if (null == invitedUser) {
                message.setData(MessageStorehouse.M505());
                user.sendMsg(message);
                return;
            }
            if (invitedUser.getState() == 2 || invitedUser.getState() == 3) {
                message.setData(MessageStorehouse.M507());
                user.sendMsg(message);
                return;
            }
            if (invitedUser.getState() == 1) {
                //给被邀请人发送消息
                JSONObject json = (JSONObject) JSONObject.toJSON(user);
                json.put("rno", proom.getRno());
                Message invitedMessage = new Message(INVITEED_FRIENDS, MessageStorehouse.it.M000(json));
                invitedUser.sendMsg(invitedMessage);

                //给邀请人回复消息
                message.setData(MessageStorehouse.M200());
                user.sendMsg(message);
                return;
            }
            message.setData(MessageStorehouse.M201());
            user.sendMsg(message);
            return;
        } catch (Exception e){
            e.printStackTrace();
        }

    }

}
