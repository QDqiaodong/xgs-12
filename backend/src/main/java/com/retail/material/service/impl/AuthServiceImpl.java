package com.retail.material.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.retail.material.config.JwtProperties;
import com.retail.material.dto.LoginDTO;
import com.retail.material.entity.SysUser;
import com.retail.material.exception.BusinessException;
import com.retail.material.mapper.SysUserMapper;
import com.retail.material.security.JwtTokenProvider;
import com.retail.material.service.AuthService;
import com.retail.material.vo.LoginVO;
import com.retail.material.vo.UserInfoVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    public AuthServiceImpl(SysUserMapper sysUserMapper, 
                          JwtTokenProvider jwtTokenProvider, 
                          JwtProperties jwtProperties,
                          StringRedisTemplate stringRedisTemplate) {
        this.sysUserMapper = sysUserMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysUser user = sysUserMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw BusinessException.of(401, "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw BusinessException.of(401, "账号已被禁用");
        }

        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw BusinessException.of(401, "用户名或密码错误");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setExpiresIn(jwtProperties.getExpiration());
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setRole(user.getRole());

        return loginVO;
    }

    @Override
    public void logout() {
        String token = getTokenFromRequest();
        if (token != null) {
            String key = TOKEN_BLACKLIST_PREFIX + token;
            stringRedisTemplate.opsForValue().set(key, "1", jwtProperties.getExpiration(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public UserInfoVO getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw BusinessException.of(401, "未登录");
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.of(401, "用户不存在");
        }

        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserId(user.getId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setRealName(user.getRealName());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setEmail(user.getEmail());
        userInfoVO.setRole(user.getRole());
        userInfoVO.setStoreId(user.getStoreId());
        userInfoVO.setStatus(user.getStatus());

        return userInfoVO;
    }

    private Long getCurrentUserId() {
        String token = getTokenFromRequest();
        if (token != null && jwtTokenProvider.validateToken(token)) {
            if (isTokenBlacklisted(token)) {
                return null;
            }
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        return null;
    }

    private String getTokenFromRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        jakarta.servlet.http.HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader(jwtProperties.getHeader());
        if (authHeader != null && authHeader.startsWith(jwtProperties.getPrefix() + " ")) {
            return authHeader.substring(jwtProperties.getPrefix().length() + 1);
        }
        return null;
    }

    private boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
}
