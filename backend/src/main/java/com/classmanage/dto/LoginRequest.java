package com.classmanage.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** ADMIN / TEACHER / STUDENT / CADRE */
    @NotBlank(message = "请选择角色")
    private String role;
}
