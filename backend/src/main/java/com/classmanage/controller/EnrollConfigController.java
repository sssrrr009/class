package com.classmanage.controller;

import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.entity.EnrollConfig;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.service.EnrollConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 选课配置（管理员定时开启/关闭选课）
 */
@RestController
@RequestMapping("/api/enroll-config")
public class EnrollConfigController {

    @Autowired
    private EnrollConfigService enrollConfigService;

    /**
     * 获取当前选课配置
     */
    @GetMapping
    public Result<EnrollConfig> get() {
        List<EnrollConfig> list = enrollConfigService.list();
        if (list.isEmpty()) {
            return Result.ok(null);
        }
        return Result.ok(list.get(0));
    }

    /**
     * 更新选课配置（管理员）
     */
    @PutMapping
    @RequireRole({Role.ADMIN})
    public Result<Void> update(@RequestBody EnrollConfig config) {
        List<EnrollConfig> list = enrollConfigService.list();
        EnrollConfig target;
        if (list.isEmpty()) {
            target = new EnrollConfig();
        } else {
            target = list.get(0);
        }
        target.setIsOpen(config.getIsOpen());
        target.setStartTime(config.getStartTime());
        target.setEndTime(config.getEndTime());
        if (target.getId() == null) {
            enrollConfigService.save(target);
        } else {
            enrollConfigService.updateById(target);
        }
        return Result.ok();
    }

    /**
     * 校验选课是否开放（供选课接口调用）
     */
    public void checkOpen() {
        List<EnrollConfig> list = enrollConfigService.list();
        if (list.isEmpty()) {
            throw new BusinessException("选课尚未开放");
        }
        EnrollConfig config = list.get(0);
        if (config.getIsOpen() == null || config.getIsOpen() != 1) {
            throw new BusinessException("选课未开放");
        }
        LocalDateTime now = LocalDateTime.now();
        if (config.getStartTime() != null && now.isBefore(config.getStartTime())) {
            throw new BusinessException("选课尚未开始");
        }
        if (config.getEndTime() != null && now.isAfter(config.getEndTime())) {
            throw new BusinessException("选课已结束");
        }
    }
}
