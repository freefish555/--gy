package com.gydl.djbh.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 登录日志 */
@Data
@TableName("t_login_log")
public class TLoginLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    /** SM4加密 */
    private String realName;
    @TableField("login_ip")
    private String clientIp;
    private String browser;
    /** 登录结果: 1=成功 0=失败 */
    @TableField("login_status")
    private Integer loginResult;
    private String failReason;
    @TableField(value = "login_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private String dataHmac;
}
