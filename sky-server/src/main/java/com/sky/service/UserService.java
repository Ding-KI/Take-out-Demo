package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {
    /**
     * 用户微信登录
     * @param userLoginDTO 用户登录数据传输对象
     * @return 登录结果
     */
    User wxLogin(UserLoginDTO userLoginDTO);


}
