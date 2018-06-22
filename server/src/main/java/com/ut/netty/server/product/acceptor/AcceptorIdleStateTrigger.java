package com.ut.netty.server.product.acceptor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ut.netty.server.product.common.CommonMemory;
import com.ut.netty.server.product.common.Constants;
import com.ut.netty.server.product.common.message.Message;
import com.ut.netty.server.product.entity.PUser;
import com.ut.netty.server.product.entity.Proom;
import com.ut.netty.server.product.redis.RedisUtil;
import com.ut.netty.server.product.serializer.SerializerHolder;
import com.ut.netty.server.product.utils.SpringUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import static com.ut.netty.server.product.common.MessageConfig.PROOM_SYNC_ROOM;

/**
 * @author Superman
 * @description
 * @time
 * @modifytime 2018年5月15日09:39:22
 */
@ChannelHandler.Sharable
public class AcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AcceptorIdleStateTrigger.class);

    private RedisUtil redisUtil = SpringUtil.getBean(RedisUtil.class);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("客户端【" + ctx.channel().remoteAddress() + "】 下线");
        logout(ctx);
        super.channelInactive(ctx);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        try {
            if (evt instanceof IdleStateEvent) {
                IdleState state = ((IdleStateEvent) evt).state();
                if (state == IdleState.READER_IDLE) {
                    logger.info("客户端【" + ctx.channel().remoteAddress() + "】空闲60s,强制关闭");
//                    logout(ctx);
                    ctx.channel().close();
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logout(ChannelHandlerContext ctx) {
        //根据通信id获取userid
        String channelId = ctx.channel().id().asShortText();
        int userId = CommonMemory.getUserIdByChannelId(channelId);
        if (userId != 0) {
            //根据userid获取房间
            Proom proom = redisUtil.getRoomByuserId(userId);

            try {
                if (null != proom) {
                    synchronized (proom) {
                        int rno = proom.getRno();
                        //如果房间只有一个人,直接解散房间
                        if (proom.players().size() <= 1) {
                            redisUtil.delObject(Constants.KEY_OF_ROOM + rno);
                            //删除用户与房间的关联
                            redisUtil.delObject(Constants.playerKey(proom.getRno(),userId));
                            int no = redisUtil.getrnoByUserId(userId);
                            if (no ==proom.getRno()){
                                redisUtil.delObject(Constants.KEY_OF_USER_ROOM+userId);
                            }
                            CommonMemory.cancelchedule(rno+"");
                        } else {
                            //如果房间是未开始的状态,掉线直接踢掉
                            if (proom.state() == Constants.ROOM_STATE_READY) {
                                //踢掉玩家
                                proom.removePlayer(userId);
                                //删掉用户房间的关联
                                redisUtil.delObject(Constants.playerKey(proom.getRno(),userId));
                                int no = redisUtil.getrnoByUserId(userId);
                                if (no ==proom.getRno()){
                                    redisUtil.delObject(Constants.KEY_OF_USER_ROOM+userId);
                                }
                                //如果是房主掉线的话,需要把房主转到下一个人
                                if (userId == proom.getHostId()) {
                                    proom.setHostId(proom.players().iterator().next());
                                    PUser p = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.playerKey(proom.getRno(),
                                            proom.players().iterator().next())), PUser.class);
                                    p.setStatus(1);
                                    //更新玩家信息
                                    redisUtil.putObject(Constants.playerKey(proom.getRno(),p.getId()), SerializerHolder.serializerImpl()
                                            .writeObject(p));
                                    redisUtil.putObject(Constants.KEY_OF_ROOM + rno, SerializerHolder.serializerImpl().writeObject(proom));
                                }
                                syncRoom(proom);
                            } else {
                                //如果房间已经开始, 把用户设置为掉线状态
                                PUser p = SerializerHolder.serializerImpl().readObject(redisUtil.getObject(Constants.playerKey(proom.getRno(),
                                        userId)),PUser.class);
                                p.setStatus(-1);
                                //更新玩家信息
                                redisUtil.putObject(Constants.playerKey(proom.getRno(),p.getId()), SerializerHolder.serializerImpl().writeObject(p));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                redisUtil.delObject(Constants.KEY_OF_USER + userId);
                CommonMemory.removeChannelByUserId(userId);
            }

        }
    }

    /**
     * 给房间内所有人同步房间信息
     */
    public void syncRoom(Proom proom) {
        System.out.println(Thread.currentThread().getName() + "房间同步信息...............");
        try {
            synchronized (proom) {
                if (null != proom) {
                    List<PUser> players = redisUtil.players(proom.players(), proom.getRno());
                    JSONObject jsonObject = (JSONObject) JSONObject.toJSON(proom);
                    jsonObject.put("players", JSONArray.toJSON(players));
                    Message message = new Message(PROOM_SYNC_ROOM, jsonObject);
                    for (PUser user : players) {
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
}
