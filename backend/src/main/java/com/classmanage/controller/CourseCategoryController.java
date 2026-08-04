package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.entity.Course;
import com.classmanage.entity.CourseCategory;
import com.classmanage.entity.MajorPlan;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.service.CourseCategoryService;
import com.classmanage.service.CourseService;
import com.classmanage.service.MajorPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程类别管理
 */
@RestController
@RequestMapping("/api/course-category")
public class CourseCategoryController {

    @Autowired
    private CourseCategoryService courseCategoryService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private MajorPlanService majorPlanService;

    @GetMapping("/list")
    public Result<List<CourseCategory>> list() {
        return Result.ok(courseCategoryService.list());
    }

    @PostMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> add(@RequestBody CourseCategory category) {
        if (!StringUtils.hasText(category.getCategoryCode()) || !StringUtils.hasText(category.getCategoryName())) {
            throw new BusinessException("类别编号与名称不能为空");
        }
        category.setId(null);
        courseCategoryService.save(category);
        return Result.ok();
    }

    @PutMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> update(@RequestBody CourseCategory category) {
        if (category.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        courseCategoryService.updateById(category);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> delete(@PathVariable Long id) {
        long courseCount = courseService.count(new LambdaQueryWrapper<Course>().eq(Course::getCategoryId, id));
        long planCount = majorPlanService.count(new LambdaQueryWrapper<MajorPlan>().eq(MajorPlan::getCategoryId, id));
        if (courseCount > 0) {
            throw new BusinessException("该类别下存在课程，无法删除");
        }
        if (planCount > 0) {
            throw new BusinessException("该类别在专业计划中，无法删除");
        }
        courseCategoryService.removeById(id);
        return Result.ok();
    }
}
