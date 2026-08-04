package com.classmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 选课配置 实体类
 */
@Data
@TableName("enroll_config")
public class EnrollConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 是否开放选课 0关闭 1开放 */
    private Integer isOpen;

    /** 选课开始时间 */
    private LocalDateTime startTime;

    /** 选课结束时间 */
    private LocalDateTime endTime;

    private LocalDateTime createTime;
}
