package com.gydl.djbh.service;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务
 */
public interface SysConfigService {
    String getConfig(String key, String defaultValue);
    int getIntConfig(String key, int defaultValue);
    boolean getBoolConfig(String key, boolean defaultValue);
    void setConfig(String key, String value);
    Map<String, String> getAllConfigs();
    List<Map<String, Object>> listConfigs();
    void batchSave(Map<String, String> configs);
}
