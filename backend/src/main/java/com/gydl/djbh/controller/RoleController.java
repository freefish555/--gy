package com.gydl.djbh.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gydl.djbh.dto.resp.Result;
import com.gydl.djbh.entity.TRole;
import com.gydl.djbh.mapper.TRoleMapper;
import com.gydl.djbh.mapper.TRolePermMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 角色权限管理
 */
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class RoleController {

    private final TRoleMapper roleMapper;
    private final TRolePermMapper rolePermMapper;

    /** 获取所有角色列表（含各角色的权限ID列表） */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<List<Map<String, Object>>> listRoles() {
        List<TRole> roles = roleMapper.selectList(
            new LambdaQueryWrapper<TRole>().orderByAsc(TRole::getId)
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (TRole r : roles) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getId());
            item.put("roleCode", r.getRoleCode());
            item.put("roleName", r.getRoleName());
            item.put("roleDesc", r.getRoleDesc());
            item.put("isSystem", r.getIsSystem());
            item.put("status", r.getStatus());
            item.put("permissionIds", rolePermMapper.findPermIdsByRoleId(r.getId()));
            result.add(item);
        }
        return Result.ok(result);
    }

    /** 获取所有权限列表 */
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<List<Map<String, Object>>> listPermissions() {
        return Result.ok(rolePermMapper.findAllPermissions());
    }

    /** 更新角色的权限配置 */
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<?> updateRolePermissions(@PathVariable Long roleId,
                                           @RequestBody Map<String, Object> body) {
        TRole role = roleMapper.selectById(roleId);
        if (role == null) return Result.fail("角色不存在");
        if ("SUPER_ADMIN".equals(role.getRoleCode())) return Result.fail("超级管理员权限不可修改");

        @SuppressWarnings("unchecked")
        List<Object> permIdObjs = (List<Object>) body.get("permissionIds");
        List<Long> permIds = new ArrayList<>();
        if (permIdObjs != null) {
            for (Object obj : permIdObjs) {
                if (obj != null) permIds.add(Long.valueOf(obj.toString()));
            }
        }
        // 清除旧权限，重新分配
        rolePermMapper.deleteByRoleId(roleId);
        for (Long permId : permIds) {
            rolePermMapper.insertRolePerm(roleId, permId);
        }
        return Result.ok("权限更新成功");
    }

    /** 更新角色基本信息（名称、描述） */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<?> updateRole(@PathVariable Long roleId, @RequestBody TRole req) {
        TRole role = roleMapper.selectById(roleId);
        if (role == null) return Result.fail("角色不存在");
        if ("SUPER_ADMIN".equals(role.getRoleCode())) return Result.fail("超级管理员信息不可修改");
        if (req.getRoleName() != null && !req.getRoleName().isEmpty()) role.setRoleName(req.getRoleName());
        if (req.getRoleDesc() != null) role.setRoleDesc(req.getRoleDesc());
        roleMapper.updateById(role);
        return Result.ok("更新成功");
    }
}
