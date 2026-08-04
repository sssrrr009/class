package com.classmanage.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录用户上下文信息
 */
@Data
@AllArgsConstructor
public class LoginUser {
    private Long userId;
    private Role role;
}
