package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.College;
import com.classmanage.mapper.CollegeMapper;
import com.classmanage.service.CollegeService;
import org.springframework.stereotype.Service;

/**
 * College 服务实现
 */
@Service
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {
}
