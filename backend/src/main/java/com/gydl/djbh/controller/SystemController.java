package com.gydl.djbh.controller;

import com.gydl.djbh.dto.resp.Result;
import com.gydl.djbh.entity.TDict;
import com.gydl.djbh.entity.TDictItem;
import com.gydl.djbh.mapper.TDictItemMapper;
import com.gydl.djbh.mapper.TDictMapper;
import com.gydl.djbh.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统设置控制器
 */
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final SysConfigService sysConfigService;
    private final TDictMapper dictMapper;
    private final TDictItemMapper dictItemMapper;

    /** 获取所有系统配置 */
    @GetMapping("/config")
    @PreAuthorize("hasAnyAuthority('system:config','system:key')")
    public Result<List<Map<String, Object>>> getConfigs() {
        return Result.ok(sysConfigService.listConfigs());
    }

    /** 批量保存系统配置 */
    @PostMapping("/config")
    @PreAuthorize("hasAnyAuthority('system:config','system:key')")
    public Result<?> saveConfigs(@RequestBody Map<String, String> configs) {
        sysConfigService.batchSave(configs);
        return Result.ok("保存成功");
    }

    /** 获取单个配置 */
    @GetMapping("/config/{key}")
    public Result<String> getConfig(@PathVariable String key) {
        return Result.ok(sysConfigService.getConfig(key, ""));
    }

    // ========== 字典管理 ==========

    /** 获取所有字典 */
    @GetMapping("/dict")
    public Result<List<TDict>> listDicts() {
        return Result.ok(dictMapper.findAll());
    }

    /** 获取字典项（按字典编码） */
    @GetMapping("/dict/{dictCode}/items")
    public Result<List<TDictItem>> listDictItems(@PathVariable String dictCode) {
        return Result.ok(dictItemMapper.findByDictCode(dictCode));
    }

    /** 新增字典项 */
    @PostMapping("/dict/{dictCode}/items")
    @PreAuthorize("hasAuthority('system:dict')")
    public Result<?> addDictItem(@PathVariable String dictCode, @RequestBody TDictItem item) {
        TDict dict = dictMapper.findByCode(dictCode);
        if (dict == null) return Result.fail("字典不存在");
        item.setDictId(dict.getId());
        item.setStatus(1);
        dictItemMapper.insert(item);
        return Result.ok("添加成功");
    }

    /** 更新字典项 */
    @PutMapping("/dict/items/{id}")
    @PreAuthorize("hasAuthority('system:dict')")
    public Result<?> updateDictItem(@PathVariable Long id, @RequestBody TDictItem item) {
        item.setId(id);
        dictItemMapper.updateById(item);
        return Result.ok("更新成功");
    }

    /** 删除字典项 */
    @DeleteMapping("/dict/items/{id}")
    @PreAuthorize("hasAuthority('system:dict')")
    public Result<?> deleteDictItem(@PathVariable Long id) {
        dictItemMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    /** 获取项目类型下拉 */
    @GetMapping("/dict/project-types")
    public Result<List<TDictItem>> projectTypes() {
        return Result.ok(dictItemMapper.findByDictCode("PROJECT_TYPE"));
    }

    /** 获取所属行业下拉 */
    @GetMapping("/dict/industries")
    public Result<List<TDictItem>> industries() {
        return Result.ok(dictItemMapper.findByDictCode("INDUSTRY"));
    }
}
