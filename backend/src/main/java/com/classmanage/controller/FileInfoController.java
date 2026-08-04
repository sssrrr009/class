package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.FileStatVO;
import com.classmanage.dto.PageResult;
import com.classmanage.entity.FileCategory;
import com.classmanage.entity.FileInfo;
import com.classmanage.security.RequireLogin;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.security.UserContext;
import com.classmanage.service.FileCategoryService;
import com.classmanage.service.FileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件信息管理（上传/下载/统计）
 */
@RestController
@RequestMapping("/api/file")
public class FileInfoController {

    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private FileCategoryService fileCategoryService;

    @Value("${classmanage.file.upload-dir}")
    private String uploadDir;

    /**
     * 分页搜索文件
     */
    @GetMapping("/page")
    @RequireLogin
    public Result<PageResult<FileInfo>> page(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Long categoryId) {
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(FileInfo::getFileName, keyword).or().like(FileInfo::getDescription, keyword));
        }
        if (categoryId != null) {
            wrapper.eq(FileInfo::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(FileInfo::getCreateTime);
        Page<FileInfo> p = fileInfoService.page(new Page<>(page, size), wrapper);
        return Result.ok(new PageResult<>(p.getTotal(), p.getRecords()));
    }

    /**
     * 上传文件（管理员/教师）
     */
    @PostMapping
    @RequireRole({Role.ADMIN, Role.TEACHER})
    public Result<Void> upload(@RequestParam("file") MultipartFile file,
                                @RequestParam(required = false) Long categoryId,
                                @RequestParam(required = false) String description) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new BusinessException("文件名不合法");
        }
        // 大小校验 20MB
        if (file.getSize() > 20L * 1024 * 1024) {
            throw new BusinessException("文件不能超过 20MB");
        }
        try {
            // 目录: uploads/yyyy/MM/
            String datePath = LocalDate.now().getYear() + "/" + String.format("%02d", LocalDate.now().getMonthValue());
            File dir = new File(uploadDir + datePath);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new BusinessException("创建上传目录失败");
            }
            String ext = "";
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0) {
                ext = originalName.substring(dot);
            }
            String storeName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = Paths.get(dir.getAbsolutePath(), storeName);
            file.transferTo(target.toAbsolutePath());

            FileInfo info = new FileInfo();
            info.setFileName(originalName);
            info.setCategoryId(categoryId);
            info.setDescription(description);
            info.setFilePath(datePath + "/" + storeName);
            info.setFileSize(file.getSize());
            info.setUploader(userName());
            fileInfoService.save(info);
            return Result.ok();
        } catch (IOException e) {
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }
    }

    /**
     * 修改文件信息
     */
    @PutMapping
    @RequireRole({Role.ADMIN, Role.TEACHER})
    public Result<Void> update(@RequestBody FileInfo info) {
        if (info.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        fileInfoService.updateById(info);
        return Result.ok();
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN, Role.TEACHER})
    public Result<Void> delete(@PathVariable Long id) {
        FileInfo info = fileInfoService.getById(id);
        if (info != null) {
            // 删除物理文件
            try {
                File f = new File(uploadDir + info.getFilePath());
                if (f.exists()) {
                    f.delete();
                }
            } catch (Exception ignored) {
            }
            fileInfoService.removeById(id);
        }
        return Result.ok();
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{id}")
    @RequireLogin
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        FileInfo info = fileInfoService.getById(id);
        if (info == null) {
            throw new BusinessException("文件不存在");
        }
        File file = new File(uploadDir + info.getFilePath());
        if (!file.exists()) {
            throw new BusinessException("文件已丢失");
        }
        String encoded = URLEncoder.encode(info.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(new FileSystemResource(file));
    }

    /**
     * 文件类型占比统计
     */
    @GetMapping("/stat")
    @RequireLogin
    public Result<List<FileStatVO>> stat() {
        List<FileCategory> categories = fileCategoryService.list();
        List<FileStatVO> list = new ArrayList<>();
        long total = fileInfoService.count();
        for (FileCategory c : categories) {
            long count = fileInfoService.count(new LambdaQueryWrapper<FileInfo>().eq(FileInfo::getCategoryId, c.getId()));
            FileStatVO vo = new FileStatVO();
            vo.setName(c.getCategoryName());
            vo.setValue(count);
            list.add(vo);
        }
        // 未分类
        long uncategorized = fileInfoService.count(new LambdaQueryWrapper<FileInfo>().isNull(FileInfo::getCategoryId));
        if (uncategorized > 0) {
            FileStatVO vo = new FileStatVO();
            vo.setName("未分类");
            vo.setValue(uncategorized);
            list.add(vo);
        }
        return Result.ok(list);
    }

    private String userName() {
        Role role = UserContext.getRole();
        if (role == Role.TEACHER) {
            return "教师";
        }
        return "管理员";
    }
}
