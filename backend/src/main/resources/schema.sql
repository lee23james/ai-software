CREATE TABLE IF NOT EXISTS job_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(128) NOT NULL,
    company_name VARCHAR(128) NOT NULL,
    city VARCHAR(64) NOT NULL,
    salary_min INT NOT NULL DEFAULT 0,
    salary_max INT NOT NULL DEFAULT 0,
    education VARCHAR(32) NOT NULL DEFAULT '不限',
    experience VARCHAR(32) NOT NULL DEFAULT '不限',
    skill_tags VARCHAR(512) NOT NULL DEFAULT '',
    job_description VARCHAR(2000),
    publish_time TIMESTAMP,
    status INT NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_job_city ON job_info (city);
CREATE INDEX IF NOT EXISTS idx_job_education ON job_info (education);
CREATE INDEX IF NOT EXISTS idx_job_publish_time ON job_info (publish_time);
