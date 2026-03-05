package com.gydl.djbh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gydl.djbh.entity.TSysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TSysConfigMapper extends BaseMapper<TSysConfig> {

    @Select("SELECT * FROM t_sys_config WHERE config_key = #{key} LIMIT 1")
    TSysConfig findByKey(@Param("key") String key);
}
