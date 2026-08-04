package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.PageResult;
import com.classmanage.entity.*;
import com.classmanage.security.RequireLogin;
import com.classmanage.security.Role;
import com.classmanage.security.UserContext;
import com.classmanage.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知公告管理（分级发布）
 * 范围：SCHOOL学校 / COLLEGE学院 / MAJOR专业 / CLASS班级 / PERSONAL个人
 */
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private ClassInfoService classInfoService;
    @Autowired
    private MajorService majorService;
    @Autowired
    private CollegeService collegeService;
    @Autowired
    private CadreService cadreService;

    /**
     * 分页查询公告（按角色聚合可见范围）
     */
    @GetMapping("/page")
    @RequireLogin
    public Result<PageResult<Notice>> page(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size) {
        Long userId = UserContext.getUserId();
        Role role = UserContext.getRole();
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();

        if (role == Role.ADMIN) {
            // 管理员可见全部
            wrapper.orderByDesc(Notice::getCreateTime);
        } else if (role == Role.TEACHER) {
            // 教师：本人发布的 + 学校级
            wrapper.and(w -> w.eq(Notice::getScope, "SCHOOL").or().eq(Notice::getPublisherName, userName(userId)));
            wrapper.orderByDesc(Notice::getCreateTime);
        } else if (role == Role.STUDENT || role == Role.CADRE) {
            // 学生：学校级 + 本人学院/专业/班级 + 发给本人的
            Student student = studentService.getById(userId);
            Long classId = student != null ? student.getClassId() : null;
            Long classIdTmp = classId;
            Long majorIdTmp = null;
            Long collegeIdTmp = null;
            if (classIdTmp != null) {
                ClassInfo cls = classInfoService.getById(classIdTmp);
                if (cls != null) {
                    majorIdTmp = cls.getMajorId();
                    if (majorIdTmp != null) {
                        Major major = majorService.getById(majorIdTmp);
                        collegeIdTmp = major != null ? major.getCollegeId() : null;
                    }
                }
            }
            final Long classIdF = classIdTmp;
            final Long majorIdF = majorIdTmp;
            final Long collegeIdF = collegeIdTmp;
            wrapper.and(w -> {
                w.eq(Notice::getScope, "SCHOOL");
                if (collegeIdF != null) w.or(i -> i.eq(Notice::getScope, "COLLEGE").eq(Notice::getTargetCollegeId, collegeIdF));
                if (majorIdF != null) w.or(i -> i.eq(Notice::getScope, "MAJOR").eq(Notice::getTargetMajorId, majorIdF));
                if (classIdF != null) w.or(i -> i.eq(Notice::getScope, "CLASS").eq(Notice::getTargetClassId, classIdF));
                w.or(i -> i.eq(Notice::getScope, "PERSONAL").eq(Notice::getTargetStudentId, userId))
                .or(i -> i.eq(Notice::getScope, "PERSONAL").apply("(FIND_IN_SET({0}, target_student_ids))", userId));
            });
            wrapper.orderByDesc(Notice::getCreateTime);
        }
        Page<Notice> p = noticeService.page(new Page<>(page, size), wrapper);
        // 学生端过滤未到定时发布时间的公告; 管理员/教师端全部可见(含定时预览)
        java.util.List<Notice> list = p.getRecords();
        if (role == Role.STUDENT || role == Role.CADRE) {
            list = list.stream()
                    .filter(n -> n.getPublishTime() == null || !n.getPublishTime().isAfter(java.time.LocalDateTime.now()))
                    .collect(Collectors.toList());
        }
        return Result.ok(new PageResult<>(p.getTotal(), list));
    }

    /**
     * 发布公告（管理员/教师/干部共用，按角色校验发布范围）
     */
    @PostMapping
    @RequireLogin
    public Result<Void> add(@RequestBody Notice notice) {
        Long userId = UserContext.getUserId();
        Role role = UserContext.getRole();
        String scope = notice.getScope();

        // 发布人信息
        notice.setPublisherRole(role.getValue());
        notice.setPublisherName(userName(userId));
        notice.setId(null);

        // 范围权限校验
        validateScope(role, userId, scope, notice);
        // 个人公告多选: 若传了 targetStudentIds 则校验为班级内学生
        if ("PERSONAL".equals(scope) && notice.getTargetStudentIds() != null && !notice.getTargetStudentIds().isEmpty()) {
            // 班长只能发给本班学生
            if (role == Role.CADRE) {
                Cadre cadre = cadreService.getOne(new LambdaQueryWrapper<Cadre>().eq(Cadre::getStudentId, userId));
                if (cadre != null && "MONITOR".equals(cadre.getCadreType())) {
                    long studentCount = studentService.count(new LambdaQueryWrapper<Student>()
                            .eq(Student::getClassId, cadre.getClassId())
                            .in(Student::getId, java.util.Arrays.stream(notice.getTargetStudentIds().split(","))
                                    .map(String::trim).filter(s -> !s.isEmpty()).map(Long::valueOf).collect(Collectors.toList())));
                    int ids = notice.getTargetStudentIds().split(",").length;
                    if (studentCount != ids) {
                        throw new BusinessException("只能选择本班级的学生");
                    }
                }
            }
        }
        noticeService.save(notice);
        return Result.ok();
    }

    @PutMapping
    @RequireLogin
    public Result<Void> update(@RequestBody Notice notice) {
        if (notice.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        Notice old = noticeService.getById(notice.getId());
        if (old == null) {
            throw new BusinessException("公告不存在");
        }
        checkOwner(old);
        validateScope(UserContext.getRole(), UserContext.getUserId(), notice.getScope(), notice);
        noticeService.updateById(notice);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    public Result<Void> delete(@PathVariable Long id) {
        Notice old = noticeService.getById(id);
        if (old == null) {
            return Result.ok();
        }
        checkOwner(old);
        noticeService.removeById(id);
        return Result.ok();
    }

    /**
     * 范围校验
     */
    private void validateScope(Role role, Long userId, String scope, Notice notice) {
        if (scope == null || !List.of("SCHOOL", "COLLEGE", "MAJOR", "CLASS", "PERSONAL").contains(scope)) {
            throw new BusinessException("公告范围不合法");
        }
        switch (role) {
            case ADMIN:
                break; // 管理员可发任意范围
            case TEACHER: {
                if ("SCHOOL".equals(scope)) {
                    throw new BusinessException("教师无权发布学校级公告");
                }
                // 教师仅能发布本人所属学院及其下级范围（学院/专业/班级/个人）
                break;
            }
            case CADRE: {
                java.util.List<Cadre> cadreList = cadreService.list(new LambdaQueryWrapper<Cadre>().eq(Cadre::getStudentId, userId));
                if (cadreList.isEmpty()) {
                    throw new BusinessException("干部身份异常");
                }
                boolean allowed = false;
                for (Cadre cadre : cadreList) {
                    if (checkCadreScope(cadre, scope, notice)) { allowed = true; break; }
                }
                if (!allowed) {
                    throw new BusinessException("当前干部身份无权发布该范围公告");
                }
                break;
            }
            case STUDENT:
                throw new BusinessException("学生无权发布公告");
        }
    }

    private void checkOwner(Notice notice) {
        Role role = UserContext.getRole();
        if (role == Role.ADMIN) {
            return;
        }
        if (!notice.getPublisherRole().equals(role.getValue())) {
            throw new BusinessException(403, "无权操作他人发布的公告");
        }
    }


    /**
     * 校验单个干部身份的公告发布范围
     */
    private boolean checkCadreScope(Cadre cadre, String scope, Notice notice) {
        if ("MONITOR".equals(cadre.getCadreType())) {
            // 班长仅能发布本班级公告(含班级内个人)
            if ("CLASS".equals(scope)) {
                return cadre.getClassId() != null && cadre.getClassId().equals(notice.getTargetClassId());
            }
            if ("PERSONAL".equals(scope)) {
                return cadre.getClassId() != null;
            }
            return false;
        }
        if ("UNION".equals(cadre.getCadreType())) {
            // 学生会不含班级公告,范围限定 学校/学院/专业/个人
            if ("CLASS".equals(scope)) {
                return false;
            }
            String us = cadre.getUnionScope();
            if ("SCHOOL".equals(us)) {
                return true; // 校级可向下发布
            }
            if ("COLLEGE".equals(us)) {
                return !"SCHOOL".equals(scope); // 学院学生会: 学院/专业/个人
            }
            if ("MAJOR".equals(us)) {
                return "MAJOR".equals(scope) || "PERSONAL".equals(scope); // 专业学生会: 专业/个人
            }
            return false;
        }
        return false;
    }

    private String userName(Long userId) {
        Role role = UserContext.getRole();
        if (role == Role.TEACHER) {
            Teacher t = teacherService.getById(userId);
            return t != null ? t.getName() : null;
        }
        if (role == Role.STUDENT || role == Role.CADRE) {
            Student s = studentService.getById(userId);
            return s != null ? s.getName() : null;
        }
        return "管理员";
    }
}
