package com.gydl.djbh.controller;

import com.gydl.djbh.dto.req.StaffQueryReq;
import com.gydl.djbh.dto.resp.PageResult;
import com.gydl.djbh.dto.resp.Result;
import com.gydl.djbh.entity.TStaff;
import com.gydl.djbh.service.StaffService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 人员管理控制器
 */
@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    /** 分页查询 */
    @GetMapping("/page")
    @PreAuthorize("hasAnyAuthority('system:staff','project:view:all','project:view:own')")
    public Result<PageResult<Map<String, Object>>> page(StaffQueryReq req) {
        return Result.ok(staffService.page(req));
    }

    /** 获取所有在职人员（下拉列表用） */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(staffService.listActive());
    }

    /** 按职位筛选在职人员（下拉列表用） */
    @GetMapping("/listByPosition")
    public Result<List<Map<String, Object>>> listByPosition(@RequestParam(required = false) String position) {
        return Result.ok(staffService.listByPosition(position));
    }

    /** 详情 */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:staff')")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.ok(staffService.detail(id));
    }

    /** 新增 */
    @PostMapping
    @PreAuthorize("hasAuthority('system:staff')")
    public Result<Long> create(@RequestBody TStaff staff) {
        return Result.ok("新增成功", staffService.create(staff));
    }

    /** 编辑 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:staff')")
    public Result<?> update(@PathVariable Long id, @RequestBody TStaff staff) {
        staffService.update(id, staff);
        return Result.ok("更新成功");
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:staff')")
    public Result<?> delete(@PathVariable Long id) {
        staffService.delete(id);
        return Result.ok("删除成功");
    }

    /** 启用/停用（切换） */
    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('system:staff')")
    public Result<?> toggle(@PathVariable Long id) {
        staffService.toggleStatus(id);
        return Result.ok("操作成功");
    }

    /** 设置状态（前端传入具体状态值） */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:staff')")
    public Result<?> setStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = body.get("status") != null ? Integer.valueOf(body.get("status").toString()) : null;
        if (status == null) return Result.fail("状态值不能为空");
        staffService.setStatus(id, status);
        return Result.ok("操作成功");
    }

    /** 导出人员列表（Excel） */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('system:staff')")
    public void export(StaffQueryReq req, HttpServletResponse response) throws Exception {
        staffService.exportExcel(req, response);
    }

    /** 下载导入模板 */
    @GetMapping("/import/template")
    @PreAuthorize("hasAuthority('system:staff')")
    public void downloadImportTemplate(HttpServletResponse response) throws Exception {
        staffService.downloadImportTemplate(response);
    }

    /** 导入人员（Excel） */
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('system:staff')")
    public Result<Map<String, Object>> importStaff(@RequestParam("file") MultipartFile file) throws Exception {
        return Result.ok(staffService.importStaff(file));
    }
}
