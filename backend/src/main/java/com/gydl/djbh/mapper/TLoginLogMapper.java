package com.gydl.djbh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gydl.djbh.entity.TLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TLoginLogMapper extends BaseMapper<TLoginLog> {

    @Select("<script>" +
            "SELECT * FROM t_login_log WHERE 1=1 " +
            "<if test='username != null and username != \"\"'>AND username LIKE CONCAT('%',#{username},'%') </if>" +
            "<if test='loginResult != null'>AND login_status = #{loginResult} </if>" +
            "<if test='startTime != null'>AND login_at &gt;= #{startTime} </if>" +
            "<if test='endTime != null'>AND login_at &lt;= #{endTime} </if>" +
            "ORDER BY login_at DESC LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<TLoginLog> findPage(@Param("username") String username, @Param("loginResult") Integer loginResult,
                             @Param("startTime") String startTime, @Param("endTime") String endTime,
                             @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM t_login_log WHERE 1=1 " +
            "<if test='username != null and username != \"\"'>AND username LIKE CONCAT('%',#{username},'%') </if>" +
            "<if test='loginResult != null'>AND login_status = #{loginResult} </if>" +
            "<if test='startTime != null'>AND login_at &gt;= #{startTime} </if>" +
            "<if test='endTime != null'>AND login_at &lt;= #{endTime} </if>" +
            "</script>")
    long countPage(@Param("username") String username, @Param("loginResult") Integer loginResult,
                   @Param("startTime") String startTime, @Param("endTime") String endTime);
}
