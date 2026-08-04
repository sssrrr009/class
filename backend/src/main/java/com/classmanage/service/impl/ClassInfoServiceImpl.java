package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.ClassInfo;
import com.classmanage.mapper.ClassInfoMapper;
import com.classmanage.service.ClassInfoService;
import org.springframework.stereotype.Service;

/**
 * ClassInfo 服务实现
 */
@Service
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoMapper, ClassInfo> implements ClassInfoService {
}
