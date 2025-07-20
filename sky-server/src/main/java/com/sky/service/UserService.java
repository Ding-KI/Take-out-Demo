package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {
    /**
     * User login
     * @param userLoginDTO User login data transfer object
     * @return Login result
     */
    User wxLogin(UserLoginDTO userLoginDTO);


}
