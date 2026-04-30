from __future__ import annotations

import csv
from datetime import date, timedelta


def main() -> None:
    cities = ["上海", "杭州", "深圳", "北京", "广州", "南京", "苏州", "成都", "武汉", "西安"]
    companies = [
        "示例科技", "数智未来", "云启数据", "智策科技", "星河软件",
        "晨光互联", "极客引擎", "蓝海系统", "启明网络", "未来智联",
    ]
    roles = [
        ("Java 后端开发工程师", ["Java", "Spring Boot", "MySQL", "Redis"]),
        ("前端开发工程师", ["Vue", "Element Plus", "Axios", "ECharts"]),
        ("数据分析师", ["Python", "SQL", "Excel", "Tableau"]),
        ("算法工程师", ["Python", "FastAPI", "机器学习", "LLM"]),
        ("测试开发工程师", ["接口测试", "自动化测试", "Postman", "SQL"]),
        ("运维开发工程师", ["Linux", "Docker", "K8s", "Prometheus"]),
    ]
    educations = ["大专", "本科", "硕士", "不限"]
    experiences = ["不限", "1年以内", "1-3年", "3-5年"]

    output = "sample_jobs_generated.csv"
    total = 300
    start_date = date(2026, 1, 1)

    with open(output, "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.writer(f)
        writer.writerow(
            [
                "job_name",
                "company_name",
                "city",
                "salary_min",
                "salary_max",
                "education",
                "experience",
                "skill_tags",
                "publish_time",
            ]
        )

        for i in range(total):
            role, role_skills = roles[i % len(roles)]
            city = cities[i % len(cities)]
            company = companies[(i * 3) % len(companies)]
            edu = educations[(i // 2) % len(educations)]
            exp = experiences[(i // 3) % len(experiences)]
            salary_min = 7000 + (i % 8) * 1000
            salary_max = salary_min + 3000 + (i % 4) * 1000
            day = start_date + timedelta(days=i % 120)
            skills = ",".join(role_skills)
            writer.writerow(
                [
                    role,
                    company,
                    city,
                    salary_min,
                    salary_max,
                    edu,
                    exp,
                    skills,
                    day.isoformat(),
                ]
            )

    print(f"Generated {total} rows -> {output}")


if __name__ == "__main__":
    main()
