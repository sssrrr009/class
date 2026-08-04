package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.FileInfo;
import com.classmanage.mapper.FileInfoMapper;
import com.classmanage.service.FileInfoService;
import org.springframework.stereotype.Service;

/**
 * FileInfo 服务实现
 */
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {
}
