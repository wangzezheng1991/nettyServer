package com.ut.netty.server.product.entity;

import java.util.*;

/**
 * @Description: 个人竞技房间
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/17 11:08
 */
public class Proom extends Room {

    private String routeName; //线路名称
    private int routeId; //线路id
    private float mileage; //里程
    private String routeDesc; //线路描述
    private String routeImg;//线路图片
    private String resistanceObj;//阻力obj
    private String coordinateObj; //坐标obj
    private String mapImg; //地图图片
    private int hostId; //房主id
    //    private ConcurrentHashMap<String, PUser> players =null;//玩家集合
    private LinkedHashSet<Integer> players = null;

    //房间的是否开始骑行 0:未开始  1:正在骑行  2:结束骑行
    private byte state = 0;

    private long startTime = System.currentTimeMillis();

    public Proom(int rno, short type, int routeId, String routeName, float mileage, String routeDesc, String routeImg, String resistanceObj, String
            coordinateObj, String mapImg, Integer hostId) {
        super(rno, type);
        this.routeName = routeName;
        this.routeId = routeId;
        this.mileage = mileage;
        this.routeDesc = routeDesc;
        this.routeImg = routeImg;
        this.resistanceObj = resistanceObj;
        this.coordinateObj = coordinateObj;
        this.mapImg = mapImg;
        this.hostId = hostId;
//        players = new ConcurrentHashMap<>(5);
        players = new LinkedHashSet<>();
    }

    public void state(byte state) {
        this.state = state;
    }

    public byte state() {
        return this.state;
    }

    public void updateStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    public long startTime() {
        return this.startTime;
    }

    public float getMileage() {
        return mileage;
    }

    public void setMileage(float mileage) {
        this.mileage = mileage;
    }

    public String getResistanceObj() {
        return resistanceObj;
    }

    public void setResistanceObj(String resistanceObj) {
        this.resistanceObj = resistanceObj;
    }

    public String getCoordinateObj() {
        return coordinateObj;
    }

    public void setCoordinateObj(String coordinateObj) {
        this.coordinateObj = coordinateObj;
    }

    public String getMapImg() {
        return mapImg;
    }

    public void setMapImg(String mapImg) {
        this.mapImg = mapImg;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteDesc() {
        return routeDesc;
    }

    public void setRouteDesc(String routeDesc) {
        this.routeDesc = routeDesc;
    }

    public String getRouteImg() {
        return routeImg;
    }

    public void setRouteImg(String routeImg) {
        this.routeImg = routeImg;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public LinkedHashSet<Integer> players() {
        return this.players;
    }

    public synchronized void addPlayer(int userId) {
        this.players.add(userId);
    }

    public synchronized void removePlayer(Integer userId) {
        this.players.remove(userId);

    }

}
