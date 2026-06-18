package com.retail.material.service;

import com.retail.material.dto.LoginDTO;
import com.retail.material.vo.LoginVO;
import com.retail.material.vo.UserInfoVO;

public interface AuthService {

    LoginVO login(LoginDTO loginDTO);

    void logout();

    UserInfoVO getCurrentUser();
}