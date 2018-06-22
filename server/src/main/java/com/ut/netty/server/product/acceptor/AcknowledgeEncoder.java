package com.ut.netty.server.product.acceptor;

import com.alibaba.fastjson.JSON;
import com.ut.netty.server.product.common.Acknowledge;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import static com.ut.netty.server.product.common.NettyCommonProtocol.ACK;
import static com.ut.netty.server.product.common.NettyCommonProtocol.MAGIC;

/**
 * @author Surperman
 * @description ack的编码器
 * @time
 * @modifytime
 */
@ChannelHandler.Sharable
public class AcknowledgeEncoder extends MessageToByteEncoder<Acknowledge> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Acknowledge ack, ByteBuf out) throws Exception {
        byte[] bytes = JSON.toJSONBytes(ack);
        out.writeShort(MAGIC)
                .writeByte(ACK)
                .writeByte(0)
                .writeLong(ack.getSequence())
                .writeInt(bytes.length)
                .writeBytes(bytes);
    }
}
