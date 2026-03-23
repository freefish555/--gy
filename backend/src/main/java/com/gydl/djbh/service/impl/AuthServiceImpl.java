package com.gydl.djbh.service.impl;

import com.gydl.djbh.crypto.SM4Util;
import com.gydl.djbh.dto.req.LoginReq;
import com.gydl.djbh.dto.resp.LoginResp;
import com.gydl.djbh.entity.TUser;
import com.gydl.djbh.exception.BusinessException;
import com.gydl.djbh.mapper.TUserMapper;
import com.gydl.djbh.security.JwtUtil;
import com.gydl.djbh.security.TotpUtil;
import com.gydl.djbh.service.AuthService;
import com.gydl.djbh.service.LogService;
import com.gydl.djbh.service.SysConfigService;
import com.gydl.djbh.utils.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TUserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final TotpUtil totpUtil;
    private final PasswordEncoder passwordEncoder;
    private final SM4Util sm4Util;
    private final SysConfigService sysConfigService;
    private final LogService logService;

    @Override
    @Transactional
    public LoginResp login(LoginReq req, String clientIp) {
        // 1. 查询用户
        TUser user = userMapper.findByUsername(req.getUsername());
        if (user == null) {
            logService.recordLoginFail(req.getUsername(), clientIp, "用户不存在");
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 2. 检查账号状态
        if (user.getStatus() == 0) {
            logService.recordLoginFail(req.getUsername(), clientIp, "账号已禁用");
            throw new BusinessException(401, "账号已被禁用，请联系管理员");
        }

        // 3. 检查账号是否被锁定
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            logService.recordLoginFail(req.getUsername(), clientIp, "账号已锁定");
            throw new BusinessException(401, "账号已被锁定，请稍后重试或联系管理员");
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            handleLoginFail(user);
            logService.recordLoginFail(req.getUsername(), clientIp, "密码错误");
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 5. 验证通过，重置失败次数
        resetLoginFail(user);

        // 6. 获取权限列表
        List<String> permissions = userMapper.findPermissionsByUserId(user.getId());

        // 7. 检查是否需要TOTP验证
        if (user.getTotpEnabled() != null && user.getTotpEnabled() == 1) {
            // 生成临时Token（短时效，仅用于TOTP验证流程）
            String tempToken = jwtUtil.generateToken(user.getId(), user.getUsername(),
                    user.getRoleCode(), List.of("TOTP_PENDING"));
            return LoginResp.builder()
                    .requireTotp(true)
                    .token(tempToken)  // 临时token，前端验证TOTP时带上
                    .expiresIn(300L)   // 5分钟有效
                    .build();
        }

        // 8. 直接颁发正式Token
        return buildLoginResp(user, permissions, clientIp);
    }

    @Override
    public LoginResp verifyTotp(String tempToken, int totpCode, String clientIp) {
        if (!jwtUtil.validateToken(tempToken)) {
            throw new BusinessException(401, "临时凭证已过期，请重新登录");
        }

        Long userId = jwtUtil.getUserId(tempToken);
        TUser user = userMapper.findByIdWithRole(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        // 验证TOTP码
        String secret = sm4Util.decrypt(user.getTotpSecret());
        if (!totpUtil.verify(secret, totpCode)) {
            logService.recordLoginFail(user.getUsername(), clientIp, "TOTP验证码错误");
            throw new BusinessException(401, "动态验证码错误");
        }

        List<String> permissions = userMapper.findPermissionsByUserId(userId);
        return buildLoginResp(user, permissions, clientIp);
    }

    @Override
    public void logout(String token) {
        // JWT无状态，服务端可以将Token加入黑名单（使用Cache或Redis实现）
        // 简化版：直接记录操作日志
        log.info("用户登出");
    }

    @Override
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        Long userId = SecurityContextUtil.getCurrentUserId();
        // 使用 selectByIdWithPassword 以确保 passwordHash 字段被查出
        TUser user = userMapper.selectByIdWithPassword(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException("原密码错误");
        }

        // 验证新密码复杂度
        validatePasswordComplexity(newPassword);

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFirstLogin(0);
        user.setPasswordChangedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public Map<String, String> generateTotpSecret() {
        Long userId = SecurityContextUtil.getCurrentUserId();
        TUser user = userMapper.selectById(userId);
        String secret = totpUtil.generateSecret();
        String qrUrl = totpUtil.generateQrUrl(user.getUsername(), secret);
        return Map.of("secret", secret, "qrUrl", qrUrl);
    }

    @Override
    @Transactional
    public void confirmTotpBind(String secret, int code) {
        if (!totpUtil.verify(secret, code)) {
            throw new BusinessException("验证码错误，TOTP绑定失败");
        }
        Long userId = SecurityContextUtil.getCurrentUserId();
        TUser user = userMapper.selectById(userId);
        user.setTotpSecret(sm4Util.encrypt(secret));
        user.setTotpEnabled(1);
        userMapper.updateById(user);
    }

    // ==================== 私有方法 ====================

    private LoginResp buildLoginResp(TUser user, List<String> permissions, String clientIp) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(),
                user.getRoleCode(), permissions);

        // 记录登录日志
        logService.recordLoginSuccess(user.getUsername(), sm4Util.decrypt(user.getRealName()), clientIp);

        // 更新最后登录信息
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        userMapper.updateById(user);

        return LoginResp.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(sm4Util.decrypt(user.getRealName()))
                .roleCode(user.getRoleCode())
                .roleName(user.getRoleName())
                .permissions(permissions)
                .staffId(user.getStaffId())
                .requireTotp(false)
                .firstLogin(user.getFirstLogin() == 1)
                .expiresIn(28800L)
                .build();
    }

    private void handleLoginFail(TUser user) {
        int failCount = user.getLoginFailCount() + 1;
        user.setLoginFailCount(failCount);

        int maxFail = sysConfigService.getIntConfig("LOGIN_MAX_FAIL", 5);
        if (failCount >= maxFail) {
            int lockMinutes = sysConfigService.getIntConfig("LOGIN_LOCK_MINUTES", 30);
            if (lockMinutes > 0) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
            }
        }
        userMapper.updateById(user);
    }

    private void resetLoginFail(TUser user) {
        if (user.getLoginFailCount() > 0 || user.getLockedUntil() != null) {
            user.setLoginFailCount(0);
            user.setLockedUntil(null);
            userMapper.updateById(user);
        }
    }

    private void validatePasswordComplexity(String password) {
        int minLength = sysConfigService.getIntConfig("PASSWORD_MIN_LENGTH", 8);
        if (password.length() < minLength) {
            throw new BusinessException("密码长度不能少于" + minLength + "位");
        }
        boolean requireUpper = sysConfigService.getBoolConfig("PASSWORD_REQUIRE_UPPER", true);
        if (requireUpper && !password.matches(".*[A-Z].*")) {
            throw new BusinessException("密码必须包含大写字母");
        }
        boolean requireLower = sysConfigService.getBoolConfig("PASSWORD_REQUIRE_LOWER", true);
        if (requireLower && !password.matches(".*[a-z].*")) {
            throw new BusinessException("密码必须包含小写字母");
        }
        boolean requireNumber = sysConfigService.getBoolConfig("PASSWORD_REQUIRE_NUMBER", true);
        if (requireNumber && !password.matches(".*[0-9].*")) {
            throw new BusinessException("密码必须包含数字");
        }
    }
}
