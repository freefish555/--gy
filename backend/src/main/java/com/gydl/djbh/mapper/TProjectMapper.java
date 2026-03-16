package com.gydl.djbh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gydl.djbh.entity.TProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TProjectMapper extends BaseMapper<TProject> {

    @Select("<script>" +
            "SELECT p.*, pm.real_name as project_manager_name, pl.real_name as project_leader_name, " +
            "dt.item_label as project_type_name, di.item_label as industry_name " +
            "FROM t_project p " +
            "LEFT JOIN t_staff pm ON p.project_manager_id = pm.id " +
            "LEFT JOIN t_staff pl ON p.project_leader_id = pl.id " +
            "LEFT JOIN t_dict_item dt ON p.project_type_id = dt.id " +
            "LEFT JOIN t_dict_item di ON p.industry_id = di.id " +
            "WHERE 1=1 " +
            "<if test='projectNo != null and projectNo != \"\"'>AND p.project_no LIKE CONCAT('%',#{projectNo},'%') </if>" +
            "<if test='projectName != null and projectName != \"\"'>AND p.project_name LIKE CONCAT('%',#{projectName},'%') </if>" +
            "<if test='customerName != null and customerName != \"\"'>AND p.customer_name LIKE CONCAT('%',#{customerName},'%') </if>" +
            "<if test='projectManagerId != null'>AND p.project_manager_id = #{projectManagerId} </if>" +
            "<if test='projectLeaderId != null'>AND p.project_leader_id = #{projectLeaderId} </if>" +
            "<if test='projectTypeId != null'>AND p.project_type_id = #{projectTypeId} </if>" +
            "<if test='industryId != null'>AND p.industry_id = #{industryId} </if>" +
            "<if test='yearBelong != null and yearBelong != \"\"'>AND p.year_belong = #{yearBelong} </if>" +
            "ORDER BY p.created_at DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<TProject> findPage(@Param("projectNo") String projectNo, @Param("projectName") String projectName,
                             @Param("customerName") String customerName, @Param("projectManagerId") Long projectManagerId,
                             @Param("projectLeaderId") Long projectLeaderId,
                             @Param("projectTypeId") Long projectTypeId, @Param("industryId") Long industryId,
                             @Param("yearBelong") String yearBelong,
                             @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM t_project p WHERE 1=1 " +
            "<if test='projectNo != null and projectNo != \"\"'>AND p.project_no LIKE CONCAT('%',#{projectNo},'%') </if>" +
            "<if test='projectName != null and projectName != \"\"'>AND p.project_name LIKE CONCAT('%',#{projectName},'%') </if>" +
            "<if test='customerName != null and customerName != \"\"'>AND p.customer_name LIKE CONCAT('%',#{customerName},'%') </if>" +
            "<if test='projectManagerId != null'>AND p.project_manager_id = #{projectManagerId} </if>" +
            "<if test='projectLeaderId != null'>AND p.project_leader_id = #{projectLeaderId} </if>" +
            "<if test='projectTypeId != null'>AND p.project_type_id = #{projectTypeId} </if>" +
            "<if test='industryId != null'>AND p.industry_id = #{industryId} </if>" +
            "<if test='yearBelong != null and yearBelong != \"\"'>AND p.year_belong = #{yearBelong} </if>" +
            "</script>")
    long countPage(@Param("projectNo") String projectNo, @Param("projectName") String projectName,
                    @Param("customerName") String customerName, @Param("projectManagerId") Long projectManagerId,
                    @Param("projectLeaderId") Long projectLeaderId,
                    @Param("projectTypeId") Long projectTypeId, @Param("industryId") Long industryId,
                    @Param("yearBelong") String yearBelong);

    @Select("SELECT p.*, pm.real_name as project_manager_name, pl.real_name as project_leader_name, " +
            "dt.item_label as project_type_name, di.item_label as industry_name " +
            "FROM t_project p " +
            "LEFT JOIN t_staff pm ON p.project_manager_id = pm.id " +
            "LEFT JOIN t_staff pl ON p.project_leader_id = pl.id " +
            "LEFT JOIN t_dict_item dt ON p.project_type_id = dt.id " +
            "LEFT JOIN t_dict_item di ON p.industry_id = di.id " +
            "WHERE p.id = #{id}")
    TProject findByIdWithInfo(@Param("id") Long id);

    @Select("<script>" +
            "SELECT pm.real_name as name, COUNT(p.id) as cnt " +
            "FROM t_project p LEFT JOIN t_staff pm ON p.project_manager_id = pm.id " +
            "WHERE 1=1 " +
            "<if test='year != null and year != \"\"'>AND p.year_belong = #{year} </if>" +
            "GROUP BY p.project_manager_id, pm.real_name ORDER BY cnt DESC" +
            "</script>")
    List<Map<String, Object>> statsByManager(@Param("year") String year);

    @Select("<script>" +
            "SELECT di.item_label as name, COUNT(p.id) as cnt " +
            "FROM t_project p LEFT JOIN t_dict_item di ON p.project_type_id = di.id " +
            "WHERE 1=1 " +
            "<if test='year != null and year != \"\"'>AND p.year_belong = #{year} </if>" +
            "GROUP BY p.project_type_id, di.item_label ORDER BY cnt DESC" +
            "</script>")
    List<Map<String, Object>> statsByType(@Param("year") String year);

    @Select("<script>" +
            "SELECT di.item_label as name, COUNT(p.id) as cnt " +
            "FROM t_project p LEFT JOIN t_dict_item di ON p.industry_id = di.id " +
            "WHERE 1=1 " +
            "<if test='year != null and year != \"\"'>AND p.year_belong = #{year} </if>" +
            "GROUP BY p.industry_id, di.item_label ORDER BY cnt DESC" +
            "</script>")
    List<Map<String, Object>> statsByIndustry(@Param("year") String year);
}
