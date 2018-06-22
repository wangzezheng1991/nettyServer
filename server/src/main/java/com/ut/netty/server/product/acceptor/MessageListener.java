package com.ut.netty.server.product.acceptor;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/28 10:57
 */
public class MessageListener implements ChannelFutureListener {
    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

    private static volatile MessageListener messageListener =null;

    private MessageListener() {
    }

    public static MessageListener getInstance(){
        if (messageListener ==null){
            synchronized (MessageListener.class) {
                if (messageListener ==null){
                    messageListener = new MessageListener();
                }
            }
        }
        return messageListener;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess()) {
            logger.info("send fail,reason is {}", channelFuture.cause().getMessage());
        }
    }
}
