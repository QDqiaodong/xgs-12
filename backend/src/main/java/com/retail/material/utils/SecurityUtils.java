package com.retail.material.utils;

import com.retail.material.config.JwtProperties;
import com.retail.material.security.JwtTokenProvider;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private static JwtTokenProvider jwtTokenProvider;
    private static JwtProperties jwtProperties;

    public SecurityUtils(JwtTokenProvider jwtTokenProvider, JwtProperties jwtProperties) {
        SecurityUtils.jwtTokenProvider = jwtTokenProvider;
        SecurityUtils.jwtProperties = jwtProperties;
    }

    public static Long getCurrentUserId() {
        String token = RequestUtils.getToken();
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        return null;
    }

    public static String getCurrentUsername() {
        String token = RequestUtils.getToken();
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getUsernameFromToken(token);
        }
        return null;
    }
}