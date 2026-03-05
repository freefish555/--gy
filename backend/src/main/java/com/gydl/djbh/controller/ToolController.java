package com.gydl.djbh.controller;

import com.gydl.djbh.dto.resp.Result;
import com.gydl.djbh.entity.TEvalDevice;
import com.gydl.djbh.entity.TPentestTool;
import com.gydl.djbh.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 测评工具管理控制器
 */
@RestController
@RequestMapping("/tool")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    // ========== 硬件测评设备 ==========

    @GetMapping("/device/list")
    public Result<List<Map<String, Object>>> listDevices() {
        return Result.ok(toolService.listDevices());
    }

    @GetMapping("/device/{id}")
    @PreAuthorize("hasAuthority('system:device')")
    public Result<Map<String, Object>> detailDevice(@PathVariable Long id) {
        return Result.ok(toolService.detailDevice(id));
    }

    @PostMapping("/device")
    @PreAuthorize("hasAuthority('system:device')")
    public Result<Long> createDevice(@RequestBody TEvalDevice device) {
        return Result.ok("新增成功", toolService.createDevice(device));
    }

    @PutMapping("/device/{id}")
    @PreAuthorize("hasAuthority('system:device')")
    public Result<?> updateDevice(@PathVariable Long id, @RequestBody TEvalDevice device) {
        toolService.updateDevice(id, device);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/device/{id}")
    @PreAuthorize("hasAuthority('system:device')")
    public Result<?> deleteDevice(@PathVariable Long id) {
        toolService.deleteDevice(id);
        return Result.ok("删除成功");
    }

    /** 按人员ID查询其设备 */
    @GetMapping("/device/by-staff/{staffId}")
    public Result<List<Map<String, Object>>> devicesByStaff(@PathVariable Long staffId) {
        return Result.ok(toolService.listDevicesByStaff(staffId));
    }

    // ========== 渗透软件工具 ==========

    @GetMapping("/pentest/list")
    public Result<List<Map<String, Object>>> listTools() {
        return Result.ok(toolService.listTools());
    }

    @GetMapping("/pentest/{id}")
    @PreAuthorize("hasAuthority('system:device')")
    public Result<Map<String, Object>> detailTool(@PathVariable Long id) {
        return Result.ok(toolService.detailTool(id));
    }

    @PostMapping("/pentest")
    @PreAuthorize("hasAuthority('system:device')")
    public Result<Long> createTool(@RequestBody TPentestTool tool) {
        return Result.ok("新增成功", toolService.createTool(tool));
    }

    @PutMapping("/pentest/{id}")
    @PreAuthorize("hasAuthority('system:device')")
    public Result<?> updateTool(@PathVariable Long id, @RequestBody TPentestTool tool) {
        toolService.updateTool(id, tool);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/pentest/{id}")
    @PreAuthorize("hasAuthority('system:device')")
    public Result<?> deleteTool(@PathVariable Long id) {
        toolService.deleteTool(id);
        return Result.ok("删除成功");
    }
}
