package com.gydl.djbh.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 操作日志 */
@Data
@TableName("t_operation_log")
public class TOperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String module;
    @TableField("action_type")
    private String action;
    @TableField("action_desc")
    private String description;
    /** 操作结果: 1=成功 0=失败 */
    @TableField("op_result")
    private Integer result;
    @TableField("fail_reason")
    private String errorMsg;
    @TableField("op_ip")
    private String clientIp;
    /** 请求耗时(ms) - 表中无此列，跳过 */
    @TableField(exist = false)
    private Long costMs;
    @TableField(value = "op_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private String dataHmac;
}
