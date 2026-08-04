package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.Course;
import com.classmanage.mapper.CourseMapper;
import com.classmanage.service.CourseService;
import org.springframework.stereotype.Service;

/**
 * Course 服务实现
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
}
