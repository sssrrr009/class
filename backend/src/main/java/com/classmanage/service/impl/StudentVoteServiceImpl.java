package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.StudentVote;
import com.classmanage.mapper.StudentVoteMapper;
import com.classmanage.service.StudentVoteService;
import org.springframework.stereotype.Service;

/**
 * StudentVote 服务实现
 */
@Service
public class StudentVoteServiceImpl extends ServiceImpl<StudentVoteMapper, StudentVote> implements StudentVoteService {
}
