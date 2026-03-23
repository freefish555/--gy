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
            "<if test='projectRegion != null and projectRegion != \"\"'>AND p.project_region = #{projectRegion} </if>" +
            "ORDER BY p.created_at DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<TProject> findPage(@Param("projectNo") String projectNo, @Param("projectName") String projectName,
                             @Param("customerName") String customerName, @Param("projectManagerId") Long projectManagerId,
                             @Param("projectLeaderId") Long projectLeaderId,
                             @Param("projectTypeId") Long projectTypeId, @Param("industryId") Long industryId,
                             @Param("yearBelong") String yearBelong,
                             @Param("projectRegion") String projectRegion,
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
            "<if test='projectRegion != null and projectRegion != \"\"'>AND p.project_region = #{projectRegion} </if>" +
            "</script>")
    long countPage(@Param("projectNo") String projectNo, @Param("projectName") String projectName,
                    @Param("customerName") String customerName, @Param("projectManagerId") Long projectManagerId,
                    @Param("projectLeaderId") Long projectLeaderId,
                    @Param("projectTypeId") Long projectTypeId, @Param("industryId") Long industryId,
                    @Param("yearBelong") String yearBelong,
                    @Param("projectRegion") String projectRegion);

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

    /** 按编写人统计被测系统数量（总数、2级、3级、质量审核平均得分） */
    @Select("<script>" +
            "SELECT s.id as writerId, s.real_name as writerName, " +
            "COUNT(ps.id) as sysTotal, " +
            "SUM(CASE WHEN ps.sys_level = 2 THEN 1 ELSE 0 END) as sysL2, " +
            "SUM(CASE WHEN ps.sys_level = 3 THEN 1 ELSE 0 END) as sysL3, " +
            "ROUND(AVG(CASE WHEN ps.quality_score IS NOT NULL THEN ps.quality_score END), 2) as avgQualityScore " +
            "FROM t_project_system ps " +
            "LEFT JOIN t_staff s ON ps.writer_id = s.id " +
            "LEFT JOIN t_project p ON ps.project_id = p.id " +
            "WHERE ps.writer_id IS NOT NULL " +
            "<if test='year != null and year != \"\"'>AND p.year_belong = #{year} </if>" +
            "GROUP BY ps.writer_id, s.real_name ORDER BY sysTotal DESC" +
            "</script>")
    List<Map<String, Object>> statsByWriter(@Param("year") String year);

    /** 按项目经理统计项目数和系统数（项目数、系统总数、2级系统数、3级系统数） */
    @Select("<script>" +
            "SELECT pm.id as managerId, pm.real_name as managerName, " +
            "COUNT(DISTINCT p.id) as projectCnt, " +
            "SUM(COALESCE(p.sys_count_l2, 0) + COALESCE(p.sys_count_l3, 0)) as sysTotal, " +
            "SUM(COALESCE(p.sys_count_l2, 0)) as sysL2, " +
            "SUM(COALESCE(p.sys_count_l3, 0)) as sysL3 " +
            "FROM t_project p " +
            "LEFT JOIN t_staff pm ON p.project_manager_id = pm.id " +
            "WHERE p.project_manager_id IS NOT NULL " +
            "<if test='year != null and year != \"\"'>AND p.year_belong = #{year} </if>" +
            "GROUP BY p.project_manager_id, pm.real_name ORDER BY projectCnt DESC" +
            "</script>")
    List<Map<String, Object>> statsByManagerDetail(@Param("year") String year);

    /** 按项目地区统计项目数 */
    @Select("<script>" +
            "SELECT di.item_label as name, COUNT(p.id) as cnt " +
            "FROM t_project p LEFT JOIN t_dict_item di ON p.project_region = di.item_value " +
            "LEFT JOIN t_dict d ON di.dict_id = d.id AND d.dict_code = 'PROJECT_REGION' " +
            "WHERE p.project_region IS NOT NULL AND p.project_region != '' " +
            "<if test='year != null and year != \"\"'>AND p.year_belong = #{year} </if>" +
            "GROUP BY p.project_region, di.item_label ORDER BY cnt DESC" +
            "</script>")
    List<Map<String, Object>> statsByRegion(@Param("year") String year);

    /** 查询指定年份的所有项目（用于合同金额统计，Java层解密求和） */
    @Select("<script>" +
            "SELECT id, contract_amount, contract_date, year_belong " +
            "FROM t_project WHERE 1=1 " +
            "<if test='year != null and year != \"\"'>AND year_belong = #{year} </if>" +
            "</script>")
    List<Map<String, Object>> findForAmountStats(@Param("year") String year);
}
