package com.gydl.djbh.utils;

import com.gydl.djbh.security.JwtUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类
 */
public class SecurityContextUtil {

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("当前用户未登录");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof JwtUserDetails jwtUser) {
            return jwtUser.getUserId();
        }
        throw new RuntimeException("无法获取当前用户ID");
    }

    public static Long getCurrentUserIdSafe() {
        try {
            return getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof JwtUserDetails jwtUser) {
            return jwtUser.getUsername();
        }
        if (auth.getPrincipal() instanceof String s) {
            return s;
        }
        return auth.getName();
    }

    public static String getCurrentUsernameSafe() {
        try {
            return getCurrentUsername();
        } catch (Exception e) {
            return null;
        }
    }
}
