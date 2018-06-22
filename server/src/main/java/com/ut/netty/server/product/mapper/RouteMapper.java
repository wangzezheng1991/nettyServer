package com.ut.netty.server.product.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/21 13:16
 */
public interface RouteMapper {
    /**
     * 查询线路列表
     * @param model
     * @return
     */
    List<Map<String, Object>> findRoutes(@Param("model") int model);

    /**
     * 查询默认线路
     * @param model
     * @return
     */
    Map<String, Object> findDefualtRoute(@Param("model") int model);
    /**
     * 根据线路id查询线路
     * @param model
     * @return
     */
    Map<String, Object> findRouteById(@Param("model") int model, @Param("id") int id);
}
