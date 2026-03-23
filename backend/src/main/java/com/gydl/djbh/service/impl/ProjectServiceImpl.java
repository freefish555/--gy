package com.gydl.djbh.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.gydl.djbh.crypto.SM4Util;
import com.gydl.djbh.dto.req.ProjectQueryReq;
import com.gydl.djbh.dto.req.ProjectSaveReq;
import com.gydl.djbh.dto.resp.PageResult;
import com.gydl.djbh.dto.resp.ProjectDetailResp;
import com.gydl.djbh.entity.TProject;
import com.gydl.djbh.entity.TProjectSystem;
import com.gydl.djbh.exception.BusinessException;
import com.gydl.djbh.entity.TProjectMember;
import com.gydl.djbh.mapper.TDictItemMapper;
import com.gydl.djbh.mapper.TProjectMapper;
import com.gydl.djbh.mapper.TProjectMemberMapper;
import com.gydl.djbh.mapper.TProjectSystemMapper;
import com.gydl.djbh.mapper.TStaffMapper;
import com.gydl.djbh.service.LogService;
import com.gydl.djbh.service.ProjectService;
import com.gydl.djbh.utils.SecurityContextUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 项目管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final TProjectMapper projectMapper;
    private final TProjectSystemMapper systemMapper;
    private final TProjectMemberMapper memberMapper;
    private final TStaffMapper staffMapper;
    private final TDictItemMapper dictItemMapper;
    private final SM4Util sm4Util;
    private final LogService logService;

    // 导入模板列头
    private static final String[] IMPORT_HEADERS = {
        "项目编号*", "项目名称*", "客户名称*", "客户地址", "联系人姓名", "联系人电话",
        "系统名称合并", "2级系统数量", "3级系统数量",
        "项目类型(电力/外围)", "所属行业",
        "合同签订日期(yyyy-MM-dd)", "合同金额(元)",
        "纸质归档(0否/1是)", "电子归档(0否/1是)",
        "测评准备阶段(起)", "测评准备阶段(止)", "方案编制阶段(起)", "方案编制阶段(止)",
        "现场测评阶段(起)", "现场测评阶段(止)", "报告编制阶段(起)", "报告编制阶段(止)",
        "任务书任命表时间(yyyy-MM-dd)", "所属年份(yyyy)", "业务人员", "项目地区", "报告邮寄日期(yyyy-MM-dd)", "报告邮寄单号", "备注"
    };

    @Override
    public PageResult<ProjectDetailResp> page(ProjectQueryReq req) {
        // For encrypted fields (projectName, customerName) or member name filters or recordNo (sub-table),
        // we fetch all matching other criteria then filter in memory
        boolean hasNameFilter = (req.getProjectName() != null && !req.getProjectName().isEmpty())
                || (req.getCustomerName() != null && !req.getCustomerName().isEmpty())
                || (req.getMemberName() != null && !req.getMemberName().isEmpty())
                || (req.getActualMemberName() != null && !req.getActualMemberName().isEmpty())
                || (req.getProjectLeaderName() != null && !req.getProjectLeaderName().isEmpty())
                || (req.getRecordNo() != null && !req.getRecordNo().isEmpty())
                || (req.getKeyword() != null && !req.getKeyword().isEmpty());

        if (hasNameFilter) {
            // Fetch all (ignore pagination for now) then filter in memory
            List<TProject> allList = projectMapper.findPage(
                    req.getProjectNo(), null, null,
                    req.getProjectManagerId(), req.getProjectLeaderId(),
                    req.getProjectTypeId(), req.getIndustryId(),
                    req.getYearBelong(), req.getProjectRegion(), 0, Integer.MAX_VALUE);

            // Decrypt and filter
            List<TProject> filtered = new ArrayList<>();
            for (TProject p : allList) {
                if (req.getProjectName() != null && !req.getProjectName().isEmpty()) {
                    String decrypted = decryptIfNotNull(p.getProjectName());
                    if (decrypted == null || !normalizeName(decrypted).contains(normalizeName(req.getProjectName()))) continue;
                }
                if (req.getCustomerName() != null && !req.getCustomerName().isEmpty()) {
                    String decrypted = decryptIfNotNull(p.getCustomerName());
                    if (decrypted == null || !normalizeName(decrypted).contains(normalizeName(req.getCustomerName()))) continue;
                }
                // 按项目组成员姓名过滤（registered_evaluator角色）
                if (req.getMemberName() != null && !req.getMemberName().isEmpty()) {
                    if (!projectHasMemberByName(p.getId(), req.getMemberName(), "registered_evaluator")) continue;
                }
                // 按实际测评人员姓名过滤（actual_member角色）
                if (req.getActualMemberName() != null && !req.getActualMemberName().isEmpty()) {
                    if (!projectHasMemberByName(p.getId(), req.getActualMemberName(), "actual_member")) continue;
                }
                // 按项目负责人姓名过滤（project_leader角色，也包含project_manager）
                if (req.getProjectLeaderName() != null && !req.getProjectLeaderName().isEmpty()) {
                    String leaderName = decryptIfNotNull(p.getProjectLeaderName());
                    if (leaderName == null || !normalizeName(leaderName).contains(normalizeName(req.getProjectLeaderName()))) continue;
                }
                // 按备案编号过滤（子表 t_project_system.record_no，明文，模糊匹配）
                if (req.getRecordNo() != null && !req.getRecordNo().isEmpty()) {
                    List<com.gydl.djbh.entity.TProjectSystem> sysList = systemMapper.findByProjectId(p.getId());
                    boolean matched = sysList.stream().anyMatch(sys ->
                            sys.getRecordNo() != null && sys.getRecordNo().contains(req.getRecordNo()));
                    if (!matched) continue;
                }
                // keyword：项目编号 OR 解密后项目名称（模糊匹配，用于归档管理快速搜索）
                if (req.getKeyword() != null && !req.getKeyword().isEmpty()) {
                    String kw = normalizeName(req.getKeyword());
                    boolean noMatch = true;
                    // 编号匹配（明文）
                    if (p.getProjectNo() != null && normalizeName(p.getProjectNo()).contains(kw)) {
                        noMatch = false;
                    }
                    // 名称匹配（解密后）
                    if (noMatch) {
                        String decryptedName = decryptIfNotNull(p.getProjectName());
                        if (decryptedName != null && normalizeName(decryptedName).contains(kw)) {
                            noMatch = false;
                        }
                    }
                    if (noMatch) continue;
                }
                filtered.add(p);
            }

            long total = filtered.size();
            int offset = (req.getPageNum() - 1) * req.getPageSize();
            int end = Math.min(offset + req.getPageSize(), filtered.size());
            List<TProject> pageList = offset < filtered.size() ? filtered.subList(offset, end) : new ArrayList<>();

            List<ProjectDetailResp> result = new ArrayList<>();
            for (TProject p : pageList) {
                result.add(toResp(p, false));
            }
            return PageResult.of(total, req.getPageNum(), req.getPageSize(), result);
        }

        int offset = (req.getPageNum() - 1) * req.getPageSize();
        List<TProject> list = projectMapper.findPage(
                req.getProjectNo(), null, null, req.getProjectManagerId(),
                req.getProjectLeaderId(),
                req.getProjectTypeId(), req.getIndustryId(),
                req.getYearBelong(), req.getProjectRegion(), offset, req.getPageSize());
        long total = projectMapper.countPage(
                req.getProjectNo(), null, null, req.getProjectManagerId(),
                req.getProjectLeaderId(),
                req.getProjectTypeId(), req.getIndustryId(), req.getYearBelong(), req.getProjectRegion());

        List<ProjectDetailResp> result = new ArrayList<>();
        for (TProject p : list) {
            result.add(toResp(p, false));
        }
        return PageResult.of(total, req.getPageNum(), req.getPageSize(), result);
    }

    /**
     * 标准化姓名：去除空格、标点符号后转小写，用于模糊匹配
     */
    private String normalizeName(String name) {
        if (name == null) return "";
        return name.replaceAll("[\\s\\p{P}\\p{Z}]", "").toLowerCase();
    }

    /**
     * 判断项目是否有指定角色且姓名匹配的成员
     */
    private boolean projectHasMemberByName(Long projectId, String nameFilter, String roleType) {
        List<TProjectMember> members = memberMapper.findByProjectId(projectId);
        String normalizedFilter = normalizeName(nameFilter);
        for (TProjectMember m : members) {
            if (roleType != null && !roleType.equals(m.getRoleType())) continue;
            if (m.getMemberId() != null) {
                com.gydl.djbh.entity.TStaff staff = staffMapper.selectById(m.getMemberId());
                if (staff != null) {
                    String realName = decryptIfNotNull(staff.getRealName());
                    if (realName != null && normalizeName(realName).contains(normalizedFilter)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ProjectDetailResp detail(Long id) {
        TProject project = projectMapper.findByIdWithInfo(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return toResp(project, true);
    }

    @Override
    @Transactional
    public Long create(ProjectSaveReq req) {
        // 检查项目编号唯一性
        if (projectMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TProject>()
                        .eq(TProject::getProjectNo, req.getProjectNo())) > 0) {
            throw new BusinessException("项目编号已存在：" + req.getProjectNo());
        }
        TProject project = buildEntity(req);
        project.setCreatedBy(SecurityContextUtil.getCurrentUserIdSafe());
        projectMapper.insert(project);

        saveSystems(project.getId(), req.getSystems());
        saveMembers(project.getId(), req);
        logService.recordOperation("project", "CREATE", "新增项目: " + req.getProjectNo(), "SUCCESS");
        return project.getId();
    }

    @Override
    @Transactional
    public void update(Long id, ProjectSaveReq req) {
        TProject existing = projectMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("项目不存在");
        }
        TProject project = buildEntity(req);
        project.setId(id);
        projectMapper.updateById(project);

        systemMapper.deleteByProjectId(id);
        saveSystems(id, req.getSystems());
        memberMapper.deleteByProjectId(id);
        saveMembers(id, req);
        logService.recordOperation("project", "UPDATE", "编辑项目: " + req.getProjectNo(), "SUCCESS");
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        projectMapper.deleteById(id);
        systemMapper.deleteByProjectId(id);
        memberMapper.deleteByProjectId(id);
        logService.recordOperation("project", "DELETE", "删除项目ID: " + id, "SUCCESS");
    }

    @Override
    @Transactional
    public void batchUpdate(List<Long> ids, String action, Object value) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            TProject project = projectMapper.selectById(id);
            if (project == null) continue;
            if ("manager".equals(action) && value != null) {
                project.setProjectManagerId(Long.parseLong(value.toString()));
                projectMapper.updateById(project);
            }
        }
        logService.recordOperation("project", "BATCH_UPDATE", "批量操作" + ids.size() + "个项目: " + action, "SUCCESS");
    }

    // ==================== 导出Excel ====================

    @Override
    public void exportExcel(ProjectQueryReq req, HttpServletResponse response) throws Exception {
        // 若pageSize<=0则导出全部
        if (req.getPageSize() <= 0) {
            req.setPageSize(100000);
        }
        req.setPageNum(1);
        PageResult<ProjectDetailResp> result = page(req);
        List<ProjectDetailResp> records = result.getRecords();

        String fileName = URLEncoder.encode("项目列表导出_" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);

        // 构建导出数据（List<List<Object>>）
        List<List<String>> head = buildExportHead();
        List<List<Object>> data = buildExportData(records);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("项目列表");

        // 样式：表头
        XSSFCellStyle headStyle = workbook.createCellStyle();
        headStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        XSSFFont headFont = workbook.createFont();
        headFont.setBold(true);
        headFont.setColor(IndexedColors.WHITE.getIndex());
        headStyle.setFont(headFont);
        headStyle.setAlignment(HorizontalAlignment.CENTER);

        // 样式：数据行
        XSSFCellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setWrapText(false);

        // 写表头
        Row headRow = sheet.createRow(0);
        for (int i = 0; i < head.size(); i++) {
            Cell cell = headRow.createCell(i);
            cell.setCellValue(head.get(i).get(0));
            cell.setCellStyle(headStyle);
            sheet.setColumnWidth(i, 5000);
        }

        // 写数据
        for (int r = 0; r < data.size(); r++) {
            Row row = sheet.createRow(r + 1);
            List<Object> rowData = data.get(r);
            for (int c = 0; c < rowData.size(); c++) {
                Cell cell = row.createCell(c);
                Object val = rowData.get(c);
                cell.setCellValue(val == null ? "" : val.toString());
                cell.setCellStyle(dataStyle);
            }
        }

        workbook.write(response.getOutputStream());
        workbook.close();
        logService.recordOperation("project", "EXPORT", "导出项目列表，共" + records.size() + "条", "SUCCESS");
    }

    /**
     * 导出全部项目完整版（多Sheet）
     * Sheet1: 项目主表（含人员汇总列）
     * Sheet2: 人员详情（每人一行，含角色信息）
     * Sheet3: 被测系统详情
     */
    @Override
    public void exportExcelFull(HttpServletResponse response) throws Exception {
        String fileName = URLEncoder.encode("项目完整导出_" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);

        // 获取全部项目
        ProjectQueryReq req = new ProjectQueryReq();
        req.setPageNum(1);
        req.setPageSize(100000);
        PageResult<ProjectDetailResp> result = page(req);
        List<ProjectDetailResp> records = result.getRecords();
        // 加载详情（含系统和人员）
        List<ProjectDetailResp> detailRecords = new ArrayList<>();
        for (ProjectDetailResp p : records) {
            detailRecords.add(toResp(projectMapper.findByIdWithInfo(p.getId()), true));
        }

        XSSFWorkbook workbook = new XSSFWorkbook();

        // --- 通用样式 ---
        XSSFCellStyle headStyle = workbook.createCellStyle();
        headStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        XSSFFont headFont = workbook.createFont();
        headFont.setBold(true);
        headFont.setColor(IndexedColors.WHITE.getIndex());
        headStyle.setFont(headFont);
        headStyle.setAlignment(HorizontalAlignment.CENTER);

        XSSFCellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // ===== Sheet1: 项目主表 =====
        XSSFSheet sheet1 = workbook.createSheet("项目主表");
        List<List<String>> head1 = buildExportHead();
        Row h1 = sheet1.createRow(0);
        for (int i = 0; i < head1.size(); i++) {
            Cell c = h1.createCell(i);
            c.setCellValue(head1.get(i).get(0));
            c.setCellStyle(headStyle);
            sheet1.setColumnWidth(i, 5000);
        }
        List<List<Object>> data1 = buildExportData(detailRecords);
        for (int r = 0; r < data1.size(); r++) {
            Row row = sheet1.createRow(r + 1);
            List<Object> rowData = data1.get(r);
            for (int c = 0; c < rowData.size(); c++) {
                Cell cell = row.createCell(c);
                Object val = rowData.get(c);
                cell.setCellValue(val == null ? "" : val.toString());
                cell.setCellStyle(dataStyle);
            }
        }

        // ===== Sheet2: 人员详情 =====
        XSSFSheet sheet2 = workbook.createSheet("人员详情");
        String[] memberHeads = { "项目编号", "项目名称", "角色", "人员姓名" };
        Map<String, String> roleNameMap = new java.util.LinkedHashMap<>();
        roleNameMap.put("project_manager", "项目经理");
        roleNameMap.put("project_leader", "项目负责人");
        roleNameMap.put("registered_evaluator", "登记测评师");
        roleNameMap.put("actual_member", "实际测评人员");
        roleNameMap.put("survey_editor", "调研表编制人");
        roleNameMap.put("plan_editor", "方案编制人");
        roleNameMap.put("report_editor", "报告编制人");
        roleNameMap.put("network_evaluator", "网络测评人员");
        roleNameMap.put("host_evaluator", "主机测评人员");
        roleNameMap.put("physical_evaluator", "物理测评人员");
        roleNameMap.put("tool_scanner", "工具扫描人员");
        roleNameMap.put("pentest_member", "渗透测试人员");

        Row mh = sheet2.createRow(0);
        for (int i = 0; i < memberHeads.length; i++) {
            Cell c = mh.createCell(i);
            c.setCellValue(memberHeads[i]);
            c.setCellStyle(headStyle);
            sheet2.setColumnWidth(i, 5500);
        }
        int memberRow = 1;
        for (ProjectDetailResp p : detailRecords) {
            if (p.getMembers() == null) continue;
            for (Map<String, Object> m : p.getMembers()) {
                String roleType = (String) m.get("memberRole");
                String staffName = (String) m.get("staffName");
                if (staffName == null || staffName.isEmpty()) continue;
                Row row = sheet2.createRow(memberRow++);
                row.createCell(0).setCellValue(p.getProjectNo());
                row.createCell(1).setCellValue(p.getProjectName());
                row.createCell(2).setCellValue(roleNameMap.getOrDefault(roleType, roleType));
                row.createCell(3).setCellValue(staffName);
                for (int c = 0; c < 4; c++) row.getCell(c).setCellStyle(dataStyle);
            }
        }

        // ===== Sheet3: 被测系统详情 =====
        XSSFSheet sheet3 = workbook.createSheet("被测系统详情");
        String[] sysHeads = { "项目编号", "项目名称", "系统序号", "系统名称", "系统级别", "测评依据", "备案编号",
                              "编写人", "审核人员", "报告结论", "质量审核得分" };
        Row sh = sheet3.createRow(0);
        for (int i = 0; i < sysHeads.length; i++) {
            Cell c = sh.createCell(i);
            c.setCellValue(sysHeads[i]);
            c.setCellStyle(headStyle);
            sheet3.setColumnWidth(i, 5500);
        }
        int sysRow = 1;
        for (ProjectDetailResp p : detailRecords) {
            if (p.getSystems() == null) continue;
            for (Map<String, Object> sys : p.getSystems()) {
                Row row = sheet3.createRow(sysRow++);
                row.createCell(0).setCellValue(p.getProjectNo());
                row.createCell(1).setCellValue(p.getProjectName());
                Object seq = sys.get("sysSeq");
                row.createCell(2).setCellValue(seq != null ? seq.toString() : "");
                row.createCell(3).setCellValue(sys.get("sysName") != null ? sys.get("sysName").toString() : "");
                Object level = sys.get("sysLevel");
                row.createCell(4).setCellValue(level != null ? (level.toString().equals("2") ? "二级" : level.toString().equals("3") ? "三级" : level.toString()) : "");
                row.createCell(5).setCellValue(sys.get("evalIndex") != null ? sys.get("evalIndex").toString() : "");
                row.createCell(6).setCellValue(sys.get("recordNo") != null ? sys.get("recordNo").toString() : "");
                row.createCell(7).setCellValue(sys.get("writerName") != null ? sys.get("writerName").toString() : "");
                row.createCell(8).setCellValue(sys.get("reviewerName") != null ? sys.get("reviewerName").toString() : "");
                row.createCell(9).setCellValue(sys.get("reportConclusion") != null ? sys.get("reportConclusion").toString() : "");
                Object qs = sys.get("qualityScore");
                row.createCell(10).setCellValue(qs != null ? qs.toString() : "");
                for (int c = 0; c < 11; c++) if (row.getCell(c) != null) row.getCell(c).setCellStyle(dataStyle);
            }
        }

        workbook.write(response.getOutputStream());
        workbook.close();
        logService.recordOperation("project", "EXPORT_FULL", "导出全部项目完整版，共" + detailRecords.size() + "条", "SUCCESS");
    }

    private List<List<String>> buildExportHead() {
        String[] cols = {
            "项目编号", "项目名称", "客户名称", "客户地址", "联系人姓名", "联系人电话",
            "系统名称合并", "2级系统数量", "3级系统数量",
            "项目类型", "所属行业", "项目经理", "项目负责人",
            "合同签订日期", "合同金额(元)", "纸质归档", "电子归档",
            "测评准备阶段", "方案编制阶段", "现场测评阶段", "报告编制阶段",
            "任务书任命表时间", "所属年份", "业务人员", "项目地区",
            "报告邮寄日期", "报告邮寄单号", "备注",
            // 所有角色人员列
            "登记测评师(项目组成员)", "实际测评人员", "调研表编制人", "方案编制人",
            "报告编制人", "网络测评人员", "主机测评人员", "物理测评人员",
            "工具扫描人员", "渗透测试人员",
            "创建时间"
        };
        List<List<String>> head = new ArrayList<>();
        for (String col : cols) {
            head.add(Collections.singletonList(col));
        }
        return head;
    }

    private List<List<Object>> buildExportData(List<ProjectDetailResp> records) {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<List<Object>> data = new ArrayList<>();
        for (ProjectDetailResp p : records) {
            List<Object> row = new ArrayList<>();
            row.add(p.getProjectNo());
            row.add(p.getProjectName());
            row.add(p.getCustomerName());
            row.add(p.getCustomerAddress());
            row.add(p.getCustomerContact());
            row.add(p.getCustomerPhone());
            row.add(p.getSystemNameMerged());
            row.add(p.getSysCountL2());
            row.add(p.getSysCountL3());
            row.add(p.getProjectTypeName());
            row.add(p.getIndustryName());
            row.add(p.getProjectManagerName());
            row.add(p.getProjectLeaderName());
            row.add(p.getContractDate() != null ? p.getContractDate().format(dateFmt) : "");
            row.add(p.getContractAmount());
            row.add(p.getPaperArchived() != null && p.getPaperArchived() == 1 ? "已归档" : "未归档");
            row.add(p.getElectronicArchived() != null && p.getElectronicArchived() == 1 ? "已归档" : "未归档");
            row.add(p.getPhasePrepare());
            row.add(p.getPhasePlan());
            row.add(p.getPhaseOnsite());
            row.add(p.getPhaseReport());
            row.add(p.getTaskAppointDate() != null ? p.getTaskAppointDate().format(dateFmt) : "");
            row.add(p.getYearBelong());
            row.add(p.getBusinessPerson());
            row.add(p.getProjectRegion());
            row.add(p.getReportMailDate() != null ? p.getReportMailDate().format(dateFmt) : "");
            row.add(p.getReportMailNo());
            row.add(p.getRemark());
            // 角色人员
            row.add(p.getProjectGroupMembers());
            row.add(p.getActualMemberNames());
            row.add(p.getSurveyEditorNames());
            row.add(p.getPlanEditorNames());
            row.add(p.getReportEditorNames());
            row.add(p.getNetworkEvaluatorNames());
            row.add(p.getHostEvaluatorNames());
            row.add(p.getPhysicalEvaluatorNames());
            row.add(p.getToolScannerNames());
            row.add(p.getPentestMemberNames());
            row.add(p.getCreatedAt() != null ? p.getCreatedAt().format(dtFmt) : "");
            data.add(row);
        }
        return data;
    }

    // ==================== 导入模板下载 ====================

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws Exception {
        String fileName = URLEncoder.encode("项目批量导入模板.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("项目导入");
        XSSFSheet dictSheet = workbook.createSheet("填写说明");

        // 表头样式
        XSSFCellStyle headStyle = workbook.createCellStyle();
        headStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        XSSFFont headFont = workbook.createFont();
        headFont.setBold(true);
        headStyle.setFont(headFont);
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setWrapText(true);

        // 必填列样式（红色标注）
        XSSFCellStyle requiredStyle = workbook.createCellStyle();
        requiredStyle.cloneStyleFrom(headStyle);
        XSSFFont requiredFont = workbook.createFont();
        requiredFont.setBold(true);
        requiredFont.setColor(IndexedColors.RED.getIndex());
        requiredStyle.setFont(requiredFont);

        // 写表头
        Row headerRow = sheet.createRow(0);
        headerRow.setHeight((short)900);
        for (int i = 0; i < IMPORT_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(IMPORT_HEADERS[i]);
            boolean required = IMPORT_HEADERS[i].endsWith("*");
            cell.setCellStyle(required ? requiredStyle : headStyle);
            sheet.setColumnWidth(i, 5500);
        }

        // 写一行示例数据
        Row exampleRow = sheet.createRow(1);
        String[] example = {
            "CP26-0101-01", "某单位信息系统等保测评项目", "某单位有限公司", "南京市XX路XX号",
            "张三", "13800138000",
            "某系统、某系统2", "1", "1",
            "电力", "电力", "2026-01-01", "50000",
            "0", "0",
            "2026-01-01", "2026-01-10", "2026-01-11", "2026-01-20",
            "2026-01-21", "2026-01-30", "2026-02-01", "2026-02-20",
            "2026-01-01", "2026", "", "华东地区", "", "", ""
        };
        XSSFCellStyle exampleStyle = workbook.createCellStyle();
        exampleStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        exampleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        exampleStyle.setBorderBottom(BorderStyle.THIN);
        exampleStyle.setBorderLeft(BorderStyle.THIN);
        exampleStyle.setBorderRight(BorderStyle.THIN);
        exampleStyle.setBorderTop(BorderStyle.THIN);
        for (int i = 0; i < example.length && i < IMPORT_HEADERS.length; i++) {
            Cell cell = exampleRow.createCell(i);
            cell.setCellValue(example[i]);
            cell.setCellStyle(exampleStyle);
        }

        // 写填写说明sheet
        String[][] instructions = {
            {"字段", "说明"},
            {"带*号字段", "为必填项，不能为空"},
            {"项目编号*", "唯一标识，导入时若已存在则跳过（报错提示）"},
            {"系统名称合并", "若不填，系统将根据导入后录入的被测系统自动生成；可手动填写，如：系统A、系统B"},
            {"2级/3级系统数量", "若不填默认为0，可手动填写整数"},
            {"项目类型", "填写字典中已配置的项目类型名称，如：电力、外围"},
            {"所属行业", "填写字典中已配置的行业名称，如：电力、医疗、政府、金融"},
            {"合同金额", "填写数字，如：50000"},
            {"纸质归档/电子归档", "填写0（未归档）或1（已归档）"},
            {"各阶段日期", "格式：yyyy-MM-dd，如：2026-01-01"},
            {"所属年份", "4位年份，如：2026"},
            {"系统名称合并", "用顿号（、）分隔多个系统名称"},
        };
        XSSFCellStyle instrHeadStyle = workbook.createCellStyle();
        instrHeadStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        instrHeadStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        instrHeadStyle.setBorderBottom(BorderStyle.THIN);
        instrHeadStyle.setBorderLeft(BorderStyle.THIN);
        instrHeadStyle.setBorderRight(BorderStyle.THIN);
        instrHeadStyle.setBorderTop(BorderStyle.THIN);
        XSSFFont instrFont = workbook.createFont();
        instrFont.setBold(true);
        instrHeadStyle.setFont(instrFont);
        dictSheet.setColumnWidth(0, 7000);
        dictSheet.setColumnWidth(1, 18000);
        for (int r = 0; r < instructions.length; r++) {
            Row row = dictSheet.createRow(r);
            for (int c = 0; c < 2; c++) {
                Cell cell = row.createCell(c);
                cell.setCellValue(instructions[r][c]);
                if (r == 0) cell.setCellStyle(instrHeadStyle);
            }
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // ==================== 导入项目 ====================

    @Override
    @Transactional
    public Map<String, Object> importProjects(MultipartFile file) throws Exception {
        List<Map<Integer, String>> rows = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                int rowIndex = context.readRowHolder().getRowIndex();
                if (rowIndex >= 1) { // 跳过表头行（0），跳过示例行（1），从第2行开始
                    if (rowIndex >= 2) rows.add(data);
                }
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).headRowNumber(0).sheet().doRead();

        int successCount = 0;
        int skipCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Map<Integer, String> row = rows.get(i);
            int excelRow = i + 3; // 实际行号（表头1，示例2，数据从3开始）
            try {
                String projectNo = trimNull(row.get(0));
                String projectName = trimNull(row.get(1));
                String customerName = trimNull(row.get(2));

                // 必填校验
                if (projectNo.isEmpty()) {
                    errors.add("第" + excelRow + "行：项目编号为空，跳过");
                    skipCount++;
                    continue;
                }
                if (projectName.isEmpty()) {
                    errors.add("第" + excelRow + "行：项目名称为空，跳过");
                    skipCount++;
                    continue;
                }
                if (customerName.isEmpty()) {
                    errors.add("第" + excelRow + "行：客户名称为空，跳过");
                    skipCount++;
                    continue;
                }

                // 项目编号重复校验
                long existCount = projectMapper.selectCount(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TProject>()
                                .eq(TProject::getProjectNo, projectNo));
                if (existCount > 0) {
                    errors.add("第" + excelRow + "行：项目编号[" + projectNo + "]已存在，跳过");
                    skipCount++;
                    continue;
                }

                TProject p = new TProject();
                p.setProjectNo(projectNo);
                p.setProjectName(encryptIfNotNull(projectName));
                p.setCustomerName(encryptIfNotNull(customerName));
                p.setCustomerAddress(encryptIfNotNull(trimNull(row.get(3))));
                p.setCustomerContact(encryptIfNotNull(trimNull(row.get(4))));
                p.setCustomerPhone(encryptIfNotNull(trimNull(row.get(5))));
                p.setSystemNameMerged(trimNull(row.get(6)));
                p.setSysCountL2(parseIntSafe(row.get(7), 0));
                p.setSysCountL3(parseIntSafe(row.get(8), 0));

                // 项目类型：按名称查字典
                String projectTypeName = trimNull(row.get(9));
                if (!projectTypeName.isEmpty()) {
                    Long typeId = dictItemMapper.findIdByLabelAndDictCode(projectTypeName, "PROJECT_TYPE");
                    p.setProjectTypeId(typeId);
                }
                // 所属行业
                String industryName = trimNull(row.get(10));
                if (!industryName.isEmpty()) {
                    Long indId = dictItemMapper.findIdByLabelAndDictCode(industryName, "INDUSTRY");
                    p.setIndustryId(indId);
                }

                p.setContractDate(parseDateSafe(row.get(11)));
                p.setContractAmount(encryptIfNotNull(trimNull(row.get(12))));
                p.setPaperArchived(parseIntSafe(row.get(13), 0));
                p.setElectronicArchived(parseIntSafe(row.get(14), 0));

                // 阶段时间：起止两列合并
                p.setPhasePrepare(buildPhaseRange(row.get(15), row.get(16)));
                p.setPhasePlan(buildPhaseRange(row.get(17), row.get(18)));
                p.setPhaseOnsite(buildPhaseRange(row.get(19), row.get(20)));
                p.setPhaseReport(buildPhaseRange(row.get(21), row.get(22)));

                p.setTaskAppointDate(parseDateSafe(row.get(23)));
                p.setYearBelong(trimNull(row.get(24)));
                p.setBusinessPerson(encryptIfNotNull(trimNull(row.get(25))));
                p.setProjectRegion(trimNull(row.get(26)));
                p.setReportMailDate(parseDateSafe(row.get(27)));
                p.setReportMailNo(trimNull(row.get(28)));
                p.setRemark(encryptIfNotNull(trimNull(row.get(29))));
                p.setCreatedBy(SecurityContextUtil.getCurrentUserIdSafe());

                projectMapper.insert(p);
                successCount++;
            } catch (Exception e) {
                log.warn("导入第{}行失败: {}", excelRow, e.getMessage());
                errors.add("第" + excelRow + "行：导入失败 - " + e.getMessage());
                skipCount++;
            }
        }

        logService.recordOperation("project", "IMPORT", "批量导入项目: 成功" + successCount + "条, 跳过" + skipCount + "条", "SUCCESS");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("successCount", successCount);
        result.put("skipCount", skipCount);
        result.put("totalCount", rows.size());
        result.put("errors", errors);
        return result;
    }

    // ==================== 统计 ====================

    @Override
    public List<Map<String, Object>> statsByManager(String year) {
        return projectMapper.statsByManager(year);
    }

    @Override
    public List<Map<String, Object>> statsByStaff(String year) {
        return projectMapper.statsByManager(year);
    }

    @Override
    public List<Map<String, Object>> statsByLevel(String year) {
        // 从数据库实际统计2级和3级系统数量
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TProject> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            if (year != null && !year.isEmpty()) {
                wrapper.eq(TProject::getYearBelong, year);
            }
            List<TProject> allProjects = projectMapper.selectList(wrapper);
            long l2Total = 0, l3Total = 0;
            for (TProject p : allProjects) {
                if (p.getSysCountL2() != null) l2Total += p.getSysCountL2();
                if (p.getSysCountL3() != null) l3Total += p.getSysCountL3();
            }
            return List.of(
                    Map.of("name", "二级", "cnt", l2Total),
                    Map.of("name", "三级", "cnt", l3Total),
                    Map.of("name", "四级", "cnt", 0L)
            );
        } catch (Exception e) {
            return List.of(
                    Map.of("name", "二级", "cnt", 0L),
                    Map.of("name", "三级", "cnt", 0L),
                    Map.of("name", "四级", "cnt", 0L)
            );
        }
    }

    @Override
    public List<Map<String, Object>> statsByType(String year) {
        return projectMapper.statsByType(year);
    }

    @Override
    public List<Map<String, Object>> statsByIndustry(String year) {
        return projectMapper.statsByIndustry(year);
    }

    @Override
    public List<Map<String, Object>> statsByWriter(String year) {
        List<Map<String, Object>> raw = projectMapper.statsByWriter(year);
        // 解密编写人姓名
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : raw) {
            Map<String, Object> item = new java.util.HashMap<>(row);
            Object writerName = item.get("writerName");
            if (writerName != null) {
                item.put("writerName", decryptIfNotNull(writerName.toString()));
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> statsByManagerDetail(String year) {
        List<Map<String, Object>> raw = projectMapper.statsByManagerDetail(year);
        // 解密项目经理姓名
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : raw) {
            Map<String, Object> item = new java.util.HashMap<>(row);
            Object managerName = item.get("managerName");
            if (managerName != null) {
                item.put("managerName", decryptIfNotNull(managerName.toString()));
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> statsByRegion(String year) {
        return projectMapper.statsByRegion(year);
    }

    @Override
    public Map<String, Object> statsByAmount(String year) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        try {
            List<Map<String, Object>> rows = projectMapper.findForAmountStats(year);
            // 月度金额：key=月份(1-12)，value=金额累计(元)
            java.math.BigDecimal[] monthly = new java.math.BigDecimal[13];
            for (int i = 1; i <= 12; i++) monthly[i] = java.math.BigDecimal.ZERO;
            java.math.BigDecimal totalYuan = java.math.BigDecimal.ZERO;

            for (Map<String, Object> row : rows) {
                Object amtObj = row.get("contract_amount");
                if (amtObj == null || amtObj.toString().isEmpty()) continue;
                String decrypted = decryptIfNotNull(amtObj.toString());
                if (decrypted == null || decrypted.isEmpty()) continue;
                java.math.BigDecimal amt;
                try { amt = new java.math.BigDecimal(decrypted.replaceAll("[,，]", "")); }
                catch (Exception ex) { continue; }
                // 年度总额
                totalYuan = totalYuan.add(amt);
                // 月度：按contract_date月份归属
                Object dateObj = row.get("contract_date");
                if (dateObj != null && !dateObj.toString().isEmpty()) {
                    String dateStr = dateObj.toString(); // yyyy-MM-dd 或 yyyy-MM-dd xx:xx:xx
                    try {
                        int month = Integer.parseInt(dateStr.substring(5, 7));
                        if (month >= 1 && month <= 12) {
                            monthly[month] = monthly[month].add(amt);
                        }
                    } catch (Exception ignored) {}
                }
            }
            // 年度总额转万元
            java.math.BigDecimal totalWan = totalYuan.divide(
                new java.math.BigDecimal("10000"), 2, java.math.RoundingMode.HALF_UP);
            result.put("totalWan", totalWan.toPlainString());

            // 月度列表
            List<Map<String, Object>> monthlyList = new ArrayList<>();
            String[] monthNames = {"","1月","2月","3月","4月","5月","6月",
                                   "7月","8月","9月","10月","11月","12月"};
            for (int i = 1; i <= 12; i++) {
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("month", monthNames[i]);
                m.put("amountWan", monthly[i].divide(
                    new java.math.BigDecimal("10000"), 2, java.math.RoundingMode.HALF_UP).toPlainString());
                monthlyList.add(m);
            }
            result.put("monthly", monthlyList);
        } catch (Exception e) {
            result.put("totalWan", "0.00");
            result.put("monthly", java.util.Collections.emptyList());
        }
        return result;
    }

    @Override
    public Map<String, Object> statsSummary(String year) {
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            // Total count
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TProject> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            if (year != null && !year.isEmpty()) {
                wrapper.eq(TProject::getYearBelong, year);
            }
            List<TProject> allProjects = projectMapper.selectList(wrapper);
            long total = allProjects.size();
            long l2Total = 0, l3Total = 0;
            for (TProject p : allProjects) {
                if (p.getSysCountL2() != null) l2Total += p.getSysCountL2();
                if (p.getSysCountL3() != null) l3Total += p.getSysCountL3();
            }
            result.put("total", total);
            result.put("inProgress", 0);
            result.put("completed", total);
            result.put("totalAmountWan", "0");
            result.put("sysCountL2", l2Total);
            result.put("sysCountL3", l3Total);
            result.put("typeDist", statsByType(year));
            result.put("industryDist", statsByIndustry(year));
            result.put("statusDist", java.util.Arrays.asList(
                new java.util.HashMap<>(java.util.Map.of("name", "全部项目", "value", total))
            ));
            result.put("monthlyAmount", java.util.Collections.emptyList());
        } catch (Exception e) {
            result.put("total", 0);
            result.put("inProgress", 0);
            result.put("completed", 0);
            result.put("totalAmountWan", "0");
            result.put("sysCountL2", 0);
            result.put("sysCountL3", 0);
            result.put("typeDist", java.util.Collections.emptyList());
            result.put("industryDist", java.util.Collections.emptyList());
            result.put("statusDist", java.util.Collections.emptyList());
            result.put("monthlyAmount", java.util.Collections.emptyList());
        }
        return result;
    }

    // ==================== 私有方法 ====================

    private TProject buildEntity(ProjectSaveReq req) {
        TProject p = new TProject();
        p.setProjectNo(req.getProjectNo());
        p.setProjectName(encryptIfNotNull(req.getProjectName()));
        p.setCustomerName(encryptIfNotNull(req.getCustomerName()));
        p.setCustomerAddress(encryptIfNotNull(req.getCustomerAddress()));
        p.setCustomerContact(encryptIfNotNull(req.getCustomerContact()));
        p.setCustomerPhone(encryptIfNotNull(req.getCustomerPhone()));
        p.setSystemNameMerged(req.getSystemNameMerged());
        p.setSysCountL2(req.getSysCountL2() != null ? req.getSysCountL2() : 0);
        p.setSysCountL3(req.getSysCountL3() != null ? req.getSysCountL3() : 0);
        p.setProjectTypeId(req.getProjectTypeId());
        p.setIndustryId(req.getIndustryId());
        p.setProjectManagerId(req.getProjectManagerId());
        p.setProjectLeaderId(req.getProjectLeaderId());
        p.setPaperArchived(req.getPaperArchived() != null ? req.getPaperArchived() : 0);
        p.setElectronicArchived(req.getElectronicArchived() != null ? req.getElectronicArchived() : 0);
        p.setContractAmount(encryptIfNotNull(req.getContractAmount()));
        p.setYearBelong(req.getYearBelong());
        p.setBusinessPerson(encryptIfNotNull(req.getBusinessPerson()));
        p.setProjectRegion(req.getProjectRegion());
        p.setRemark(encryptIfNotNull(req.getRemark()));
        p.setReportMailNo(req.getReportMailNo());

        if (req.getContractDate() != null && !req.getContractDate().isEmpty()) {
            try { p.setContractDate(LocalDate.parse(req.getContractDate())); } catch (Exception ignored) {}
        }
        if (req.getTaskAppointDate() != null && !req.getTaskAppointDate().isEmpty()) {
            try { p.setTaskAppointDate(LocalDate.parse(req.getTaskAppointDate())); } catch (Exception ignored) {}
        }
        if (req.getReportMailDate() != null && !req.getReportMailDate().isEmpty()) {
            try { p.setReportMailDate(LocalDate.parse(req.getReportMailDate())); } catch (Exception ignored) {}
        }

        p.setPhasePrepare(req.getPhasePrepare());
        p.setPhasePlan(req.getPhasePlan());
        p.setPhaseOnsite(req.getPhaseOnsite());
        p.setPhaseReport(req.getPhaseReport());
        return p;
    }

    private void saveSystems(Long projectId, List<ProjectSaveReq.ProjectSystemItem> systems) {
        if (systems == null || systems.isEmpty()) return;
        for (int i = 0; i < systems.size(); i++) {
            ProjectSaveReq.ProjectSystemItem item = systems.get(i);
            if (item.getSysName() == null || item.getSysName().trim().isEmpty()) continue;
            TProjectSystem sys = new TProjectSystem();
            sys.setProjectId(projectId);
            // 若前端未传序号，自动按顺序赋值
            sys.setSysSeq(item.getSysSeq() != null ? item.getSysSeq() : (i + 1));
            sys.setSysName(encryptIfNotNull(item.getSysName()));
            sys.setSysLevel(item.getSysLevel());
            sys.setEvalIndex(item.getEvalIndex() != null ? item.getEvalIndex() : "");
            sys.setRecordNo(item.getRecordNo());
            sys.setWriterId(item.getWriterId());
            sys.setReviewerId(item.getReviewerId());
            sys.setReportConclusion(item.getReportConclusion());
            sys.setQualityScore(item.getQualityScore());
            systemMapper.insert(sys);
        }
    }

    private ProjectDetailResp toResp(TProject p, boolean loadDetail) {
        ProjectDetailResp resp = new ProjectDetailResp();
        resp.setId(p.getId());
        resp.setProjectNo(p.getProjectNo());
        resp.setProjectName(decryptIfNotNull(p.getProjectName()));
        resp.setCustomerName(decryptIfNotNull(p.getCustomerName()));
        resp.setCustomerAddress(decryptIfNotNull(p.getCustomerAddress()));
        resp.setCustomerContact(decryptIfNotNull(p.getCustomerContact()));
        resp.setCustomerPhone(decryptIfNotNull(p.getCustomerPhone()));
        resp.setSystemNameMerged(p.getSystemNameMerged());
        resp.setSysCountL2(p.getSysCountL2());
        resp.setSysCountL3(p.getSysCountL3());
        resp.setProjectTypeId(p.getProjectTypeId());
        resp.setProjectTypeName(p.getProjectTypeName());
        resp.setIndustryId(p.getIndustryId());
        resp.setIndustryName(p.getIndustryName());
        resp.setProjectManagerId(p.getProjectManagerId());
        resp.setProjectManagerName(decryptIfNotNull(p.getProjectManagerName()));
        resp.setProjectLeaderId(p.getProjectLeaderId());
        resp.setProjectLeaderName(decryptIfNotNull(p.getProjectLeaderName()));
        resp.setContractDate(p.getContractDate());
        resp.setContractAmount(decryptIfNotNull(p.getContractAmount()));
        resp.setPaperArchived(p.getPaperArchived());
        resp.setElectronicArchived(p.getElectronicArchived());
        resp.setPhasePrepare(p.getPhasePrepare());
        resp.setPhasePlan(p.getPhasePlan());
        resp.setPhaseOnsite(p.getPhaseOnsite());
        resp.setPhaseReport(p.getPhaseReport());
        resp.setTaskAppointDate(p.getTaskAppointDate());
        resp.setYearBelong(p.getYearBelong());
        resp.setBusinessPerson(decryptIfNotNull(p.getBusinessPerson()));
        resp.setProjectRegion(p.getProjectRegion());
        resp.setRemark(decryptIfNotNull(p.getRemark()));
        resp.setReportMailDate(p.getReportMailDate());
        resp.setReportMailNo(p.getReportMailNo());
        resp.setCreatedAt(p.getCreatedAt());
        resp.setUpdatedAt(p.getUpdatedAt());
        resp.setCreatedBy(p.getCreatedBy());

        // 系统总数 = 2级 + 3级
        int l2 = p.getSysCountL2() != null ? p.getSysCountL2() : 0;
        int l3 = p.getSysCountL3() != null ? p.getSysCountL3() : 0;
        resp.setSysCount(l2 + l3);

        // 项目组成员（registered_evaluator角色）
        List<TProjectMember> memberList4Resp = memberMapper.findByProjectId(p.getId());
        List<String> registeredEvaluatorNames = new ArrayList<>();
        Set<Long> seenStaffIds = new java.util.LinkedHashSet<>();
        for (TProjectMember m : memberList4Resp) {
            if ("registered_evaluator".equals(m.getRoleType()) && m.getMemberId() != null) {
                if (seenStaffIds.add(m.getMemberId())) {
                    com.gydl.djbh.entity.TStaff staff = staffMapper.selectById(m.getMemberId());
                    if (staff != null) {
                        String name = decryptIfNotNull(staff.getRealName());
                        if (name != null && !name.isEmpty()) registeredEvaluatorNames.add(name);
                    }
                }
            }
        }
        resp.setProjectGroupMembers(String.join("、", registeredEvaluatorNames));

        // 填充所有角色人员姓名（用于宽表导出和列表显示）
        resp.setActualMemberNames(getMemberNamesByRole(memberList4Resp, "actual_member"));
        resp.setSurveyEditorNames(getMemberNamesByRole(memberList4Resp, "survey_editor"));
        resp.setPlanEditorNames(getMemberNamesByRole(memberList4Resp, "plan_editor"));
        resp.setReportEditorNames(getMemberNamesByRole(memberList4Resp, "report_editor"));
        resp.setNetworkEvaluatorNames(getMemberNamesByRole(memberList4Resp, "network_evaluator"));
        resp.setHostEvaluatorNames(getMemberNamesByRole(memberList4Resp, "host_evaluator"));
        resp.setPhysicalEvaluatorNames(getMemberNamesByRole(memberList4Resp, "physical_evaluator"));
        resp.setToolScannerNames(getMemberNamesByRole(memberList4Resp, "tool_scanner"));
        resp.setPentestMemberNames(getMemberNamesByRole(memberList4Resp, "pentest_member"));

        if (loadDetail) {
            // 被测系统
            List<TProjectSystem> sysList = systemMapper.findByProjectId(p.getId());
            List<Map<String, Object>> sysResult = new ArrayList<>();
            for (TProjectSystem sys : sysList) {
                Map<String, Object> sysMap = new HashMap<>();
                sysMap.put("id", sys.getId());
                sysMap.put("sysSeq", sys.getSysSeq());
                sysMap.put("sysName", decryptIfNotNull(sys.getSysName()));
                sysMap.put("sysLevel", sys.getSysLevel());
                sysMap.put("evalIndex", sys.getEvalIndex());
                sysMap.put("recordNo", sys.getRecordNo());
                sysMap.put("writerId", sys.getWriterId());
                sysMap.put("reviewerId", sys.getReviewerId());
                sysMap.put("reportConclusion", sys.getReportConclusion());
                sysMap.put("qualityScore", sys.getQualityScore());
                // 编写人姓名
                if (sys.getWriterId() != null) {
                    com.gydl.djbh.entity.TStaff writer = staffMapper.selectById(sys.getWriterId());
                    sysMap.put("writerName", writer != null ? decryptIfNotNull(writer.getRealName()) : "");
                } else {
                    sysMap.put("writerName", "");
                }
                // 审核人员姓名
                if (sys.getReviewerId() != null) {
                    com.gydl.djbh.entity.TStaff reviewer = staffMapper.selectById(sys.getReviewerId());
                    sysMap.put("reviewerName", reviewer != null ? decryptIfNotNull(reviewer.getRealName()) : "");
                } else {
                    sysMap.put("reviewerName", "");
                }
                sysResult.add(sysMap);
            }
            resp.setSystems(sysResult);

            // 项目人员（按角色分组，前端按 memberRole 筛选），复用 memberList4Resp
            List<Map<String, Object>> memberResult = new ArrayList<>();
            for (TProjectMember m : memberList4Resp) {
                Map<String, Object> mMap = new HashMap<>();
                mMap.put("staffId", m.getMemberId());
                mMap.put("memberRole", m.getRoleType());
                // 查询人员姓名
                if (m.getMemberId() != null) {
                    com.gydl.djbh.entity.TStaff staff = staffMapper.selectById(m.getMemberId());
                    if (staff != null) {
                        mMap.put("staffName", decryptIfNotNull(staff.getRealName()));
                        mMap.put("staffNo", staff.getStaffNo());
                    } else {
                        mMap.put("staffName", "");
                        mMap.put("staffNo", "");
                    }
                } else {
                    mMap.put("staffName", "");
                    mMap.put("staffNo", "");
                }
                memberResult.add(mMap);
            }
            resp.setMembers(memberResult);
        }
        return resp;
    }

    // ==================== 人员保存 ====================

    private void saveMembers(Long projectId, ProjectSaveReq req) {
        saveMembersByRole(projectId, "project_manager",
                req.getProjectManagerId() != null ? List.of(req.getProjectManagerId()) : null);
        saveMembersByRole(projectId, "project_leader",
                req.getProjectLeaderId() != null ? List.of(req.getProjectLeaderId()) : null);
        saveMembersByRole(projectId, "registered_evaluator", req.getRegisteredEvaluatorIds());
        saveMembersByRole(projectId, "actual_member", req.getActualMemberIds());
        saveMembersByRole(projectId, "survey_editor", req.getSurveyEditorIds());
        saveMembersByRole(projectId, "plan_editor", req.getPlanEditorIds());
        saveMembersByRole(projectId, "report_editor", req.getReportEditorIds());
        saveMembersByRole(projectId, "network_evaluator", req.getNetworkEvaluatorIds());
        saveMembersByRole(projectId, "host_evaluator", req.getHostEvaluatorIds());
        saveMembersByRole(projectId, "physical_evaluator", req.getPhysicalEvaluatorIds());
        saveMembersByRole(projectId, "tool_scanner", req.getToolScannerIds());
        saveMembersByRole(projectId, "pentest_member", req.getPentestMemberIds());
    }

    private void saveMembersByRole(Long projectId, String roleType, List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) return;
        for (Long memberId : memberIds) {
            if (memberId == null) continue;
            TProjectMember m = new TProjectMember();
            m.setProjectId(projectId);
            m.setMemberId(memberId);
            m.setRoleType(roleType);
            memberMapper.insert(m);
        }
    }

    /**
     * 获取指定角色的成员姓名，顿号分隔
     */
    private String getMemberNamesByRole(List<TProjectMember> members, String roleType) {
        List<String> names = new ArrayList<>();
        Set<Long> seen = new java.util.LinkedHashSet<>();
        for (TProjectMember m : members) {
            if (roleType.equals(m.getRoleType()) && m.getMemberId() != null) {
                if (seen.add(m.getMemberId())) {
                    com.gydl.djbh.entity.TStaff staff = staffMapper.selectById(m.getMemberId());
                    if (staff != null) {
                        String name = decryptIfNotNull(staff.getRealName());
                        if (name != null && !name.isEmpty()) names.add(name);
                    }
                }
            }
        }
        return String.join("、", names);
    }

    private String encryptIfNotNull(String value) {
        if (value == null || value.isEmpty()) return value;
        try { return sm4Util.encrypt(value); } catch (Exception e) { return value; }
    }

    private String decryptIfNotNull(String value) {
        if (value == null || value.isEmpty()) return value;
        try { return sm4Util.decrypt(value); } catch (Exception e) { return value; }
    }

    private String trimNull(String value) {
        if (value == null) return "";
        return value.trim();
    }

    private int parseIntSafe(String value, int defaultVal) {
        if (value == null || value.trim().isEmpty()) return defaultVal;
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return defaultVal; }
    }

    private LocalDate parseDateSafe(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            String v = value.trim();
            // 支持 yyyy-MM-dd 和 yyyy/MM/dd 格式
            v = v.replace("/", "-");
            return LocalDate.parse(v);
        } catch (Exception e) { return null; }
    }

    private String buildPhaseRange(String start, String end) {
        String s = trimNull(start);
        String e = trimNull(end);
        if (s.isEmpty() && e.isEmpty()) return null;
        return s + "~" + e;
    }
}
