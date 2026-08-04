package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.MajorPlan;
import com.classmanage.mapper.MajorPlanMapper;
import com.classmanage.service.MajorPlanService;
import org.springframework.stereotype.Service;

/**
 * MajorPlan 服务实现
 */
@Service
public class MajorPlanServiceImpl extends ServiceImpl<MajorPlanMapper, MajorPlan> implements MajorPlanService {
}
