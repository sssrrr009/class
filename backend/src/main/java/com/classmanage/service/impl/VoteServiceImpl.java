package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.Vote;
import com.classmanage.mapper.VoteMapper;
import com.classmanage.service.VoteService;
import org.springframework.stereotype.Service;

/**
 * Vote 服务实现
 */
@Service
public class VoteServiceImpl extends ServiceImpl<VoteMapper, Vote> implements VoteService {
}
