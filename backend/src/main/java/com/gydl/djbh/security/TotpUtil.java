package com.gydl.djbh.security;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Google Authenticator TOTP 双因子认证工具
 */
@Component
@RequiredArgsConstructor
public class TotpUtil {

    private static final String ISSUER = "DJBH-System";
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    /**
     * 生成TOTP密钥（新用户绑定时调用）
     *
     * @return TOTP密钥字符串（Base32编码，需SM4加密后存入数据库）
     */
    public String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    /**
     * 生成扫码绑定的二维码URL（otpauth://协议）
     *
     * @param username 用户名
     * @param secret   TOTP密钥
     * @return 二维码URL，前端用qrcode库渲染成二维码图片
     */
    public String generateQrUrl(String username, String secret) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                ISSUER, username,
                new GoogleAuthenticatorKey.Builder(secret).build()
        );
    }

    /**
     * 验证TOTP动态码
     *
     * @param secret 用户的TOTP密钥（明文，解密后传入）
     * @param code   用户输入的6位动态码
     * @return 是否验证通过
     */
    public boolean verify(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}
