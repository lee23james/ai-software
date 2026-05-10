-- H2 (MODE=MySQL) bootstrap schema — must match entities used by auth / resume / matching.
-- Remote MySQL full DDL lives in sql/schema-mysql.sql; keep this file in sync for local dev.

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(64),
    phone VARCHAR(32),
    email VARCHAR(128),
    role VARCHAR(32) NOT NULL DEFAULT 'student',
    status INT NOT NULL DEFAULT 1,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_username ON sys_user (username);

CREATE TABLE IF NOT EXISTS student_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    school VARCHAR(128),
    major VARCHAR(128),
    grade VARCHAR(32),
    education VARCHAR(32),
    target_city VARCHAR(64),
    target_position VARCHAR(128),
    expected_salary_min INT,
    expected_salary_max INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_profile_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_student_profile_user_id ON student_profile (user_id);

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

CREATE TABLE IF NOT EXISTS resume_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resume_name VARCHAR(128) NOT NULL,
    file_url VARCHAR(512),
    file_type VARCHAR(32),
    resume_text CLOB,
    target_position VARCHAR(128),
    parse_status INT NOT NULL DEFAULT 0,
    is_default INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_info_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_resume_info_user_id ON resume_info (user_id);
CREATE INDEX IF NOT EXISTS idx_resume_info_parse_status ON resume_info (parse_status);

CREATE TABLE IF NOT EXISTS resume_parse_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    parsed_name VARCHAR(64),
    parsed_education VARCHAR(64),
    parsed_school VARCHAR(128),
    parsed_major VARCHAR(128),
    parsed_skills_json CLOB,
    parsed_projects_json CLOB,
    suggestions CLOB,
    raw_result_json CLOB,
    model_name VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parse_result_resume FOREIGN KEY (resume_id) REFERENCES resume_info (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_resume_parse_result_resume_id ON resume_parse_result (resume_id);

CREATE TABLE IF NOT EXISTS resume_skill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    skill_id BIGINT,
    source VARCHAR(32) NOT NULL DEFAULT 'manual',
    confidence DECIMAL(5,2),
    skill_name VARCHAR(64),
    skill_level INT,
    years_of_experience DECIMAL(4,1),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_skill_resume FOREIGN KEY (resume_id) REFERENCES resume_info (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_resume_skill_resume_id ON resume_skill (resume_id);
CREATE INDEX IF NOT EXISTS idx_resume_skill_name ON resume_skill (skill_name);

CREATE TABLE IF NOT EXISTS job_match_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    match_score DECIMAL(10,2) NOT NULL,
    skill_score DECIMAL(10,2),
    experience_score DECIMAL(10,2),
    education_score DECIMAL(10,2),
    city_score DECIMAL(10,2),
    salary_score DECIMAL(10,2),
    matched_skills_json CLOB,
    missing_skills_json CLOB,
    suggestion CLOB,
    raw_result_json CLOB,
    model_name VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_match_record_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_match_record_resume FOREIGN KEY (resume_id) REFERENCES resume_info (id) ON DELETE CASCADE,
    CONSTRAINT fk_match_record_job FOREIGN KEY (job_id) REFERENCES job_info (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_job_match_record_user_id ON job_match_record (user_id);
CREATE INDEX IF NOT EXISTS idx_job_match_record_resume_job ON job_match_record (resume_id, job_id);
CREATE INDEX IF NOT EXISTS idx_job_match_record_score ON job_match_record (match_score);
