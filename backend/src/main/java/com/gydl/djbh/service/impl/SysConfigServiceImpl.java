package com.gydl.djbh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gydl.djbh.entity.TSysConfig;
import com.gydl.djbh.mapper.TSysConfigMapper;
import com.gydl.djbh.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private final TSysConfigMapper configMapper;

    @Override
    public String getConfig(String key, String defaultValue) {
        TSysConfig config = configMapper.findByKey(key);
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isEmpty()) {
            return defaultValue;
        }
        return config.getConfigValue();
    }

    @Override
    public int getIntConfig(String key, int defaultValue) {
        String value = getConfig(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolConfig(String key, boolean defaultValue) {
        String value = getConfig(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    @Override
    public void setConfig(String key, String value) {
        TSysConfig config = configMapper.findByKey(key);
        if (config == null) {
            config = new TSysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setIsEncrypted(0);
            configMapper.insert(config);
        } else {
            config.setConfigValue(value);
            configMapper.updateById(config);
        }
    }

    @Override
    public Map<String, String> getAllConfigs() {
        List<TSysConfig> list = configMapper.selectList(null);
        Map<String, String> result = new HashMap<>();
        for (TSysConfig c : list) {
            // 密钥类脱敏
            if (c.getIsEncrypted() != null && c.getIsEncrypted() == 1) {
                result.put(c.getConfigKey(), maskKey(c.getConfigValue()));
            } else {
                result.put(c.getConfigKey(), c.getConfigValue());
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listConfigs() {
        List<TSysConfig> list = configMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        for (TSysConfig c : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("configKey", c.getConfigKey());
            item.put("configDesc", c.getConfigDesc());
            item.put("isEncrypted", c.getIsEncrypted());
            item.put("updatedAt", c.getUpdatedAt());
            // 密钥类脱敏
            if (c.getIsEncrypted() != null && c.getIsEncrypted() == 1) {
                item.put("configValue", maskKey(c.getConfigValue()));
                item.put("hasValue", c.getConfigValue() != null && !c.getConfigValue().isEmpty());
            } else {
                item.put("configValue", c.getConfigValue());
                item.put("hasValue", true);
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public void batchSave(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            setConfig(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 密钥脱敏：只显示前4位和后4位，中间用*替代
     */
    private String maskKey(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.length() <= 8) return "****";
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }
}
