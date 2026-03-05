package com.gydl.djbh.controller;

import com.gydl.djbh.dto.req.LogQueryReq;
import com.gydl.djbh.dto.resp.PageResult;
import com.gydl.djbh.dto.resp.Result;
import com.gydl.djbh.service.LogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 日志管理控制器
 */
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

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
}
