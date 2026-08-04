package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.PageResult;
import com.classmanage.entity.ClassInfo;
import com.classmanage.entity.Major;
import com.classmanage.entity.MajorPlan;
import com.classmanage.service.ClassInfoService;
import com.classmanage.service.MajorPlanService;
import com.classmanage.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 专业管理
 */
@RestController
@RequestMapping("/api/major")
public class MajorController {

    @Autowired
    private MajorService majorService;
    @Autowired
    private ClassInfoService classInfoService;
    @Autowired
    private MajorPlanService majorPlanService;

    @GetMapping("/list")
    public Result<List<Major>> list(@RequestParam(required = false) Long collegeId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        if (collegeId != null) {
            wrapper.eq(Major::getCollegeId, collegeId);
        }
        wrapper.orderByAsc(Major::getId);
        return Result.ok(majorService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<PageResult<Major>> page(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Major::getMajorName, keyword).or().like(Major::getMajorCode, keyword));
        }
        wrapper.orderByAsc(Major::getId);
        Page<Major> p = majorService.page(new Page<>(page, size), wrapper);
        return Result.ok(new PageResult<>(p.getTotal(), p.getRecords()));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Major major) {
        major.setId(null);
        majorService.save(major);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Major major) {
        if (major.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        majorService.updateById(major);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        long classCount = classInfoService.count(new LambdaQueryWrapper<ClassInfo>().eq(ClassInfo::getMajorId, id));
        long planCount = majorPlanService.count(new LambdaQueryWrapper<MajorPlan>().eq(MajorPlan::getMajorId, id));
        if (classCount > 0) {
            throw new BusinessException("该专业下存在班级，无法删除");
        }
        if (planCount > 0) {
            throw new BusinessException("该专业下存在专业计划，无法删除");
        }
        majorService.removeById(id);
        return Result.ok();
    }
}
