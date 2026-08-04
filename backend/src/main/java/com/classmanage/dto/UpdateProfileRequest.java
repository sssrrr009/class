package com.classmanage.dto;

import lombok.Data;

/**
 * 修改个人资料请求
 */
@Data
public class UpdateProfileRequest {
    private String username;
    private String realName;
    private String gender;
    private String phone;
}
