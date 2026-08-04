package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.EnrollConfig;
import com.classmanage.mapper.EnrollConfigMapper;
import com.classmanage.service.EnrollConfigService;
import org.springframework.stereotype.Service;

/**
 * EnrollConfig 服务实现
 */
@Service
public class EnrollConfigServiceImpl extends ServiceImpl<EnrollConfigMapper, EnrollConfig> implements EnrollConfigService {
}
