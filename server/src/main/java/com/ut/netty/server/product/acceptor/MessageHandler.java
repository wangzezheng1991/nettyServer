package com.ut.netty.server.product.acceptor;

import com.ut.netty.server.product.common.Acknowledge;
import com.ut.netty.server.product.common.message.Message;
import com.ut.netty.server.product.handler.CommonHandler;
import com.ut.netty.server.product.handler.Logichandler;
import com.ut.netty.server.product.handler.LoginHandler;
import com.ut.netty.server.product.utils.SpringUtil;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ut.netty.server.product.common.MessageConfig.*;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/10 16:59
 */
@ChannelHandler.Sharable
public class MessageHandler extends SimpleChannelInboundHandler<Message>{
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    //登录处理
    private final LoginHandler loginHandler = SpringUtil.getBean(LoginHandler.class);
    //业务逻辑
    private final Logichandler logicHandler = SpringUtil.getBean(Logichandler.class);
    //其他逻辑处理
    private final CommonHandler commonHandler = SpringUtil.getBean(CommonHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) {
        try {
            Channel channel = ctx.channel();
            logger.info("客户端【" + channel.remoteAddress() + "】　消息:》》》" + message.toString());
            if (REQUEST == message.sign()) {
                sendACK(channel, message.sequence());
                Object data = "Superman给你回复消息啦!!" + message.data();
                message.sign(RESPONSE);
                message.data(data);
                send(channel, message);
                return;
            }
            if (LOGIN == message.sign()) {
                sendACK(channel, message.getSequence());
                loginHandler.login(channel, message);
                return;
            }
            if (RECONNECT == message.sign()) {
                sendACK(channel, message.getSequence());
                commonHandler.reconnect(channel, message);
                return;
            }
            if (FRIENDS == message.sign()) {
                sendACK(channel, message.getSequence());
                commonHandler.findFriends(channel, message);
                return;
            }
            if (INVITE_FRIENDS == message.sign()) {
                sendACK(channel, message.getSequence());
                commonHandler.inviteFriend(channel, message);
                return;
            }
            if (PROOM_RANG_START < message.sign() && message.sign() < PROOM_RANG_END) {
                if (PROOM_UPDATE !=message.sign()){
                    sendACK(channel, message.getSequence());
                }
                logicHandler.hander(channel, message);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void send(Channel channel, Object message) {
        //获取到channel发送双方规定的message格式的信息
        channel.writeAndFlush(message).syncUninterruptibly().addListener(MessageListener.getInstance());
    }

    protected void sendACK(Channel channel, long sequence) {
        // 接收到发布信息的时候，要给Client端回复ACK
        Acknowledge ack = new Acknowledge(sequence);
        channel.writeAndFlush(ack).syncUninterruptibly().addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

}
