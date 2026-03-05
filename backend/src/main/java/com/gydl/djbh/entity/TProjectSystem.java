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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    private String dataHmac;
}
