package com.gydl.djbh.service;

import com.gydl.djbh.dto.req.LoginReq;
import com.gydl.djbh.dto.resp.LoginResp;
import java.util.Map;

public interface AuthService {
    LoginResp login(LoginReq req, String clientIp);
    LoginResp verifyTotp(String tempToken, int totpCode, String clientIp);
    void logout(String token);
    void changePassword(String oldPassword, String newPassword);
    Map<String, String> generateTotpSecret();
    void confirmTotpBind(String secret, int code);
}
