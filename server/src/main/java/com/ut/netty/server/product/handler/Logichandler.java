package com.ut.netty.server.product.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ut.netty.server.product.common.CommonMemory;
import com.ut.netty.server.product.common.Constants;
import com.ut.netty.server.product.common.message.Message;
import com.ut.netty.server.product.common.message.MessageStorehouse;
import com.ut.netty.server.product.entity.PUser;
import com.ut.netty.server.product.entity.Proom;
import com.ut.netty.server.product.entity.User;
import com.ut.netty.server.product.mapper.RouteMapper;
import com.ut.netty.server.product.mapper.UserMapper;
import com.ut.netty.server.product.redis.RedisUtil;
import com.ut.netty.server.product.serializer.SerializerHolder;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

import static com.ut.netty.server.product.common.MessageConfig.*;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/17 13:51
 */
@Component
public class Logichandler {
    private static Logger logger = LoggerFactory.getLogger(Logichandler.class);
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RouteMapper routeMapper;
    @Autowired
    private UserMapper userMapper;
    //定时任务
    private final ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(5);
    //倒计时
    private ConcurrentHashMap<String, Integer> countdownMap = new ConcurrentHashMap<>();

    public void hander(Channel channel, Message message){
        try {
            String channelId = channel.id().asShortText();
            int userId = CommonMemory.getUserIdByChannelId(channelId);
            User user = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_USER + userId), User.class);
            if (check(channel, message, user)) {
                switch (message.sign()) {
                    case PROOM_CREATE:
                        createRoom(channel, user, message);
                        break;
                    case PROOM_ROUTES:
                        findRoomRoutes(channel, user, message);
                        break;
                    case PROOM_CHANGE_ROUTE:
                        changeRoute(channel, user, message);
                        break;
                    case PROOM_JOIN:
                        joinRoom(channel, user, message);
                        break;
                    case PROOM_INFO:
                        getRoomInfo(channel, user, message);
                        break;
                    case PROOM_KICK:
                        kickPlayer(channel, user, message);
                        break;
                    case PROOM_READY:
                        ready(channel, user, message);
                        break;
                    case PROOM_DISBAND:
                        disband(channel, user, message);
                        break;
                    case PROOM_RANDOM_INVITE:
                        randomInvite(channel, user, message);
                        break;
                    case PROOM_EXIT:
                        exit(channel, user, message);
                        break;
                    case PROOM_START:
                        start(channel, user, message);
                        break;
                    case PROOM_UPDATE:
                        update(channel, user, message);
                        break;
                    case PROOM_READY_END:
                        readyEnd(channel, user, message);
                        break;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readyEnd(Channel channel, User user, Message message) throws Exception {
        Proom proom = redisUtil.getRoomByuserId(user.getId());
        if (null !=proom){
            synchronized (proom){
                if (proom.state() ==Constants.ROOM_STATE_RIDING){
                    //1.房间状态置为结束
                    proom.state(Constants.ROOM_STATE_READY_END);
                    //2.更新房间信息
                    redisUtil.putObject(Constants.KEY_OF_ROOM + proom.getRno(), SerializerHolder.serializerImpl().writeObject(proom));
                    countdownMap.put(proom.getRno()+"", 20);
                }
            }
        }
    }

    private void end(int rno)  throws Exception{
        Proom proom = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_ROOM + rno), Proom.class);
        if (null !=proom){
            synchronized (proom){
                //1.房间状态置为结束
                proom.state(Constants.ROOM_STATE_END);
                //2.更新房间信息
                redisUtil.putObject(Constants.KEY_OF_ROOM + proom.getRno(), SerializerHolder.serializerImpl().writeObject(proom));
                //3.发送结果报表数据
                Message message = new Message(PROOM_END, report(redisUtil.players(proom.players(), proom.getRno())));
                for ( Integer uid : proom.players()){
                    PUser pUser = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.playerKey(proom.getRno(),uid)), PUser
                            .class);
                    if (pUser.getStatus()==2){
                        pUser.sendMsg(message);
                    }
                }
                //4.更新用户状态
                for ( Integer uid : proom.players()){
                    PUser pUser = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.playerKey(proom.getRno(),uid)), PUser
                            .class);
                    if (null !=pUser){
                        redisUtil.delObject(Constants.playerKey(proom.getRno(),uid));
                        int no = redisUtil.getrnoByUserId(uid);
                        if (no ==rno){
                            redisUtil.delObject(Constants.KEY_OF_USER_ROOM+uid);
                        }
                        User u = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_USER + pUser.getId()), User.class);
                        if (null !=u){
                            u.setState(1);
                            //更新用户的信息
                            redisUtil.putObject(Constants.KEY_OF_USER + u.getId(), SerializerHolder.serializerImpl().writeObject(u));
                        }
                    }
                    System.out.println("用户骑行数据:userId= "+uid+"  数据==>>"+CommonMemory.readRideData(uid));
                }
                //5.删除房间
                redisUtil.delObject(Constants.KEY_OF_ROOM + proom.getRno());
                //6.保存到数据库
            }
        }
    }

    private void update(Channel channel, User user, Message message) throws Exception{
        Proom proom = redisUtil.getRoomByuserId(user.getId());
        if (null !=proom && (proom.state() == Constants.ROOM_STATE_RIDING || proom.state() == Constants.ROOM_STATE_READY_END)){
            PUser player = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.playerKey(proom.getRno(),user.getId())),
                    PUser.class);
            player.update(message.data());
            //更新玩家的信息
            redisUtil.putObject(Constants.playerKey(proom.getRno(),user.getId()), SerializerHolder.serializerImpl().writeObject(player));
            return;
        }else {
            message.data(MessageStorehouse.M512());
            user.sendMsg(message);
            return;
        }
    }

    private void start(Channel channel, User user, Message message) throws Exception {
        //1.校验是否是房主
        Proom proom = checkHost(message, user);
        if (null == proom)
            return;
        synchronized (proom) {
            //校验是否大于2个人
            if (proom.players().size() <2){
                message.data(MessageStorehouse.M511());
                user.sendMsg(message);
                return;
            }
            //2.校验是否都已准备
            for (Integer uid : proom.players()){
                PUser pUser = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.playerKey(proom.getRno(),uid)),PUser.class);
                if (pUser.getStatus() !=1){
                    //有玩家未准备
                    message.data(MessageStorehouse.M510());
                    user.sendMsg(message);
                    return;
                }
            }
            //3.修改用户状态
            for (Integer uid : proom.players()){
                User u = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_USER + uid), User.class);
                PUser pUser = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.playerKey(proom.getRno(),uid)),PUser.class);
                if (u.getState() ==2){
                    u.setState(3);
                    //更新用户的信息
                    redisUtil.putObject(Constants.KEY_OF_USER + user.getId(), SerializerHolder.serializerImpl().writeObject(user));
                }
                pUser.setStatus(2);
                //更新玩家的信息
                redisUtil.putObject(Constants.playerKey(proom.getRno(),uid), SerializerHolder.serializerImpl().writeObject(pUser));
            }
            proom.state(Constants.ROOM_STATE_RIDING);

            //4.通知玩家骑行开始
            for (Integer uid : proom.players()){
                JSONObject jsonObject = (JSONObject) JSONObject.toJSON(proom);
                jsonObject.put("players", JSONArray.toJSON(redisUtil.players(proom.players(), proom.getRno())));
                message.data(MessageStorehouse.it.M000(jsonObject));
                User u = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_USER + uid), User.class);
                u.sendMsg(message);
                CommonMemory.tempFile(uid);
            }
            proom.updateStartTime();
            //更新房间信息
            redisUtil.putObject(Constants.KEY_OF_ROOM + proom.getRno(), SerializerHolder.serializerImpl().writeObject(proom));
        }
        //3秒后开始同步用户数据
        sync(proom.getRno());
    }

    private void exit(Channel channel, User user, Message message) throws Exception {
        //根据用户id查询房间
        Proom proom = redisUtil.getRoomByuserId(user.getId());
        if (null != proom) {
            //如果房间只有一个人,直接解散房间
            if (proom.players().size() <= 1) {
                redisUtil.delObject(Constants.KEY_OF_ROOM + proom.getRno());
                //删除用户与房间的关联
                redisUtil.delObject(Constants.playerKey(proom.getRno(),user.getId()));
                CommonMemory.cancelchedule(proom.getRno()+"");
            } else {
                synchronized (proom) {
                    PUser pUser = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.playerKey(proom.getRno(),user.getId())),
                            PUser.class);
                    if (proom.state() == Constants.ROOM_STATE_RIDING || proom.state() == Constants.ROOM_STATE_READY_END) {
                        //如果房间已经开始,把用户状态置为空闲,玩家状态置为掉线
                        pUser.setStatus(-1);
                        //更新玩家信息
                        redisUtil.putObject(Constants.playerKey(proom.getRno(),user.getId()), SerializerHolder.serializerImpl().writeObject(pUser));
                    } else {
                        //如果房间未开始,直接踢出该玩家
                        proom.removePlayer(user.getId());
                        //如果是房主掉线的话,需要把房主转到下一个人
                        if (user.getId() == proom.getHostId()){
                            Integer next = proom.players().iterator().next();
                            proom.setHostId(next);
                            PUser p = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.playerKey(proom.getRno(),next)),
                                    PUser.class);
                            p.setStatus(1);
                            //更新玩家信息
                            redisUtil.putObject(Constants.playerKey(proom.getRno(),next), SerializerHolder.serializerImpl().writeObject(p));
                            //更新房间信息
                            redisUtil.putObject(Constants.KEY_OF_ROOM + proom.getRno(), SerializerHolder.serializerImpl().writeObject(proom));
                        }
                        //删除用户与房间的关联
                        redisUtil.delObject(Constants.playerKey(proom.getRno(), user.getId()));
                        syncRoom(proom);
                    }
                }
            }
            user.setState(1);
            //更新用户的信息
            redisUtil.putObject(Constants.KEY_OF_USER + user.getId(), SerializerHolder.serializerImpl().writeObject(user));
            int no = redisUtil.getrnoByUserId(user.getId());
            if (no ==proom.getRno()){
                redisUtil.delObject(Constants.KEY_OF_USER_ROOM+user.getId());
            }
            return;
        }
    }

    private void randomInvite(Channel channel, User user, Message message) throws Exception {
        //1.校验是否是房主
        Proom proom = checkHost(message, user);
        if (null == proom)
            return;
        synchronized (proom) {
            //查询房主所有好友
            List<Integer> hostFriends = userMapper.findHostFriends(user.getId());
            //发送邀请消息
            JSONObject json = (JSONObject) JSONObject.toJSON(user);
            json.put("rno", proom.getRno());
            Message invitedMessage = new Message(INVITEED_FRIENDS, MessageStorehouse.it.M000(json));
            //随机邀请人数
            int invitedCount = ThreadLocalRandom.current().nextInt(1, (5 - proom.players().size()) + 1);
            int i = 0;
            for (Integer fid : hostFriends) {
                User u = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_USER + fid), User.class);
                if (null != u && u.getState() == 1) {
                    //给被邀请人发送消息
                    u.sendMsg(invitedMessage);
                    if (invitedCount == ++i)
                        return;
                }
            }
        }
    }

    private void disband(Channel channel, User user, Message message) throws Exception {
        //1.校验是否是房主
        Proom proom = checkHost(message, user);
        if (null == proom)
            return;
        synchronized (proom) {
            //删除房间
            redisUtil.delObject(Constants.KEY_OF_ROOM + proom.getRno());
            for (Integer uid : proom.players()) {
                //修改用户状态
                User u = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_USER + uid), User.class);
                u.setState(1);
                redisUtil.putObject(Constants.KEY_OF_USER + u.getId(), SerializerHolder.serializerImpl().writeObject(u));
                //删除用户与房间的关联
                redisUtil.delObject(Constants.playerKey(proom.getRno(),u.getId()));
                int no = redisUtil.getrnoByUserId(uid);
                if (no ==proom.getRno()){
                    redisUtil.delObject(Constants.KEY_OF_USER_ROOM + u.getId());
                }
                //通知玩家房间已被解散
                message.data(MessageStorehouse.M200());
                u.sendMsg(message);
                CommonMemory.cancelchedule(proom.getRno()+"");
            }
        }
        proom = null;
        return;
    }

    private void ready(Channel channel, User user, Message message) throws Exception {
        //1.校验必要参数
        JSONObject jsonObject = JSONObject.parseObject(message.data().toString());
        if (null == jsonObject.get("status")) {
            message.setData(MessageStorehouse.M502());
            user.sendMsg(message);
            return;
        }
        // state: 0:取消准备 1:准备
        int status = Integer.parseInt(jsonObject.get("status").toString());
        Proom proom = redisUtil.getRoomByuserId(user.getId());
        if (null == proom) {
            message.setData(MessageStorehouse.M506());
            user.sendMsg(message);
            return;
        }
        synchronized (proom) {
            PUser pUser = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.playerKey(proom.getRno(),user.getId())), PUser
                    .class);
            pUser.setStatus(status);
            //更新玩家信息
            redisUtil.putObject(Constants.playerKey(proom.getRno(),pUser.getId()), SerializerHolder.serializerImpl().writeObject(pUser));
        }
        //同步房间信息
        syncRoom(proom);
        return;
    }

    private void kickPlayer(Channel channel, User user, Message message) throws Exception {
        //1.校验必要参数
        JSONObject jsonObject = JSONObject.parseObject(message.data().toString());
        if (null == jsonObject.get("id")) {
            message.setData(MessageStorehouse.M502());
            user.sendMsg(message);
            return;
        }
        //2.校验是否是房主
        Proom proom = checkHost(message, user);
        if (null == proom)
            return;
        //被踢用户id
        int kickedId = Integer.parseInt(jsonObject.get("id").toString());
        //3.踢出用户
        synchronized (proom) {
            //踢掉玩家
            proom.removePlayer(kickedId);
            //用户状态置为空闲状态
            User kickedUser = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_USER + kickedId), User.class);
            kickedUser.setState(1);
            //删掉用户与房间的关联
            redisUtil.delObject(Constants.playerKey(proom.getRno(),kickedId));
            int no = redisUtil.getrnoByUserId(kickedId);
            if (no ==proom.getRno()){
                redisUtil.delObject(Constants.KEY_OF_USER_ROOM + kickedId);
            }
            //更新房间信息
            redisUtil.putObject(Constants.KEY_OF_ROOM + proom.getRno(), SerializerHolder.serializerImpl().writeObject(proom));
            //更新被踢用户信息
            redisUtil.putObject(Constants.KEY_OF_USER + kickedId, SerializerHolder.serializerImpl().writeObject(kickedUser));
            //给被踢用户发送消息
            message.sign(PROOM_KICKED);
            message.data("");
            kickedUser.sendMsg(message);
        }
        //同步房间消息
        syncRoom(proom);
        return;
    }

    private void getRoomInfo(Channel channel, User user, Message message) throws Exception {
        Proom proom = redisUtil.getRoomByuserId(user.getId());
        if (null != proom) {
            synchronized (proom) {
                JSONObject jsonObject = (JSONObject) JSONObject.toJSON(proom);
                jsonObject.put("players", JSONArray.toJSON(redisUtil.players(proom.players(), proom.getRno())));
                message.data(jsonObject);
            }
            user.sendMsg(message);
        }
        return;
    }

    private void joinRoom(Channel channel, User user, Message message) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(message.data().toString());
        //1.校验参数
        if (null == jsonObject.get("rno")) {
            message.setData(MessageStorehouse.M502());
            user.sendMsg(message);
            return;
        }
        //2.校验房间是否存在
        int rno = Integer.parseInt(jsonObject.get("rno").toString());
        Proom proom = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_ROOM + rno), Proom.class);
        if (null == proom || proom.state() != Constants.ROOM_STATE_READY) {
            message.setData(MessageStorehouse.M504());
            user.sendMsg(message);
            return;
        }
        //3.校验房间是否已满
        if (proom.players().size() >= 5) {
            message.setData(MessageStorehouse.M508());
            user.sendMsg(message);
            return;
        }

        synchronized (proom) {
            user.setState(2);
            PUser pUser = new PUser(user);
            proom.addPlayer(user.getId());
            redisUtil.putObject(Constants.KEY_OF_USER + user.getId(), SerializerHolder.serializerImpl().writeObject(user));
            redisUtil.putObject(Constants.playerKey(proom.getRno(),user.getId()), SerializerHolder.serializerImpl().writeObject(pUser));
            redisUtil.putObject(Constants.KEY_OF_ROOM + rno, SerializerHolder.serializerImpl().writeObject(proom));
            redisUtil.putString(Constants.KEY_OF_USER_ROOM + user.getId(), rno+"");
            message.setData(MessageStorehouse.M200());
            user.sendMsg(message);
            syncRoom(proom);
        }
        return;
    }

    private void changeRoute(Channel channel, User user, Message message) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(message.data().toString());
        //1.校验参数
        if (null == jsonObject.get("routeId")) {
            message.setData(MessageStorehouse.M502());
            user.sendMsg(message);
            return;
        }
        //2.校验是否是房主
        Proom proom = checkHost(message, user);
        if (null == proom)
            return;
        synchronized (proom) {
            //根据线路id查询线路
            int routeId = jsonObject.getIntValue("routeId");
            Map<String, Object> route = routeMapper.findRouteById(1, routeId);
            proom.setRouteId(Integer.parseInt(route.get("routeId").toString()));
            proom.setRouteName(route.get("routeName").toString());
            proom.setMileage(Float.parseFloat(route.get("mileage").toString()));
            proom.setRouteDesc(route.get("routeDesc").toString());
            proom.setRouteImg(route.get("routeImg").toString());
            proom.setResistanceObj(route.get("resistanceObj").toString());
            proom.setCoordinateObj(route.get("coordinateObj").toString());
            proom.setMapImg(route.get("mapImg").toString());
            redisUtil.putObject(Constants.KEY_OF_ROOM + proom.getRno(), SerializerHolder.serializerImpl().writeObject(proom));
            syncRoom(proom);
        }
    }

    private void findRoomRoutes(Channel channel, User user, Message message) throws Exception {
        List<Map<String, Object>> routes = routeMapper.findRoutes(1);
        JSONArray jsonArray = (JSONArray) JSONArray.toJSON(routes);
        message.setData(jsonArray);
        user.sendMsg(message);
        return;
    }

    private void createRoom(Channel channel, User user, Message message) throws Exception {
        //1.查询一条默认线路
        Map<String, Object> route = routeMapper.findDefualtRoute(1);
        if (null == route || route.size() < 1) {
            message.setData(MessageStorehouse.M201());
            user.sendMsg(message);
            return;
        }
        //2.创建一个房间
        final int rno = (int) redisUtil.generatedRoomId();
        Proom room = new Proom(rno, Constants.ROOM_OF_TYPE_PERSONAL, Integer.parseInt(route.get("routeId").toString()),
                route.get("routeName").toString(), Float.parseFloat(route.get("mileage").toString()), route.get("routeDesc").toString(),
                route.get("routeImg").toString(),route.get("resistanceObj").toString(), route.get("coordinateObj").toString(),
                route.get("mapImg").toString(), user.getId());
        user.setState(2);
        PUser pUser =new PUser(user);
        pUser.setStatus(1);
        room.addPlayer(user.getId());
        redisUtil.putObject(Constants.KEY_OF_ROOM + rno, SerializerHolder.serializerImpl().writeObject(room));
        redisUtil.putObject(Constants.playerKey(rno,user.getId()), SerializerHolder.serializerImpl().writeObject(pUser));
        redisUtil.putObject(Constants.KEY_OF_USER + user.getId(), SerializerHolder.serializerImpl().writeObject(user));
        redisUtil.putString(Constants.KEY_OF_USER_ROOM + user.getId(), rno+"");

        message.setData(MessageStorehouse.M200());
        user.sendMsg(message);
//        syncRoom(room);
        return;
    }

    private boolean check(Channel channel, Message message, User user) throws Exception {
        //1.校验用户有没有登录
        if (null == user) {
            message.setData(MessageStorehouse.M500());
            channel.writeAndFlush(message).syncUninterruptibly();
            return false;
        }
        //2.判断用户是否在其他房间内
        if (PROOM_CREATE == message.sign()) {
            int rno = redisUtil.getrnoByUserId(user.getId());
            if (rno !=0) {
                message.setData(MessageStorehouse.M501());
                user.sendMsg(message);
                return false;
            }
        }
        return true;
    }

    private Proom checkHost(Message message, User user) throws Exception {
        //1.校验是否是房主
        Proom proom = redisUtil.getRoomByuserId(user.getId());
        if (null == proom) {
            message.setData(MessageStorehouse.M506());
            user.sendMsg(message);
            return null;
        }
        if (proom.getHostId() != user.getId()) {
            message.setData(MessageStorehouse.M509());
            user.sendMsg(message);
            return null;
        }
        return proom;
    }

    private void sync(final int rno) throws Exception {
        ScheduledFuture<?> scheduledFuture = scheduExec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    syncPlayersData(rno);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 3000, 1000, TimeUnit.MILLISECONDS);
        CommonMemory.addSchedule(rno+"", scheduledFuture);
    }

    private void syncPlayersData(final int rno) throws Exception {
        //根据房间号获取房间
        Proom proom = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.KEY_OF_ROOM + rno), Proom.class);
        if (null !=proom){
             int online =0;
            if (proom.state() == Constants.ROOM_STATE_RIDING){
                Object json = sortPlayers(redisUtil.players(proom.players(), proom.getRno()));
                for ( Integer uid : proom.players()){
                    PUser pUser = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.playerKey(proom.getRno(),uid)),PUser
                            .class);
                    if (pUser.getStatus()==2){
                        pUser.sendMsg(MessageStorehouse.it.syncUserData(json));
                        online++;
                    }
                }
                if (online ==0){
                    gameOver(rno);
                }
                return;
            }
            if (proom.state() == Constants.ROOM_STATE_READY_END){
                Integer countdown = countdownMap.get(rno + "");
                Object json = sortPlayers(redisUtil.players(proom.players(), proom.getRno()));
                for ( Integer uid : proom.players()){
                    PUser pUser = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.playerKey(proom.getRno(),uid)),PUser
                            .class);
                    if (pUser.getStatus()==2){
                        pUser.sendMsg(MessageStorehouse.it.syncUserData(countdown, json));
                        online++;
                    }
                }
                countdownMap.put(rno + "", --countdown);
                if (online ==0 || countdown==0){
                    gameOver(rno);
                }
                return;
            }
            if (proom.state() == Constants.ROOM_STATE_END) {
                gameOver(rno);
                return;
            }
        }else {
            CommonMemory.cancelchedule(rno+"");
        }
    }

    /**
     * 给房间内所有人同步房间信息
     */
    public void syncRoom(Proom proom) throws Exception {
        System.out.println(Thread.currentThread().getName()+"房间同步信息...............");
        try {
            synchronized(proom){
                if (null !=proom) {
                    List<PUser> players = redisUtil.players(proom.players(), proom.getRno());
                    JSONObject jsonObject = (JSONObject) JSONObject.toJSON(proom);
                    jsonObject.put("players", JSONArray.toJSON(players));
                    Message message = new Message(PROOM_SYNC_ROOM, jsonObject);
                    for (PUser user : players){
                        if (null != user && (user.getStatus() == 0 || user.getStatus() == 1)) {
                            user.sendMsg(message);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gameOver(int rno) {
        try {
            this.end(rno);
            CommonMemory.cancelchedule(rno+"");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private Object sortPlayers(Collection<PUser> players) throws Exception {
        Map pmap=null;
        List<Map> list = new ArrayList<>(5);
        List<PUser> plist = new ArrayList<>(players);
        Collections.sort(plist);
        for (PUser player : plist) {
            pmap = new HashMap();
            pmap.put("id", player.getId());
            pmap.put("mil", player.mil);
            pmap.put("status", player.getStatus());
            list.add(pmap);
        }
        return JSONArray.toJSON(list);
    }
    private Object report(Collection<PUser> players) throws Exception {
        List<PUser> plist = new ArrayList<>(players);
        List<Map> list = new ArrayList<>(5);
        Collections.sort(plist);
        Map pmap=null;
        for (PUser player : plist) {
            pmap = new HashMap();
            pmap.put("id", player.getId());
            pmap.put("mil", player.mil);
            pmap.put("cal", player.cal());
            pmap.put("status", player.getStatus());
            pmap.put("spe",  new BigDecimal(player.mil * 3600).divide(new BigDecimal(player.sec()), 1, BigDecimal.ROUND_HALF_UP).doubleValue());
            pmap.put("rt", player.sec());
            pmap.put("score", new BigDecimal(player.mil).setScale(1, BigDecimal.ROUND_DOWN).floatValue());
            list.add(pmap);
        }
        return JSONArray.toJSON(list);
    }


}
