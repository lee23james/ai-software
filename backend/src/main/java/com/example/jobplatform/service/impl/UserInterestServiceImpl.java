package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jobplatform.dto.InterestJobItemDTO;
import com.example.jobplatform.dto.SaveInterestJobsRequestDTO;
import com.example.jobplatform.entity.AccountUser;
import com.example.jobplatform.entity.UserProfile;
import com.example.jobplatform.mapper.AccountUserMapper;
import com.example.jobplatform.mapper.UserProfileMapper;
import com.example.jobplatform.service.UserInterestService;
import com.example.jobplatform.vo.InterestJobVO;
import com.example.jobplatform.vo.InterestJobsPayloadVO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserInterestServiceImpl implements UserInterestService {

    private final UserProfileMapper userProfileMapper;
    private final AccountUserMapper accountUserMapper;

    public UserInterestServiceImpl(UserProfileMapper userProfileMapper, AccountUserMapper accountUserMapper) {
        this.userProfileMapper = userProfileMapper;
        this.accountUserMapper = accountUserMapper;
    }

    @Override
    public void saveInterestJobs(SaveInterestJobsRequestDTO request) {
        ensureUserExists(request.getUserId());
        Integer salMin = request.getExpectedSalaryMin();
        Integer salMax = request.getExpectedSalaryMax();
        if (salMin != null && salMax != null && salMin > salMax) {
            throw new IllegalArgumentException("期望薪资下限不能高于上限");
        }
        String mergedPositions = request.getJobs().stream()
            .map(InterestJobItemDTO::getJobName)
            .map(String::trim)
            .distinct()
            .collect(Collectors.joining(","));
        UserProfile profile = userProfileMapper.selectOne(
            new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, request.getUserId())
        );
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(request.getUserId());
            profile.setTargetPosition(mergedPositions);
            profile.setExpectedSalaryMin(salMin);
            profile.setExpectedSalaryMax(salMax);
            userProfileMapper.insert(profile);
            return;
        }
        profile.setTargetPosition(mergedPositions);
        profile.setExpectedSalaryMin(salMin);
        profile.setExpectedSalaryMax(salMax);
        userProfileMapper.updateById(profile);
    }

    @Override
    public InterestJobsPayloadVO listInterestJobs(Long userId) {
        ensureUserExists(userId);
        UserProfile profile = userProfileMapper.selectOne(
            new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId)
        );
        if (profile == null || profile.getTargetPosition() == null || profile.getTargetPosition().isBlank()) {
            return new InterestJobsPayloadVO(List.of(), profile == null ? null : profile.getExpectedSalaryMin(),
                profile == null ? null : profile.getExpectedSalaryMax());
        }
        List<String> jobs = Arrays.stream(profile.getTargetPosition().split(","))
            .map(String::trim)
            .filter(item -> !item.isBlank())
            .toList();
        List<InterestJobVO> vos = java.util.stream.IntStream.range(0, jobs.size())
            .mapToObj(i -> new InterestJobVO(jobs.get(i), Math.min(i + 1, 5), "student_profile"))
            .toList();
        return new InterestJobsPayloadVO(vos, profile.getExpectedSalaryMin(), profile.getExpectedSalaryMax());
    }

    private void ensureUserExists(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        AccountUser user = accountUserMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
    }
}
