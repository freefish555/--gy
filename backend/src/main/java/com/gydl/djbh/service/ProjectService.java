package com.gydl.djbh.service;

import com.gydl.djbh.dto.req.ProjectQueryReq;
import com.gydl.djbh.dto.req.ProjectSaveReq;
import com.gydl.djbh.dto.resp.PageResult;
import com.gydl.djbh.dto.resp.ProjectDetailResp;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 项目管理服务
 */
public interface ProjectService {
    PageResult<ProjectDetailResp> page(ProjectQueryReq req);
    ProjectDetailResp detail(Long id);
    Long create(ProjectSaveReq req);
    void update(Long id, ProjectSaveReq req);
    void delete(Long id);
    void batchUpdate(List<Long> ids, String action, Object value);

    /** 导出Excel（按查询条件或全部） */
    void exportExcel(ProjectQueryReq req, HttpServletResponse response) throws Exception;

    /** 导出全部项目（完整版多Sheet：项目主表+人员详情+被测系统） */
    void exportExcelFull(HttpServletResponse response) throws Exception;

    /** 下载导入模板 */
    void downloadImportTemplate(HttpServletResponse response) throws Exception;

    /** 批量导入项目 */
    Map<String, Object> importProjects(MultipartFile file) throws Exception;

    // 统计方法
    List<Map<String, Object>> statsByManager(String year);
    List<Map<String, Object>> statsByStaff(String year);
    List<Map<String, Object>> statsByLevel(String year);
    List<Map<String, Object>> statsByType(String year);
    List<Map<String, Object>> statsByIndustry(String year);

    /** 汇总统计（供前端统计页面使用） */
    Map<String, Object> statsSummary(String year);
}
