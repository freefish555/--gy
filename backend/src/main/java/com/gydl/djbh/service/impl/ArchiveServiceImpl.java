package com.gydl.djbh.service.impl;

import com.gydl.djbh.crypto.SM4Util;
import com.gydl.djbh.entity.*;
import com.gydl.djbh.exception.BusinessException;
import com.gydl.djbh.mapper.*;
import com.gydl.djbh.service.ArchiveService;
import com.gydl.djbh.utils.SecurityContextUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 归档材料生成服务实现
 * 核心功能：读取Word模板 -> 替换占位符 -> 打包ZIP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveServiceImpl implements ArchiveService {

    private final TProjectMapper projectMapper;
    private final TProjectSystemMapper systemMapper;
    private final TStaffMapper staffMapper;
    private final TEvalDeviceMapper deviceMapper;
    private final TPentestToolMapper toolMapper;
    private final TArchiveTemplateMapper templateMapper;
    private final SM4Util sm4Util;

    @Value("${app.file.template-path:./data/templates}")
    private String templateDir;

    @Value("${app.file.archive-path:./data/archives}")
    private String archiveDir;

    @Override
    public byte[] generateArchive(Long projectId, List<Long> templateIds,
                                   List<Long> selectedStaffIds, List<Long> selectedDeviceIds) throws Exception {
        // 1. 加载项目信息
        TProject project = projectMapper.findByIdWithInfo(projectId);
        if (project == null) throw new BusinessException("项目不存在");

        // 2. 加载被测系统
        List<TProjectSystem> systems = systemMapper.findByProjectId(projectId);

        // 3. 构建占位符映射表
        Map<String, String> placeholders = buildPlaceholders(project, systems);

        // 4. 加载模板列表
        List<TArchiveTemplate> templates;
        if (templateIds == null || templateIds.isEmpty()) {
            templates = templateMapper.findAllActive();
        } else {
            templates = new ArrayList<>();
            for (Long tid : templateIds) {
                TArchiveTemplate t = templateMapper.selectById(tid);
                if (t != null && t.getStatus() == 1) templates.add(t);
            }
        }

        if (templates.isEmpty()) {
            throw new BusinessException("没有可用的归档模板，请先在系统设置中上传模板");
        }

        // 5. 生成ZIP
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            String projectNoPlain = project.getProjectNo();

            for (TArchiveTemplate template : templates) {
                try {
                    byte[] docBytes;
                    // 判断是否是工具清单模板
                    if ("tool_list".equals(template.getTemplateCode())) {
                        docBytes = generateToolListDoc(project, selectedStaffIds, selectedDeviceIds);
                    } else {
                        docBytes = processTemplate(template, placeholders, systems);
                    }

                    if (docBytes != null) {
                        // 文件名：原模板名，替换xmbh为项目编号
                        String fileName = template.getFileOriginalName()
                                .replace("xmbh", projectNoPlain)
                                .replace("XMBH", projectNoPlain);
                        zos.putNextEntry(new ZipEntry(fileName));
                        zos.write(docBytes);
                        zos.closeEntry();
                    }
                } catch (Exception e) {
                    log.warn("处理模板 [{}] 时出错: {}", template.getTemplateName(), e.getMessage());
                }
            }
        }

        return baos.toByteArray();
    }

    @Override
    public List<Map<String, Object>> listTemplates() {
        List<TArchiveTemplate> list = templateMapper.findAllActive();
        List<Map<String, Object>> result = new ArrayList<>();
        for (TArchiveTemplate t : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("templateName", t.getTemplateName());
            map.put("templateCode", t.getTemplateCode());
            map.put("templateCategory", t.getTemplateCategory());
            map.put("fileName", t.getFileOriginalName());
            map.put("fileSize", t.getFileSize());
            map.put("version", t.getVersion());
            map.put("status", t.getStatus());
            map.put("updatedAt", t.getUploadAt());
            result.add(map);
        }
        return result;
    }

    @Override
    public Long uploadTemplate(String templateName, String templateCode,
                                String templateCategory, byte[] fileBytes, String originalFilename) {
        // 确保目录存在
        Path dir = Paths.get(templateDir);
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            throw new BusinessException("创建模板目录失败");
        }

        // 生成存储文件名（防重名）
        String storedName = System.currentTimeMillis() + "_" + originalFilename;
        Path filePath = dir.resolve(storedName);

        try {
            Files.write(filePath, fileBytes);
        } catch (Exception e) {
            throw new BusinessException("保存模板文件失败: " + e.getMessage());
        }

        // 查找同code的现有模板，若存在则更新版本
        TArchiveTemplate existing = null;
        List<TArchiveTemplate> existingList = templateMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TArchiveTemplate>()
                        .eq(TArchiveTemplate::getTemplateCode, templateCode));
        if (!existingList.isEmpty()) {
            existing = existingList.get(0);
            existing.setTemplateName(templateName);
            existing.setTemplateCategory(templateCategory);
            existing.setFileOriginalName(originalFilename);
            existing.setFilePath(filePath.toString());
            existing.setFileSize((long) fileBytes.length);
            existing.setVersion("v" + (parseVersionNum(existing.getVersion()) + 1) + ".0");
            existing.setStatus(1);
            templateMapper.updateById(existing);
            return existing.getId();
        }

        TArchiveTemplate template = new TArchiveTemplate();
        template.setTemplateName(templateName);
        template.setTemplateCode(templateCode);
        template.setTemplateCategory(templateCategory);
        template.setFileOriginalName(originalFilename);
        template.setFilePath(filePath.toString());
        template.setFileSize((long) fileBytes.length);
        template.setVersion("v1.0");
        template.setIsDefault(1);
        template.setStatus(1);
        template.setUploadBy(SecurityContextUtil.getCurrentUserIdSafe());
        templateMapper.insert(template);
        return template.getId();
    }

    @Override
    public void deleteTemplate(Long id) {
        TArchiveTemplate template = templateMapper.selectById(id);
        if (template == null) throw new BusinessException("模板不存在");
        template.setStatus(0);
        templateMapper.updateById(template);
    }

    @Override
    public void replaceTemplate(Long id, byte[] fileBytes, String originalFilename) {
        TArchiveTemplate template = templateMapper.selectById(id);
        if (template == null) throw new BusinessException("模板不存在");

        // 保存新文件
        Path dir = Paths.get(templateDir);
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            throw new BusinessException("创建模板目录失败");
        }
        String storedName = System.currentTimeMillis() + "_" + originalFilename;
        Path filePath = dir.resolve(storedName);
        try {
            Files.write(filePath, fileBytes);
        } catch (Exception e) {
            throw new BusinessException("保存模板文件失败: " + e.getMessage());
        }

        // 更新数据库记录
        template.setFileOriginalName(originalFilename);
        template.setFilePath(filePath.toString());
        template.setFileSize((long) fileBytes.length);
        template.setVersion("v" + (parseVersionNum(template.getVersion()) + 1) + ".0");
        template.setStatus(1);
        templateMapper.updateById(template);
    }

    @Override
    public List<Map<String, Object>> listArchiveHistory(Long projectId) {
        // 返回归档记录（如果有归档日志表，此处扩展）
        return new ArrayList<>();
    }

    // In-memory store for generated archives (projectId -> zip bytes)
    private final Map<Long, byte[]> archiveStore = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public String storeArchive(Long projectId, byte[] zipBytes) {
        archiveStore.put(projectId, zipBytes);
        return "archive_" + projectId + ".zip";
    }

    @Override
    public byte[] getStoredArchive(Long projectId) {
        return archiveStore.get(projectId);
    }

    @Override
    public void downloadTemplate(Long id, HttpServletResponse response) throws Exception {
        TArchiveTemplate template = templateMapper.selectById(id);
        if (template == null) throw new BusinessException("模板不存在");

        Path filePath = Paths.get(template.getFilePath());
        if (!Files.exists(filePath)) {
            throw new BusinessException("模板文件不存在，请重新上传");
        }

        byte[] fileBytes = Files.readAllBytes(filePath);
        String fileName = template.getFileOriginalName() != null
                ? template.getFileOriginalName() : "template.docx";

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + java.net.URLEncoder.encode(fileName, "UTF-8") + "\"");
        response.setContentLength(fileBytes.length);
        response.getOutputStream().write(fileBytes);
        response.getOutputStream().flush();
    }

    // ==================== 核心：占位符替换 ====================

    /**
     * 处理单个Word模板，替换占位符
     */
    private byte[] processTemplate(TArchiveTemplate template, Map<String, String> placeholders,
                                    List<TProjectSystem> systems) throws Exception {
        Path filePath = Paths.get(template.getFilePath());
        if (!Files.exists(filePath)) {
            log.warn("模板文件不存在: {}", filePath);
            return null;
        }

        byte[] fileBytes = Files.readAllBytes(filePath);
        String lowerName = template.getFileOriginalName() != null ? template.getFileOriginalName().toLowerCase() : filePath.getFileName().toString().toLowerCase();

        if (lowerName.endsWith(".docx")) {
            return replaceDocxPlaceholders(fileBytes, placeholders);
        } else if (lowerName.endsWith(".doc")) {
            // .doc格式：尝试作为docx处理，或直接返回原文件
            try {
                return replaceDocxPlaceholders(fileBytes, placeholders);
            } catch (Exception e) {
                log.warn("处理.doc文件失败，返回原文件: {}", e.getMessage());
                return fileBytes;
            }
        } else if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
            // Excel模板处理（简化版：直接返回）
            return fileBytes;
        }

        return fileBytes;
    }

    /**
     * 替换 .docx 文件中的占位符
     * 支持格式：{{xmbh}} 或 ${xmbh}
     */
    private byte[] replaceDocxPlaceholders(byte[] fileBytes, Map<String, String> placeholders) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(fileBytes))) {
            // 替换段落中的占位符
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                replaceParagraphPlaceholders(paragraph, placeholders);
            }

            // 替换表格中的占位符
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            replaceParagraphPlaceholders(paragraph, placeholders);
                        }
                    }
                }
            }

            // 替换页眉页脚
            for (XWPFHeader header : doc.getHeaderList()) {
                for (XWPFParagraph p : header.getParagraphs()) {
                    replaceParagraphPlaceholders(p, placeholders);
                }
            }
            for (XWPFFooter footer : doc.getFooterList()) {
                for (XWPFParagraph p : footer.getParagraphs()) {
                    replaceParagraphPlaceholders(p, placeholders);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 替换段落中的占位符
     * 注意：Word可能将一个{{xmbh}}拆分到多个Run中，需要合并后再替换
     * 支持三种格式：{{xmbh}}、${xmbh}、裸占位符xmbh（模板直接使用变量名）
     */
    private void replaceParagraphPlaceholders(XWPFParagraph paragraph, Map<String, String> placeholders) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) return;

        // 将所有Run文本合并
        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text != null) fullText.append(text);
        }

        String combined = fullText.toString();
        if (combined.isEmpty()) return;

        // 检查是否含有任何占位符（{{...}}、${...} 或 裸变量名）
        boolean hasPlaceholder = combined.contains("{{") || combined.contains("${");
        if (!hasPlaceholder) {
            // 检查是否含有裸占位符（模板直接使用变量名，如 xmbh、xmmc 等）
            for (String key : placeholders.keySet()) {
                if (combined.contains(key)) {
                    hasPlaceholder = true;
                    break;
                }
            }
        }
        if (!hasPlaceholder) return;

        // 执行替换
        String replaced = combined;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() != null ? entry.getValue() : "";
            // 先替换带括号格式（优先级高）
            replaced = replaced.replace("{{" + key + "}}", value);
            replaced = replaced.replace("${" + key + "}", value);
            // 也尝试带空格的格式
            replaced = replaced.replace("{{ " + key + " }}", value);
            // 最后替换裸占位符（整词匹配，避免误替换）
            replaced = replaced.replace(key, value);
        }

        if (!replaced.equals(combined)) {
            // 将替换后的文本写入第一个Run，清空其余Run
            if (!runs.isEmpty()) {
                runs.get(0).setText(replaced, 0);
                for (int i = 1; i < runs.size(); i++) {
                    runs.get(i).setText("", 0);
                }
            }
        }
    }

    /**
     * 生成测评工具清单文档
     */
    private byte[] generateToolListDoc(TProject project, List<Long> selectedStaffIds,
                                        List<Long> selectedDeviceIds) throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            // 标题
            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText(project.getProjectNo() + " 测评工具清单");
            titleRun.setBold(true);
            titleRun.setFontSize(14);

            doc.createParagraph(); // 空行

            // 表1：硬件测评设备
            XWPFParagraph p1 = doc.createParagraph();
            p1.createRun().setText("一、测评设备清单");

            XWPFTable deviceTable = doc.createTable();
            // 表头
            XWPFTableRow headerRow = deviceTable.getRow(0);
            setCell(headerRow, 0, "序号");
            headerRow.addNewTableCell(); setCell(headerRow, 1, "设备编号");
            headerRow.addNewTableCell(); setCell(headerRow, 2, "设备名称");
            headerRow.addNewTableCell(); setCell(headerRow, 3, "型号");
            headerRow.addNewTableCell(); setCell(headerRow, 4, "使用人");

            // 添加选定设备行（按选择的人员过滤）
            List<TEvalDevice> devices;
            if (selectedStaffIds != null && !selectedStaffIds.isEmpty()) {
                devices = new ArrayList<>();
                for (Long staffId : selectedStaffIds) {
                    devices.addAll(deviceMapper.findByStaffId(staffId));
                }
                // 也添加无固定使用人的扫描设备（如果有选中的设备ID）
                if (selectedDeviceIds != null) {
                    for (Long deviceId : selectedDeviceIds) {
                        TEvalDevice d = deviceMapper.selectById(deviceId);
                        if (d != null) devices.add(d);
                    }
                }
            } else {
                devices = deviceMapper.findAllWithStaff();
            }

            int seq = 1;
            for (TEvalDevice d : devices) {
                XWPFTableRow row = deviceTable.createRow();
                setCell(row, 0, String.valueOf(seq++));
                setCell(row, 1, d.getDeviceNo() != null ? d.getDeviceNo() : "");
                setCell(row, 2, d.getDeviceName() != null ? d.getDeviceName() : "");
                setCell(row, 3, d.getDeviceModel() != null ? d.getDeviceModel() : "");
                setCell(row, 4, d.getOwnerStaffName() != null ? decrypt(d.getOwnerStaffName()) : "/");
            }

            doc.createParagraph(); // 空行

            // 表2：渗透软件工具（全部导出）
            XWPFParagraph p2 = doc.createParagraph();
            p2.createRun().setText("二、渗透软件工具清单");

            XWPFTable toolTable = doc.createTable();
            XWPFTableRow toolHeader = toolTable.getRow(0);
            setCell(toolHeader, 0, "序号");
            toolHeader.addNewTableCell(); setCell(toolHeader, 1, "工具编号");
            toolHeader.addNewTableCell(); setCell(toolHeader, 2, "工具名称");
            toolHeader.addNewTableCell(); setCell(toolHeader, 3, "版本号");

            List<TPentestTool> tools = toolMapper.findAllActive();
            seq = 1;
            for (TPentestTool t : tools) {
                XWPFTableRow row = toolTable.createRow();
                setCell(row, 0, String.valueOf(seq++));
                setCell(row, 1, t.getToolNo() != null ? t.getToolNo() : "");
                setCell(row, 2, t.getToolName() != null ? t.getToolName() : "");
                setCell(row, 3, t.getToolVersion() != null ? t.getToolVersion() : "");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        }
    }

    private void setCell(XWPFTableRow row, int index, String text) {
        XWPFTableCell cell = row.getCell(index);
        if (cell == null) cell = row.addNewTableCell();
        cell.setText(text);
    }

    /**
     * 构建项目占位符映射表（与模板中的{{xmbh}}等对应）
     */
    private Map<String, String> buildPlaceholders(TProject project, List<TProjectSystem> systems) {
        Map<String, String> map = new HashMap<>();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        DateTimeFormatter dateFmt2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 基础字段
        map.put("xmbh", project.getProjectNo());
        map.put("xmmc", decrypt(project.getProjectName()));
        map.put("khmc", decrypt(project.getCustomerName()));
        map.put("khdz", decrypt(project.getCustomerAddress()));
        map.put("lxr", decrypt(project.getCustomerContact()));
        map.put("lxdh", decrypt(project.getCustomerPhone()));
        map.put("rwbh", project.getProjectNo()); // taskNo已删除，使用项目编号替代
        map.put("xtmc", project.getSystemNameMerged());
        map.put("xtsl", systems != null ? String.valueOf(systems.size()) : "0");

        // 日期
        if (project.getContractDate() != null) {
            map.put("htrq", project.getContractDate().format(dateFmt2));
            map.put("htrqcn", project.getContractDate().format(dateFmt));
        } else {
            map.put("htrq", "");
            map.put("htrqcn", "");
        }
        if (project.getTaskAppointDate() != null) {
            map.put("sqsj", project.getTaskAppointDate().format(dateFmt2));
        } else {
            map.put("sqsj", "");
        }

        // 阶段时间
        map.put("cpzbjd", project.getPhasePrepare() != null ? project.getPhasePrepare() : "");
        map.put("fabzjd", project.getPhasePlan() != null ? project.getPhasePlan() : "");
        map.put("xccpjd", project.getPhaseOnsite() != null ? project.getPhaseOnsite() : "");
        map.put("bgbzjd", project.getPhaseReport() != null ? project.getPhaseReport() : "");

        // 年份
        map.put("year", project.getYearBelong() != null ? project.getYearBelong() :
                String.valueOf(LocalDate.now().getYear()));

        // 当前日期
        map.put("today", LocalDate.now().format(dateFmt));
        map.put("todaydate", LocalDate.now().format(dateFmt2));

        // 被测系统（多系统时拼接）
        if (systems != null && !systems.isEmpty()) {
            StringBuilder sysNames = new StringBuilder();
            StringBuilder sysLevels = new StringBuilder();
            StringBuilder sysRecords = new StringBuilder();
            StringBuilder sysDetail = new StringBuilder();

            for (int i = 0; i < systems.size(); i++) {
                TProjectSystem sys = systems.get(i);
                String sysName = decrypt(sys.getSysName());
                String level = sys.getSysLevel() != null ? sys.getSysLevel() + "级" : "";
                String recordNo = sys.getRecordNo() != null ? sys.getRecordNo() : "";
                String evalIndex = sys.getEvalIndex() != null ? sys.getEvalIndex() : "";

                if (i > 0) {
                    sysNames.append("、");
                    sysLevels.append("、");
                    sysRecords.append("、");
                }
                sysNames.append(sysName);
                sysLevels.append(level);
                sysRecords.append(recordNo);
                sysDetail.append(String.format("%s/%s/%s/%s", sysName, level, evalIndex, recordNo));
                if (i < systems.size() - 1) sysDetail.append("\n");

                // 单个系统占位符（按序号）
                int sysIdx = i + 1;
                map.put("xtmc" + sysIdx, sysName);
                map.put("xtdj" + sysIdx, level);
                map.put("xtzs" + sysIdx, evalIndex);
                map.put("bah" + sysIdx, recordNo);
            }

            map.put("xtmclist", sysNames.toString());
            map.put("xtdjlist", sysLevels.toString());
            map.put("bahlist", sysRecords.toString());
            map.put("xtdetail", sysDetail.toString());

            // 首个系统信息（兼容单系统模板）
            TProjectSystem first = systems.get(0);
            map.put("xtdj", first.getSysLevel() != null ? first.getSysLevel() + "级" : "");
            map.put("xtzs", first.getEvalIndex() != null ? first.getEvalIndex() : "");
            map.put("bah", first.getRecordNo() != null ? first.getRecordNo() : "");
            map.put("xmdj", first.getSysLevel() != null ? first.getSysLevel() + "级" : "");

            // 系统等级名称备案（格式: 2级/S2A2/系统1/320123）
            StringBuilder xmdjba = new StringBuilder();
            for (TProjectSystem sys : systems) {
                if (xmdjba.length() > 0) xmdjba.append("\n");
                xmdjba.append(sys.getSysLevel()).append("级/")
                        .append(sys.getEvalIndex() != null ? sys.getEvalIndex() : "").append("/")
                        .append(decrypt(sys.getSysName())).append("/")
                        .append(sys.getRecordNo() != null ? sys.getRecordNo() : "");
            }
            map.put("xmdjba", xmdjba.toString());
        }

        return map;
    }

    private String decrypt(String value) {
        if (value == null || value.isEmpty()) return "";
        try { return sm4Util.decrypt(value); } catch (Exception e) { return value; }
    }

    private int parseVersionNum(String version) {
        if (version == null || version.isEmpty()) return 1;
        try {
            String num = version.replaceAll("[^0-9]", "");
            return num.isEmpty() ? 1 : Integer.parseInt(num);
        } catch (Exception e) {
            return 1;
        }
    }
}
