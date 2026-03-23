package com.gydl.djbh.dto.req;

import lombok.Data;

@Data
public class StaffQueryReq {
    private String keyword;
    private String realName;   // 前端也可能传 realName 作为搜索字段
    private String department;
    private String roleLevel;
    private Integer status;
    private Integer page = 1;
    private Integer pageNum = 1;   // 前端使用 pageNum
    private Integer pageSize = 20;

    /** 兼容前端的 pageNum 字段 */
    public int getPage() {
        // 优先使用 pageNum（前端传的），其次使用 page
        if (pageNum != null && pageNum > 0) return pageNum;
        if (page != null && page > 0) return page;
        return 1;
    }

    /** 获取关键字（兼容 keyword 和 realName 字段） */
    public String getSearchKeyword() {
        if (keyword != null && !keyword.isEmpty()) return keyword;
        if (realName != null && !realName.isEmpty()) return realName;
        return null;
    }
}
