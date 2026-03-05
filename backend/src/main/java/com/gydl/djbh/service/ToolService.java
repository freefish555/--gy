package com.gydl.djbh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gydl.djbh.entity.TEvalDevice;
import com.gydl.djbh.entity.TPentestTool;

import java.util.List;
import java.util.Map;

/**
 * 测评工具管理服务
 */
public interface ToolService {
    // 硬件测评设备
    List<Map<String, Object>> listDevices();
    Map<String, Object> detailDevice(Long id);
    Long createDevice(TEvalDevice device);
    void updateDevice(Long id, TEvalDevice device);
    void deleteDevice(Long id);

    // 渗透软件工具
    List<Map<String, Object>> listTools();
    Map<String, Object> detailTool(Long id);
    Long createTool(TPentestTool tool);
    void updateTool(Long id, TPentestTool tool);
    void deleteTool(Long id);
    
    // 获取可用设备（按人员ID查询）
    List<Map<String, Object>> listDevicesByStaff(Long staffId);
}
