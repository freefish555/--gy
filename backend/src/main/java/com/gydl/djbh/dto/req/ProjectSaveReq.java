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

    @NotBlank(message = "系统名称合并不能为空")
    private String systemNameMerged;

    @NotBlank(message = "任务编号不能为空")
    private String taskNo;

    private Long projectTypeId;
    private Long industryId;
    private Integer projectStatus;
    private Integer paperArchived;
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
        @NotNull(message = "系统序号不能为空")
        private Integer sysSeq;
        @NotBlank(message = "系统名称不能为空")
        private String sysName;
        @NotNull(message = "系统等级不能为空")
        private Integer sysLevel;
        @NotBlank(message = "测评指标不能为空")
        private String evalIndex;
        private String recordNo;
    }
}
