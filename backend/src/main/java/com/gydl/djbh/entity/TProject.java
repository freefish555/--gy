package com.gydl.djbh.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目主表实体
 */
@Data
@TableName("t_project")
public class TProject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String projectNo;

    /** SM4加密 */
    private String projectName;

    /** SM4加密 */
    private String customerName;

    /** SM4加密 */
    private String customerAddress;

    /** SM4加密 */
    private String customerContact;

    /** SM4加密 */
    private String customerPhone;

    /** SM4加密 */
    private String systemNameMerged;

    private String taskNo;

    private Long projectTypeId;

    private Long industryId;

    private Long projectManagerId;

    private Long projectLeaderId;

    private LocalDate contractDate;

    /** SM4加密 */
    private String contractAmount;

    /** 0待启动/1已分配/2进行中/3已完成/4电子归档 */
    private Integer projectStatus;

    /** 纸质归档: 0否/1是 */
    private Integer paperArchived;

    private String phasePrepare;

    private String phasePlan;

    private String phaseOnsite;

    private String phaseReport;

    private LocalDate taskAppointDate;

    private String yearBelong;

    /** SM4加密 */
    private String businessPerson;

    private String projectRegion;

    /** SM4加密 */
    private String remark;

    private LocalDate reportMailDate;

    private String reportMailNo;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private String dataHmac;

    // ---- 非DB字段（关联查询） ----
    @TableField(exist = false)
    private String projectManagerName;

    @TableField(exist = false)
    private String projectLeaderName;

    @TableField(exist = false)
    private String projectTypeName;

    @TableField(exist = false)
    private String industryName;
}
