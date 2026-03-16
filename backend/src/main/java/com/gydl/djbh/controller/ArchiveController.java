package com.gydl.djbh.controller;

import com.gydl.djbh.dto.resp.Result;
import com.gydl.djbh.service.ArchiveService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 归档管理控制器
 */
@RestController
@RequestMapping("/archive")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    /** 获取模板列表 */
    @GetMapping("/templates")
    public Result<List<Map<String, Object>>> listTemplates() {
        return Result.ok(archiveService.listTemplates());
    }

    /** 上传模板 */
    @PostMapping("/templates/upload")
    @PreAuthorize("hasAuthority('archive:template')")
    public Result<Long> uploadTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "templateName", required = false) String templateName,
            @RequestParam(value = "templateCode", required = false) String templateCode,
            @RequestParam(value = "templateCategory", required = false) String templateCategory) throws Exception {
        String fname = file.getOriginalFilename() != null ? file.getOriginalFilename() : "template.docx";
        if (templateName == null || templateName.isEmpty()) templateName = fname;
        if (templateCode == null || templateCode.isEmpty()) templateCode = "tpl_" + System.currentTimeMillis();
        if (templateCategory == null || templateCategory.isEmpty()) templateCategory = "其他";
        Long id = archiveService.uploadTemplate(templateName, templateCode, templateCategory,
                file.getBytes(), fname);
        return Result.ok("上传成功", id);
    }

    /** 下载模板文件 */
    @GetMapping("/template/{id}/download")
    public void downloadTemplate(@PathVariable Long id, HttpServletResponse response) throws Exception {
        archiveService.downloadTemplate(id, response);
    }

    /** 删除/停用模板 */
    @DeleteMapping("/templates/{id}")
    @PreAuthorize("hasAuthority('archive:template')")
    public Result<?> deleteTemplate(@PathVariable Long id) {
        archiveService.deleteTemplate(id);
        return Result.ok("删除成功");
    }

    /** 生成归档材料（返回JSON结果，前端再调下载接口） */
    @PostMapping("/generate/{projectId}")
    @PreAuthorize("hasAnyAuthority('archive:create')")
    public Result<Map<String, Object>> generateArchive(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> body) throws Exception {

        List<Long> templateIds = castLongList(body.get("templateIds"));
        List<Long> selectedStaffIds = castLongList(body.get("selectedStaffIds"));
        List<Long> selectedDeviceIds = castLongList(body.get("selectedDeviceIds"));

        byte[] zipBytes = archiveService.generateArchive(projectId, templateIds,
                selectedStaffIds, selectedDeviceIds);

        String downloadKey = archiveService.storeArchive(projectId, zipBytes);

        Map<String, Object> result = new HashMap<>();
        result.put("downloadKey", downloadKey);
        result.put("fileCount", templateIds.size());
        List<Map<String, Object>> fileList = new ArrayList<>();
        fileList.add(Map.of("fileName", "归档材料.zip", "success", true, "message", "已生成 " + zipBytes.length + " 字节"));
        result.put("files", fileList);
        return Result.ok(result);
    }

    /** 下载已生成的归档包 */
    @GetMapping("/download/{projectId}")
    @PreAuthorize("hasAnyAuthority('archive:create')")
    public void downloadArchive(@PathVariable Long projectId,
                                 HttpServletResponse response) throws Exception {
        byte[] zipBytes = archiveService.getStoredArchive(projectId);
        if (zipBytes == null || zipBytes.length == 0) {
            response.sendError(404, "归档文件不存在，请先生成");
            return;
        }

        String fileName = "归档材料-" + projectId + "-" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".zip";

        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        response.setContentLength(zipBytes.length);
        response.getOutputStream().write(zipBytes);
    }

    /** 获取项目归档历史 */
    @GetMapping("/history/{projectId}")
    public Result<List<Map<String, Object>>> archiveHistory(@PathVariable Long projectId) {
        return Result.ok(archiveService.listArchiveHistory(projectId));
    }

    private List<Long> castLongList(Object obj) {
        if (obj == null) return new ArrayList<>();
        List<?> raw = (List<?>) obj;
        List<Long> result = new ArrayList<>();
        for (Object item : raw) {
            if (item instanceof Number) result.add(((Number) item).longValue());
        }
        return result;
    }
}
