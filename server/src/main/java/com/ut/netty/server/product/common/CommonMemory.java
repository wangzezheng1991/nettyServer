package com.ut.netty.server.product.common;

import com.ut.netty.server.product.utils.GZIP;
import io.netty.channel.Channel;
import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/18 16:56
 */
public class CommonMemory {
    private static ConcurrentHashMap<String, Channel> channelMap =new  ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, String> userChannelMap =new  ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, Future> scheduleMap =new  ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, BufferedWriter> writerMap =new  ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, String> tempFileMap =new  ConcurrentHashMap<>();


    public synchronized static void addChannel(String channelId, String userId,  Channel channel) {
        channelMap.put(channelId, channel);
        userChannelMap.put(userId, channelId);
    }

    public static Channel getChannel(String userId) {
        String channelId = userChannelMap.get(userId);
        if (channelId ==null)
            return null;
        Channel channel =channelMap.get(channelId);
        return channel;
    }

    public static int getUserIdByChannelId(String channelId) {
        int userId =0;
        Iterator<Map.Entry<String, String>> entries = userChannelMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            if(channelId.equals(entry.getValue())) {
                userId = Integer.parseInt(entry.getKey());
            }
        }
        return userId;
    }

    public synchronized static void removeChannelByChannelId(String channelId) {
        channelMap.remove(channelId);
        Iterator<Map.Entry<String, String>> entries = userChannelMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            String key =  entry.getKey();
            String value =  entry.getValue();
            if(channelId.equals(value)) {
                userChannelMap.remove(key);
            }
        }
    }

    public synchronized static void removeChannelByUserId(int userId) {
        String channelId = userChannelMap.remove(userId+"");
        channelMap.remove(channelId);
    }

    public synchronized static void updateChannelUser(String userId, Channel newchannel, Channel oldchannel) {
        //先关闭原有的通道
        channelMap.remove(oldchannel.id().asShortText());
        userChannelMap.remove(userId);
        oldchannel.close();
        //更新新的通信通道
        channelMap.put(newchannel.id().asShortText(), newchannel);
        userChannelMap.put(userId, newchannel.id().asShortText());
    }

    public static void addSchedule(String rno, Future future) {
        scheduleMap.put(rno, future);
    }

    public static void cancelchedule(String rno) {
        Future future = scheduleMap.get(rno);
        if (null !=future)
            future.cancel(true);
    }

    public static void tempFile(Integer uid) {
        try {
            File tempFile = File.createTempFile("user", ".data");
            tempFile.deleteOnExit();
            BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));
            out.write("[");
            writerMap.put(uid+"", out);
            tempFileMap.put(uid+"", tempFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeRideData(Integer uid, Object data) {
        try {
            BufferedWriter out = writerMap.get(uid + "");
            if (null !=out){
                out.write(data.toString()+",");
                out.flush();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public static String readRideData(Integer uid) {
        StringBuilder sb = new StringBuilder();
        BufferedReader in =null;
        String tempFile ="";
        try {
            BufferedWriter out = writerMap.get(uid + "");
            if (null !=out){
                out.close();
                writerMap.remove(uid + "");
            }
            tempFile = tempFileMap.get(uid+"");
            in  = new BufferedReader(new FileReader(tempFile));
            String s = null;
            while ((s =in.readLine()) !=null){
                sb.append(s);
            }
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (null !=in)
                    in.close();
                tempFileMap.remove(uid+"");
                File file = new File(tempFile);
                file.delete();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        int index = sb.lastIndexOf(",");
        if (index <1)
            return "";
        sb.replace(index,index+1, "]");
        System.out.println(sb.toString());
        return GZIP.compress(sb.toString());
    }

}
