package com.gydl.djbh.service.impl;

import com.gydl.djbh.crypto.SM4Util;
import com.gydl.djbh.dto.req.ProjectQueryReq;
import com.gydl.djbh.dto.req.ProjectSaveReq;
import com.gydl.djbh.dto.resp.PageResult;
import com.gydl.djbh.dto.resp.ProjectDetailResp;
import com.gydl.djbh.entity.TProject;
import com.gydl.djbh.entity.TProjectSystem;
import com.gydl.djbh.exception.BusinessException;
import com.gydl.djbh.mapper.TProjectMapper;
import com.gydl.djbh.mapper.TProjectSystemMapper;
import com.gydl.djbh.mapper.TStaffMapper;
import com.gydl.djbh.service.LogService;
import com.gydl.djbh.service.ProjectService;
import com.gydl.djbh.utils.SecurityContextUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final TProjectMapper projectMapper;
    private final TProjectSystemMapper systemMapper;
    private final TStaffMapper staffMapper;
    private final SM4Util sm4Util;
    private final LogService logService;

    @Override
    public PageResult<ProjectDetailResp> page(ProjectQueryReq req) {
        int offset = (req.getPageNum() - 1) * req.getPageSize();
        Integer statusInt = null;
        if (req.getProjectStatus() != null && !req.getProjectStatus().isEmpty()) {
            try { statusInt = Integer.parseInt(req.getProjectStatus()); } catch (Exception ignored) {}
        }
        List<TProject> list = projectMapper.findPage(
                req.getProjectNo(), encryptIfNotNull(req.getProjectName()),
                encryptIfNotNull(req.getCustomerName()), req.getProjectManagerId(),
                req.getProjectLeaderId(), statusInt,
                req.getProjectTypeId(), req.getIndustryId(),
                req.getYearBelong(), offset, req.getPageSize());
        long total = projectMapper.countPage(
                req.getProjectNo(), encryptIfNotNull(req.getProjectName()),
                encryptIfNotNull(req.getCustomerName()), req.getProjectManagerId(),
                req.getProjectLeaderId(), statusInt,
                req.getProjectTypeId(), req.getIndustryId(), req.getYearBelong());

        List<ProjectDetailResp> result = new ArrayList<>();
        for (TProject p : list) {
            result.add(toResp(p, false));
        }
        return PageResult.of(total, req.getPageNum(), req.getPageSize(), result);
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
        TProject project = buildEntity(req);
        project.setCreatedBy(SecurityContextUtil.getCurrentUserIdSafe());
        projectMapper.insert(project);

        // 保存被测系统
        saveSystems(project.getId(), req.getSystems());

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

        // 重建被测系统
        systemMapper.deleteByProjectId(id);
        saveSystems(id, req.getSystems());

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
        logService.recordOperation("project", "DELETE", "删除项目ID: " + id, "SUCCESS");
    }

    @Override
    @Transactional
    public void batchUpdate(List<Long> ids, String action, Object value) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            TProject project = projectMapper.selectById(id);
            if (project == null) continue;
            if ("status".equals(action) && value != null) {
                project.setProjectStatus(Integer.parseInt(value.toString()));
                projectMapper.updateById(project);
            } else if ("manager".equals(action) && value != null) {
                project.setProjectManagerId(Long.parseLong(value.toString()));
                projectMapper.updateById(project);
            }
        }
        logService.recordOperation("project", "BATCH_UPDATE", "批量操作" + ids.size() + "个项目: " + action, "SUCCESS");
    }

    @Override
    public void export(ProjectQueryReq req, HttpServletResponse response) throws Exception {
        req.setPageSize(10000);
        req.setPageNum(1);
        PageResult<ProjectDetailResp> result = page(req);

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode("项目列表.csv", StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("项目编号,项目名称,客户名称,系统名称,项目类型,所属行业,项目经理,项目状态,合同签订日期,任务编号,年度\n");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (ProjectDetailResp p : result.getRecords()) {
            sb.append(csvEscape(p.getProjectNo())).append(",");
            sb.append(csvEscape(p.getProjectName())).append(",");
            sb.append(csvEscape(p.getCustomerName())).append(",");
            sb.append(csvEscape(p.getSystemNameMerged())).append(",");
            sb.append(csvEscape(p.getProjectTypeName())).append(",");
            sb.append(csvEscape(p.getIndustryName())).append(",");
            sb.append(csvEscape(p.getProjectManagerName())).append(",");
            sb.append(csvEscape(p.getProjectStatusName())).append(",");
            sb.append(p.getContractDate() != null ? p.getContractDate().format(fmt) : "").append(",");
            sb.append(csvEscape(p.getTaskNo())).append(",");
            sb.append(csvEscape(p.getYearBelong())).append("\n");
        }
        response.getWriter().write(sb.toString());
    }

    @Override
    public List<Map<String, Object>> statsByManager(String year) {
        return projectMapper.statsByManager(year);
    }

    @Override
    public List<Map<String, Object>> statsByStaff(String year) {
        // 简化：按项目经理统计，可扩展为成员统计
        return projectMapper.statsByManager(year);
    }

    @Override
    public List<Map<String, Object>> statsByLevel(String year) {
        // 查所有项目的被测系统按等级统计
        return List.of(
                Map.of("name", "二级", "cnt", 0L),
                Map.of("name", "三级", "cnt", 0L),
                Map.of("name", "四级", "cnt", 0L)
        );
    }

    @Override
    public List<Map<String, Object>> statsByType(String year) {
        return projectMapper.statsByType(year);
    }

    @Override
    public List<Map<String, Object>> statsByIndustry(String year) {
        return projectMapper.statsByIndustry(year);
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
        p.setTaskNo(req.getTaskNo());
        p.setProjectTypeId(req.getProjectTypeId());
        p.setIndustryId(req.getIndustryId());
        p.setProjectManagerId(req.getProjectManagerId());
        p.setProjectLeaderId(req.getProjectLeaderId());
        p.setProjectStatus(req.getProjectStatus() != null ? req.getProjectStatus() : 0);
        p.setPaperArchived(req.getPaperArchived() != null ? req.getPaperArchived() : 0);
        p.setContractAmount(encryptIfNotNull(req.getContractAmount()));
        p.setYearBelong(req.getYearBelong());
        p.setBusinessPerson(encryptIfNotNull(req.getBusinessPerson()));
        p.setProjectRegion(req.getProjectRegion());
        p.setRemark(encryptIfNotNull(req.getRemark()));
        p.setReportMailNo(req.getReportMailNo());

        // 日期解析
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
        for (ProjectSaveReq.ProjectSystemItem item : systems) {
            TProjectSystem sys = new TProjectSystem();
            sys.setProjectId(projectId);
            sys.setSysSeq(item.getSysSeq());
            sys.setSysName(encryptIfNotNull(item.getSysName()));
            sys.setSysLevel(item.getSysLevel());
            sys.setEvalIndex(item.getEvalIndex());
            sys.setRecordNo(item.getRecordNo());
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
        resp.setTaskNo(p.getTaskNo());
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
        resp.setProjectStatus(p.getProjectStatus());
        resp.setProjectStatusName(getStatusName(p.getProjectStatus()));
        resp.setPaperArchived(p.getPaperArchived());
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

        if (loadDetail) {
            // 加载被测系统列表
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
                sysResult.add(sysMap);
            }
            resp.setSystems(sysResult);
        }
        return resp;
    }

    private String getStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待启动";
            case 1 -> "已分配";
            case 2 -> "进行中";
            case 3 -> "已完成";
            case 4 -> "电子归档";
            default -> "未知";
        };
    }

    private String encryptIfNotNull(String value) {
        if (value == null || value.isEmpty()) return value;
        try { return sm4Util.encrypt(value); } catch (Exception e) { return value; }
    }

    private String decryptIfNotNull(String value) {
        if (value == null || value.isEmpty()) return value;
        try { return sm4Util.decrypt(value); } catch (Exception e) { return value; }
    }

    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
