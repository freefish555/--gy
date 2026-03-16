package com.gydl.djbh.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
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
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/**
 * 人员管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StaffServiceImpl extends ServiceImpl<TStaffMapper, TStaff> implements StaffService {

    private final SM4Util sm4Util;
    private final LogService logService;

    private static final String[] EXPORT_HEADERS = {
            "工号*", "姓名*", "部门", "职位", "职级", "证书编号", "证书到期日(YYYY-MM-DD)", "电话", "邮箱", "状态(1启用/0停用)"
    };

    @Override
    public PageResult<Map<String, Object>> page(StaffQueryReq req) {
        int offset = (req.getPage() - 1) * req.getPageSize();
        // 兼容前端 realName 字段作为搜索关键词
        String keyword = req.getKeyword();
        if ((keyword == null || keyword.isEmpty()) && req.getRealName() != null && !req.getRealName().isEmpty()) {
            keyword = req.getRealName();
        }
        List<TStaff> list = baseMapper.findPage(keyword, req.getRoleLevel(),
                req.getStatus(), offset, req.getPageSize());
        long total = baseMapper.countPage(keyword, req.getRoleLevel(), req.getStatus());

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
    public void setStatus(Long id, Integer status) {
        TStaff staff = baseMapper.selectById(id);
        if (staff == null) throw new BusinessException("人员不存在");
        staff.setStatus(status);
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

    @Override
    public void exportExcel(StaffQueryReq req, HttpServletResponse response) throws Exception {
        // 导出全量匹配结果（不分页）
        req.setPage(1);
        req.setPageSize(10000);
        int offset = 0;
        // 兼容前端 realName 字段作为搜索关键词
        String keyword = req.getKeyword();
        if ((keyword == null || keyword.isEmpty()) && req.getRealName() != null && !req.getRealName().isEmpty()) {
            keyword = req.getRealName();
        }
        List<TStaff> list = baseMapper.findPage(keyword, req.getRoleLevel(),
                req.getStatus(), offset, 10000);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("人员清单");
            // 表头
            Row header = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(EXPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }
            // 数据行
            int rowNum = 1;
            for (TStaff s : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(s.getStaffNo() != null ? s.getStaffNo() : "");
                row.createCell(1).setCellValue(decrypt(s.getRealName()));
                row.createCell(2).setCellValue(s.getDepartment() != null ? s.getDepartment() : "");
                row.createCell(3).setCellValue(s.getPosition() != null ? s.getPosition() : "");
                row.createCell(4).setCellValue(s.getRoleLevel() != null ? s.getRoleLevel() : "");
                row.createCell(5).setCellValue(decrypt(s.getCertNo()));
                row.createCell(6).setCellValue(s.getCertExpire() != null ? s.getCertExpire().toString() : "");
                row.createCell(7).setCellValue(decrypt(s.getPhone()));
                row.createCell(8).setCellValue(decrypt(s.getEmail()));
                row.createCell(9).setCellValue(s.getStatus() != null ? s.getStatus() : 1);
            }
            String filename = URLEncoder.encode("人员清单.xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            wb.write(response.getOutputStream());
        }
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("人员导入模板");
            Row header = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(EXPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }
            // 示例行
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("S001");
            example.createCell(1).setCellValue("张三");
            example.createCell(2).setCellValue("技术部");
            example.createCell(3).setCellValue("测评工程师");
            example.createCell(4).setCellValue("junior");
            example.createCell(5).setCellValue("CERT-2024-001");
            example.createCell(6).setCellValue("2026-12-31");
            example.createCell(7).setCellValue("13800138000");
            example.createCell(8).setCellValue("zhangsan@example.com");
            example.createCell(9).setCellValue(1);

            String filename = URLEncoder.encode("人员导入模板.xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            wb.write(response.getOutputStream());
        }
    }

    @Override
    public Map<String, Object> importStaff(MultipartFile file) throws Exception {
        List<Map<Integer, String>> rows = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                rows.add(data);
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).headRowNumber(1).sheet().doRead();

        int success = 0, skip = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Map<Integer, String> row = rows.get(i);
            try {
                String staffNo = row.getOrDefault(0, "").trim();
                String realName = row.getOrDefault(1, "").trim();
                if (staffNo.isEmpty() || realName.isEmpty()) {
                    errors.add("第" + (i + 2) + "行：工号和姓名不能为空");
                    skip++;
                    continue;
                }
                TStaff staff = new TStaff();
                staff.setStaffNo(staffNo);
                staff.setRealName(encrypt(realName));
                staff.setDepartment(row.getOrDefault(2, "").trim());
                staff.setPosition(row.getOrDefault(3, "").trim());
                staff.setRoleLevel(row.getOrDefault(4, "middle").trim());
                String certNo = row.getOrDefault(5, "").trim();
                staff.setCertNo(certNo.isEmpty() ? null : encrypt(certNo));
                String certExpireStr = row.getOrDefault(6, "").trim();
                if (!certExpireStr.isEmpty()) {
                    try { staff.setCertExpire(LocalDate.parse(certExpireStr)); } catch (Exception e) { /* ignore */ }
                }
                String phone = row.getOrDefault(7, "").trim();
                staff.setPhone(phone.isEmpty() ? null : encrypt(phone));
                String email = row.getOrDefault(8, "").trim();
                staff.setEmail(email.isEmpty() ? null : encrypt(email));
                String statusStr = row.getOrDefault(9, "1").trim();
                staff.setStatus("0".equals(statusStr) ? 0 : 1);
                staff.setCreatedBy(SecurityContextUtil.getCurrentUserIdSafe());
                baseMapper.insert(staff);
                success++;
            } catch (Exception e) {
                errors.add("第" + (i + 2) + "行：" + e.getMessage());
                skip++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", rows.size());
        result.put("successCount", success);
        result.put("skipCount", skip);
        result.put("errors", errors);
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
