package com.gydl.djbh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gydl.djbh.entity.TOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TOperationLogMapper extends BaseMapper<TOperationLog> {

    @Select("<script>" +
            "SELECT * FROM t_operation_log WHERE 1=1 " +
            "<if test='username != null and username != \"\"'>AND username LIKE CONCAT('%',#{username},'%') </if>" +
            "<if test='module != null and module != \"\"'>AND module = #{module} </if>" +
            "<if test='action != null and action != \"\"'>AND action_type = #{action} </if>" +
            "<if test='result != null'>AND op_result = #{result} </if>" +
            "<if test='startTime != null'>AND op_at &gt;= #{startTime} </if>" +
            "<if test='endTime != null'>AND op_at &lt;= #{endTime} </if>" +
            "ORDER BY op_at DESC LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<TOperationLog> findPage(@Param("username") String username, @Param("module") String module,
                                  @Param("action") String action, @Param("result") Integer result,
                                  @Param("startTime") String startTime, @Param("endTime") String endTime,
                                  @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM t_operation_log WHERE 1=1 " +
            "<if test='username != null and username != \"\"'>AND username LIKE CONCAT('%',#{username},'%') </if>" +
            "<if test='module != null and module != \"\"'>AND module = #{module} </if>" +
            "<if test='action != null and action != \"\"'>AND action_type = #{action} </if>" +
            "<if test='result != null'>AND op_result = #{result} </if>" +
            "<if test='startTime != null'>AND op_at &gt;= #{startTime} </if>" +
            "<if test='endTime != null'>AND op_at &lt;= #{endTime} </if>" +
            "</script>")
    long countPage(@Param("username") String username, @Param("module") String module,
                   @Param("action") String action, @Param("result") Integer result,
                   @Param("startTime") String startTime, @Param("endTime") String endTime);
}
