package com.classmanage.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩展示对象
 */
@Data
public class ScoreVO {
    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Long courseId;
    private String courseName;
    private BigDecimal score;
    private String gradeLevel;
    private LocalDateTime updateTime;
}
