package com.ut.netty.server.product.common;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/17 11:44
 */
public class Constants {
    /**
     * redis key 用户
     */
    public static String KEY_OF_USER = "USER_";
    /**
     * redis key 玩家
     */
//    public static String KEY_OF_PLAYER = "PLAYER_";
    /**
     * redis room房间
     */
    public static String KEY_OF_ROOM = "ROOM_";
    /**
     * redis key 用户与房间
     */
    public static String KEY_OF_USER_ROOM = "USER_ROOM_";

    /**
     * 个人竞技房间类型
     */
    public static final short ROOM_OF_TYPE_PERSONAL = 100;

    //房间状态 0:准备   1:正在骑行   2:结束
    public static final byte ROOM_STATE_READY = 0;
    public static final byte ROOM_STATE_RIDING = 1;
    public static final byte ROOM_STATE_READY_END = 2;
    public static final byte ROOM_STATE_END = 3;

    public static String playerKey(int rno, int uid) {
            return "PLAYER_"+rno+"_"+uid;
    }
}
