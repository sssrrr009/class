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
 * StudentVote 实体类
 */
@Data
@TableName("student_vote")
public class StudentVote implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 活动 */
    private Long voteId;

    /** 学生 */
    private Long studentId;

    /** 选项 */
    private Long optionId;

    /** 投票时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime voteTime;

}