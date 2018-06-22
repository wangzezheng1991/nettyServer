package com.ut.netty.server.product.common;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/17 13:58
 */
public class MessageConfig {
    /**
     * 普通消息请求
     */
    public static final short REQUEST =1;
    /**
     * 普通消息请求
     */
    public static final short RESPONSE =2;
    /**
     * 登录消息
     */
    public static final short LOGIN =3;
    /**
     * 用户重新连接消息
     */
    public static final short RECONNECT =4;
    /**
     * 用户好友列表
     */
    public static final short FRIENDS =5;
    /**
     * 邀请好友
     */
    public static final short INVITE_FRIENDS=6;
    /**
     * 被邀请好友消息
     */
    public static final short INVITEED_FRIENDS=7;
    /**
     * 登录成功,且不需要重连
     */
    public static final short LOGIN_NO_REC = 100;
    /**
     * 登录失败 用户不存在
     */
    public static final short LOGIN_FAIL = 101;
    /**
     * 登录成功,可以重连进入房间
     */
    public static final short LOGIN_REC = 102;
    /**
     * 断网30秒内自动重新连接
     */
    public static final short LOGIN_RECONNECT = 103;

    /**
     * 操作成功
     */
    public static final short SUCCESS = 200;
    /**
     * 操作失败
     */
    public static final short FAIL= 201;

    /**
     * 用户未登录
     */
    public static final short NO_LOGIN = 500;
    /**
     * 用户还在其他房间内
     */
    public static final short USER_IN_OTHER_ROOM = 501;
    /**
     * 缺少必要参数
     */
    public static final short LACK_REQUEST_PARAM = 502;
    /**
     * 房间号不正确
     */
    public static final short ROOM_NO_NOT_RIGHT = 504;
    /**
     * 被邀请人不在线
     */
    public static final short INVITED_USER_OFFLINE= 505;
    /**
     * 当前用户不在房间内
     */
    public static final short USER_NOT_IN_ROOM= 506;
    /**
     * 被邀请人在其他房间内
     */
    public static final short INVITED_USER_IN_OTHER_ROOM= 507;
    /**
     * 房间人数已满
     */
    public static final short USER_FULL= 508;
    /**
     * 用户不是房主
     */
    public static final short USER_NOT_HOST= 509;
    /**
     * 玩家未准备
     */
    public static final short USER_NOT_READY= 510;
    /**
     * 玩家不足
     */
    public static final short USER_NOT_ENOUGH= 511;
    /**
     * 房间不存在或者房间已结束
     */
    public static final short ROOM_NOT_EXIST= 512;

    /**
     * 个人竞技命令范围
     */
    public static final int PROOM_RANG_START=10000;

    /**
     * 创建个人竞技房间
     */
    public static final int PROOM_CREATE=10001;
    /**
     * 查询个人竞技房间线路
     */
    public static final int PROOM_ROUTES =10002;
    /**
     * 修改个人竞技房间线路
     */
    public static final int PROOM_CHANGE_ROUTE =10003;
    /**
     * 个人竞技房间房主踢出玩家
     */
    public static final int PROOM_KICK =10004;
    /**
     * 个人竞技房间玩家被踢出消息
     */
    public static final int PROOM_KICKED =10005;
    /**
     * 个人竞技房间玩家准备
     */
    public static final int PROOM_READY=10006;
    /**
     * 同步个人竞技房间信息
     */
    public static final int PROOM_SYNC_ROOM=10007;
    /**
     * 个人竞技加入房间
     */
    public static final int PROOM_JOIN=10008;
    /**
     * 个人竞技房间信息
     */
    public static final int PROOM_INFO=10009;
    /**
     * 个人竞技房间房主随机邀请
     */
    public static final int PROOM_RANDOM_INVITE=10010;
    /**
     * 个人竞技房间房主解散房间
     */
    public static final int PROOM_DISBAND =10011;
    /**
     * 个人竞技房间用户退出房间
     */
    public static final int PROOM_EXIT =10012;
    /**
     * 个人竞技房间开始骑行
     */
    public static final int PROOM_START =10013;
    /**
     * 个人竞技房间同步用户数据
     */
    public static final int PROOM_SYNC_PLAYERDATA =10014;
    /**
     * 个人竞技房间更新用户数据
     */
    public static final int PROOM_UPDATE =10015;
    /**
     * 个人竞技房间准备结束
     */
    public static final int PROOM_READY_END =10016;
    /**
     * 个人竞技房间结束
     */
    public static final int PROOM_END =10017;

    /**
     * 个人竞技命令范围结束
     */
    public static final int PROOM_RANG_END=10100;

}
