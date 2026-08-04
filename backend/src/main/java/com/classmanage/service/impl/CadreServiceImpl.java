package com.classmanage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classmanage.entity.Cadre;
import com.classmanage.mapper.CadreMapper;
import com.classmanage.service.CadreService;
import org.springframework.stereotype.Service;

/**
 * Cadre 服务实现
 */
@Service
public class CadreServiceImpl extends ServiceImpl<CadreMapper, Cadre> implements CadreService {
}
