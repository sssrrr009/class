package com.classmanage.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 投票活动展示对象
 */
@Data
public class VoteVO {
    private Long id;
    private String title;
    private String content;
    private String creatorRole;
    private String creatorName;
    private Integer maxVotes;
    private Integer showResult;
    private Integer status;
    private LocalDateTime createTime;
    private List<String> options;
    private List<Long> optionIds;
}
