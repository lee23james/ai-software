package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.mapper.JobInfoMapper;
import com.example.jobplatform.service.AnalysisService;
import com.example.jobplatform.vo.ChartItemVO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final JobInfoMapper jobInfoMapper;

    public AnalysisServiceImpl(JobInfoMapper jobInfoMapper) {
        this.jobInfoMapper = jobInfoMapper;
    }

    @Override
    public List<ChartItemVO> cityJobCount() {
        QueryWrapper<JobInfo> wrapper = new QueryWrapper<JobInfo>()
            .select("city as name", "COUNT(*) as value")
            .eq("status", 1)
            .groupBy("city")
            .orderByDesc("value")
            .last("LIMIT 20");

        List<Map<String, Object>> rows = jobInfoMapper.selectMaps(wrapper);
        return rows.stream()
            .map(row -> new ChartItemVO(
                String.valueOf(row.get("name")),
                ((Number) row.get("value")).intValue()
            ))
            .toList();
    }

    @Override
    public List<ChartItemVO> educationRequirementCount() {
        QueryWrapper<JobInfo> wrapper = new QueryWrapper<JobInfo>()
            .select("education as name", "COUNT(*) as value")
            .eq("status", 1)
            .groupBy("education")
            .orderByDesc("value");

        return jobInfoMapper.selectMaps(wrapper).stream()
            .map(row -> new ChartItemVO(
                String.valueOf(row.get("name")),
                ((Number) row.get("value")).intValue()
            ))
            .toList();
    }

    @Override
    public List<ChartItemVO> salaryRangeCount() {
        QueryWrapper<JobInfo> wrapper = new QueryWrapper<JobInfo>()
            .select(
                "CASE " +
                    "WHEN (salary_min + salary_max) / 2 < 8000 THEN '<8k' " +
                    "WHEN (salary_min + salary_max) / 2 < 12000 THEN '8k-12k' " +
                    "WHEN (salary_min + salary_max) / 2 < 16000 THEN '12k-16k' " +
                    "ELSE '16k+' END as name",
                "COUNT(*) as value"
            )
            .eq("status", 1)
            .groupBy("name")
            .orderByDesc("value");

        return jobInfoMapper.selectMaps(wrapper).stream()
            .map(row -> new ChartItemVO(
                String.valueOf(row.get("name")),
                ((Number) row.get("value")).intValue()
            ))
            .toList();
    }

    @Override
    public List<ChartItemVO> topSkillCount(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        QueryWrapper<JobInfo> wrapper = new QueryWrapper<JobInfo>()
            .select("skill_tags")
            .eq("status", 1);

        Map<String, Integer> counter = new HashMap<>();
        jobInfoMapper.selectList(wrapper).stream()
            .map(JobInfo::getSkillTags)
            .filter(Objects::nonNull)
            .forEach(tags -> Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .forEach(tag -> counter.merge(tag, 1, Integer::sum)));

        return counter.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(safeLimit)
            .map(entry -> new ChartItemVO(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    public List<ChartItemVO> experienceRequirementCount() {
        QueryWrapper<JobInfo> wrapper = new QueryWrapper<JobInfo>()
            .select("experience as name", "COUNT(*) as value")
            .eq("status", 1)
            .groupBy("experience")
            .orderByDesc("value");

        return jobInfoMapper.selectMaps(wrapper).stream()
            .map(row -> new ChartItemVO(
                String.valueOf(row.get("name")),
                ((Number) row.get("value")).intValue()
            ))
            .toList();
    }

    @Override
    public List<ChartItemVO> companyJobCount(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        QueryWrapper<JobInfo> wrapper = new QueryWrapper<JobInfo>()
            .select("company_name as name", "COUNT(*) as value")
            .eq("status", 1)
            .groupBy("company_name")
            .orderByDesc("value")
            .last("LIMIT " + safeLimit);

        return jobInfoMapper.selectMaps(wrapper).stream()
            .map(row -> new ChartItemVO(
                String.valueOf(row.get("name")),
                ((Number) row.get("value")).intValue()
            ))
            .toList();
    }
}

