package com.ut.netty.server.product.serializer;

import com.ut.netty.server.product.serializer.protostuff.ProtoStuffSerializer;

/**
 * 
 * @author Superman
 * @description 序列化工具 obj to byte 用于网络传输
 * @time 2018年5月15日18:51:43
 * @modifytime
 */
public final class SerializerHolder {

	//使用google的protostuff
	//protostuff 是一个支持各种格式的一个序列化Java类库，包括 JSON、XML、YAML等格式。
    private static final Serializer serializer = new ProtoStuffSerializer();

    public static Serializer serializerImpl() {
        return serializer;
    }
}
