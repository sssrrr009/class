package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.Teacher;
import com.classmanage.mapper.TeacherMapper;
import com.classmanage.service.TeacherService;
import org.springframework.stereotype.Service;

/**
 * Teacher 服务实现
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {
}
