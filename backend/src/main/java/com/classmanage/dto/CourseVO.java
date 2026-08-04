package com.classmanage.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程展示对象（含任课教师姓名、课程类别）
 */
@Data
public class CourseVO {
    private Long id;
    private String courseCode;
    private String courseName;
    private Long categoryId;
    private String categoryName;
    private Integer courseHours;
    private BigDecimal credit;
    private Integer capacity;
    private Long teacherId;
    private String teacherName;
    private String classTime;
    private String location;
    private String status;
}
