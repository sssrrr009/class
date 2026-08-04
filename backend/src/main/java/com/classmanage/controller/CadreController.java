package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.CadreVO;
import com.classmanage.dto.PageResult;
import com.classmanage.entity.Cadre;
import com.classmanage.entity.Student;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.service.CadreService;
import com.classmanage.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 学生干部管理（管理员）
 */
@RestController
@RequestMapping("/api/cadre")
public class CadreController {

    @Autowired
    private CadreService cadreService;
    @Autowired
    private StudentService studentService;

    @GetMapping("/page")
    public Result<PageResult<CadreVO>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Cadre> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Cadre::getCadreType, keyword);
        }
        wrapper.orderByAsc(Cadre::getId);
        Page<Cadre> p = cadreService.page(new Page<>(page, size), wrapper);
        List<CadreVO> list = p.getRecords().stream().map(this::toVO).collect(java.util.stream.Collectors.toList());
        return Result.ok(new PageResult<>(p.getTotal(), list));
    }

    @PostMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> add(@RequestBody Cadre cadre) {
        cadre.setId(null);
        validate(cadre);
        // 允许同一学生担任多个干部身份(如既是班长又是学生会)
        cadreService.save(cadre);
        return Result.ok();
    }

    @PutMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> update(@RequestBody Cadre cadre) {
        if (cadre.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        validate(cadre);
        cadreService.updateById(cadre);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> delete(@PathVariable Long id) {
        cadreService.removeById(id);
        return Result.ok();
    }

    private void validate(Cadre cadre) {
        if (cadre.getStudentId() == null) {
            throw new BusinessException("必须选择学生");
        }
        if ("MONITOR".equals(cadre.getCadreType()) && cadre.getClassId() == null) {
            throw new BusinessException("班长必须关联班级");
        }
        if ("UNION".equals(cadre.getCadreType()) && !StringUtils.hasText(cadre.getUnionScope())) {
            throw new BusinessException("学生会干部必须设置权限范围");
        }
    }

    private CadreVO toVO(Cadre c) {
        CadreVO vo = new CadreVO();
        vo.setId(c.getId());
        vo.setStudentId(c.getStudentId());
        vo.setCadreType(c.getCadreType());
        vo.setClassId(c.getClassId());
        vo.setUnionScope(c.getUnionScope());
        vo.setCreateTime(c.getCreateTime());
        Student s = studentService.getById(c.getStudentId());
        if (s != null) {
            vo.setStudentNo(s.getStudentNo());
            vo.setStudentName(s.getName());
        }
        return vo;
    }
}
