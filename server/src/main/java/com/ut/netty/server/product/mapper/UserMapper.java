package com.ut.netty.server.product.mapper;

import com.ut.netty.server.product.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: Superman
 * @Company: www.km1930.com
 * @Create 2018/5/16 13:27
 */
public interface UserMapper {
    User getUserById(@Param("id") Integer id);

    List<Map> findFriends(@Param("id") Integer id);

    List<Integer> findHostFriends(@Param("id") Integer id);
}
