package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.ChangePasswordRequest;
import com.classmanage.dto.UpdateProfileRequest;
import com.classmanage.entity.Admin;
import com.classmanage.entity.Student;
import com.classmanage.entity.Teacher;
import com.classmanage.security.LoginUser;
import com.classmanage.security.RequireLogin;
import com.classmanage.security.Role;
import com.classmanage.security.UserContext;
import com.classmanage.service.AdminService;
import com.classmanage.service.StudentService;
import com.classmanage.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 个人中心
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping("/info")
    @RequireLogin
    public Result<Map<String, Object>> info() {
        LoginUser lu = UserContext.get();
        Map<String, Object> map = new HashMap<>();
        map.put("role", lu.getRole().getValue());
        map.put("userId", lu.getUserId());
        switch (lu.getRole()) {
            case ADMIN: {
                Admin a = adminService.getById(lu.getUserId());
                if (a != null) {
                    map.put("username", a.getUsername());
                    map.put("realName", a.getRealName());
                }
                break;
            }
            case TEACHER: {
                Teacher t = teacherService.getById(lu.getUserId());
                if (t != null) {
                    map.put("username", t.getTeacherNo());
                    map.put("realName", t.getName());
                    map.put("phone", t.getPhone());
                    map.put("collegeId", t.getCollegeId());
                    map.put("title", t.getTitle());
                }
                break;
            }
            case STUDENT:
            case CADRE: {
                Student s = studentService.getById(lu.getUserId());
                if (s != null) {
                    map.put("username", s.getStudentNo());
                    map.put("realName", s.getName());
                    map.put("gender", s.getGender());
                    map.put("phone", s.getPhone());
                    map.put("classId", s.getClassId());
                }
                break;
            }
        }
        return Result.ok(map);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @RequireLogin
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest req) {
        LoginUser lu = UserContext.get();
        if (!verifyPassword(lu, req.getOldPassword())) {
            throw new BusinessException("原密码错误");
        }
        String newPwd = encoder.encode(req.getNewPassword());
        switch (lu.getRole()) {
            case ADMIN:
                adminService.update(new LambdaUpdateWrapper<Admin>()
                        .eq(Admin::getId, lu.getUserId()).set(Admin::getPassword, newPwd));
                break;
            case TEACHER:
                teacherService.update(new LambdaUpdateWrapper<Teacher>()
                        .eq(Teacher::getId, lu.getUserId()).set(Teacher::getPassword, newPwd));
                break;
            case STUDENT:
            case CADRE:
                studentService.update(new LambdaUpdateWrapper<Student>()
                        .eq(Student::getId, lu.getUserId()).set(Student::getPassword, newPwd));
                break;
        }
        return Result.ok();
    }

    /**
     * 修改个人资料
     */
    @PutMapping("/profile")
    @RequireLogin
    public Result<Void> updateProfile(@RequestBody UpdateProfileRequest req) {
        LoginUser lu = UserContext.get();
        switch (lu.getRole()) {
            case ADMIN:
                adminService.update(new LambdaUpdateWrapper<Admin>()
                        .eq(Admin::getId, lu.getUserId())
                        .set(Admin::getUsername, req.getUsername())
                        .set(Admin::getRealName, req.getRealName()));
                break;
            case TEACHER:
                teacherService.update(new LambdaUpdateWrapper<Teacher>()
                        .eq(Teacher::getId, lu.getUserId())
                        .set(Teacher::getPhone, req.getPhone()));
                break;
            case STUDENT:
            case CADRE:
                studentService.update(new LambdaUpdateWrapper<Student>()
                        .eq(Student::getId, lu.getUserId())
                        .set(Student::getName, req.getRealName())
                        .set(Student::getGender, req.getGender())
                        .set(Student::getPhone, req.getPhone()));
                break;
        }
        return Result.ok();
    }

    private boolean verifyPassword(LoginUser lu, String rawPwd) {
        if (rawPwd == null) {
            return false;
        }
        switch (lu.getRole()) {
            case ADMIN: {
                Admin a = adminService.getById(lu.getUserId());
                return a != null && encoder.matches(rawPwd, a.getPassword());
            }
            case TEACHER: {
                Teacher t = teacherService.getById(lu.getUserId());
                return t != null && encoder.matches(rawPwd, t.getPassword());
            }
            case STUDENT:
            case CADRE: {
                Student s = studentService.getById(lu.getUserId());
                return s != null && encoder.matches(rawPwd, s.getPassword());
            }
        }
        return false;
    }
}
