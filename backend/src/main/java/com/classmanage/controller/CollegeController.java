package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.PageResult;
import com.classmanage.entity.College;
import com.classmanage.entity.Major;
import com.classmanage.entity.Teacher;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.service.CollegeService;
import com.classmanage.service.MajorService;
import com.classmanage.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 学院管理
 */
@RestController
@RequestMapping("/api/college")
public class CollegeController {

    @Autowired
    private CollegeService collegeService;
    @Autowired
    private MajorService majorService;
    @Autowired
    private TeacherService teacherService;

    @GetMapping("/list")
    public Result<java.util.List<College>> list() {
        return Result.ok(collegeService.list());
    }

    @GetMapping("/page")
    public Result<PageResult<College>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(College::getCollegeName, keyword);
        }
        wrapper.orderByAsc(College::getId);
        Page<College> p = collegeService.page(new Page<>(page, size), wrapper);
        return Result.ok(new PageResult<>(p.getTotal(), p.getRecords()));
    }

    @PostMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> add(@RequestBody College college) {
        college.setId(null);
        collegeService.save(college);
        return Result.ok();
    }

    @PutMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> update(@RequestBody College college) {
        if (college.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        collegeService.updateById(college);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> delete(@PathVariable Long id) {
        long majorCount = majorService.count(new LambdaQueryWrapper<Major>().eq(Major::getCollegeId, id));
        long teacherCount = teacherService.count(new LambdaQueryWrapper<Teacher>().eq(Teacher::getCollegeId, id));
        if (majorCount > 0) {
            throw new BusinessException("该学院下存在专业，无法删除");
        }
        if (teacherCount > 0) {
            throw new BusinessException("该学院下存在教师，无法删除");
        }
        collegeService.removeById(id);
        return Result.ok();
    }
}
