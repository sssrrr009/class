package com.classmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classmanage.common.BusinessException;
import com.classmanage.common.Result;
import com.classmanage.dto.VoteResultVO;
import com.classmanage.entity.Student;
import com.classmanage.entity.StudentVote;
import com.classmanage.entity.Vote;
import com.classmanage.entity.VoteOption;
import com.classmanage.security.RequireRole;
import com.classmanage.security.Role;
import com.classmanage.security.UserContext;
import com.classmanage.service.StudentService;
import com.classmanage.service.StudentVoteService;
import com.classmanage.service.VoteOptionService;
import com.classmanage.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生投票管理
 */
@RestController
@RequestMapping("/api/vote")
public class StudentVoteController {

    @Autowired
    private StudentVoteService studentVoteService;
    @Autowired
    private VoteService voteService;
    @Autowired
    private VoteOptionService voteOptionService;
    @Autowired
    private StudentService studentService;

    /**
     * 学生投票（校验多票数上限）
     * body: { "voteId": 1, "optionIds": [1, 2] }
     */
    @PostMapping("/cast")
    @RequireRole({Role.STUDENT, Role.CADRE})
    public Result<Void> cast(@RequestBody CastRequest req) {
        Long studentId = UserContext.getUserId();
        Vote vote = voteService.getById(req.getVoteId());
        if (vote == null) {
            throw new BusinessException("投票活动不存在");
        }
        if (vote.getStatus() == null || vote.getStatus() != 1) {
            throw new BusinessException("该投票已结束");
        }
        if (req.getOptionIds() == null || req.getOptionIds().isEmpty()) {
            throw new BusinessException("请选择至少一个选项");
        }
        if (req.getOptionIds().size() > vote.getMaxVotes()) {
            throw new BusinessException("该活动每人最多投 " + vote.getMaxVotes() + " 票");
        }
        // 校验选项属于该活动
        List<VoteOption> opts = voteOptionService.list(
                new LambdaQueryWrapper<VoteOption>().eq(VoteOption::getVoteId, vote.getId()));
        List<Long> validIds = opts.stream().map(VoteOption::getId).collect(Collectors.toList());
        for (Long oid : req.getOptionIds()) {
            if (!validIds.contains(oid)) {
                throw new BusinessException("选项不合法");
            }
        }
        // 已投数量
        long voted = studentVoteService.count(new LambdaQueryWrapper<StudentVote>()
                .eq(StudentVote::getVoteId, vote.getId())
                .eq(StudentVote::getStudentId, studentId));
        if (voted + req.getOptionIds().size() > vote.getMaxVotes()) {
            throw new BusinessException("投票数超出上限（每人最多 " + vote.getMaxVotes() + " 票）");
        }
        // 同选项不重复
        for (Long oid : req.getOptionIds()) {
            long dup = studentVoteService.count(new LambdaQueryWrapper<StudentVote>()
                    .eq(StudentVote::getVoteId, vote.getId())
                    .eq(StudentVote::getStudentId, studentId)
                    .eq(StudentVote::getOptionId, oid));
            if (dup > 0) {
                throw new BusinessException("不能对同一选项重复投票");
            }
        }
        for (Long oid : req.getOptionIds()) {
            StudentVote sv = new StudentVote();
            sv.setVoteId(vote.getId());
            sv.setStudentId(studentId);
            sv.setOptionId(oid);
            studentVoteService.save(sv);
        }
        return Result.ok();
    }

    /**
     * 学生查看本人投票记录
     */
    @GetMapping("/my")
    @RequireRole({Role.STUDENT, Role.CADRE})
    public Result<List<MyVoteVO>> my() {
        Long studentId = UserContext.getUserId();
        List<StudentVote> list = studentVoteService.list(
                new LambdaQueryWrapper<StudentVote>().eq(StudentVote::getStudentId, studentId));
        Map<Long, Vote> voteMap = voteService.listByIds(
                list.stream().map(StudentVote::getVoteId).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(Vote::getId, v -> v));
        Map<Long, VoteOption> optMap = voteOptionService.listByIds(
                list.stream().map(StudentVote::getOptionId).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(VoteOption::getId, o -> o));

        List<MyVoteVO> result = new ArrayList<>();
        for (StudentVote sv : list) {
            MyVoteVO vo = new MyVoteVO();
            vo.setId(sv.getId());
            vo.setVoteId(sv.getVoteId());
            vo.setVoteTitle(voteMap.get(sv.getVoteId()) != null ? voteMap.get(sv.getVoteId()).getTitle() : null);
            vo.setOptionText(optMap.get(sv.getOptionId()) != null ? optMap.get(sv.getOptionId()).getOptionText() : null);
            vo.setVoteTime(sv.getVoteTime());
            result.add(vo);
        }
        return Result.ok(result);
    }

    /**
     * 删除本人投票记录
     */
    @DeleteMapping("/my/{id}")
    @RequireRole({Role.STUDENT, Role.CADRE})
    public Result<Void> deleteMy(@PathVariable Long id) {
        StudentVote sv = studentVoteService.getById(id);
        if (sv != null && sv.getStudentId().equals(UserContext.getUserId())) {
            studentVoteService.removeById(id);
        }
        return Result.ok();
    }

    /**
     * 投票结果统计（饼图数据）
     */
    @GetMapping("/result/{voteId}")
    public Result<List<VoteResultVO>> result(@PathVariable Long voteId) {
        Vote vote = voteService.getById(voteId);
        if (vote != null && vote.getShowResult() != null && vote.getShowResult() != 1) {
            // 不公开结果
            Role r = UserContext.getRole();
            if (r != Role.ADMIN && r != Role.TEACHER) {
                throw new BusinessException(403, "投票结果暂未公开");
            }
        }
        List<VoteOption> opts = voteOptionService.list(
                new LambdaQueryWrapper<VoteOption>().eq(VoteOption::getVoteId, voteId));
        List<VoteResultVO> list = new ArrayList<>();
        for (VoteOption opt : opts) {
            long count = studentVoteService.count(new LambdaQueryWrapper<StudentVote>()
                    .eq(StudentVote::getVoteId, voteId)
                    .eq(StudentVote::getOptionId, opt.getId()));
            VoteResultVO vo = new VoteResultVO();
            vo.setName(opt.getOptionText());
            vo.setValue(count);
            list.add(vo);
        }
        return Result.ok(list);
    }

    /**
     * 所有学生投票详情（管理员端饼图）
     */
    @GetMapping("/detail")
    @RequireRole({Role.ADMIN, Role.TEACHER})
    public Result<List<VoteResultVO>> detail() {
        List<Vote> votes = voteService.list();
        List<VoteResultVO> list = new ArrayList<>();
        for (Vote v : votes) {
            long count = studentVoteService.count(new LambdaQueryWrapper<StudentVote>().eq(StudentVote::getVoteId, v.getId()));
            VoteResultVO vo = new VoteResultVO();
            vo.setName(v.getTitle());
            vo.setValue(count);
            list.add(vo);
        }
        return Result.ok(list);
    }

    public static class CastRequest {
        private Long voteId;
        private List<Long> optionIds;
        public Long getVoteId() { return voteId; }
        public void setVoteId(Long voteId) { this.voteId = voteId; }
        public List<Long> getOptionIds() { return optionIds; }
        public void setOptionIds(List<Long> optionIds) { this.optionIds = optionIds; }
    }

    @lombok.Data
    public static class MyVoteVO {
        private Long id;
        private Long voteId;
        private String voteTitle;
        private String optionText;
        private java.time.LocalDateTime voteTime;
    }
}
