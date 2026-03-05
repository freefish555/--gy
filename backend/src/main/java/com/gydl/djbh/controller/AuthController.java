package com.gydl.djbh.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import com.gydl.djbh.dto.req.LoginReq;
import com.gydl.djbh.dto.resp.LoginResp;
import com.gydl.djbh.dto.resp.Result;
import com.gydl.djbh.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器（登录、验证码、TOTP验证）
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CacheManager cacheManager;

    /**
     * 获取图形验证码
     */
    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 5);
        String captchaId = IdUtil.fastSimpleUUID();
        String captchaCode = captcha.getCode().toLowerCase();

        // 缓存验证码 5分钟
        Cache cache = cacheManager.getCache("captcha");
        if (cache != null) {
            cache.put(captchaId, captchaCode);
        }

        return Result.ok(Map.of(
                "captchaId", captchaId,
                "captchaImg", captcha.getImageBase64Data()
        ));
    }

    /**
     * 用户名密码登录
     * 返回token或requireTotp=true时需进行TOTP二次验证
     */
    @PostMapping("/login")
    public Result<LoginResp> login(@Valid @RequestBody LoginReq req,
                                    HttpServletRequest request) {
        String clientIp = getClientIp(request);
        LoginResp resp = authService.login(req, clientIp);
        return Result.ok(resp);
    }

    /**
     * TOTP二次验证
     */
    @PostMapping("/totp/verify")
    public Result<LoginResp> verifyTotp(@RequestBody Map<String, Object> body,
                                         HttpServletRequest request) {
        String tempToken = (String) body.get("tempToken");
        int totpCode = (Integer) body.get("totpCode");
        String clientIp = getClientIp(request);
        LoginResp resp = authService.verifyTotp(tempToken, totpCode, clientIp);
        return Result.ok(resp);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = extractToken(request);
        authService.logout(token);
        return Result.ok("已成功登出");
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<?> changePassword(@RequestBody Map<String, String> body,
                                     HttpServletRequest request) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        authService.changePassword(oldPassword, newPassword);
        return Result.ok("密码修改成功");
    }

    /**
     * 绑定TOTP（生成二维码）
     */
    @PostMapping("/totp/bind")
    public Result<Map<String, String>> bindTotp() {
        Map<String, String> result = authService.generateTotpSecret();
        return Result.ok(result);
    }

    /**
     * 确认绑定TOTP
     */
    @PostMapping("/totp/confirm")
    public Result<?> confirmTotp(@RequestBody Map<String, Object> body) {
        String secret = (String) body.get("secret");
        int code = (Integer) body.get("code");
        authService.confirmTotpBind(secret, code);
        return Result.ok("TOTP双因子认证绑定成功");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
