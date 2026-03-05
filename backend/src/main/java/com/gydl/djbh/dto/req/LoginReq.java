package com.gydl.djbh.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginReq {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 图形验证码（根据系统配置决定是否必填） */
    private String captchaCode;

    /** 验证码ID */
    private String captchaId;
}
