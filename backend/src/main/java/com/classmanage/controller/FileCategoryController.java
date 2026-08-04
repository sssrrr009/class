package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.entity.FileCategory;
import com.classmanage.entity.FileInfo;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.service.FileCategoryService;
import com.classmanage.service.FileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件分类管理
 */
@RestController
@RequestMapping("/api/category")
public class FileCategoryController {

    @Autowired
    private FileCategoryService fileCategoryService;
    @Autowired
    private FileInfoService fileInfoService;

    @GetMapping("/list")
    public Result<List<FileCategory>> list() {
        return Result.ok(fileCategoryService.list());
    }

    @PostMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> add(@RequestBody FileCategory category) {
        if (!StringUtils.hasText(category.getCategoryName())) {
            throw new BusinessException("分类名称不能为空");
        }
        category.setId(null);
        fileCategoryService.save(category);
        return Result.ok();
    }

    @PutMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> update(@RequestBody FileCategory category) {
        if (category.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        fileCategoryService.updateById(category);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> delete(@PathVariable Long id) {
        long count = fileInfoService.count(new LambdaQueryWrapper<FileInfo>().eq(FileInfo::getCategoryId, id));
        if (count > 0) {
            throw new BusinessException("该分类下存在文件，无法删除");
        }
        fileCategoryService.removeById(id);
        return Result.ok();
    }
}
