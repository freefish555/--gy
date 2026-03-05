package com.gydl.djbh.controller;

import com.gydl.djbh.dto.req.ProjectQueryReq;
import com.gydl.djbh.dto.req.ProjectSaveReq;
import com.gydl.djbh.dto.resp.PageResult;
import com.gydl.djbh.dto.resp.ProjectDetailResp;
import com.gydl.djbh.dto.resp.Result;
import com.gydl.djbh.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 项目列表（分页）
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('project:view:all','project:view:own')")
    public Result<PageResult<ProjectDetailResp>> page(ProjectQueryReq req) {
        return Result.ok(projectService.page(req));
    }

    /**
     * 项目详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('project:view:all','project:view:own')")
    public Result<ProjectDetailResp> detail(@PathVariable Long id) {
        return Result.ok(projectService.detail(id));
    }

    /**
     * 新增项目
     */
    @PostMapping
    @PreAuthorize("hasAuthority('project:create')")
    public Result<Long> create(@Valid @RequestBody ProjectSaveReq req) {
        Long id = projectService.create(req);
        return Result.ok("项目新增成功", id);
    }

    /**
     * 编辑项目
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('project:update:all','project:update:own')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody ProjectSaveReq req) {
        projectService.update(id, req);
        return Result.ok("项目更新成功");
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('project:delete')")
    public Result<?> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.ok("项目删除成功");
    }

    /**
     * 批量操作（批量修改状态/分配经理）
     */
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
     * 导出项目列表
     */
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('project:export')")
    public Result<String> export(@RequestBody ProjectQueryReq req,
                                  jakarta.servlet.http.HttpServletResponse response) throws Exception {
        projectService.export(req, response);
        return Result.ok("导出成功");
    }

    /**
     * 项目统计 - 按项目经理
     */
    @GetMapping("/stats/manager")
    @PreAuthorize("hasAuthority('project:stats')")
    public Result<?> statsByManager(@RequestParam(required = false) String year) {
        return Result.ok(projectService.statsByManager(year));
    }

    /**
     * 项目统计 - 按人员名称
     */
    @GetMapping("/stats/staff")
    @PreAuthorize("hasAuthority('project:stats')")
    public Result<?> statsByStaff(@RequestParam(required = false) String year) {
        return Result.ok(projectService.statsByStaff(year));
    }

    /**
     * 项目统计 - 按系统级别
     */
    @GetMapping("/stats/level")
    @PreAuthorize("hasAuthority('project:stats')")
    public Result<?> statsByLevel(@RequestParam(required = false) String year) {
        return Result.ok(projectService.statsByLevel(year));
    }

    /**
     * 项目统计 - 按项目类型
     */
    @GetMapping("/stats/type")
    @PreAuthorize("hasAuthority('project:stats')")
    public Result<?> statsByType(@RequestParam(required = false) String year) {
        return Result.ok(projectService.statsByType(year));
    }

    /**
     * 项目统计 - 按行业
     */
    @GetMapping("/stats/industry")
    @PreAuthorize("hasAuthority('project:stats')")
    public Result<?> statsByIndustry(@RequestParam(required = false) String year) {
        return Result.ok(projectService.statsByIndustry(year));
    }
}
