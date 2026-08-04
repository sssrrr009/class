package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.LoginRequest;
import com.classmanage.dto.LoginResponse;
import com.classmanage.entity.Admin;
import com.classmanage.entity.Cadre;
import com.classmanage.entity.Student;
import com.classmanage.entity.Teacher;
import com.classmanage.mapper.AdminMapper;
import com.classmanage.mapper.StudentMapper;
import com.classmanage.mapper.TeacherMapper;
import com.classmanage.security.JwtUtil;
import com.classmanage.security.Role;
import com.classmanage.service.CadreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 登录认证
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private CadreService cadreService;
    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 登录：管理员/教师/学生/干部
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        Role role;
        try {
            role = Role.valueOf(req.getRole().toUpperCase());
        } catch (Exception e) {
            throw new BusinessException(400, "角色选择不正确");
        }

        LoginResponse resp = new LoginResponse();
        switch (role) {
            case ADMIN: {
                Admin admin = adminMapper.selectOne(
                        new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, req.getUsername()));
                if (admin == null || !encoder.matches(req.getPassword(), admin.getPassword())) {
                    throw new BusinessException(400, "用户名或密码错误");
                }
                resp.setUserId(admin.getId());
                resp.setUsername(admin.getUsername());
                resp.setRealName(admin.getRealName());
                break;
            }
            case TEACHER: {
                Teacher t = teacherMapper.selectOne(
                        new LambdaQueryWrapper<Teacher>().eq(Teacher::getTeacherNo, req.getUsername()));
                if (t == null || !encoder.matches(req.getPassword(), t.getPassword())) {
                    throw new BusinessException(400, "用户名或密码错误");
                }
                resp.setUserId(t.getId());
                resp.setUsername(t.getTeacherNo());
                resp.setRealName(t.getName());
                break;
            }
            case STUDENT:
            case CADRE: {
                Student s = studentMapper.selectOne(
                        new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, req.getUsername()));
                if (s == null || !encoder.matches(req.getPassword(), s.getPassword())) {
                    throw new BusinessException(400, "用户名或密码错误");
                }
                // 干部角色登录时,需确认该学生已被设置为干部
                if (role == Role.CADRE) {
                    long cadreCount = cadreService.count(
                            new LambdaQueryWrapper<Cadre>().eq(Cadre::getStudentId, s.getId()));
                    if (cadreCount == 0) {
                        throw new BusinessException(403, "该账号不是学生干部，无权以干部身份登录");
                    }
                }
                resp.setUserId(s.getId());
                resp.setUsername(s.getStudentNo());
                resp.setRealName(s.getName());
                break;
            }
            default:
                throw new BusinessException(400, "角色选择不正确");
        }

        resp.setToken(jwtUtil.createToken(resp.getUserId(), role.getValue()));
        resp.setRole(role.getValue());
        return Result.ok(resp);
    }
}
