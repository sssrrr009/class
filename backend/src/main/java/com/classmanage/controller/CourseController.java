package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.CourseVO;
import com.classmanage.dto.PageResult;
import com.classmanage.entity.Course;
import com.classmanage.entity.Teacher;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.service.CourseService;
import com.classmanage.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程信息管理
 */
@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private com.classmanage.service.CourseCategoryService courseCategoryService;

    @GetMapping("/list")
    public Result<List<Course>> list() {
        return Result.ok(courseService.list());
    }

    /**
     * 教师查看自己授课的课程
     */
    @GetMapping("/my")
    @RequireRole({Role.TEACHER})
    public Result<List<CourseVO>> my() {
        java.util.List<Course> courses = courseService.list(
                new LambdaQueryWrapper<Course>().eq(Course::getTeacherId, com.classmanage.security.UserContext.getUserId()));
        return Result.ok(courses.stream().map(this::toVO).collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/page")
    public Result<PageResult<CourseVO>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Course::getCourseName, keyword).or().like(Course::getCourseCode, keyword));
        }
        wrapper.orderByAsc(Course::getId);
        Page<Course> p = courseService.page(new Page<>(page, size), wrapper);
        java.util.List<CourseVO> list = p.getRecords().stream().map(this::toVO).collect(java.util.stream.Collectors.toList());
        return Result.ok(new PageResult<>(p.getTotal(), list));
    }

    @PostMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> add(@RequestBody Course course) {
        course.setId(null);
        courseService.save(course);
        return Result.ok();
    }

    @PutMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> update(@RequestBody Course course) {
        if (course.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        courseService.updateById(course);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.ok();
    }

    private CourseVO toVO(Course c) {
        CourseVO vo = new CourseVO();
        vo.setId(c.getId());
        vo.setCourseCode(c.getCourseCode());
        vo.setCourseName(c.getCourseName());
        vo.setCourseHours(c.getCourseHours());
        vo.setCredit(c.getCredit());
        vo.setTeacherId(c.getTeacherId());
        vo.setClassTime(c.getClassTime());
        vo.setLocation(c.getLocation());
        vo.setStatus(c.getStatus());
        vo.setCapacity(c.getCapacity());
        vo.setCategoryId(c.getCategoryId());
        Teacher t = c.getTeacherId() == null ? null : teacherService.getById(c.getTeacherId());
        vo.setTeacherName(t != null ? t.getName() : null);
        if (c.getCategoryId() != null) {
            com.classmanage.entity.CourseCategory cat = courseCategoryService.getById(c.getCategoryId());
            vo.setCategoryName(cat != null ? cat.getCategoryName() : null);
        }
        return vo;
    }
}
