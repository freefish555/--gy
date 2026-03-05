package com.gydl.djbh.service;

import java.util.List;
import java.util.Map;

/**
 * 归档服务
 */
public interface ArchiveService {
    /**
     * 生成归档材料 ZIP
     * @param projectId 项目ID
     * @param templateIds 选择的模板ID列表（null=全部）
     * @param selectedStaffIds 选择的人员ID列表（用于工具清单）
     * @param selectedDeviceIds 选择的设备ID列表
     * @return ZIP文件字节数组
     */
    byte[] generateArchive(Long projectId, List<Long> templateIds, 
                           List<Long> selectedStaffIds, List<Long> selectedDeviceIds) throws Exception;

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
     * 获取归档历史记录
     */
    List<Map<String, Object>> listArchiveHistory(Long projectId);
}
