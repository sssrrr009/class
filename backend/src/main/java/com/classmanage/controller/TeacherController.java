package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.PageResult;
import com.classmanage.entity.ClassInfo;
import com.classmanage.entity.Course;
import com.classmanage.entity.Teacher;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.service.ClassInfoService;
import com.classmanage.service.CourseService;
import com.classmanage.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师管理（管理员）
 */
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private ClassInfoService classInfoService;
    @Autowired
    private CourseService courseService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping("/list")
    public Result<List<Teacher>> list() {
        return Result.ok(teacherService.list());
    }

    @GetMapping("/page")
    public Result<PageResult<Teacher>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Teacher::getTeacherNo, keyword).or().like(Teacher::getName, keyword));
        }
        wrapper.orderByAsc(Teacher::getId);
        Page<Teacher> p = teacherService.page(new Page<>(page, size), wrapper);
        return Result.ok(new PageResult<>(p.getTotal(), p.getRecords()));
    }

    @PostMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> add(@RequestBody Teacher teacher) {
        teacher.setId(null);
        // 默认密码 123456
        if (!StringUtils.hasText(teacher.getPassword())) {
            teacher.setPassword(encoder.encode("123456"));
        } else {
            teacher.setPassword(encoder.encode(teacher.getPassword()));
        }
        teacherService.save(teacher);
        return Result.ok();
    }

    @PutMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> update(@RequestBody Teacher teacher) {
        if (teacher.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        teacherService.updateById(teacher);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> delete(@PathVariable Long id) {
        long classCount = classInfoService.count(new LambdaQueryWrapper<ClassInfo>().eq(ClassInfo::getTeacherId, id));
        long courseCount = courseService.count(new LambdaQueryWrapper<Course>().eq(Course::getTeacherId, id));
        if (classCount > 0) {
            throw new BusinessException("该教师仍担任班主任，无法删除");
        }
        if (courseCount > 0) {
            throw new BusinessException("该教师仍任课，无法删除");
        }
        teacherService.removeById(id);
        return Result.ok();
    }
}
