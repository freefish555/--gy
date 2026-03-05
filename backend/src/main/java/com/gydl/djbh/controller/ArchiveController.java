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
import java.util.List;
import java.util.Map;

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
            @RequestParam("templateName") String templateName,
            @RequestParam("templateCode") String templateCode,
            @RequestParam("templateCategory") String templateCategory) throws Exception {
        Long id = archiveService.uploadTemplate(templateName, templateCode, templateCategory,
                file.getBytes(), file.getOriginalFilename());
        return Result.ok("上传成功", id);
    }

    /** 删除/停用模板 */
    @DeleteMapping("/templates/{id}")
    @PreAuthorize("hasAuthority('archive:template')")
    public Result<?> deleteTemplate(@PathVariable Long id) {
        archiveService.deleteTemplate(id);
        return Result.ok("删除成功");
    }

    /** 生成归档材料并下载ZIP */
    @PostMapping("/generate/{projectId}")
    @PreAuthorize("hasAnyAuthority('archive:create')")
    public void generateArchive(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> body,
            HttpServletResponse response) throws Exception {

        @SuppressWarnings("unchecked")
        List<Long> templateIds = (List<Long>) body.get("templateIds");
        @SuppressWarnings("unchecked")
        List<Long> selectedStaffIds = (List<Long>) body.get("selectedStaffIds");
        @SuppressWarnings("unchecked")
        List<Long> selectedDeviceIds = (List<Long>) body.get("selectedDeviceIds");

        byte[] zipBytes = archiveService.generateArchive(projectId, templateIds,
                selectedStaffIds, selectedDeviceIds);

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
}
