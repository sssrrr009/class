package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.CourseCategory;
import com.classmanage.mapper.CourseCategoryMapper;
import com.classmanage.service.CourseCategoryService;
import org.springframework.stereotype.Service;

/**
 * CourseCategory 服务实现
 */
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {
}
