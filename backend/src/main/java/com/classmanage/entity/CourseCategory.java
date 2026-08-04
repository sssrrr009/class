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
 * 课程类别 实体类
 */
@Data
@TableName("course_category")
public class CourseCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 类别编号(如080109=大学英语二) */
    private String categoryCode;

    /** 类别名称 */
    private String categoryName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
