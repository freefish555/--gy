package com.gydl.djbh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gydl.djbh.entity.TProjectSystem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TProjectSystemMapper extends BaseMapper<TProjectSystem> {

    @Select("SELECT * FROM t_project_system WHERE project_id = #{projectId} ORDER BY sys_no")
    List<TProjectSystem> findByProjectId(@Param("projectId") Long projectId);

    @Delete("DELETE FROM t_project_system WHERE project_id = #{projectId}")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
