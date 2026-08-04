package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.Score;
import com.classmanage.mapper.ScoreMapper;
import com.classmanage.service.ScoreService;
import org.springframework.stereotype.Service;

/**
 * Score 服务实现
 */
@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {
}
