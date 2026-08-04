package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.PageResult;
import com.classmanage.dto.ScoreVO;
import com.classmanage.entity.Course;
import com.classmanage.entity.Score;
import com.classmanage.entity.Student;
import com.classmanage.entity.StudentCourse;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.security.UserContext;
import com.classmanage.service.CourseService;
import com.classmanage.service.ScoreService;
import com.classmanage.service.StudentCourseService;
import com.classmanage.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生成绩管理
 */
@RestController
@RequestMapping("/api/score")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private StudentCourseService studentCourseService;

    /**
     * 分页查询成绩（管理员/教师）
     */
    @GetMapping("/page")
    @RequireRole({Role.ADMIN, Role.TEACHER})
    public Result<PageResult<ScoreVO>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            // 按学生学号/姓名关联查询，简化：先查学生再过滤
        }
        wrapper.orderByDesc(Score::getId);
        Page<Score> p = scoreService.page(new Page<>(page, size), wrapper);
        List<ScoreVO> list = p.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return Result.ok(new PageResult<>(p.getTotal(), list));
    }

    /**
     * 学生查询本人成绩
     */
    @GetMapping("/my")
    @RequireRole({Role.STUDENT, Role.CADRE})
    public Result<List<ScoreVO>> my() {
        Long studentId = UserContext.getUserId();
        List<Score> scores = scoreService.list(
                new LambdaQueryWrapper<Score>().eq(Score::getStudentId, studentId));
        return Result.ok(scores.stream().map(this::toVO).collect(Collectors.toList()));
    }

    /**
     * 录入成绩（限已选课学生）
     */
    @PostMapping
    @RequireRole({Role.ADMIN, Role.TEACHER})
    public Result<Void> add(@RequestBody Score score) {
        if (score.getStudentId() == null || score.getCourseId() == null || score.getScore() == null) {
            throw new BusinessException("请填写学生、课程与分数");
        }
        // 教师只能录自己授课课程的成绩
        Role role = UserContext.getRole();
        if (role == Role.TEACHER) {
            Course c = courseService.getById(score.getCourseId());
            if (c == null || !UserContext.getUserId().equals(c.getTeacherId())) {
                throw new BusinessException("只能录入自己授课课程的成绩");
            }
        }
        // 校验已选课
        long enroll = studentCourseService.count(new LambdaQueryWrapper<StudentCourse>()
                .eq(StudentCourse::getStudentId, score.getStudentId())
                .eq(StudentCourse::getCourseId, score.getCourseId()));
        if (enroll == 0) {
            throw new BusinessException("该学生未选修此课程，无法录入成绩");
        }
        // 重复录入校验
        long exist = scoreService.count(new LambdaQueryWrapper<Score>()
                .eq(Score::getStudentId, score.getStudentId())
                .eq(Score::getCourseId, score.getCourseId()));
        if (exist > 0) {
            throw new BusinessException("该学生此课程成绩已录入");
        }
        score.setId(null);
        score.setGradeLevel(mapGrade(score.getScore()));
        scoreService.save(score);
        return Result.ok();
    }

    /**
     * 修改成绩
     */
    @PutMapping
    @RequireRole({Role.ADMIN, Role.TEACHER})
    public Result<Void> update(@RequestBody Score score) {
        if (score.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        if (score.getScore() != null) {
            score.setGradeLevel(mapGrade(score.getScore()));
        }
        scoreService.updateById(score);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN, Role.TEACHER})
    public Result<Void> delete(@PathVariable Long id) {
        scoreService.removeById(id);
        return Result.ok();
    }

    /**
     * 分数 -> 等级映射
     * >=90 优秀 / 75-89 良好 / 60-74 及格 / <60 不及格
     */
    private String mapGrade(BigDecimal score) {
        if (score.compareTo(new BigDecimal("90")) >= 0) return "优秀";
        if (score.compareTo(new BigDecimal("75")) >= 0) return "良好";
        if (score.compareTo(new BigDecimal("60")) >= 0) return "及格";
        return "不及格";
    }

    private ScoreVO toVO(Score s) {
        ScoreVO vo = new ScoreVO();
        vo.setId(s.getId());
        vo.setStudentId(s.getStudentId());
        vo.setCourseId(s.getCourseId());
        vo.setScore(s.getScore());
        vo.setGradeLevel(s.getGradeLevel());
        vo.setUpdateTime(s.getUpdateTime());
        Student st = s.getStudentId() == null ? null : studentService.getById(s.getStudentId());
        Course c = s.getCourseId() == null ? null : courseService.getById(s.getCourseId());
        vo.setStudentNo(st != null ? st.getStudentNo() : null);
        vo.setStudentName(st != null ? st.getName() : null);
        vo.setCourseName(c != null ? c.getCourseName() : null);
        return vo;
    }
}
