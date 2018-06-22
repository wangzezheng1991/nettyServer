package com.ut.netty.server.product.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ut.netty.server.product.common.CommonMemory;
import com.ut.netty.server.product.common.message.Message;
import com.ut.netty.server.product.entity.PUser;
import com.ut.netty.server.product.entity.Proom;
import com.ut.netty.server.product.redis.RedisUtil;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.ut.netty.server.product.common.NettyCommonProtocol.RESPONSE;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/18 14:13
 */
@RestController
public class UserController {
    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping("login")
    private String login(String uid) {
        Channel channel = CommonMemory.getChannel(uid);
        Message message = new Message();
        message.sign(RESPONSE);
        message.setSequence(message.sequenceGenerator.getAndIncrement());
        message.data("本消息来自浏览器");
        channel.writeAndFlush(message).syncUninterruptibly();
//        byte[] bytes = JSON.toJSONBytes(message);
//        channel.alloc().buffer().writeShort(MAGIC)
//                .writeByte(message.sign())
//                .writeByte(0)
//                .writeLong(0)
//                .writeInt(bytes.length)
//                .writeBytes(bytes);
        return channel.id().asShortText();

    }

    @RequestMapping("players")
    @ResponseBody
    private String players(Integer uid) {
        Proom proom = redisUtil.getRoomByuserId(uid);
        List<PUser> players = redisUtil.players(proom.players(), proom.getRno());
        return JSONArray.toJSON(players).toString();
    }

    @RequestMapping("room")
    @ResponseBody
    private String room(Integer uid) {
        Proom proom = redisUtil.getRoomByuserId(uid);
        return JSONObject.toJSON(proom).toString();
    }


}
