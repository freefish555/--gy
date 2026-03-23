package com.gydl.djbh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gydl.djbh.entity.TRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TRoleMapper extends BaseMapper<TRole> {

    @Select("SELECT * FROM t_role WHERE status = 1 ORDER BY id")
    List<TRole> findAllActive();
}
