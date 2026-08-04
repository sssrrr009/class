package com.classmanage.dto;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {
    private String token;
    private String role;
    private Long userId;
    private String username;
    private String realName;
}
