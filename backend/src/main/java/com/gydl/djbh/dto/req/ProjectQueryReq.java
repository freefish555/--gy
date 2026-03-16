package com.gydl.djbh.dto.req;

import lombok.Data;

/**
 * 项目查询请求（支持多条件组合查询）
 */
@Data
public class ProjectQueryReq {
    private int pageNum = 1;
    private int pageSize = 20;

    /** 项目编号（模糊） */
    private String projectNo;
    /** 项目名称（模糊，解密后匹配） */
    private String projectName;
    /** 备案编号（模糊，查子表） */
    private String recordNo;
    /** 客户名称（模糊，解密后匹配） */
    private String customerName;
    /** 客户联系人（模糊，解密后匹配） */
    private String customerContact;
    /** 项目经理ID */
    private Long projectManagerId;
    /** 登记测评师ID */
    private Long registeredEvaluatorId;
    /** 项目负责人ID */
    private Long projectLeaderId;
    /** 项目类型字典项ID */
    private Long projectTypeId;
    /** 所属行业字典项ID */
    private Long industryId;
    /** 业务人员（模糊） */
    private String businessPerson;
    /** 合同签订日期起 */
    private String contractDateFrom;
    /** 合同签订日期止 */
    private String contractDateTo;
    /** 所属年份 */
    private String yearBelong;
    /** 排序字段 */
    private String sortField = "created_at";
    /** 排序方向 asc/desc */
    private String sortOrder = "desc";
}
