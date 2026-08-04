package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.MajorPlanVO;
import com.classmanage.dto.PageResult;
import com.classmanage.entity.CourseCategory;
import com.classmanage.entity.Major;
import com.classmanage.entity.MajorPlan;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.service.CourseCategoryService;
import com.classmanage.service.MajorPlanService;
import com.classmanage.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * 专业计划管理
 */
@RestController
@RequestMapping("/api/plan")
public class MajorPlanController {

    @Autowired
    private MajorPlanService majorPlanService;
    @Autowired
    private MajorService majorService;
    @Autowired
    private CourseCategoryService courseCategoryService;

    @GetMapping("/page")
    public Result<PageResult<MajorPlanVO>> page(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "10") long size,
                                                 @RequestParam(required = false) Long majorId) {
        LambdaQueryWrapper<MajorPlan> wrapper = new LambdaQueryWrapper<>();
        if (majorId != null) {
            wrapper.eq(MajorPlan::getMajorId, majorId);
        }
        wrapper.orderByAsc(MajorPlan::getMajorId).orderByAsc(MajorPlan::getYearLevel).orderByAsc(MajorPlan::getSemester);
        Page<MajorPlan> p = majorPlanService.page(new Page<>(page, size), wrapper);
        java.util.List<MajorPlanVO> list = p.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return Result.ok(new PageResult<>(p.getTotal(), list));
    }

    @PostMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> add(@RequestBody MajorPlan plan) {
        plan.setId(null);
        if (majorPlanService.count(new LambdaQueryWrapper<MajorPlan>()
                .eq(MajorPlan::getMajorId, plan.getMajorId())
                .eq(MajorPlan::getCategoryId, plan.getCategoryId())) > 0) {
            throw new BusinessException("该课程类别已在专业计划中");
        }
        majorPlanService.save(plan);
        return Result.ok();
    }

    @PutMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> update(@RequestBody MajorPlan plan) {
        if (plan.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        majorPlanService.updateById(plan);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> delete(@PathVariable Long id) {
        majorPlanService.removeById(id);
        return Result.ok();
    }

    private MajorPlanVO toVO(MajorPlan p) {
        MajorPlanVO vo = new MajorPlanVO();
        vo.setId(p.getId());
        vo.setMajorId(p.getMajorId());
        vo.setCategoryId(p.getCategoryId());
        vo.setYearLevel(p.getYearLevel());
        vo.setSemester(p.getSemester());
        Major m = majorService.getById(p.getMajorId());
        CourseCategory cat = courseCategoryService.getById(p.getCategoryId());
        vo.setMajorName(m != null ? m.getMajorName() : null);
        vo.setCategoryName(cat != null ? cat.getCategoryName() : null);
        vo.setCategoryCode(cat != null ? cat.getCategoryCode() : null);
        return vo;
    }
}
