package com.gydl.djbh.dto.resp;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 登录响应
 */
@Data
@Builder
public class LoginResp {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String roleCode;
    private String roleName;
    private List<String> permissions;
    /** 关联人员清单ID（用于测评师编辑权限匹配） */
    private Long staffId;
    /** 是否需要TOTP验证（true时前端跳转二次验证页） */
    private Boolean requireTotp;
    /** 是否首次登录（true时强制修改密码） */
    private Boolean firstLogin;
    /** Token过期时间（秒） */
    private Long expiresIn;
}
