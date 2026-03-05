package com.gydl.djbh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gydl.djbh.entity.TEvalDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TEvalDeviceMapper extends BaseMapper<TEvalDevice> {

    @Select("SELECT d.*, s.real_name as owner_staff_name FROM t_eval_device d " +
            "LEFT JOIN t_staff s ON d.owner_staff_id = s.id " +
            "ORDER BY d.device_no")
    List<TEvalDevice> findAllWithStaff();

    @Select("SELECT d.*, s.real_name as owner_staff_name FROM t_eval_device d " +
            "LEFT JOIN t_staff s ON d.owner_staff_id = s.id " +
            "WHERE d.owner_staff_id = #{staffId} AND d.status = 1 " +
            "ORDER BY d.device_no")
    List<TEvalDevice> findByStaffId(@Param("staffId") Long staffId);

    @Select("SELECT d.*, s.real_name as owner_staff_name FROM t_eval_device d " +
            "LEFT JOIN t_staff s ON d.owner_staff_id = s.id " +
            "WHERE d.id = #{id}")
    TEvalDevice findByIdWithStaff(@Param("id") Long id);
}
