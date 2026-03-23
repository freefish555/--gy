package com.gydl.djbh.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gydl.djbh.crypto.SM4Util;
import com.gydl.djbh.dto.resp.Result;
import com.gydl.djbh.entity.TRole;
import com.gydl.djbh.entity.TUser;
import com.gydl.djbh.exception.BusinessException;
import com.gydl.djbh.mapper.TRoleMapper;
import com.gydl.djbh.mapper.TUserMapper;
import com.gydl.djbh.service.SysConfigService;
import com.gydl.djbh.utils.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统用户管理控制器
 */
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class UserController {

    private final TUserMapper userMapper;
    private final TRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final SM4Util sm4Util;
    private final SysConfigService sysConfigService;

    /**
     * 密码复杂度校验（与AuthServiceImpl保持一致）
     */
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
        boolean requireSpecial = sysConfigService.getBoolConfig("PASSWORD_REQUIRE_SPECIAL", false);
        if (requireSpecial && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new BusinessException("密码必须包含特殊字符");
        }
    }

    /**
     * 获取所有角色列表（供下拉选择）
     */
    @GetMapping("/role/all")
    @PreAuthorize("hasAnyAuthority('system:user','SUPER_ADMIN')")
    public Result<List<Map<String, Object>>> listRoles() {
        List<TRole> roles = roleMapper.findAllActive();
        List<Map<String, Object>> result = new ArrayList<>();
        for (TRole r : roles) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("roleCode", r.getRoleCode());
            item.put("roleName", r.getRoleName());
            item.put("roleDesc", r.getRoleDesc());
            result.add(item);
        }
        return Result.ok(result);
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/user/list")
    @PreAuthorize("hasAuthority('system:user')")
    public Result<List<Map<String, Object>>> listUsers() {
        List<TUser> users = userMapper.selectList(
                new LambdaQueryWrapper<TUser>().orderByAsc(TUser::getId)
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (TUser u : users) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", u.getId());
            item.put("username", u.getUsername());
            item.put("realName", safeDecrypt(u.getRealName()));
            item.put("roleId", u.getRoleId());
            // Get role name by joining
            TUser withRole = userMapper.findByIdWithRole(u.getId());
            item.put("roleName", withRole != null ? withRole.getRoleName() : "");
            item.put("roleCode", withRole != null ? withRole.getRoleCode() : "");
            item.put("phone", safeDecrypt(u.getPhone()));
            item.put("email", safeDecrypt(u.getEmail()));
            item.put("staffId", u.getStaffId());
            item.put("totpEnabled", u.getTotpEnabled());
            item.put("status", u.getStatus());
            item.put("lastLoginAt", u.getLastLoginAt());
            item.put("firstLogin", u.getFirstLogin());
            item.put("createdAt", u.getCreatedAt());
            result.add(item);
        }
        return Result.ok(result);
    }

    /**
     * 新增用户
     */
    @PostMapping("/user")
    @PreAuthorize("hasAuthority('system:user')")
    public Result<Long> createUser(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String realName = (String) body.get("realName");
        String password = (String) body.get("password");
        Object roleIdObj = body.get("roleId");
        String phone = (String) body.get("phone");
        String email = (String) body.get("email");

        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (realName == null || realName.trim().isEmpty()) {
            throw new BusinessException("真实姓名不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        validatePasswordComplexity(password);
        if (roleIdObj == null) {
            throw new BusinessException("请选择角色");
        }

        // Check username unique
        TUser existing = userMapper.findByUsername(username);
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }

        Long roleId = Long.valueOf(roleIdObj.toString());
        Long currentUserId = SecurityContextUtil.getCurrentUserId();

        // staffId（关联人员清单，可选）
        Long staffId = null;
        Object staffIdObj = body.get("staffId");
        if (staffIdObj != null && !staffIdObj.toString().isEmpty()) {
            try { staffId = Long.valueOf(staffIdObj.toString()); } catch (Exception ignored) {}
        }

        TUser user = new TUser();
        user.setUsername(username.trim());
        user.setRealName(sm4Util.encrypt(realName.trim()));
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRoleId(roleId);
        user.setStaffId(staffId);
        user.setPhone(phone != null && !phone.isEmpty() ? sm4Util.encrypt(phone) : null);
        user.setEmail(email != null && !email.isEmpty() ? sm4Util.encrypt(email) : null);
        user.setStatus(1);
        user.setFirstLogin(1);
        user.setTotpEnabled(0);
        user.setLoginFailCount(0);
        user.setCreatedBy(currentUserId);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDataHmac("");
        userMapper.insert(user);

        return Result.ok("新增成功", user.getId());
    }

    /**
     * 更新用户
     */
    @PutMapping("/user/{id}")
    @PreAuthorize("hasAuthority('system:user')")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        TUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");

        String realName = (String) body.get("realName");
        Object roleIdObj = body.get("roleId");
        String phone = (String) body.get("phone");
        String email = (String) body.get("email");

        if (realName != null && !realName.trim().isEmpty()) {
            user.setRealName(sm4Util.encrypt(realName.trim()));
        }
        if (roleIdObj != null) {
            user.setRoleId(Long.valueOf(roleIdObj.toString()));
        }
        if (phone != null) {
            user.setPhone(phone.isEmpty() ? null : sm4Util.encrypt(phone));
        }
        if (email != null) {
            user.setEmail(email.isEmpty() ? null : sm4Util.encrypt(email));
        }
        // staffId（关联人员清单，可选，传null表示清除关联）
        if (body.containsKey("staffId")) {
            Object staffIdObj = body.get("staffId");
            if (staffIdObj == null || staffIdObj.toString().isEmpty()) {
                user.setStaffId(null);
            } else {
                try { user.setStaffId(Long.valueOf(staffIdObj.toString())); } catch (Exception ignored) {}
            }
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        return Result.ok("更新成功");
    }

    /**
     * 重置密码
     */
    @PutMapping("/user/{id}/password")
    @PreAuthorize("hasAuthority('system:user')")
    public Result<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        TUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");

        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        validatePasswordComplexity(newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFirstLogin(1);
        user.setPasswordChangedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.ok("密码重置成功");
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/user/{id}/status")
    @PreAuthorize("hasAuthority('system:user')")
    public Result<?> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        TUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");

        // 不允许禁用自己
        Long currentUserId = SecurityContextUtil.getCurrentUserId();
        if (currentUserId.equals(id)) {
            throw new BusinessException("不能禁用自己的账号");
        }

        Integer status = body.get("status");
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.ok("操作成功");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasAuthority('system:user')")
    public Result<?> deleteUser(@PathVariable Long id) {
        TUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");

        Long currentUserId = SecurityContextUtil.getCurrentUserId();
        if (currentUserId.equals(id)) {
            throw new BusinessException("不能删除自己的账号");
        }
        userMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    private String safeDecrypt(String value) {
        if (value == null || value.isEmpty()) return "";
        return sm4Util.decrypt(value);
    }
}
