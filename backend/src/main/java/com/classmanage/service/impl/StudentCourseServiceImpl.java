package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.StudentCourse;
import com.classmanage.mapper.StudentCourseMapper;
import com.classmanage.service.StudentCourseService;
import org.springframework.stereotype.Service;

/**
 * StudentCourse 服务实现
 */
@Service
public class StudentCourseServiceImpl extends ServiceImpl<StudentCourseMapper, StudentCourse> implements StudentCourseService {
}
