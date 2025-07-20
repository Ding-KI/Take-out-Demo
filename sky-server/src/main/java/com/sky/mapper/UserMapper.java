package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    // Query by openId
    @Select("select * from user where openid = #{openid}")
    User getByOpenId(String openId);

    // Insert new user data
    void insert(User user);

    @Select("select * from user where id = #{id}")
    User getById(Long userId);

    // Dynamic count user number - new user, daily user
    Integer countByMap(Map map);
}
