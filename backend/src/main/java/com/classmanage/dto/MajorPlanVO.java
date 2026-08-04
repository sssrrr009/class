package com.classmanage.dto;

import lombok.Data;

/**
 * 专业计划展示对象
 */
@Data
public class MajorPlanVO {
    private Long id;
    private Long majorId;
    private String majorName;
    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private Integer yearLevel;
    private Integer semester;
}
