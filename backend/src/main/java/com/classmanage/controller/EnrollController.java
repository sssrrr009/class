package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.CourseVO;
import com.classmanage.entity.*;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.security.UserContext;
import com.classmanage.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生选课管理（v2：课程类别 + 选课窗口 + 容量限制）
 */
@RestController
@RequestMapping("/api/enroll")
public class EnrollController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private ClassInfoService classInfoService;
    @Autowired
    private MajorPlanService majorPlanService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private StudentCourseService studentCourseService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private CourseCategoryService courseCategoryService;
    @Autowired
    private EnrollConfigController enrollConfigController;

    /**
     * 可选课程类别：本专业计划内的课程类别
     */
    @GetMapping("/categories")
    @RequireRole({Role.STUDENT, Role.CADRE})
    public Result<List<CourseCategory>> categories() {
        Student student = studentService.getById(UserContext.getUserId());
        if (student == null || student.getClassId() == null) {
            throw new BusinessException("请先分配班级");
        }
        ClassInfo cls = classInfoService.getById(student.getClassId());
        if (cls == null || cls.getMajorId() == null) {
            throw new BusinessException("班级未关联专业，无法选课");
        }
        List<MajorPlan> plans = majorPlanService.list(
                new LambdaQueryWrapper<MajorPlan>().eq(MajorPlan::getMajorId, cls.getMajorId()));
        if (plans.isEmpty()) {
            throw new BusinessException("本专业暂无专业计划");
        }
        List<Long> categoryIds = plans.stream().map(MajorPlan::getCategoryId).distinct().collect(Collectors.toList());
        return Result.ok(courseCategoryService.listByIds(categoryIds));
    }

    /**
     * 某课程类别下的可选课程（未开课/开课中）
     */
    @GetMapping("/candidates")
    @RequireRole({Role.STUDENT, Role.CADRE})
    public Result<List<CourseVO>> candidates(@RequestParam(required = false) Long categoryId) {
        Student student = studentService.getById(UserContext.getUserId());
        if (student == null || student.getClassId() == null) {
            throw new BusinessException("请先分配班级");
        }
        ClassInfo cls = classInfoService.getById(student.getClassId());
        if (cls == null || cls.getMajorId() == null) {
            throw new BusinessException("班级未关联专业，无法选课");
        }

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .in(Course::getStatus, "NOT_STARTED", "OPENING");
        if (categoryId != null) {
            // 校验该类别在本专业计划内
            long planCount = majorPlanService.count(new LambdaQueryWrapper<MajorPlan>()
                    .eq(MajorPlan::getMajorId, cls.getMajorId())
                    .eq(MajorPlan::getCategoryId, categoryId));
            if (planCount == 0) {
                throw new BusinessException("该课程类别不在本专业计划内");
            }
            wrapper.eq(Course::getCategoryId, categoryId);
        }
        List<Course> courses = courseService.list(wrapper);
        return Result.ok(courses.stream().map(this::toVO).collect(Collectors.toList()));
    }

    /**
     * 已选课程
     */
    @GetMapping("/my")
    @RequireRole({Role.STUDENT, Role.CADRE})
    public Result<List<CourseVO>> my() {
        Long studentId = UserContext.getUserId();
        List<StudentCourse> scs = studentCourseService.list(
                new LambdaQueryWrapper<StudentCourse>().eq(StudentCourse::getStudentId, studentId));
        if (scs.isEmpty()) {
            return Result.ok(java.util.Collections.emptyList());
        }
        List<Long> ids = scs.stream().map(StudentCourse::getCourseId).collect(Collectors.toList());
        List<Course> courses = courseService.listByIds(ids);
        return Result.ok(courses.stream().map(this::toVO).collect(Collectors.toList()));
    }

    /**
     * 选课（选课窗口校验 + 课程类别校验 + 容量随机分配）
     * 返回 status: 1=已选中, 2=候选（超员随机落选）
     */
    @PostMapping("/{courseId}")
    @RequireRole({Role.STUDENT, Role.CADRE})
    @Transactional
    public Result<java.util.Map<String, Object>> enroll(@PathVariable Long courseId) {
        // 1. 选课窗口校验
        enrollConfigController.checkOpen();

        Student student = studentService.getById(UserContext.getUserId());
        if (student == null || student.getClassId() == null) {
            throw new BusinessException("请先分配班级");
        }
        ClassInfo cls = classInfoService.getById(student.getClassId());
        if (cls == null || cls.getMajorId() == null) {
            throw new BusinessException("班级未关联专业，无法选课");
        }
        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        // 2. 课程类别计划校验
        if (course.getCategoryId() != null) {
            long planCount = majorPlanService.count(new LambdaQueryWrapper<MajorPlan>()
                    .eq(MajorPlan::getMajorId, cls.getMajorId())
                    .eq(MajorPlan::getCategoryId, course.getCategoryId()));
            if (planCount == 0) {
                throw new BusinessException("该课程类别不在本专业计划内，无法选修");
            }
        } else {
            throw new BusinessException("该课程未设置课程类别");
        }
        // 3. 课程状态校验
        if (!"NOT_STARTED".equals(course.getStatus()) && !"OPENING".equals(course.getStatus())) {
            throw new BusinessException("该课程已结课，无法选课");
        }
        // 4. 重复选课校验
        long exist = studentCourseService.count(new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getStudentId, student.getId())
                .eq(StudentCourse::getCourseId, courseId));
        if (exist > 0) {
            throw new BusinessException("已选择该课程");
        }

        // 5. 容量与随机分配
        long enrolled = studentCourseService.count(new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getCourseId, courseId)
                .eq(StudentCourse::getStatus, 1)); // 已选中人数
        int capacity = course.getCapacity() == null ? 50 : course.getCapacity();
        int status;
        if (enrolled < capacity) {
            status = 1; // 未满，直接选中
        } else {
            // 满员：新选课者作为候选，随机决定是否顶替
            status = Math.random() < 0.5 ? 1 : 2;
            if (status == 1) {
                // 顶替：随机将一名已选中者改为未选中
                List<StudentCourse> selected = studentCourseService.list(
                        new LambdaQueryWrapper<StudentCourse>()
                                .eq(StudentCourse::getCourseId, courseId)
                                .eq(StudentCourse::getStatus, 1));
                if (!selected.isEmpty()) {
                    StudentCourse victim = selected.get((int) (Math.random() * selected.size()));
                    victim.setStatus(2);
                    studentCourseService.updateById(victim);
                }
            }
        }
        StudentCourse sc = new StudentCourse();
        sc.setStudentId(student.getId());
        sc.setCourseId(courseId);
        sc.setStatus(status);
        studentCourseService.save(sc);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("status", status);
        result.put("message", status == 1 ? "选课成功" : "课程已满员，您已进入候选名单，等待随机分配");
        return Result.ok(result);
    }

    /**
     * 退课
     */
    @DeleteMapping("/{courseId}")
    @RequireRole({Role.STUDENT, Role.CADRE})
    public Result<Void> drop(@PathVariable Long courseId) {
        Long studentId = UserContext.getUserId();
        studentCourseService.remove(new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getStudentId, studentId)
                .eq(StudentCourse::getCourseId, courseId));
        return Result.ok();
    }

    private CourseVO toVO(Course c) {
        CourseVO vo = new CourseVO();
        vo.setId(c.getId());
        vo.setCourseCode(c.getCourseCode());
        vo.setCourseName(c.getCourseName());
        vo.setCourseHours(c.getCourseHours());
        vo.setCredit(c.getCredit());
        vo.setCapacity(c.getCapacity());
        vo.setCategoryId(c.getCategoryId());
        vo.setTeacherId(c.getTeacherId());
        vo.setClassTime(c.getClassTime());
        vo.setLocation(c.getLocation());
        vo.setStatus(c.getStatus());
        Teacher t = c.getTeacherId() == null ? null : teacherService.getById(c.getTeacherId());
        vo.setTeacherName(t != null ? t.getName() : null);
        if (c.getCategoryId() != null) {
            CourseCategory cat = courseCategoryService.getById(c.getCategoryId());
            vo.setCategoryName(cat != null ? cat.getCategoryName() : null);
        }
        return vo;
    }
}
