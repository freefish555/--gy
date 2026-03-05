package com.gydl.djbh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gydl.djbh.entity.TDict;
import com.gydl.djbh.entity.TDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TDictMapper extends BaseMapper<TDict> {

    @Select("SELECT * FROM t_dict ORDER BY sort_order")
    List<TDict> findAll();

    @Select("SELECT * FROM t_dict WHERE dict_code = #{dictCode} LIMIT 1")
    TDict findByCode(@Param("dictCode") String dictCode);
}
