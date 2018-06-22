package com.ut.netty.server.product.entity;

import com.alibaba.fastjson.JSONObject;
import com.ut.netty.server.product.common.CommonMemory;
import java.util.*;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/21 16:15
 */
public class PUser extends User implements Comparable<PUser> {
    private int status = 0; //状态(-1:掉线; 0:组队中,未准备; 1:组队中,已准备; 2:骑行中)

    public volatile float mil = 0; //里程
    private short cal = 0; //卡路里
    //    private float spe =0; //速度
//    private short res =0; //阻力
    private long sec = 1;
    ;
    private PUser() {
    }

    public PUser(User user) {
        super.setId(user.getId());
        super.setName(user.getName());
        super.setImg(user.getImg());
        super.setState(user.getState());
    }

    public long sec() {
        return this.sec;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public synchronized void update(Object data) {
        Map map = JSONObject.parseObject(data.toString(), HashMap.class);
        this.mil = Float.parseFloat(map.get("mil").toString());
        this.cal = Short.parseShort(map.get("cal").toString());
        this.sec = Long.parseLong(map.get("sec").toString());
        CommonMemory.writeRideData(this.getId(),data);
    }

    public short cal() {
        return this.cal;
    }

    @Override
    public String toString() {
        return "PUser{" +
                "id=" + super.getId() +
                "name=" + super.getName() +
                "img=" + super.getImg() +
                "state=" + super.getState() +
                "status=" + status +
                '}';
    }

    @Override
    public int compareTo(PUser pUser) {
        return this.mil == pUser.mil ? (this.sec() > pUser.sec() ? 1 : -1) : (this.mil > pUser.mil ? -1 : 1);
    }
}
