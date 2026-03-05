package com.gydl.djbh.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gydl.djbh.crypto.SM4Util;
import com.gydl.djbh.dto.req.StaffQueryReq;
import com.gydl.djbh.dto.resp.PageResult;
import com.gydl.djbh.entity.TStaff;
import com.gydl.djbh.exception.BusinessException;
import com.gydl.djbh.mapper.TStaffMapper;
import com.gydl.djbh.service.LogService;
import com.gydl.djbh.service.StaffService;
import com.gydl.djbh.utils.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人员管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StaffServiceImpl extends ServiceImpl<TStaffMapper, TStaff> implements StaffService {

    private final SM4Util sm4Util;
    private final LogService logService;

    @Override
    public PageResult<Map<String, Object>> page(StaffQueryReq req) {
        int offset = (req.getPage() - 1) * req.getPageSize();
        List<TStaff> list = baseMapper.findPage(req.getKeyword(), req.getRoleLevel(),
                req.getStatus(), offset, req.getPageSize());
        long total = baseMapper.countPage(req.getKeyword(), req.getRoleLevel(), req.getStatus());

        List<Map<String, Object>> result = new ArrayList<>();
        for (TStaff s : list) {
            result.add(toMap(s));
        }
        return PageResult.of(total, req.getPage(), req.getPageSize(), result);
    }

    @Override
    public Map<String, Object> detail(Long id) {
        TStaff staff = baseMapper.selectById(id);
        if (staff == null) throw new BusinessException("人员不存在");
        return toMap(staff);
    }

    @Override
    public Long create(TStaff staff) {
        staff.setRealName(encrypt(staff.getRealName()));
        staff.setCertNo(encrypt(staff.getCertNo()));
        staff.setPhone(encrypt(staff.getPhone()));
        staff.setEmail(encrypt(staff.getEmail()));
        staff.setStatus(staff.getStatus() != null ? staff.getStatus() : 1);
        staff.setCreatedBy(SecurityContextUtil.getCurrentUserIdSafe());
        baseMapper.insert(staff);
        logService.recordOperation("staff", "CREATE", "新增人员: " + staff.getStaffNo(), "SUCCESS");
        return staff.getId();
    }

    @Override
    public void update(Long id, TStaff staff) {
        TStaff existing = baseMapper.selectById(id);
        if (existing == null) throw new BusinessException("人员不存在");
        staff.setId(id);
        staff.setRealName(encrypt(staff.getRealName()));
        staff.setCertNo(encrypt(staff.getCertNo()));
        staff.setPhone(encrypt(staff.getPhone()));
        staff.setEmail(encrypt(staff.getEmail()));
        baseMapper.updateById(staff);
        logService.recordOperation("staff", "UPDATE", "编辑人员ID: " + id, "SUCCESS");
    }

    @Override
    public void delete(Long id) {
        baseMapper.deleteById(id);
        logService.recordOperation("staff", "DELETE", "删除人员ID: " + id, "SUCCESS");
    }

    @Override
    public void toggleStatus(Long id) {
        TStaff staff = baseMapper.selectById(id);
        if (staff == null) throw new BusinessException("人员不存在");
        staff.setStatus(staff.getStatus() == 1 ? 0 : 1);
        baseMapper.updateById(staff);
    }

    @Override
    public List<Map<String, Object>> listActive() {
        List<TStaff> list = baseMapper.findAllActive();
        List<Map<String, Object>> result = new ArrayList<>();
        for (TStaff s : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", s.getId());
            item.put("staffNo", s.getStaffNo());
            item.put("realName", decrypt(s.getRealName()));
            item.put("position", s.getPosition());
            item.put("roleLevel", s.getRoleLevel());
            result.add(item);
        }
        return result;
    }

    private Map<String, Object> toMap(TStaff s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getId());
        map.put("staffNo", s.getStaffNo());
        map.put("realName", decrypt(s.getRealName()));
        map.put("department", s.getDepartment());
        map.put("position", s.getPosition());
        map.put("roleLevel", s.getRoleLevel());
        map.put("certNo", decrypt(s.getCertNo()));
        map.put("certExpire", s.getCertExpire());
        map.put("phone", decrypt(s.getPhone()));
        map.put("email", decrypt(s.getEmail()));
        map.put("status", s.getStatus());
        map.put("createdAt", s.getCreatedAt());
        map.put("updatedAt", s.getUpdatedAt());
        return map;
    }

    private String encrypt(String value) {
        if (value == null || value.isEmpty()) return value;
        try { return sm4Util.encrypt(value); } catch (Exception e) { return value; }
    }

    private String decrypt(String value) {
        if (value == null || value.isEmpty()) return value;
        try { return sm4Util.decrypt(value); } catch (Exception e) { return value; }
    }
}
