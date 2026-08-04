package com.classmanage.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * StudentCourse 实体类
 */
@Data
@TableName("student_course")
public class StudentCourse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 学生 */
    private Long studentId;

    /** 课程 */
    private Long courseId;

    /** 0候选 1已选中 2未选中 */
    private Integer status;

    /** 选课时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime selectTime;

}