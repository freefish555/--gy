package com.gydl.djbh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gydl.djbh.entity.TDictItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TDictItemMapper extends BaseMapper<TDictItem> {

    @Select("SELECT * FROM t_dict_item WHERE dict_id = #{dictId} AND status = 1 ORDER BY sort_order")
    List<TDictItem> findByDictId(@Param("dictId") Long dictId);

    @Select("SELECT i.* FROM t_dict_item i INNER JOIN t_dict d ON i.dict_id = d.id " +
            "WHERE d.dict_code = #{dictCode} AND i.status = 1 ORDER BY i.sort_order")
    List<TDictItem> findByDictCode(@Param("dictCode") String dictCode);

    @Delete("DELETE FROM t_dict_item WHERE dict_id = #{dictId}")
    void deleteByDictId(@Param("dictId") Long dictId);

    @Select("SELECT i.id FROM t_dict_item i INNER JOIN t_dict d ON i.dict_id = d.id " +
            "WHERE d.dict_code = #{dictCode} AND i.item_label = #{label} AND i.status = 1 LIMIT 1")
    Long findIdByLabelAndDictCode(@Param("label") String label, @Param("dictCode") String dictCode);
}
