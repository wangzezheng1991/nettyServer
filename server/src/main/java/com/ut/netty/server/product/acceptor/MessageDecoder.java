package com.ut.netty.server.product.acceptor;

import com.alibaba.fastjson.JSON;
import com.ut.netty.server.product.common.NettyCommonProtocol;
import com.ut.netty.server.product.common.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.ut.netty.server.product.common.NettyCommonProtocol.*;

/**
 * 解码器，继承于ReplayingDecoder
 *
 * @author Superman
 * @description
 * @Company: www.km1930.com
 * @time 2018/5/15 10:06
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
public class MessageDecoder extends ReplayingDecoder<MessageDecoder.State> {
    private static final Logger logger = LoggerFactory.getLogger(MessageDecoder.class);

    //构造函数 设置初始的枚举类型是什么
    public MessageDecoder() {
        super(State.HEADER_MAGIC);
    }

    // 协议头
    private final NettyCommonProtocol header = new NettyCommonProtocol();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case HEADER_MAGIC:
                checkMagic(in.readShort());             // MAGIC
                checkpoint(MessageDecoder.State.HEADER_SIGN);
            case HEADER_SIGN:
                header.sign(in.readByte());             // 消息标志位
                checkpoint(MessageDecoder.State.HEADER_STATUS);
            case HEADER_STATUS:
                in.readByte();                          // no-op
                checkpoint(MessageDecoder.State.HEADER_ID);
            case HEADER_ID:
                header.id(in.readLong());               // 消息id
                checkpoint(MessageDecoder.State.HEADER_BODY_LENGTH);
            case HEADER_BODY_LENGTH:
                header.bodyLength(in.readInt());        // 消息体长度
                checkpoint(MessageDecoder.State.BODY);
            case BODY:
                byte[] bytes = new byte[header.bodyLength()];
                in.readBytes(bytes);
                switch (header.sign()) {
                    case HEARTBEAT:
                        logger.info("客户端【" + ctx.channel().remoteAddress() + "】　心跳");
                        break;
                    case REQUEST:
                    case LOGIN:
                    case LOGIC:
                    case COMMON:{
                        Message msg = JSON.parseObject(bytes, Message.class);
                        out.add(msg);
                        break;
                    }
                    case SERVICE_4: {
                    }
                    default:
                        throw new IllegalAccessException();
                }
                checkpoint(MessageDecoder.State.HEADER_MAGIC);
        }
    }

    private static void checkMagic(short magic) throws Signal {
        if (MAGIC != magic) {
            throw new IllegalArgumentException();
        }
    }

    enum State {
        HEADER_MAGIC,
        HEADER_SIGN,
        HEADER_STATUS,
        HEADER_ID,
        HEADER_BODY_LENGTH,
        BODY
    }
}
