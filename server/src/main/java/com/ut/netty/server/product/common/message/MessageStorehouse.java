package com.ut.netty.server.product.common.message;

import com.alibaba.fastjson.JSONObject;

import static com.ut.netty.server.product.common.MessageConfig.*;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/23 11:07
 */
public final class MessageStorehouse {
    private static JSONObject J100 = null;
    private static JSONObject J102 = null;
    private static JSONObject J103 = null;
    private static JSONObject J200 = null;
    private static JSONObject J201 = null;
    private static JSONObject J500 = null;
    private static JSONObject J501 = null;
    private static JSONObject J502 = null;
    private static JSONObject J504 = null;
    private static JSONObject J505 = null;
    private static JSONObject J506= null;
    private static JSONObject J507 = null;
    private static JSONObject J508 = null;
    private static JSONObject J509 = null;
    private static JSONObject J510 = null;
    private static JSONObject J511 = null;
    private static JSONObject J512 = null;

    public static final MessageStorehouse it = new MessageStorehouse();
    public JSONObject M000(Object data){
        JSONObject J000 = new JSONObject();
        J000.put("code", SUCCESS);
        J000.put("msg", data);
        return J000;
    }
    public static final JSONObject M100(){
        if (J100 ==null){
            J100 = new JSONObject();
        }
        J100.put("code", LOGIN_NO_REC);
        J100.put("msg", "login success and need not reconnection");
        return J100;
    }

    public static final JSONObject M102(){
        if (J102 ==null){
            J102 = new JSONObject();
        }
        J102.put("code", LOGIN_REC);
        J102.put("msg", "login success and need reconnection");
        return J102;
    }

    public static final JSONObject M103(){
        if (J103 ==null){
            J103 = new JSONObject();
        }
        J103.put("code", LOGIN_RECONNECT);
        J103.put("msg", "user reconnect in 30 seconds");
        return J103;
    }
    public static final JSONObject M200(){
        if (J200==null){
            J200 = new JSONObject();
        }
        J200.put("code", SUCCESS);
        J200.put("msg", "operate success");
        return J200;
    }
    public static final JSONObject M201(){
        if (J201==null){
            J201 = new JSONObject();
        }
        J201.put("code", FAIL);
        J201.put("msg", "operate fail");
        return J201;
    }

    public static final JSONObject M500(){
        if (J500==null){
            J500 = new JSONObject();
        }
        J500.put("code", NO_LOGIN);
        J500.put("msg", "user not login");
        return J500;
    }

    public static final JSONObject M501(){
        if (J501==null){
            J501 = new JSONObject();
        }
        J501.put("code", USER_IN_OTHER_ROOM);
        J501.put("msg", "user in other room");
        return J501;
    }

    public static final JSONObject M502(){
        if (J502==null){
            J502 = new JSONObject();
        }
        J502.put("code", LACK_REQUEST_PARAM);
        J502.put("msg", "lack request param");
        return J502;
    }
    public static final JSONObject M504(){
        if (J504==null){
            J504 = new JSONObject();
        }
        J504.put("code", ROOM_NO_NOT_RIGHT);
        J504.put("msg", "room no not right or has started");
        return J504;
    }
    public static final JSONObject M505(){
        if (J505==null){
            J505 = new JSONObject();
        }
        J505.put("code", INVITED_USER_OFFLINE);
        J505.put("msg", "invited user not online");
        return J505;
    }
    public static final JSONObject M506(){
        if (J506==null){
            J506 = new JSONObject();
        }
        J506.put("code", USER_NOT_IN_ROOM);
        J506.put("msg", "user not in room");
        return J506;
    }
    public static final JSONObject M507(){
        if (J507==null){
            J507 = new JSONObject();
        }
        J507.put("code", INVITED_USER_IN_OTHER_ROOM);
        J507.put("msg", "invited user in other room");
        return J507;
    }
    public static final JSONObject M508(){
        if (J508==null){
            J508 = new JSONObject();
        }
        J508.put("code", USER_FULL);
        J508.put("msg", "room player is full");
        return J508;
    }
    public static final JSONObject M509(){
        if (J509==null){
            J509 = new JSONObject();
        }
        J509.put("code", USER_NOT_HOST);
        J509.put("msg", "user not host");
        return J509;
    }
    public static final JSONObject M510(){
        if (J510==null){
            J510 = new JSONObject();
        }
        J510.put("code", USER_NOT_READY);
        J510.put("msg", "players not all ready");
        return J510;
    }
    public static final JSONObject M511(){
        if (J511==null){
            J511 = new JSONObject();
        }
        J511.put("code", USER_NOT_ENOUGH);
        J511.put("msg", "players not enough");
        return J511;
    }
    public static final JSONObject M512(){
        if (J512==null){
            J512 = new JSONObject();
        }
        J512.put("code", ROOM_NOT_EXIST);
        J512.put("msg", "room not exist");
        return J512;
    }

    public Message syncUserData(Object data){
        return new Message(PROOM_SYNC_PLAYERDATA,data);
    }

    public Message syncUserData(int sec, Object data){
        JSONObject J001 = new JSONObject();
        J001.put("sec", sec);
        J001.put("players", data);
        return new Message(PROOM_SYNC_PLAYERDATA,J001);
    }

}
