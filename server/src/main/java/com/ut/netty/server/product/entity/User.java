package com.ut.netty.server.product.entity;

import com.ut.netty.server.product.acceptor.MessageListener;
import com.ut.netty.server.product.common.CommonMemory;
import io.netty.channel.Channel;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/9 14:42
 */
public class User {
    private int id;//id
    private String name; //昵称
    private String img; //头像
    private int state = 1; //状态(0:离线 1:在线不在房间内; 2:在线在等待房间内; 3:在线正在骑行)

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", img='" + img + '\'' +
                ", state=" + state +
                '}';
    }

    public void sendMsg(Object message) {
        try {
            Channel channel = CommonMemory.getChannel(this.id + "");
            if (null != channel) {
                channel.writeAndFlush(message).syncUninterruptibly().addListener(MessageListener.getInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
