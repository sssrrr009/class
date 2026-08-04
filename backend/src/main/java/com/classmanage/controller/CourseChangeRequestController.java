package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.PageResult;
import com.classmanage.entity.Course;
import com.classmanage.entity.CourseChangeRequest;
import com.classmanage.entity.Teacher;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.security.UserContext;
import com.classmanage.service.CourseChangeRequestService;
import com.classmanage.service.CourseService;
import com.classmanage.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程修改申请管理
 * 教师提交修改申请,管理员审批
 */
@RestController
@RequestMapping("/api/course-change")
public class CourseChangeRequestController {

    @Autowired
    private CourseChangeRequestService requestService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private TeacherService teacherService;

    /**
     * 教师提交修改申请（只能申请自己授课的课程）
     */
    @PostMapping
    @RequireRole({Role.TEACHER})
    public Result<Void> submit(@RequestBody CourseChangeRequest req) {
        Long teacherId = UserContext.getUserId();
        Course course = courseService.getById(req.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        if (!teacherId.equals(course.getTeacherId())) {
            throw new BusinessException("只能申请修改自己授课的课程");
        }
        if (req.getNewData() == null || req.getNewData().isEmpty()) {
            throw new BusinessException("请填写修改后的课程信息");
        }
        // 校验 JSON 可解析
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            om.readTree(req.getNewData());
        } catch (Exception e) {
            throw new BusinessException("课程数据格式不合法");
        }
        req.setId(null);
        req.setTeacherId(teacherId);
        req.setStatus(0);
        req.setFieldName(null);
        req.setNewValue(null);
        requestService.save(req);
        return Result.ok();
    }

    /**
     * 教师查询自己的申请
     */
    @GetMapping("/my")
    @RequireRole({Role.TEACHER})
    public Result<List<CourseChangeRequestVO>> my() {
        Long teacherId = UserContext.getUserId();
        List<CourseChangeRequest> list = requestService.list(
                new LambdaQueryWrapper<CourseChangeRequest>()
                        .eq(CourseChangeRequest::getTeacherId, teacherId)
                        .orderByDesc(CourseChangeRequest::getCreateTime));
        return Result.ok(list.stream().map(this::toVO).collect(Collectors.toList()));
    }

    /**
     * 管理员分页查询全部申请
     */
    @GetMapping("/page")
    @RequireRole({Role.ADMIN})
    public Result<PageResult<CourseChangeRequestVO>> page(@RequestParam(defaultValue = "1") long page,
                                                           @RequestParam(defaultValue = "10") long size,
                                                           @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<CourseChangeRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(CourseChangeRequest::getStatus, status);
        }
        wrapper.orderByDesc(CourseChangeRequest::getCreateTime);
        Page<CourseChangeRequest> p = requestService.page(new Page<>(page, size), wrapper);
        return Result.ok(new PageResult<>(p.getTotal(), p.getRecords().stream().map(this::toVO).collect(Collectors.toList())));
    }

    /**
     * 管理员审批
     */
    @PutMapping("/{id}/approve")
    @RequireRole({Role.ADMIN})
    public Result<Void> approve(@PathVariable Long id, @RequestParam boolean pass,
                                @RequestParam(required = false) String reason) {
        CourseChangeRequest req = requestService.getById(id);
        if (req == null) {
            throw new BusinessException("申请不存在");
        }
        if (req.getStatus() != 0) {
            throw new BusinessException("该申请已处理");
        }
        if (pass) {
            // 应用到课程(快照式:解析 new_data 更新对应字段)
            Course course = courseService.getById(req.getCourseId());
            if (course == null) {
                throw new BusinessException("课程已不存在");
            }
            if (req.getNewData() != null && !req.getNewData().isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode node = om.readTree(req.getNewData());
                    if (node.has("courseName")) course.setCourseName(node.get("courseName").asText());
                    if (node.has("courseHours") && !node.get("courseHours").isNull()) course.setCourseHours(node.get("courseHours").asInt());
                    if (node.has("credit") && !node.get("credit").isNull()) course.setCredit(new java.math.BigDecimal(node.get("credit").asText()));
                    if (node.has("capacity") && !node.get("capacity").isNull()) course.setCapacity(node.get("capacity").asInt());
                    if (node.has("classTime")) course.setClassTime(node.get("classTime").asText());
                    if (node.has("location")) course.setLocation(node.get("location").asText());
                    if (node.has("status")) course.setStatus(node.get("status").asText());
                } catch (Exception e) {
                    throw new BusinessException("申请数据解析失败");
                }
            } else {
                // 兼容旧的单字段模式
                setFieldValue(course, req.getFieldName(), req.getNewValue());
            }
            courseService.updateById(course);
            req.setStatus(1);
        } else {
            req.setStatus(2);
            req.setRejectReason(reason);
        }
        req.setHandleTime(LocalDateTime.now());
        requestService.updateById(req);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> delete(@PathVariable Long id) {
        requestService.removeById(id);
        return Result.ok();
    }

    private String getFieldValue(Course c, String field) {
        switch (field) {
            case "courseName": return c.getCourseName();
            case "courseHours": return String.valueOf(c.getCourseHours());
            case "credit": return String.valueOf(c.getCredit());
            case "capacity": return String.valueOf(c.getCapacity());
            case "classTime": return c.getClassTime();
            case "location": return c.getLocation();
            case "status": return c.getStatus();
            default: return null;
        }
    }

    private void setFieldValue(Course c, String field, String value) {
        switch (field) {
            case "courseName": c.setCourseName(value); break;
            case "courseHours": c.setCourseHours(value == null ? null : Integer.parseInt(value)); break;
            case "credit": c.setCredit(value == null ? null : new java.math.BigDecimal(value)); break;
            case "capacity": c.setCapacity(value == null ? null : Integer.parseInt(value)); break;
            case "classTime": c.setClassTime(value); break;
            case "location": c.setLocation(value); break;
            case "status": c.setStatus(value); break;
            default: throw new BusinessException("不支持的修改字段");
        }
    }

    private CourseChangeRequestVO toVO(CourseChangeRequest r) {
        CourseChangeRequestVO vo = new CourseChangeRequestVO();
        vo.setId(r.getId());
        vo.setCourseId(r.getCourseId());
        vo.setTeacherId(r.getTeacherId());
        vo.setFieldName(r.getFieldName());
        vo.setOldValue(r.getOldValue());
        vo.setNewValue(r.getNewValue());
        vo.setStatus(r.getStatus());
        vo.setRejectReason(r.getRejectReason());
        vo.setCreateTime(r.getCreateTime());
        Course c = courseService.getById(r.getCourseId());
        vo.setCourseName(c != null ? c.getCourseName() : null);
        Teacher t = teacherService.getById(r.getTeacherId());
        vo.setTeacherName(t != null ? t.getName() : null);
        // 原值快照(当前课程完整信息)
        if (c != null) {
            java.util.Map<String, Object> oldMap = new java.util.LinkedHashMap<>();
            oldMap.put("courseName", c.getCourseName());
            oldMap.put("courseHours", c.getCourseHours());
            oldMap.put("credit", c.getCredit());
            oldMap.put("capacity", c.getCapacity());
            oldMap.put("classTime", c.getClassTime());
            oldMap.put("location", c.getLocation());
            oldMap.put("status", c.getStatus());
            try {
                vo.setOldData(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(oldMap));
            } catch (Exception ignored) {
            }
        }
        return vo;
    }

    @lombok.Data
    public static class CourseChangeRequestVO {
        private Long id;
        private Long courseId;
        private String courseName;
        private Long teacherId;
        private String teacherName;
        private String fieldName;
        private String oldValue;
        private String newValue;
        private String newData;
        private String oldData;
        private Integer status;
        private String rejectReason;
        private LocalDateTime createTime;
    }
}
