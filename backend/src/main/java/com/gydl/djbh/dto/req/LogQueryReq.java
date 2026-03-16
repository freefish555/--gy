package com.gydl.djbh.dto.req;

import lombok.Data;

@Data
public class LogQueryReq {
    private String username;
    /** 登录状态（前端字段名） */
    private Integer loginStatus;
    /** 登录状态（兼容字段名） */
    private Integer loginResult;
    /** 操作结果（前端字段名） */
    private Integer opResult;
    /** 操作结果（兼容字段名） */
    private Integer result;
    private String module;
    private String action;
    /** 时间范围（前端字段名） */
    private String timeFrom;
    private String timeTo;
    /** 时间范围（兼容字段名） */
    private String startTime;
    private String endTime;
    /** 分页参数 */
    private Integer pageNum = 1;
    private Integer page = 1;
    private Integer pageSize = 20;

    /** 获取登录结果过滤值（兼容 loginStatus 和 loginResult） */
    public Integer getLoginResultFilter() {
        if (loginStatus != null) return loginStatus;
        return loginResult;
    }

    /** 获取操作结果过滤值（兼容 opResult 和 result） */
    public Integer getOpResultFilter() {
        if (opResult != null) return opResult;
        return result;
    }

    /** 获取页码（兼容 pageNum 和 page） */
    public int getPage() {
        if (pageNum != null && pageNum > 0) return pageNum;
        if (page != null && page > 0) return page;
        return 1;
    }

    /** 获取开始时间（兼容 timeFrom 和 startTime） */
    public String getStartTime() {
        if (timeFrom != null && !timeFrom.isEmpty()) return timeFrom;
        return startTime;
    }

    /** 获取结束时间（兼容 timeTo 和 endTime） */
    public String getEndTime() {
        if (timeTo != null && !timeTo.isEmpty()) return timeTo;
        return endTime;
    }
}
