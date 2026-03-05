package com.gydl.djbh.service;

import com.gydl.djbh.dto.req.LogQueryReq;
import com.gydl.djbh.dto.resp.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 日志服务
 */
public interface LogService {
    void recordLoginSuccess(String username, String realName, String clientIp);
    void recordLoginFail(String username, String clientIp, String reason);
    void recordOperation(String module, String action, String description, String result);
    PageResult<Map<String, Object>> pageLoginLog(LogQueryReq req);
    PageResult<Map<String, Object>> pageOperationLog(LogQueryReq req);
    void exportLoginLog(LogQueryReq req, jakarta.servlet.http.HttpServletResponse response) throws Exception;
    void exportOperationLog(LogQueryReq req, jakarta.servlet.http.HttpServletResponse response) throws Exception;
}
