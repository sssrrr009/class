package com.classmanage.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 干部展示对象
 */
@Data
public class CadreVO {
    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String cadreType;
    private Long classId;
    private String unionScope;
    private LocalDateTime createTime;
}
