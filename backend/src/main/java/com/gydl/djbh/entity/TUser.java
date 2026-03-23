package com.gydl.djbh.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@TableName("t_user")
public class TUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** SM4加密存储 */
    private String realName;

    @TableField(select = false)
    private String passwordHash;

    private Long roleId;

    /** 关联人员清单ID（t_staff.id），用于测评师编辑权限匹配 */
    private Long staffId;

    /** SM4加密存储 */
    private String phone;

    /** SM4加密存储 */
    private String email;

    /** SM4加密存储 */
    private String totpSecret;

    private Integer totpEnabled;

    private Integer status;

    private Integer loginFailCount;

    private LocalDateTime lockedUntil;

    private LocalDateTime lastLoginAt;

    private String lastLoginIp;

    private LocalDateTime passwordChangedAt;

    private Integer firstLogin;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private String dataHmac;

    // 非数据库字段，关联查询
    @TableField(exist = false)
    private String roleCode;

    @TableField(exist = false)
    private String roleName;
}
