package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.Major;
import com.classmanage.mapper.MajorMapper;
import com.classmanage.service.MajorService;
import org.springframework.stereotype.Service;

/**
 * Major 服务实现
 */
@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {
}
