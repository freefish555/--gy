package com.gydl.djbh.controller;

import com.gydl.djbh.dto.req.ProjectQueryReq;
import com.gydl.djbh.dto.req.ProjectSaveReq;
import com.gydl.djbh.dto.resp.PageResult;
import com.gydl.djbh.dto.resp.ProjectDetailResp;
import com.gydl.djbh.dto.resp.Result;
import com.gydl.djbh.service.ProjectService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 项目管理控制器
 */
@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /** 项目列表（分页） */
    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('project:view:all','project:view:own')")
    public Result<PageResult<ProjectDetailResp>> page(ProjectQueryReq req) {
        return Result.ok(projectService.page(req));
    }

    /** 项目详情 */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('project:view:all','project:view:own')")
    public Result<ProjectDetailResp> detail(@PathVariable Long id) {
        return Result.ok(projectService.detail(id));
    }

    /** 新增项目 */
    @PostMapping
    @PreAuthorize("hasAuthority('project:create')")
    public Result<Long> create(@Valid @RequestBody ProjectSaveReq req) {
        Long id = projectService.create(req);
        return Result.ok("项目新增成功", id);
    }

    /** 编辑项目 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('project:update:all','project:update:own')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody ProjectSaveReq req) {
        projectService.update(id, req);
        return Result.ok("项目更新成功");
    }

    /** 删除项目 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('project:delete')")
    public Result<?> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.ok("项目删除成功");
    }

    /** 批量操作 */
    @PostMapping("/batch")
    @PreAuthorize("hasAnyAuthority('project:update:all')")
    public Result<?> batchUpdate(@RequestBody Map<String, Object> body) {
        List<Long> ids = (List<Long>) body.get("ids");
        String action = (String) body.get("action");
        Object value = body.get("value");
        projectService.batchUpdate(ids, action, value);
        return Result.ok("批量操作成功");
    }

    /**
     * 导出Excel（按查询条件）
     * exportAll=true 时导出全部
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('project:export')")
    public void exportExcel(ProjectQueryReq req,
                             @RequestParam(defaultValue = "false") boolean exportAll,
                             HttpServletResponse response) throws Exception {
        if (exportAll) {
            req.setPageSize(-1); // 标记导出全部
        }
        projectService.exportExcel(req, response);
    }

    /** 下载导入模板 */
    @GetMapping("/import/template")
    @PreAuthorize("hasAnyAuthority('project:create','project:update:all')")
    public void downloadImportTemplate(HttpServletResponse response) throws Exception {
        projectService.downloadImportTemplate(response);
    }

    /** 批量导入项目 */
    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('project:create','project:update:all')")
    public Result<Map<String, Object>> importProjects(@RequestParam("file") MultipartFile file) throws Exception {
        Map<String, Object> result = projectService.importProjects(file);
        return Result.ok("导入完成", result);
    }

    /** 项目统计汇总（供前端统计页面使用） */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('project:stats')")
    public Result<?> stats(@RequestParam(required = false) String year) {
        return Result.ok(projectService.statsSummary(year));
    }

    /** 项目统计 - 按项目经理 */
    @GetMapping("/stats/manager")
    @PreAuthorize("hasAuthority('project:stats')")
    public Result<?> statsByManager(@RequestParam(required = false) String year) {
        return Result.ok(projectService.statsByManager(year));
    }

    /** 项目统计 - 按人员 */
    @GetMapping("/stats/staff")
    @PreAuthorize("hasAuthority('project:stats')")
    public Result<?> statsByStaff(@RequestParam(required = false) String year) {
        return Result.ok(projectService.statsByStaff(year));
    }

    /** 项目统计 - 按系统级别 */
    @GetMapping("/stats/level")
    @PreAuthorize("hasAuthority('project:stats')")
    public Result<?> statsByLevel(@RequestParam(required = false) String year) {
        return Result.ok(projectService.statsByLevel(year));
    }

    /** 项目统计 - 按项目类型 */
    @GetMapping("/stats/type")
    @PreAuthorize("hasAuthority('project:stats')")
    public Result<?> statsByType(@RequestParam(required = false) String year) {
        return Result.ok(projectService.statsByType(year));
    }

    /** 项目统计 - 按行业 */
    @GetMapping("/stats/industry")
    @PreAuthorize("hasAuthority('project:stats')")
    public Result<?> statsByIndustry(@RequestParam(required = false) String year) {
        return Result.ok(projectService.statsByIndustry(year));
    }
}
