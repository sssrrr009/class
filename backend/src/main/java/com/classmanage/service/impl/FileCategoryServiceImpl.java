package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.FileCategory;
import com.classmanage.mapper.FileCategoryMapper;
import com.classmanage.service.FileCategoryService;
import org.springframework.stereotype.Service;

/**
 * FileCategory 服务实现
 */
@Service
public class FileCategoryServiceImpl extends ServiceImpl<FileCategoryMapper, FileCategory> implements FileCategoryService {
}
