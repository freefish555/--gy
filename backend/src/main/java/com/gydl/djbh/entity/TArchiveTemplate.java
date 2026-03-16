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
    /** 模板编码，如: project_plan_electric_3 */
    private String templateCode;
    /** 模板名称 */
    private String templateName;
    /** 模板分类: 项目计划书/任务书/报告等 */
    private String templateCategory;
    /** 适用项目类型 */
    private String applicableType;
    /** 适用系统等级 */
    private Integer applicableLevel;
    /** 文件存储路径 */
    private String filePath;
    /** 原始文件名 */
    private String fileOriginalName;
    /** 文件大小(字节) */
    private Long fileSize;
    /** 版本号（字符串，如v1.0） */
    private String version;
    /** 是否默认 1=是 */
    private Integer isDefault;
    /** 状态: 1=启用 0=停用 */
    private Integer status;
    /** 上传人ID */
    private Long uploadBy;
    /** 上传时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadAt;
    private String dataHmac;
}
