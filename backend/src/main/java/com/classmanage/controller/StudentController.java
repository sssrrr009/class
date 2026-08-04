package com.classmanage.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.ImportResult;
import com.classmanage.dto.PageResult;
import com.classmanage.entity.ClassInfo;
import com.classmanage.entity.Student;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.service.ClassInfoService;
import com.classmanage.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 学生管理（管理员）
 */
@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private ClassInfoService classInfoService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping("/page")
    public Result<PageResult<Student>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Long classId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Student::getStudentNo, keyword)
                    .or().like(Student::getName, keyword)
                    .or().like(Student::getIdCard, keyword));
        }
        if (classId != null) {
            wrapper.eq(Student::getClassId, classId);
        }
        wrapper.orderByAsc(Student::getId);
        Page<Student> p = studentService.page(new Page<>(page, size), wrapper);
        return Result.ok(new PageResult<>(p.getTotal(), p.getRecords()));
    }

    @PostMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> add(@RequestBody Student student) {
        student.setId(null);
        // 密码初始 = 身份证后6位
        if (!StringUtils.hasText(student.getIdCard()) || student.getIdCard().length() < 6) {
            throw new BusinessException("必须提供有效身份证号码（用于生成初始密码）");
        }
        student.setPassword(encoder.encode(student.getIdCard().substring(student.getIdCard().length() - 6)));
        studentService.save(student);
        // 更新班级人数
        if (student.getClassId() != null) {
            refreshClassCount(student.getClassId());
        }
        return Result.ok();
    }

    @PutMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> update(@RequestBody Student student) {
        if (student.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        Student old = studentService.getById(student.getId());
        studentService.updateById(student);
        if (old != null && old.getClassId() != null) {
            refreshClassCount(old.getClassId());
        }
        if (student.getClassId() != null) {
            refreshClassCount(student.getClassId());
        }
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> delete(@PathVariable Long id) {
        Student old = studentService.getById(id);
        studentService.removeById(id);
        if (old != null && old.getClassId() != null) {
            refreshClassCount(old.getClassId());
        }
        return Result.ok();
    }

    /**
     * Excel 批量导入学生
     */
    @PostMapping("/import")
    @RequireRole({Role.ADMIN})
    public Result<ImportResult> importExcel(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        List<ImportResult.RowError> errors = new ArrayList<>();
        int success = 0;
        int total = 0;

        try {
            List<StudentExcelRow> rows = new ArrayList<>();
            EasyExcel.read(file.getInputStream(), StudentExcelRow.class, new ReadListener<StudentExcelRow>() {
                @Override
                public void invoke(StudentExcelRow data, AnalysisContext context) {
                    rows.add(data);
                }
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                }
            }).sheet().doRead();

            total = rows.size();
            for (int i = 0; i < rows.size(); i++) {
                StudentExcelRow row = rows.get(i);
                int line = i + 2; // Excel 从第2行开始数据
                try {
                    if (!StringUtils.hasText(row.getStudentNo()) || !StringUtils.hasText(row.getName())) {
                        throw new BusinessException("学号、姓名不能为空");
                    }
                    if (!StringUtils.hasText(row.getIdCard()) || row.getIdCard().length() < 6) {
                        throw new BusinessException("身份证号码无效");
                    }
                    if (studentService.count(new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, row.getStudentNo())) > 0) {
                        throw new BusinessException("学号已存在");
                    }
                    Student s = new Student();
                    s.setStudentNo(row.getStudentNo());
                    s.setName(row.getName());
                    s.setGender(row.getGender());
                    s.setPhone(row.getPhone());
                    s.setIdCard(row.getIdCard());
                    s.setPassword(encoder.encode(row.getIdCard().substring(row.getIdCard().length() - 6)));
                    if (StringUtils.hasText(row.getClassNo())) {
                        ClassInfo cls = classInfoService.getOne(new LambdaQueryWrapper<ClassInfo>().eq(ClassInfo::getClassNo, row.getClassNo()));
                        if (cls == null) {
                            throw new BusinessException("班级号不存在: " + row.getClassNo());
                        }
                        s.setClassId(cls.getId());
                    }
                    studentService.save(s);
                    if (s.getClassId() != null) {
                        refreshClassCount(s.getClassId());
                    }
                    success++;
                } catch (Exception e) {
                    errors.add(new ImportResult.RowError(line, e.getMessage()));
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Excel 解析失败: " + e.getMessage());
        }

        ImportResult result = new ImportResult(total, success, errors);
        return Result.ok(result);
    }

    /**
     * Excel 导入行模型
     */
    public static class StudentExcelRow {
        private String studentNo;
        private String name;
        private String gender;
        private String phone;
        private String idCard;
        private String classNo;

        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getIdCard() { return idCard; }
        public void setIdCard(String idCard) { this.idCard = idCard; }
        public String getClassNo() { return classNo; }
        public void setClassNo(String classNo) { this.classNo = classNo; }
    }

    /**
     * 刷新班级人数
     */
    private void refreshClassCount(Long classId) {
        long count = studentService.count(new LambdaQueryWrapper<Student>().eq(Student::getClassId, classId));
        ClassInfo cls = classInfoService.getById(classId);
        if (cls != null) {
            cls.setStudentCount((int) count);
            classInfoService.updateById(cls);
        }
    }
}
