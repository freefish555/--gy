package com.gydl.djbh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gydl.djbh.entity.TProjectMember;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TProjectMemberMapper extends BaseMapper<TProjectMember> {

    @Select("SELECT * FROM t_project_member WHERE project_id = #{projectId} ORDER BY id")
    List<TProjectMember> findByProjectId(@Param("projectId") Long projectId);

    @Delete("DELETE FROM t_project_member WHERE project_id = #{projectId}")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
