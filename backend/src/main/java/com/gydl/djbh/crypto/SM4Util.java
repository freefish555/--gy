package com.gydl.djbh.crypto;

import com.gydl.djbh.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;

/**
 * SM4 对称加密工具（CBC模式 + PKCS7填充）
 * 用于加密存储敏感业务字段
 */
@Slf4j
@Component
public class SM4Util {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // SM4 密钥（16字节=128位），从数据库配置表动态加载，此处注入应用根密钥用于解密配置中的SM4密钥
    @Value("${app.crypto.root-key}")
    private String rootKey;

    // 固定IV（实际生产建议每次加密使用随机IV并与密文一起存储，此处简化使用固定IV）
    private static final byte[] FIXED_IV = "djbh2026iv000000".getBytes(StandardCharsets.UTF_8);

    private volatile byte[] sm4Key;

    @PostConstruct
    public void init() {
        log.info("SM4加密工具初始化完成，等待从系统配置加载SM4密钥");
    }

    /**
     * 设置SM4密钥（由SysConfigService在应用启动时或密钥更新时调用）
     *
     * @param hexKey 32位十六进制字符串（16字节）
     */
    public void setSm4Key(String hexKey) {
        if (hexKey == null || hexKey.length() != 32) {
            throw new BusinessException("SM4密钥格式错误，必须为32位十六进制字符串");
        }
        this.sm4Key = hexToBytes(hexKey);
        log.info("SM4密钥已更新");
    }

    /**
     * SM4-CBC 加密
     *
     * @param plaintext 明文字符串
     * @return Base64编码的密文，如果明文为null则返回null
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) return null;
        ensureKeyLoaded();
        try {
            byte[] data = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = sm4Cbc(data, true);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new BusinessException("SM4加密失败: " + e.getMessage());
        }
    }

    /**
     * SM4-CBC 解密
     *
     * @param ciphertext Base64编码的密文
     * @return 明文字符串，如果密文为null则返回null；解密失败则返回原文
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null) return null;
        ensureKeyLoaded();
        try {
            byte[] data = Base64.getDecoder().decode(ciphertext);
            byte[] decrypted = sm4Cbc(data, false);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 解密失败时返回原文（兼容未加密的历史数据）
            log.debug("SM4解密失败，返回原文: {}", e.getMessage());
            return ciphertext;
        }
    }

    /**
     * 检查SM4密钥是否已配置
     */
    public boolean isKeyConfigured() {
        return sm4Key != null && sm4Key.length == 16;
    }

    // ==================== 私有方法 ====================

    private void ensureKeyLoaded() {
        if (!isKeyConfigured()) {
            // 密钥未配置时使用默认开发密钥（仅开发模式），生产环境必须配置
            log.warn("SM4密钥未配置，使用默认开发密钥（仅用于开发测试！）");
            this.sm4Key = "djbhdefaultkey00".getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] sm4Cbc(byte[] data, boolean encrypt) throws Exception {
        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(new SM4Engine()), new PKCS7Padding());
        ParametersWithIV params = new ParametersWithIV(new KeyParameter(sm4Key), FIXED_IV);
        cipher.init(encrypt, params);

        byte[] output = new byte[cipher.getOutputSize(data.length)];
        int len = cipher.processBytes(data, 0, data.length, output, 0);
        len += cipher.doFinal(output, len);

        byte[] result = new byte[len];
        System.arraycopy(output, 0, result, 0, len);
        return result;
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
