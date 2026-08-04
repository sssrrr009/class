package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.VoteOption;
import com.classmanage.mapper.VoteOptionMapper;
import com.classmanage.service.VoteOptionService;
import org.springframework.stereotype.Service;

/**
 * VoteOption 服务实现
 */
@Service
public class VoteOptionServiceImpl extends ServiceImpl<VoteOptionMapper, VoteOption> implements VoteOptionService {
}
