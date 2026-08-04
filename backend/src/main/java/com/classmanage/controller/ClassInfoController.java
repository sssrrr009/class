package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.PageResult;
import com.classmanage.entity.ClassInfo;
import com.classmanage.entity.Major;
import com.classmanage.entity.Student;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.service.ClassInfoService;
import com.classmanage.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级信息管理
 */
@RestController
@RequestMapping("/api/class")
public class ClassInfoController {

    @Autowired
    private ClassInfoService classInfoService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private com.classmanage.service.MajorService majorService;

    @GetMapping("/list")
    public Result<List<ClassInfo>> list() {
        return Result.ok(classInfoService.list());
    }

    @GetMapping("/page")
    public Result<PageResult<ClassInfo>> page(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) Long collegeId,
                                               @RequestParam(required = false) Long majorId,
                                               @RequestParam(required = false) Long teacherId) {
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        // 按班主任过滤
        if (teacherId != null) {
            wrapper.eq(ClassInfo::getTeacherId, teacherId);
        }
        // 按专业过滤
        if (majorId != null) {
            wrapper.eq(ClassInfo::getMajorId, majorId);
        }
        // 按学院过滤: 先查该学院下的专业
        if (collegeId != null) {
            java.util.List<Major> majors = majorService.list(
                    new LambdaQueryWrapper<Major>().eq(Major::getCollegeId, collegeId));
            if (majors.isEmpty()) {
                return Result.ok(new PageResult<>(0L, java.util.Collections.emptyList()));
            }
            wrapper.in(ClassInfo::getMajorId, majors.stream().map(Major::getId).collect(java.util.stream.Collectors.toList()));
        }
        wrapper.orderByAsc(ClassInfo::getId);
        Page<ClassInfo> p = classInfoService.page(new Page<>(page, size), wrapper);
        return Result.ok(new PageResult<>(p.getTotal(), p.getRecords()));
    }

    @PostMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> add(@RequestBody ClassInfo cls) {
        cls.setId(null);
        classInfoService.save(cls);
        return Result.ok();
    }

    @PutMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> update(@RequestBody ClassInfo cls) {
        if (cls.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        classInfoService.updateById(cls);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> delete(@PathVariable Long id) {
        long studentCount = studentService.count(new LambdaQueryWrapper<Student>().eq(Student::getClassId, id));
        if (studentCount > 0) {
            throw new BusinessException("该班级下存在学生，无法删除");
        }
        classInfoService.removeById(id);
        return Result.ok();
    }
}
