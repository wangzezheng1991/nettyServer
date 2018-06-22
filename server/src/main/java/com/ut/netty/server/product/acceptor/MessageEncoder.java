package com.ut.netty.server.product.acceptor;

import com.alibaba.fastjson.JSON;
import com.ut.netty.server.product.common.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ut.netty.server.product.common.MessageConfig.*;
import static com.ut.netty.server.product.common.NettyCommonProtocol.COMMON;
import static com.ut.netty.server.product.common.NettyCommonProtocol.LOGIC;
import static com.ut.netty.server.product.common.NettyCommonProtocol.MAGIC;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/15 10:13
 */

/**
 * **************************************************************************************************
 * Protocol
 * ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
 * 2   │   1   │    1   │     8     │      4      │
 * ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
 * │       │        │           │             │
 * │  MAGIC   Sign    Status   Invoke Id   Body Length                   Body Content              │
 * │       │        │           │             │
 * └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
 * <p>
 * 消息头16个字节定长
 * = 2 // MAGIC = (short) 0xbabe
 * + 1 // 消息标志位, 用来表示消息类型
 * + 1 // 空
 * + 8 // 消息 id long 类型
 * + 4 // 消息体body长度, int类型
 */
@ChannelHandler.Sharable
public class MessageEncoder extends MessageToByteEncoder<Message> {
    private static final Logger logger = LoggerFactory.getLogger(MessageEncoder.class);
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        logger.info("IP【" + ctx.channel().remoteAddress() + "】 server send message ===>>> " + msg.toString());
        byte[] bytes = JSON.toJSONBytes(msg);
        out.writeShort(MAGIC)
                .writeByte(getSign(msg.sign()))
                .writeByte(0)
                .writeLong(0)
                .writeInt(bytes.length)
                .writeBytes(bytes);
    }

    private byte getSign(int sign){
        if (RESPONSE == sign){
            return RESPONSE;
        }
        if (LOGIN == sign){
            return LOGIN;
        }
        if (RECONNECT == sign || FRIENDS ==sign || INVITE_FRIENDS ==sign || INVITEED_FRIENDS ==sign){
            return COMMON;
        }
        if (PROOM_RANG_START < sign && sign < PROOM_RANG_END){
            return LOGIC;
        }
        return (byte) sign;
    }
}
