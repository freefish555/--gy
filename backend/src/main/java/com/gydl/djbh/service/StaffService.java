package com.gydl.djbh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gydl.djbh.dto.req.StaffQueryReq;
import com.gydl.djbh.dto.resp.PageResult;
import com.gydl.djbh.entity.TStaff;

import java.util.List;
import java.util.Map;

/**
 * 人员管理服务
 */
public interface StaffService extends IService<TStaff> {
    PageResult<Map<String, Object>> page(StaffQueryReq req);
    Map<String, Object> detail(Long id);
    Long create(TStaff staff);
    void update(Long id, TStaff staff);
    void delete(Long id);
    void toggleStatus(Long id);
    List<Map<String, Object>> listActive();
}
