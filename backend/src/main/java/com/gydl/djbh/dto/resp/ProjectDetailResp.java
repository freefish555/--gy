package com.gydl.djbh.dto.resp;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 项目详情响应DTO
 */
@Data
public class ProjectDetailResp {
    private Long id;
    private String projectNo;
    private String projectName;
    private String customerName;
    private String customerAddress;
    private String customerContact;
    private String customerPhone;
    private String systemNameMerged;
    private Integer sysCountL2;
    private Integer sysCountL3;

    // 字典关联
    private Long projectTypeId;
    private String projectTypeName;
    private Long industryId;
    private String industryName;

    // 人员
    private Long projectManagerId;
    private String projectManagerName;
    private Long projectLeaderId;
    private String projectLeaderName;

    // 合同
    private LocalDate contractDate;
    private String contractAmount;

    // 归档标识
    private Integer paperArchived;
    private Integer electronicArchived;

    // 阶段时间
    private String phasePrepare;
    private String phasePlan;
    private String phaseOnsite;
    private String phaseReport;
    private LocalDate taskAppointDate;

    // 其他
    private String yearBelong;
    private String businessPerson;
    private String projectRegion;
    private String remark;
    private LocalDate reportMailDate;
    private String reportMailNo;

    // 创建人（用于前端判断当前用户是否为项目创建者）
    private Long createdBy;

    // 时间戳
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 系统总数（L2 + L3）
    private Integer sysCount;

    // 项目组成员（role_type=registered_evaluator的成员姓名，顿号分隔）
    private String projectGroupMembers;

    // 实际测评人员（role_type=actual_member的成员姓名，顿号分隔）
    private String actualMemberNames;

    // 各角色人员姓名汇总（用于宽表导出）
    private String surveyEditorNames;
    private String planEditorNames;
    private String reportEditorNames;
    private String networkEvaluatorNames;
    private String hostEvaluatorNames;
    private String physicalEvaluatorNames;
    private String toolScannerNames;
    private String pentestMemberNames;

    // 被测系统列表
    private List<Map<String, Object>> systems;

    // 项目人员列表（各角色）
    private List<Map<String, Object>> members;
}
