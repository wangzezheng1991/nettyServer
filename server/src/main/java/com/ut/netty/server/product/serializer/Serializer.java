package com.ut.netty.server.product.serializer;


/**
 * 
 * @author Superman
 * @description 序列化工具接口
 * @time 2018年5月15日18:53:48
 * @modifytime
 */
public interface Serializer {

	/**
	 * 将obj序列化成byte数组
	 * @param obj
	 * @return
	 */
    <T> byte[] writeObject(T obj);

    /**
     * 将byte数组反序列化成class是clazz的obj对象
     * @param bytes
     * @param clazz
     * @return
     */
    <T> T readObject(byte[] bytes, Class<T> clazz);
}
