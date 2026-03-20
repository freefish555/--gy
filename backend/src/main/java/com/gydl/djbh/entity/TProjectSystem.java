package com.gydl.djbh.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 被测系统子表 */
@Data
@TableName("t_project_system")
public class TProjectSystem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Integer sysSeq;
    /** SM4加密 */
    private String sysName;
    private Integer sysLevel;
    private String evalIndex;
    private String recordNo;
    /** 编写人ID（关联t_staff） */
    private Long writerId;
    /** 审核人员ID（关联t_staff） */
    private Long reviewerId;
    /** 报告结论（字典REPORT_CONCLUSION：FH/JBFH/BFH） */
    private String reportConclusion;
    /** 质量审核得分(0-100) */
    private Integer qualityScore;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    private String dataHmac;
}
