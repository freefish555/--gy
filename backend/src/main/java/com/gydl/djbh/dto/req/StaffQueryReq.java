package com.gydl.djbh.dto.req;

import lombok.Data;

@Data
public class StaffQueryReq {
    private String keyword;
    private String roleLevel;
    private Integer status;
    private Integer page = 1;
    private Integer pageSize = 20;
}
