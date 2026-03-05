package com.gydl.djbh.crypto;

import com.gydl.djbh.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * HMAC-SM3 完整性校验工具
 * 用于计算数据库每行数据的完整性校验值
 */
@Slf4j
@Component
public class HmacSM3Util {

    private volatile byte[] hmacKey;

    /**
     * 设置HMAC密钥（由SysConfigService在应用启动时调用）
     */
    public void setHmacKey(String hexKey) {
        if (hexKey == null || hexKey.isEmpty()) {
            throw new BusinessException("HMAC密钥不能为空");
        }
        this.hmacKey = hexKey.getBytes(StandardCharsets.UTF_8);
        log.info("HMAC-SM3密钥已更新");
    }

    /**
     * 计算字符串的HMAC-SM3值
     *
     * @param data 待计算的数据
     * @return 十六进制格式的HMAC值
     */
    public String compute(String data) {
        ensureKeyLoaded();
        try {
            HMac hmac = new HMac(new SM3Digest());
            hmac.init(new KeyParameter(hmacKey));
            byte[] input = data.getBytes(StandardCharsets.UTF_8);
            hmac.update(input, 0, input.length);
            byte[] result = new byte[hmac.getMacSize()];
            hmac.doFinal(result, 0);
            return SM4Util.bytesToHex(result);
        } catch (Exception e) {
            throw new BusinessException("HMAC-SM3计算失败: " + e.getMessage());
        }
    }

    /**
     * 验证HMAC值
     *
     * @param data     待校验的数据
     * @param expected 期望的HMAC值
     * @return 是否一致
     */
    public boolean verify(String data, String expected) {
        if (expected == null || expected.isEmpty()) return false;
        return compute(data).equalsIgnoreCase(expected);
    }

    /**
     * 检查HMAC密钥是否已配置
     */
    public boolean isKeyConfigured() {
        return hmacKey != null && hmacKey.length > 0;
    }

    private void ensureKeyLoaded() {
        if (!isKeyConfigured()) {
            throw new BusinessException(503, "HMAC密钥未配置，请先在系统设置中配置密钥");
        }
    }
}
