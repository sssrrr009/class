package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.CourseChangeRequest;
import com.classmanage.mapper.CourseChangeRequestMapper;
import com.classmanage.service.CourseChangeRequestService;
import org.springframework.stereotype.Service;

/**
 * CourseChangeRequest 服务实现
 */
@Service
public class CourseChangeRequestServiceImpl extends ServiceImpl<CourseChangeRequestMapper, CourseChangeRequest> implements CourseChangeRequestService {
}
