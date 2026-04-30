package com.example.jobplatform.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.mapper.JobInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JobDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(JobDataInitializer.class);
    private static final String DEFAULT_DESCRIPTION = "由 CSV 样本导入的岗位信息";
    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    private final JobInfoMapper jobInfoMapper;

    public JobDataInitializer(JobInfoMapper jobInfoMapper) {
        this.jobInfoMapper = jobInfoMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        Long count = jobInfoMapper.selectCount(Wrappers.emptyWrapper());
        if (count != null && count > 0) {
            log.info("job_info already has {} records, skip CSV import.", count);
            return;
        }

        Path csvPath = locateCsvPath();
        if (csvPath == null) {
            log.warn("CSV file not found. Expected data/sample_jobs.csv.");
            return;
        }

        int imported = importCsv(csvPath);
        log.info("Imported {} rows from {}.", imported, csvPath);
    }

    private Path locateCsvPath() {
        List<Path> candidates = List.of(
            Paths.get("data", "sample_jobs.csv"),
            Paths.get("..", "data", "sample_jobs.csv"),
            Paths.get("..", "..", "data", "sample_jobs.csv")
        );
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private int importCsv(Path csvPath) throws IOException {
        List<JobInfo> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // header
            if (line == null) {
                return 0;
            }

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                JobInfo item = toJobInfo(line);
                rows.add(item);
            }
        }

        for (JobInfo row : rows) {
            jobInfoMapper.insert(row);
        }
        return rows.size();
    }

    private JobInfo toJobInfo(String csvLine) {
        String[] values = csvLine.split(CSV_SPLIT_REGEX, -1);
        JobInfo item = new JobInfo();

        String jobName = getValue(values, 0, "未知岗位");
        String companyName = getValue(values, 1, "未知公司");
        String city = getValue(values, 2, "未知城市");
        int salaryMin = parseIntSafe(getValue(values, 3, "0"), 0);
        int salaryMax = parseIntSafe(getValue(values, 4, "0"), 0);
        if (salaryMin > salaryMax) {
            int temp = salaryMin;
            salaryMin = salaryMax;
            salaryMax = temp;
        }

        item.setJobName(jobName);
        item.setCompanyName(companyName);
        item.setCity(city);
        item.setSalaryMin(salaryMin);
        item.setSalaryMax(salaryMax);
        item.setEducation(getValue(values, 5, "不限"));
        item.setExperience(getValue(values, 6, "不限"));
        item.setSkillTags(getValue(values, 7, ""));
        item.setPublishTime(parseDateTimeSafe(getValue(values, 8, "")));
        item.setJobDescription(DEFAULT_DESCRIPTION);
        item.setStatus(1);

        return item;
    }

    private String getValue(String[] values, int index, String defaultValue) {
        if (index >= values.length) {
            return defaultValue;
        }
        String raw = values[index].trim();
        if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) {
            raw = raw.substring(1, raw.length() - 1);
        }
        return raw.isBlank() ? defaultValue : raw;
    }

    private int parseIntSafe(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private LocalDateTime parseDateTimeSafe(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(value).atStartOfDay();
            } catch (DateTimeParseException ignoredAgain) {
                return null;
            }
        }
    }
}
