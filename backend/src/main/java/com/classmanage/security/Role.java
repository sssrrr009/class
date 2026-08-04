package com.classmanage.security;

/**
 * 系统角色
 */
public enum Role {
    /** 管理员 */
    ADMIN("ADMIN"),
    /** 教师(教职工) */
    TEACHER("TEACHER"),
    /** 学生 */
    STUDENT("STUDENT"),
    /** 学生干部 */
    CADRE("CADRE");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
