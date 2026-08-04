package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.Notice;
import com.classmanage.mapper.NoticeMapper;
import com.classmanage.service.NoticeService;
import org.springframework.stereotype.Service;

/**
 * Notice 服务实现
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {
}
