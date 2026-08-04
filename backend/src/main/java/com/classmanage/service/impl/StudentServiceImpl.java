package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.Student;
import com.classmanage.mapper.StudentMapper;
import com.classmanage.service.StudentService;
import org.springframework.stereotype.Service;

/**
 * Student 服务实现
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
}
