package com.gydl.djbh.dto.req;

import lombok.Data;

@Data
public class LogQueryReq {
    private String username;
    private Integer loginResult;
    private Integer result;
    private String module;
    private String action;
    private String startTime;
    private String endTime;
    private Integer page = 1;
    private Integer pageSize = 20;
}
