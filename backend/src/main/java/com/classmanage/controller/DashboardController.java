package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classmanage.common.Result;
import com.classmanage.entity.Course;
import com.classmanage.entity.StudentCourse;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.security.UserContext;
import com.classmanage.service.CourseService;
import com.classmanage.service.StudentCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页数据（下一节课倒计时）
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private StudentCourseService studentCourseService;
    @Autowired
    private CourseService courseService;

    // 课程时间解析: "周一 1-2节" -> (day=1, startPeriod=1)
    private static final Map<String, Integer> DAY_MAP = new HashMap<>();

    static {
        DAY_MAP.put("周一", 1); DAY_MAP.put("周二", 2); DAY_MAP.put("周三", 3);
        DAY_MAP.put("周四", 4); DAY_MAP.put("周五", 5); DAY_MAP.put("周六", 6);
        DAY_MAP.put("周日", 7); DAY_MAP.put("星期一", 1); DAY_MAP.put("星期二", 2);
        DAY_MAP.put("星期三", 3); DAY_MAP.put("星期四", 4); DAY_MAP.put("星期五", 5);
        DAY_MAP.put("星期六", 6); DAY_MAP.put("星期日", 7);
    }

    // 每节课开始时间（按常规 8:00 起, 每节 45 分钟, 课间 10 分钟）
    private static LocalTime periodStart(int period) {
        int minutes = 8 * 60 + (period - 1) * 55;
        return LocalTime.of(minutes / 60, minutes % 60);
    }

    /**
     * 学生下一节课
     */
    @GetMapping("/next-class")
    @RequireRole({Role.STUDENT, Role.CADRE})
    public Result<Map<String, Object>> nextClass() {
        Long studentId = UserContext.getUserId();
        List<StudentCourse> scs = studentCourseService.list(
                new LambdaQueryWrapper<StudentCourse>().eq(StudentCourse::getStudentId, studentId));
        if (scs.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("hasNext", false);
            return Result.ok(empty);
        }
        List<Long> courseIds = scs.stream().map(StudentCourse::getCourseId).collect(Collectors.toList());
        List<Course> courses = courseService.listByIds(courseIds);

        LocalDateTime now = LocalDateTime.now();
        int today = now.getDayOfWeek().getValue(); // 1=Mon...7=Sun
        LocalTime nowTime = now.toLocalTime();

        Course nextCourse = null;
        LocalDateTime nextStart = null;

        // 遍历未来7天,找最近的一节课
        for (int offset = 0; offset <= 7; offset++) {
            LocalDate date = now.toLocalDate().plusDays(offset);
            int day = date.getDayOfWeek().getValue();
            for (Course c : courses) {
                if (c.getClassTime() == null) continue;
                // 解析 "周一 1-2节"
                int[] parsed = parseClassTime(c.getClassTime());
                if (parsed == null) continue;
                int courseDay = parsed[0];
                int startPeriod = parsed[1];
                if (courseDay != day) continue;
                LocalDateTime courseStart = LocalDateTime.of(date, periodStart(startPeriod));
                if (courseStart.isBefore(now)) continue;
                if (nextStart == null || courseStart.isBefore(nextStart)) {
                    nextStart = courseStart;
                    nextCourse = c;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        if (nextCourse == null) {
            result.put("hasNext", false);
            return Result.ok(result);
        }
        long minutes = java.time.Duration.between(now, nextStart).toMinutes();
        result.put("hasNext", true);
        result.put("courseName", nextCourse.getCourseName());
        result.put("courseCode", nextCourse.getCourseCode());
        result.put("location", nextCourse.getLocation());
        result.put("startTime", nextStart.toString());
        result.put("minutesLeft", minutes);
        result.put("dayLeft", minutes / (24 * 60));
        result.put("hourLeft", (minutes % (24 * 60)) / 60);
        result.put("minLeft", minutes % 60);
        return Result.ok(result);
    }

    private int[] parseClassTime(String classTime) {
        for (Map.Entry<String, Integer> e : DAY_MAP.entrySet()) {
            if (classTime.contains(e.getKey())) {
                // 提取节次 "1-2节"
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*-\\s*(\\d+)").matcher(classTime);
                if (m.find()) {
                    return new int[]{e.getValue(), Integer.parseInt(m.group(1))};
                }
                m = java.util.regex.Pattern.compile("(\\d+)\\s*节").matcher(classTime);
                if (m.find()) {
                    return new int[]{e.getValue(), Integer.parseInt(m.group(1))};
                }
            }
        }
        return null;
    }
}
