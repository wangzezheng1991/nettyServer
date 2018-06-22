package com.ut.netty.server.product;

import com.ut.netty.server.product.acceptor.ChannelEventListener;
import com.ut.netty.server.product.acceptor.DefaultCommonSrvAcceptor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SrvAcceptorStartup implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ChannelEventListener channelEventListener = null;
        DefaultCommonSrvAcceptor defaultCommonSrvAcceptor = new DefaultCommonSrvAcceptor(20011, channelEventListener);
        defaultCommonSrvAcceptor.start();
    }
}
