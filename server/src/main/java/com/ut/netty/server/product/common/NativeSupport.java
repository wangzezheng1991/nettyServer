package com.ut.netty.server.product.common;

/**
 * 
 * @author Superman
 * @description
 * @copyright fjc
 * @time 2018年5月15日20:48:08
 * @modifytime
 */
public final class NativeSupport {

    private static final boolean SUPPORT_NATIVE_ET;

    static {
        boolean epoll;
        try {
            Class.forName("io.netty.channel.epoll.Native");
            epoll = true;
        } catch (Throwable e) {
            epoll = false;
        }
        SUPPORT_NATIVE_ET = epoll;
    }

    /**
     * The native socket transport for Linux using JNI.
     */
    public static boolean isSupportNativeET() {
        return SUPPORT_NATIVE_ET;
    }
}
