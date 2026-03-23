package com.gydl.djbh.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 角色-权限关联映射
 */
@Mapper
public interface TRolePermMapper {

    @Select("SELECT permission_id FROM t_role_permission WHERE role_id = #{roleId}")
    List<Long> findPermIdsByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT id, perm_code, perm_name, module, action, sort_order FROM t_permission ORDER BY module, sort_order")
    List<Map<String, Object>> findAllPermissions();

    @Delete("DELETE FROM t_role_permission WHERE role_id = #{roleId}")
    void deleteByRoleId(@Param("roleId") Long roleId);

    @Insert("INSERT INTO t_role_permission (role_id, permission_id) VALUES (#{roleId}, #{permId})")
    void insertRolePerm(@Param("roleId") Long roleId, @Param("permId") Long permId);
}
