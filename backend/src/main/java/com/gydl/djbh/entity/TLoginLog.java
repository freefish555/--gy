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
    /** 登录IP - 对应 login_ip 列 */
    private String loginIp;
    private String loginLocation;
    private String browser;
    /** 登录结果: 1=成功 0=失败 - 对应 login_status 列 */
    private Integer loginStatus;
    private String failReason;
    /** 登录时间 - 对应 login_at 列 */
    @TableField(value = "login_at", fill = FieldFill.INSERT)
    private LocalDateTime loginAt;
    /** 退出时间 - 对应 logout_at 列 */
    private LocalDateTime logoutAt;
    /** 会话时长（秒） */
    private Integer sessionDuration;
    private String dataHmac;
}
