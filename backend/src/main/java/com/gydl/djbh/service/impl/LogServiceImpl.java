package com.gydl.djbh.service.impl;

import com.gydl.djbh.dto.req.LogQueryReq;
import com.gydl.djbh.dto.resp.PageResult;
import com.gydl.djbh.entity.TLoginLog;
import com.gydl.djbh.entity.TOperationLog;
import com.gydl.djbh.mapper.TLoginLogMapper;
import com.gydl.djbh.mapper.TOperationLogMapper;
import com.gydl.djbh.service.LogService;
import com.gydl.djbh.utils.SecurityContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final TLoginLogMapper loginLogMapper;
    private final TOperationLogMapper operationLogMapper;

    @Override
    public void recordLoginSuccess(String username, String realName, String clientIp) {
        TLoginLog loginLog = new TLoginLog();
        loginLog.setUsername(username);
        loginLog.setRealName(realName);
        loginLog.setLoginIp(clientIp);
        loginLog.setLoginStatus(1);
        loginLogMapper.insert(loginLog);
    }

    @Override
    public void recordLoginFail(String username, String clientIp, String reason) {
        TLoginLog loginLog = new TLoginLog();
        loginLog.setUsername(username);
        loginLog.setLoginIp(clientIp);
        loginLog.setLoginStatus(0);
        loginLog.setFailReason(reason);
        loginLogMapper.insert(loginLog);
    }

    @Override
    public void recordOperation(String module, String action, String description, String result) {
        try {
            TOperationLog opLog = new TOperationLog();
            Long userId = SecurityContextUtil.getCurrentUserIdSafe();
            String username = SecurityContextUtil.getCurrentUsernameSafe();
            opLog.setUserId(userId);
            opLog.setUsername(username != null ? username : "SYSTEM");
            opLog.setModule(module);
            opLog.setActionType(action);
            opLog.setActionDesc(description);
            opLog.setOpResult("SUCCESS".equalsIgnoreCase(result) ? 1 : 0);
            // 获取客户端IP
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    HttpServletRequest req = attrs.getRequest();
                    String ip = req.getHeader("X-Forwarded-For");
                    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                        ip = req.getHeader("X-Real-IP");
                    }
                    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                        ip = req.getRemoteAddr();
                    }
                    if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
                    opLog.setOpIp(ip);
                }
            } catch (Exception ignored) {}
            operationLogMapper.insert(opLog);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    @Override
    public PageResult<Map<String, Object>> pageLoginLog(LogQueryReq req) {
        int offset = (req.getPage() - 1) * req.getPageSize();
        List<TLoginLog> list = loginLogMapper.findPage(
                req.getUsername(), req.getLoginResultFilter(),
                req.getStartTime(), req.getEndTime(),
                offset, req.getPageSize());
        long total = loginLogMapper.countPage(
                req.getUsername(), req.getLoginResultFilter(),
                req.getStartTime(), req.getEndTime());

        List<Map<String, Object>> result = new ArrayList<>();
        for (TLoginLog log : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", log.getId());
            item.put("username", log.getUsername());
            item.put("realName", log.getRealName());
            item.put("loginIp", log.getLoginIp());
            item.put("loginStatus", log.getLoginStatus());
            item.put("failReason", log.getFailReason());
            item.put("loginAt", log.getLoginAt());
            item.put("logoutAt", log.getLogoutAt());
            item.put("sessionDuration", log.getSessionDuration());
            result.add(item);
        }
        return PageResult.of(result, total, req.getPage(), req.getPageSize());
    }

    @Override
    public PageResult<Map<String, Object>> pageOperationLog(LogQueryReq req) {
        int offset = (req.getPage() - 1) * req.getPageSize();
        List<TOperationLog> list = operationLogMapper.findPage(
                req.getUsername(), req.getModule(), req.getAction(), req.getOpResultFilter(),
                req.getStartTime(), req.getEndTime(), offset, req.getPageSize());
        long total = operationLogMapper.countPage(
                req.getUsername(), req.getModule(), req.getAction(), req.getOpResultFilter(),
                req.getStartTime(), req.getEndTime());

        List<Map<String, Object>> result = new ArrayList<>();
        for (TOperationLog log : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", log.getId());
            item.put("userId", log.getUserId());
            item.put("username", log.getUsername());
            item.put("module", log.getModule());
            // 前端使用 actionType、actionDesc、opIp、opAt、opResult 字段名
            item.put("action", log.getActionType());
            item.put("actionType", log.getActionType());
            item.put("description", log.getActionDesc());
            item.put("actionDesc", log.getActionDesc());
            item.put("opResult", log.getOpResult());
            item.put("result", log.getOpResult());
            item.put("errorMsg", log.getFailReason());
            item.put("opIp", log.getOpIp());
            item.put("clientIp", log.getOpIp());
            item.put("costMs", log.getCostMs());
            item.put("opAt", log.getOpAt());
            item.put("createdAt", log.getOpAt());
            item.put("requestParams", log.getRequestParams());
            result.add(item);
        }
        return PageResult.of(result, total, req.getPage(), req.getPageSize());
    }

    @Override
    public void exportLoginLog(LogQueryReq req, HttpServletResponse response) throws Exception {
        req.setPageSize(10000);
        req.setPage(1);
        PageResult<Map<String, Object>> pageResult = pageLoginLog(req);
        exportToCsv(response, "login_log.csv",
                new String[]{"用户名", "真实姓名", "登录IP", "登录状态", "失败原因", "登录时间", "退出时间"},
                pageResult.getList(),
                new String[]{"username", "realName", "loginIp", "loginStatus", "failReason", "loginAt", "logoutAt"});
    }

    @Override
    public void exportOperationLog(LogQueryReq req, HttpServletResponse response) throws Exception {
        req.setPageSize(10000);
        req.setPage(1);
        PageResult<Map<String, Object>> pageResult = pageOperationLog(req);
        exportToCsv(response, "operation_log.csv",
                new String[]{"用户名", "模块", "操作类型", "操作描述", "结果", "操作IP", "操作时间"},
                pageResult.getList(),
                new String[]{"username", "module", "actionType", "actionDesc", "opResult", "opIp", "opAt"});
    }

    private void exportToCsv(HttpServletResponse response, String fileName,
                               String[] headers, List<Map<String, Object>> data, String[] fields) throws Exception {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" +
                URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        // BOM头（Excel识别UTF-8）
        sb.append('\uFEFF');
        sb.append(String.join(",", headers)).append("\n");

        for (Map<String, Object> row : data) {
            List<String> values = new ArrayList<>();
            for (String field : fields) {
                Object val = row.get(field);
                String strVal = val == null ? "" : val.toString();
                // CSV转义
                if (strVal.contains(",") || strVal.contains("\"") || strVal.contains("\n")) {
                    strVal = "\"" + strVal.replace("\"", "\"\"") + "\"";
                }
                values.add(strVal);
            }
            sb.append(String.join(",", values)).append("\n");
        }

        response.getWriter().write(sb.toString());
    }
}
