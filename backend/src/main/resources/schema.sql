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
CREATE INDEX IF NOT EXISTS idx_sys_user_role_status ON sys_user (role, status);

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
CREATE INDEX IF NOT EXISTS idx_student_profile_target_city ON student_profile (target_city);

CREATE TABLE IF NOT EXISTS company_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(128) NOT NULL,
    industry VARCHAR(128),
    city VARCHAR(64),
    company_size VARCHAR(64),
    financing_stage VARCHAR(64),
    description CLOB,
    website VARCHAR(255),
    status INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_company_info_name ON company_info (company_name);
CREATE INDEX IF NOT EXISTS idx_company_info_city_industry ON company_info (city, industry);

CREATE TABLE IF NOT EXISTS data_import_batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_no VARCHAR(64) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    source_file VARCHAR(512),
    total_count INT DEFAULT 0,
    success_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'processing',
    error_message CLOB,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP,
    CONSTRAINT fk_import_batch_user FOREIGN KEY (created_by) REFERENCES sys_user (id) ON DELETE SET NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_data_import_batch_no ON data_import_batch (batch_no);
CREATE INDEX IF NOT EXISTS idx_data_import_batch_status ON data_import_batch (status);
CREATE INDEX IF NOT EXISTS idx_data_import_batch_created_at ON data_import_batch (created_at);

CREATE TABLE IF NOT EXISTS job_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT,
    company_name VARCHAR(128) NOT NULL,
    job_name VARCHAR(128) NOT NULL,
    city VARCHAR(64) NOT NULL,
    salary_min INT NOT NULL DEFAULT 0,
    salary_max INT NOT NULL DEFAULT 0,
    education VARCHAR(64) NOT NULL DEFAULT '不限',
    experience VARCHAR(64) NOT NULL DEFAULT '不限',
    skill_tags VARCHAR(512) NOT NULL DEFAULT '',
    job_description CLOB,
    job_source VARCHAR(64),
    source_url VARCHAR(512),
    publish_time TIMESTAMP,
    expire_time TIMESTAMP,
    status INT NOT NULL DEFAULT 1,
    import_batch_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_company FOREIGN KEY (company_id) REFERENCES company_info (id) ON DELETE SET NULL,
    CONSTRAINT fk_job_import_batch FOREIGN KEY (import_batch_id) REFERENCES data_import_batch (id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_job_info_city_status ON job_info (city, status);
CREATE INDEX IF NOT EXISTS idx_job_info_salary ON job_info (salary_min, salary_max);
CREATE INDEX IF NOT EXISTS idx_job_info_publish_time ON job_info (publish_time);
CREATE INDEX IF NOT EXISTS idx_job_info_company_id ON job_info (company_id);
CREATE INDEX IF NOT EXISTS idx_job_info_import_batch_id ON job_info (import_batch_id);

CREATE TABLE IF NOT EXISTS skill_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_name VARCHAR(128) NOT NULL,
    skill_category VARCHAR(64),
    alias_names VARCHAR(512),
    description VARCHAR(512),
    status INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_skill_dict_skill_name ON skill_dict (skill_name);
CREATE INDEX IF NOT EXISTS idx_skill_dict_category ON skill_dict (skill_category);

CREATE TABLE IF NOT EXISTS job_skill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    required_level VARCHAR(32) NOT NULL DEFAULT 'required',
    weight DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_skill_job FOREIGN KEY (job_id) REFERENCES job_info (id) ON DELETE CASCADE,
    CONSTRAINT fk_job_skill_skill FOREIGN KEY (skill_id) REFERENCES skill_dict (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_job_skill_job_skill ON job_skill (job_id, skill_id);
CREATE INDEX IF NOT EXISTS idx_job_skill_skill_id ON job_skill (skill_id);

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
    job_selection_advice CLOB,
    job_selection_advice_model VARCHAR(128),
    interest_resume_advice CLOB,
    interest_resume_advice_model VARCHAR(128),
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
CREATE INDEX IF NOT EXISTS idx_resume_parse_result_created_at ON resume_parse_result (created_at);

CREATE TABLE IF NOT EXISTS resume_skill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    skill_id BIGINT,
    source VARCHAR(32) NOT NULL DEFAULT 'ai',
    confidence DECIMAL(5,2),
    skill_name VARCHAR(64),
    skill_level INT,
    years_of_experience DECIMAL(4,1),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_resume_skill_resume_skill ON resume_skill (resume_id, skill_id);
CREATE INDEX IF NOT EXISTS idx_resume_skill_skill_id ON resume_skill (skill_id);
CREATE INDEX IF NOT EXISTS idx_resume_skill_name ON resume_skill (skill_name);

CREATE TABLE IF NOT EXISTS job_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_job FOREIGN KEY (job_id) REFERENCES job_info (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_job_favorite_user_job ON job_favorite (user_id, job_id);
CREATE INDEX IF NOT EXISTS idx_job_favorite_job_id ON job_favorite (job_id);

CREATE TABLE IF NOT EXISTS job_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    application_status VARCHAR(32) NOT NULL DEFAULT 'submitted',
    apply_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_application_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_application_job FOREIGN KEY (job_id) REFERENCES job_info (id) ON DELETE CASCADE,
    CONSTRAINT fk_application_resume FOREIGN KEY (resume_id) REFERENCES resume_info (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_job_application_user_job ON job_application (user_id, job_id);
CREATE INDEX IF NOT EXISTS idx_job_application_status ON job_application (application_status);
CREATE INDEX IF NOT EXISTS idx_job_application_job_id ON job_application (job_id);

CREATE TABLE IF NOT EXISTS job_match_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    match_score INT NOT NULL,
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

CREATE TABLE IF NOT EXISTS analysis_metric_snapshot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    metric_code VARCHAR(64) NOT NULL,
    metric_name VARCHAR(128) NOT NULL,
    dimension_key VARCHAR(64),
    dimension_value VARCHAR(128),
    metric_value DECIMAL(18,2),
    metric_text VARCHAR(255),
    snapshot_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_analysis_metric_code_date ON analysis_metric_snapshot (metric_code, snapshot_date);
CREATE INDEX IF NOT EXISTS idx_analysis_metric_dimension ON analysis_metric_snapshot (dimension_key, dimension_value);

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    module_name VARCHAR(64) NOT NULL,
    operation_type VARCHAR(64) NOT NULL,
    operation_desc VARCHAR(512),
    request_path VARCHAR(255),
    request_method VARCHAR(16),
    request_params CLOB,
    ip_address VARCHAR(64),
    result_status VARCHAR(32) NOT NULL,
    error_message CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_operation_log_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_operation_log_user_id ON operation_log (user_id);
CREATE INDEX IF NOT EXISTS idx_operation_log_module_type ON operation_log (module_name, operation_type);
CREATE INDEX IF NOT EXISTS idx_operation_log_created_at ON operation_log (created_at);
