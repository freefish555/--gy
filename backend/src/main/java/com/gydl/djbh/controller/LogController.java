package com.gydl.djbh.controller;

import com.gydl.djbh.dto.req.LogQueryReq;
import com.gydl.djbh.dto.resp.PageResult;
import com.gydl.djbh.dto.resp.Result;
import com.gydl.djbh.service.LogService;
import com.gydl.djbh.service.SysConfigService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 日志管理控制器
 */
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;
    private final SysConfigService sysConfigService;

    /** 登录日志分页 */
    @GetMapping("/login/page")
    @PreAuthorize("hasAuthority('log:login:view')")
    public Result<PageResult<Map<String, Object>>> loginLogPage(LogQueryReq req) {
        return Result.ok(logService.pageLoginLog(req));
    }

    /** 操作日志分页 */
    @GetMapping("/operation/page")
    @PreAuthorize("hasAuthority('log:operation:view')")
    public Result<PageResult<Map<String, Object>>> operationLogPage(LogQueryReq req) {
        return Result.ok(logService.pageOperationLog(req));
    }

    /** 导出登录日志 */
    @GetMapping("/login/export")
    @PreAuthorize("hasAuthority('log:login:view')")
    public void exportLoginLog(LogQueryReq req, HttpServletResponse response) throws Exception {
        logService.exportLoginLog(req, response);
    }

    /** 导出操作日志 */
    @GetMapping("/operation/export")
    @PreAuthorize("hasAuthority('log:operation:view')")
    public void exportOperationLog(LogQueryReq req, HttpServletResponse response) throws Exception {
        logService.exportOperationLog(req, response);
    }

    /** 获取日志服务器配置 */
    @GetMapping("/server/config")
    @PreAuthorize("hasAuthority('log:server:config')")
    public Result<Map<String, Object>> getServerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", Boolean.parseBoolean(sysConfigService.getConfig("LOG_SERVER_ENABLED", "false")));
        config.put("host", sysConfigService.getConfig("LOG_SERVER_HOST", ""));
        config.put("port", Integer.parseInt(sysConfigService.getConfig("LOG_SERVER_PORT", "514")));
        config.put("protocol", sysConfigService.getConfig("LOG_SERVER_PROTOCOL", "UDP"));
        config.put("format", sysConfigService.getConfig("LOG_SERVER_FORMAT", "syslog"));
        return Result.ok(config);
    }

    /** 保存日志服务器配置 */
    @PostMapping("/server/config")
    @PreAuthorize("hasAuthority('log:server:config')")
    public Result<?> saveServerConfig(@RequestBody Map<String, Object> body) {
        sysConfigService.setConfig("LOG_SERVER_ENABLED", String.valueOf(body.getOrDefault("enabled", false)));
        sysConfigService.setConfig("LOG_SERVER_HOST", String.valueOf(body.getOrDefault("host", "")));
        sysConfigService.setConfig("LOG_SERVER_PORT", String.valueOf(body.getOrDefault("port", 514)));
        sysConfigService.setConfig("LOG_SERVER_PROTOCOL", String.valueOf(body.getOrDefault("protocol", "UDP")));
        sysConfigService.setConfig("LOG_SERVER_FORMAT", String.valueOf(body.getOrDefault("format", "syslog")));
        return Result.ok("保存成功");
    }
}
