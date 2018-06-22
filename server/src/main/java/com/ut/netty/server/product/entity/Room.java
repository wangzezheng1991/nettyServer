package com.ut.netty.server.product.entity;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/17 16:03
 */
public class Room {
    private int rno;
    private short type;

    private Room() {
    }

    public Room(int rno, short type) {
        super();
        this.rno = rno;
        this.type = type;
    }

    public int getRno() {
        return rno;
    }

    public void setRno(int rno) {
        this.rno = rno;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }
}
