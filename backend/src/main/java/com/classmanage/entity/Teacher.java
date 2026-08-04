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
 * Teacher 实体类
 */
@Data
@TableName("teacher")
public class Teacher implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 工号 */
    private String teacherNo;

    /** 密码 */
    private String password;

    /** 姓名 */
    private String name;

    /** 性别 */
    private String gender;

    /** 电话 */
    private String phone;

    /** 所属学院 */
    private Long collegeId;

    /** 职称 */
    private String title;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}