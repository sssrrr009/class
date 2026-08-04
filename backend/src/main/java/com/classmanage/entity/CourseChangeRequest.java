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
 * 课程修改申请 实体类
 */
@Data
@TableName("course_change_request")
public class CourseChangeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 课程 */
    private Long courseId;

    /** 申请教师 */
    private Long teacherId;

    /** 修改字段 */
    private String fieldName;

    /** 原值 */
    private String oldValue;

    /** 新值 */
    private String newValue;

    /** 修改后完整课程JSON(快照式) */
    private String newData;

    /** 0待审批 1已通过 2已拒绝 */
    private Integer status;

    /** 拒绝原因 */
    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 处理时间 */
    private LocalDateTime handleTime;
}
