package com.gydl.djbh.service;

import com.gydl.djbh.dto.req.ProjectQueryReq;
import com.gydl.djbh.dto.req.ProjectSaveReq;
import com.gydl.djbh.dto.resp.PageResult;
import com.gydl.djbh.dto.resp.ProjectDetailResp;

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
    void export(ProjectQueryReq req, jakarta.servlet.http.HttpServletResponse response) throws Exception;
    
    // 统计方法
    List<Map<String, Object>> statsByManager(String year);
    List<Map<String, Object>> statsByStaff(String year);
    List<Map<String, Object>> statsByLevel(String year);
    List<Map<String, Object>> statsByType(String year);
    List<Map<String, Object>> statsByIndustry(String year);
}
