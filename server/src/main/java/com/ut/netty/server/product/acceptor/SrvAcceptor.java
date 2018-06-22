package com.ut.netty.server.product.acceptor;

import java.net.SocketAddress;

/**
 * 
 * @author Superman
 * @description netty server端的标准接口定义
 * @time 2018年5月15日16:41:04
 * @modifytime
 */
public interface SrvAcceptor {
	
	SocketAddress localAddress();
	
	void start() throws InterruptedException;
	
	void shutdownGracefully();
	
	void start(boolean sync) throws InterruptedException;

}
