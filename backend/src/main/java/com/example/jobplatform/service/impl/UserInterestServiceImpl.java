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
            userProfileMapper.insert(profile);
            return;
        }
        profile.setTargetPosition(mergedPositions);
        userProfileMapper.updateById(profile);
    }

    @Override
    public List<InterestJobVO> listInterestJobs(Long userId) {
        ensureUserExists(userId);
        UserProfile profile = userProfileMapper.selectOne(
            new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId)
        );
        if (profile == null || profile.getTargetPosition() == null || profile.getTargetPosition().isBlank()) {
            return List.of();
        }
        List<String> jobs = Arrays.stream(profile.getTargetPosition().split(","))
            .map(String::trim)
            .filter(item -> !item.isBlank())
            .toList();
        return java.util.stream.IntStream.range(0, jobs.size())
            .mapToObj(i -> new InterestJobVO(jobs.get(i), Math.min(i + 1, 5), "student_profile"))
            .toList();
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
