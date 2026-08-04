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
 * Notice 实体类
 */
@Data
@TableName("notice")
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 范围 */
    private String scope;

    /** 目标学院 */
    private Long targetCollegeId;

    /** 目标专业 */
    private Long targetMajorId;

    /** 目标班级 */
    private Long targetClassId;

    /** 目标学生 */
    private Long targetStudentId;

    /** 发布人角色 */
    private String publisherRole;

    /** 发布人姓名 */
    private String publisherName;

    /** 附件路径 */
    private String attachmentPath;

    /** 附件名 */
    private String attachmentName;

    /** 定时发布时间 */
    private LocalDateTime publishTime;

    /** 个人公告目标学生ID(逗号分隔,支持多选) */
    private String targetStudentIds;

    /** 发布时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}