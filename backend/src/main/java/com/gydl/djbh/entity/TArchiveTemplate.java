package com.gydl.djbh.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 归档模板 */
@Data
@TableName("t_archive_template")
public class TArchiveTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 模板名称 */
    private String templateName;
    /** 模板编码，如: project_plan_electric_3 */
    private String templateCode;
    /** 模板分类: 项目计划书/任务书/报告等 */
    private String templateCategory;
    /** 文件名 */
    private String fileName;
    /** 文件存储路径 */
    private String filePath;
    /** 文件大小(字节) */
    private Long fileSize;
    /** 版本号 */
    private Integer version;
    /** 状态: 1=启用 0=停用 */
    private Integer status;
    private String remark;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    private String dataHmac;
}
