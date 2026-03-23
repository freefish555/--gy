package com.gydl.djbh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gydl.djbh.entity.TStaff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TStaffMapper extends BaseMapper<TStaff> {

    @Select("SELECT s.*, '' as real_name_plain FROM t_staff s WHERE s.status = 1 ORDER BY s.staff_no")
    List<TStaff> findAllActive();

    @Select("SELECT s.id, s.staff_no, s.real_name, s.position, s.role_level, s.cert_no, s.cert_expire, s.phone, s.status " +
            "FROM t_staff s WHERE s.id = #{id}")
    TStaff findById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT s.* FROM t_staff s WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (s.staff_no LIKE CONCAT('%',#{keyword},'%') OR s.department LIKE CONCAT('%',#{keyword},'%')) " +
            "</if>" +
            "<if test='department != null and department != \"\"'>" +
            "AND s.department LIKE CONCAT('%',#{department},'%') " +
            "</if>" +
            "<if test='roleLevel != null and roleLevel != \"\"'>" +
            "AND s.role_level = #{roleLevel} " +
            "</if>" +
            "<if test='status != null'>" +
            "AND s.status = #{status} " +
            "</if>" +
            "ORDER BY s.staff_no " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<TStaff> findPage(@Param("keyword") String keyword, @Param("roleLevel") String roleLevel,
                          @Param("status") Integer status, @Param("offset") int offset,
                          @Param("pageSize") int pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM t_staff s WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (s.staff_no LIKE CONCAT('%',#{keyword},'%') OR s.department LIKE CONCAT('%',#{keyword},'%')) " +
            "</if>" +
            "<if test='department != null and department != \"\"'>" +
            "AND s.department LIKE CONCAT('%',#{department},'%') " +
            "</if>" +
            "<if test='roleLevel != null and roleLevel != \"\"'>" +
            "AND s.role_level = #{roleLevel} " +
            "</if>" +
            "<if test='status != null'>" +
            "AND s.status = #{status} " +
            "</if>" +
            "</script>")
    long countPage(@Param("keyword") String keyword, @Param("roleLevel") String roleLevel,
                   @Param("status") Integer status);

    @Select("<script>" +
            "SELECT s.* FROM t_staff s WHERE 1=1 " +
            "<if test='department != null and department != \"\"'>" +
            "AND s.department LIKE CONCAT('%',#{department},'%') " +
            "</if>" +
            "<if test='roleLevel != null and roleLevel != \"\"'>" +
            "AND s.role_level = #{roleLevel} " +
            "</if>" +
            "<if test='status != null'>" +
            "AND s.status = #{status} " +
            "</if>" +
            "ORDER BY s.staff_no" +
            "</script>")
    List<TStaff> findAllForFilter(@Param("department") String department,
                                   @Param("roleLevel") String roleLevel,
                                   @Param("status") Integer status);
}
