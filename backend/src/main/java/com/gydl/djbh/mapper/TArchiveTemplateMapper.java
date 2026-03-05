package com.gydl.djbh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gydl.djbh.entity.TArchiveTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TArchiveTemplateMapper extends BaseMapper<TArchiveTemplate> {

    @Select("SELECT * FROM t_archive_template WHERE status = 1 ORDER BY template_category, template_name")
    List<TArchiveTemplate> findAllActive();
}
