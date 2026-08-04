package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.PageResult;
import com.classmanage.dto.VoteVO;
import com.classmanage.entity.Cadre;
import com.classmanage.entity.Student;
import com.classmanage.entity.Teacher;
import com.classmanage.entity.Vote;
import com.classmanage.entity.VoteOption;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.security.UserContext;
import com.classmanage.service.CadreService;
import com.classmanage.service.StudentService;
import com.classmanage.service.TeacherService;
import com.classmanage.service.VoteOptionService;
import com.classmanage.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 投票评选管理（管理员/教师/干部创建）
 */
@RestController
@RequestMapping("/api/vote")
@com.classmanage.security.RequireLogin
public class VoteController {

    @Autowired
    private VoteService voteService;
    @Autowired
    private VoteOptionService voteOptionService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private CadreService cadreService;

    @GetMapping("/page")
    public Result<PageResult<VoteVO>> page(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size) {
        Page<Vote> p = voteService.page(new Page<>(page, size),
                new LambdaQueryWrapper<Vote>().orderByDesc(Vote::getCreateTime));
        List<VoteVO> list = p.getRecords().stream().map(v -> toVO(v, false)).collect(Collectors.toList());
        return Result.ok(new PageResult<>(p.getTotal(), list));
    }

    /**
     * 投票详情（含选项）
     */
    @GetMapping("/{id}")
    public Result<VoteVO> detail(@PathVariable Long id) {
        Vote vote = voteService.getById(id);
        if (vote == null) {
            throw new BusinessException("投票活动不存在");
        }
        return Result.ok(toVO(vote, true));
    }

    /**
     * 创建投票活动（管理员/教师/干部共用）
     */
    @PostMapping
    public Result<Void> add(@RequestBody VoteVO body) {
        Role role = UserContext.getRole();
        if (role == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        Long userId = UserContext.getUserId();

        Vote vote = new Vote();
        vote.setTitle(body.getTitle());
        vote.setContent(body.getContent());
        vote.setMaxVotes(body.getMaxVotes() == null ? 1 : body.getMaxVotes());
        vote.setShowResult(body.getShowResult() == null ? 1 : body.getShowResult());
        vote.setStatus(1);
        vote.setCreatorRole(role.getValue());

        if (role == Role.ADMIN) {
            vote.setCreatorName("管理员");
        } else if (role == Role.TEACHER) {
            Teacher t = teacherService.getById(userId);
            vote.setCreatorName(t != null ? t.getName() : null);
            vote.setTeacherId(userId);
        } else if (role == Role.CADRE) {
            Student s = studentService.getById(userId);
            vote.setCreatorName(s != null ? s.getName() : null);
            Cadre cadre = cadreService.getOne(new LambdaQueryWrapper<Cadre>().eq(Cadre::getStudentId, userId));
            vote.setCadreId(cadre != null ? cadre.getId() : null);
        } else {
            throw new BusinessException("无权创建投票");
        }

        if (body.getOptions() == null || body.getOptions().isEmpty()) {
            throw new BusinessException("至少需要一个选项");
        }
        voteService.save(vote);
        for (String opt : body.getOptions()) {
            VoteOption vo = new VoteOption();
            vo.setVoteId(vote.getId());
            vo.setOptionText(opt);
            voteOptionService.save(vo);
        }
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Vote vote) {
        if (vote.getId() == null) {
            throw new BusinessException("ID 不能为空");
        }
        checkOwner(vote.getId());
        voteService.updateById(vote);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkOwner(id);
        voteService.removeById(id);
        return Result.ok();
    }

    private void checkOwner(Long voteId) {
        Role role = UserContext.getRole();
        if (role == Role.ADMIN) {
            return;
        }
        Vote vote = voteService.getById(voteId);
        if (vote == null) {
            return;
        }
        if (!role.getValue().equals(vote.getCreatorRole())) {
            throw new BusinessException(403, "无权操作他人创建的投票");
        }
    }

    private VoteVO toVO(Vote v, boolean withOptions) {
        VoteVO vo = new VoteVO();
        vo.setId(v.getId());
        vo.setTitle(v.getTitle());
        vo.setContent(v.getContent());
        vo.setCreatorRole(v.getCreatorRole());
        vo.setCreatorName(v.getCreatorName());
        vo.setMaxVotes(v.getMaxVotes());
        vo.setShowResult(v.getShowResult());
        vo.setStatus(v.getStatus());
        vo.setCreateTime(v.getCreateTime());
        if (withOptions) {
            List<VoteOption> opts = voteOptionService.list(
                    new LambdaQueryWrapper<VoteOption>().eq(VoteOption::getVoteId, v.getId()));
            vo.setOptions(opts.stream().map(VoteOption::getOptionText).collect(Collectors.toList()));
            vo.setOptionIds(opts.stream().map(VoteOption::getId).collect(Collectors.toList()));
        }
        return vo;
    }
}
