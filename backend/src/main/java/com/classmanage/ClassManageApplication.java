package com.classmanage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智慧班级管理系统 启动类
 */
@SpringBootApplication
@MapperScan("com.classmanage.mapper")
public class ClassManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClassManageApplication.class, args);
    }
}
