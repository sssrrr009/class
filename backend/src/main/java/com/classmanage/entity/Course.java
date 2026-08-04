package com.classmanage.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Course 实体类
 */
@Data
@TableName("course")
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 课程编号 */
    private String courseCode;

    /** 课程名称 */
    private String courseName;

    /** 课程类别 */
    private Long categoryId;

    /** 课时 */
    private Integer courseHours;

    /** 学分 */
    private BigDecimal credit;

    /** 容纳人数 */
    private Integer capacity;

    /** 任课教师 */
    private Long teacherId;

    /** 上课时间 */
    private String classTime;

    /** 上课地点 */
    private String location;

    /** 课程状态 */
    private String status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}