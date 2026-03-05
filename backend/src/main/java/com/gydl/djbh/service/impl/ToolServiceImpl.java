package com.gydl.djbh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gydl.djbh.entity.TEvalDevice;
import com.gydl.djbh.entity.TPentestTool;
import com.gydl.djbh.exception.BusinessException;
import com.gydl.djbh.mapper.TEvalDeviceMapper;
import com.gydl.djbh.mapper.TPentestToolMapper;
import com.gydl.djbh.service.LogService;
import com.gydl.djbh.service.ToolService;
import com.gydl.djbh.utils.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测评工具管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final TEvalDeviceMapper deviceMapper;
    private final TPentestToolMapper toolMapper;
    private final LogService logService;

    // ============ 硬件测评设备 ============

    @Override
    public List<Map<String, Object>> listDevices() {
        List<TEvalDevice> list = deviceMapper.findAllWithStaff();
        List<Map<String, Object>> result = new ArrayList<>();
        for (TEvalDevice d : list) {
            result.add(deviceToMap(d));
        }
        return result;
    }

    @Override
    public Map<String, Object> detailDevice(Long id) {
        TEvalDevice device = deviceMapper.findByIdWithStaff(id);
        if (device == null) throw new BusinessException("设备不存在");
        return deviceToMap(device);
    }

    @Override
    public Long createDevice(TEvalDevice device) {
        device.setStatus(device.getStatus() != null ? device.getStatus() : 1);
        device.setCreatedBy(SecurityContextUtil.getCurrentUserIdSafe());
        deviceMapper.insert(device);
        logService.recordOperation("tool", "CREATE_DEVICE", "新增设备: " + device.getDeviceNo(), "SUCCESS");
        return device.getId();
    }

    @Override
    public void updateDevice(Long id, TEvalDevice device) {
        TEvalDevice existing = deviceMapper.selectById(id);
        if (existing == null) throw new BusinessException("设备不存在");
        device.setId(id);
        deviceMapper.updateById(device);
        logService.recordOperation("tool", "UPDATE_DEVICE", "编辑设备ID: " + id, "SUCCESS");
    }

    @Override
    public void deleteDevice(Long id) {
        deviceMapper.deleteById(id);
        logService.recordOperation("tool", "DELETE_DEVICE", "删除设备ID: " + id, "SUCCESS");
    }

    @Override
    public List<Map<String, Object>> listDevicesByStaff(Long staffId) {
        List<TEvalDevice> list = deviceMapper.findByStaffId(staffId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (TEvalDevice d : list) {
            result.add(deviceToMap(d));
        }
        return result;
    }

    // ============ 渗透软件工具 ============

    @Override
    public List<Map<String, Object>> listTools() {
        List<TPentestTool> list = toolMapper.findAllActive();
        List<Map<String, Object>> result = new ArrayList<>();
        for (TPentestTool t : list) {
            result.add(toolToMap(t));
        }
        return result;
    }

    @Override
    public Map<String, Object> detailTool(Long id) {
        TPentestTool tool = toolMapper.selectById(id);
        if (tool == null) throw new BusinessException("工具不存在");
        return toolToMap(tool);
    }

    @Override
    public Long createTool(TPentestTool tool) {
        tool.setStatus(tool.getStatus() != null ? tool.getStatus() : 1);
        tool.setCreatedBy(SecurityContextUtil.getCurrentUserIdSafe());
        toolMapper.insert(tool);
        logService.recordOperation("tool", "CREATE_TOOL", "新增工具: " + tool.getToolName(), "SUCCESS");
        return tool.getId();
    }

    @Override
    public void updateTool(Long id, TPentestTool tool) {
        TPentestTool existing = toolMapper.selectById(id);
        if (existing == null) throw new BusinessException("工具不存在");
        tool.setId(id);
        toolMapper.updateById(tool);
        logService.recordOperation("tool", "UPDATE_TOOL", "编辑工具ID: " + id, "SUCCESS");
    }

    @Override
    public void deleteTool(Long id) {
        toolMapper.deleteById(id);
        logService.recordOperation("tool", "DELETE_TOOL", "删除工具ID: " + id, "SUCCESS");
    }

    private Map<String, Object> deviceToMap(TEvalDevice d) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", d.getId());
        map.put("deviceNo", d.getDeviceNo());
        map.put("deviceName", d.getDeviceName());
        map.put("deviceModel", d.getDeviceModel());
        map.put("deviceType", d.getDeviceType());
        map.put("ownerStaffId", d.getOwnerStaffId());
        map.put("ownerStaffName", d.getOwnerStaffName());
        map.put("status", d.getStatus());
        map.put("remark", d.getRemark());
        map.put("createdAt", d.getCreatedAt());
        return map;
    }

    private Map<String, Object> toolToMap(TPentestTool t) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", t.getId());
        map.put("toolNo", t.getToolNo());
        map.put("toolName", t.getToolName());
        map.put("toolVersion", t.getToolVersion());
        map.put("toolDesc", t.getToolDesc());
        map.put("status", t.getStatus());
        map.put("createdAt", t.getCreatedAt());
        return map;
    }
}
