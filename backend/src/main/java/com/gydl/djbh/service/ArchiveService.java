package com.gydl.djbh.service;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * 归档服务
 */
public interface ArchiveService {
    /**
     * 生成归档材料 ZIP
     */
    byte[] generateArchive(Long projectId, List<Long> templateIds,
                           List<Long> selectedStaffIds, List<Long> selectedDeviceIds) throws Exception;

    /**
     * 存储生成的归档文件供后续下载
     */
    String storeArchive(Long projectId, byte[] zipBytes);

    /**
     * 获取已存储的归档文件
     */
    byte[] getStoredArchive(Long projectId);

    /**
     * 下载模板文件
     */
    void downloadTemplate(Long id, HttpServletResponse response) throws Exception;

    /**
     * 获取归档模板列表
     */
    List<Map<String, Object>> listTemplates();

    /**
     * 上传归档模板
     */
    Long uploadTemplate(String templateName, String templateCode,
                        String templateCategory, byte[] fileBytes, String originalFilename);

    /**
     * 删除归档模板
     */
    void deleteTemplate(Long id);

    /**
     * 替换归档模板文件（更新指定ID模板的文件内容）
     */
    void replaceTemplate(Long id, byte[] fileBytes, String originalFilename);

    /**
     * 获取归档历史记录
     */
    List<Map<String, Object>> listArchiveHistory(Long projectId);
}
