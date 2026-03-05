package com.gydl.djbh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gydl.djbh.entity.TUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TUserMapper extends BaseMapper<TUser> {

    @Select("SELECT u.*, r.role_code, r.role_name FROM t_user u " +
            "LEFT JOIN t_role r ON u.role_id = r.id " +
            "WHERE u.username = #{username}")
    TUser findByUsername(@Param("username") String username);

    @Select("SELECT p.perm_code FROM t_permission p " +
            "INNER JOIN t_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN t_role r ON rp.role_id = r.id " +
            "INNER JOIN t_user u ON u.role_id = r.id " +
            "WHERE u.id = #{userId}")
    List<String> findPermissionsByUserId(@Param("userId") Long userId);

    @Select("SELECT u.*, r.role_code, r.role_name FROM t_user u " +
            "LEFT JOIN t_role r ON u.role_id = r.id " +
            "WHERE u.id = #{id}")
    TUser findByIdWithRole(@Param("id") Long id);
}
