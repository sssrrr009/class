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
 * Vote 实体类
 */
@Data
@TableName("vote")
public class Vote implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 主题 */
    private String title;

    /** 内容 */
    private String content;

    /** 附件路径 */
    private String attachmentPath;

    /** 附件名 */
    private String attachmentName;

    /** 创建人角色 */
    private String creatorRole;

    /** 创建人姓名 */
    private String creatorName;

    /** 教师 */
    private Long teacherId;

    /** 干部 */
    private Long cadreId;

    /** 每人最大票数 */
    private Integer maxVotes;

    /** 投票结果是否公开 1公开 0不公开 */
    private Integer showResult;

    /** 状态 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}