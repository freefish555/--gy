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
    /** 操作类型 - 对应 action_type 列 (camelCase自动映射) */
    private String actionType;
    /** 操作描述 - 对应 action_desc 列 (camelCase自动映射) */
    private String actionDesc;
    private Long targetId;
    private String targetType;
    /** 请求参数 - 对应 request_params 列 (camelCase自动映射) */
    private String requestParams;
    /** 操作结果: 1=成功 0=失败 - 对应 op_result 列 (camelCase自动映射) */
    private Integer opResult;
    /** 失败原因 - 对应 fail_reason 列 (camelCase自动映射) */
    private String failReason;
    /** 操作IP - 对应 op_ip 列 (camelCase自动映射) */
    private String opIp;
    /** 请求耗时(ms) - 表中无此列，跳过 */
    @TableField(exist = false)
    private Long costMs;
    /** 操作时间 - 对应 op_at 列 */
    @TableField(value = "op_at", fill = FieldFill.INSERT)
    private LocalDateTime opAt;
    private String dataHmac;
}
