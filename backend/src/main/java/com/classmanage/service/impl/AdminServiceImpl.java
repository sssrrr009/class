package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.Admin;
import com.classmanage.mapper.AdminMapper;
import com.classmanage.service.AdminService;
import org.springframework.stereotype.Service;

/**
 * Admin 服务实现
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
}
