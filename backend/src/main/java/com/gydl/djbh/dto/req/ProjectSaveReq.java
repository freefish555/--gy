package com.gydl.djbh.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 项目新增/编辑请求
 */
@Data
public class ProjectSaveReq {

    // ====== Tab1 基本信息 ======
    @NotBlank(message = "项目编号不能为空")
    private String projectNo;

    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    @NotBlank(message = "客户名称不能为空")
    private String customerName;

    private String customerAddress;
    private String customerContact;
    private String customerPhone;

    /** 系统名称合并（前端自动生成或手动编辑） */
    private String systemNameMerged;

    /** 二级系统数量（前端自动统计或手动编辑） */
    private Integer sysCountL2;

    /** 三级系统数量（前端自动统计或手动编辑） */
    private Integer sysCountL3;

    private Long projectTypeId;
    private Long industryId;
    private Integer paperArchived;
    private Integer electronicArchived;
    private String contractDate;
    private String contractAmount;
    private String yearBelong;
    private String businessPerson;
    private String projectRegion;
    private String remark;
    private String reportMailDate;
    private String reportMailNo;

    // ====== Tab2 被测系统列表 ======
    @Valid
    private List<ProjectSystemItem> systems;

    // ====== Tab3 人员分配 ======
    private Long projectManagerId;
    private Long projectLeaderId;
    private List<Long> registeredEvaluatorIds;
    private List<Long> actualMemberIds;
    private List<Long> surveyEditorIds;
    private List<Long> planEditorIds;
    private List<Long> reportEditorIds;
    private List<Long> networkEvaluatorIds;
    private List<Long> hostEvaluatorIds;
    private List<Long> physicalEvaluatorIds;
    private List<Long> toolScannerIds;
    private List<Long> pentestMemberIds;

    // ====== Tab4 阶段时间 ======
    private String taskAppointDate;
    private String phasePrepare;
    private String phasePlan;
    private String phaseOnsite;
    private String phaseReport;

    /**
     * 被测系统子表项
     */
    @Data
    public static class ProjectSystemItem {
        private Integer sysSeq;
        @NotBlank(message = "系统名称不能为空")
        private String sysName;
        private Integer sysLevel;
        private String evalIndex;
        private String recordNo;
        /** 编写人ID */
        private Long writerId;
        /** 审核人员ID */
        private Long reviewerId;
        /** 报告结论 */
        private String reportConclusion;
        /** 质量审核得分 */
        private Integer qualityScore;
    }
}
