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
 * Score 实体类
 */
@Data
@TableName("score")
public class Score implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 学生 */
    private Long studentId;

    /** 课程 */
    private Long courseId;

    /** 分数 */
    private BigDecimal score;

    /** 等级 */
    private String gradeLevel;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime updateTime;

}