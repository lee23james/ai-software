-- MySQL 8.0 schema for recruitment data analysis (target model + compatibility)
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(64) NOT NULL COMMENT '登录用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    real_name VARCHAR(64) NULL COMMENT '真实姓名',
    phone VARCHAR(32) NULL COMMENT '手机号',
    email VARCHAR(128) NULL COMMENT '邮箱',
    role VARCHAR(32) NOT NULL DEFAULT 'student' COMMENT '角色 student/admin',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    last_login_at DATETIME NULL COMMENT '最近登录时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_role_status (role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账号表';

CREATE TABLE IF NOT EXISTS student_profile (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    school VARCHAR(128) NULL COMMENT '学校',
    major VARCHAR(128) NULL COMMENT '专业',
    grade VARCHAR(32) NULL COMMENT '年级',
    education VARCHAR(32) NULL COMMENT '学历',
    target_city VARCHAR(64) NULL COMMENT '期望城市',
    target_position VARCHAR(128) NULL COMMENT '期望岗位',
    expected_salary_min INT NULL COMMENT '期望最低薪资',
    expected_salary_max INT NULL COMMENT '期望最高薪资',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_profile_user_id (user_id),
    KEY idx_student_profile_target_city (target_city),
    CONSTRAINT fk_student_profile_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生档案表';

CREATE TABLE IF NOT EXISTS company_info (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    company_name VARCHAR(128) NOT NULL COMMENT '公司名称',
    industry VARCHAR(128) NULL COMMENT '行业',
    city VARCHAR(64) NULL COMMENT '城市',
    company_size VARCHAR(64) NULL COMMENT '公司规模',
    financing_stage VARCHAR(64) NULL COMMENT '融资阶段',
    description TEXT NULL COMMENT '公司介绍',
    website VARCHAR(255) NULL COMMENT '官网',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_company_info_name (company_name),
    KEY idx_company_info_city_industry (city, industry)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司信息表';

CREATE TABLE IF NOT EXISTS data_import_batch (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    batch_no VARCHAR(64) NOT NULL COMMENT '批次号',
    source_type VARCHAR(32) NOT NULL COMMENT '来源类型',
    source_file VARCHAR(512) NULL COMMENT '来源文件',
    total_count INT NOT NULL DEFAULT 0 COMMENT '总数',
    success_count INT NOT NULL DEFAULT 0 COMMENT '成功数',
    failed_count INT NOT NULL DEFAULT 0 COMMENT '失败数',
    status VARCHAR(32) NOT NULL DEFAULT 'processing' COMMENT 'processing/success/failed',
    error_message TEXT NULL COMMENT '错误信息',
    created_by BIGINT NULL COMMENT '创建人',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    finished_at DATETIME NULL COMMENT '完成时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_data_import_batch_no (batch_no),
    KEY idx_data_import_batch_status (status),
    KEY idx_data_import_batch_created_at (created_at),
    CONSTRAINT fk_import_batch_user FOREIGN KEY (created_by) REFERENCES sys_user (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据导入批次表';

CREATE TABLE IF NOT EXISTS job_info (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    company_id BIGINT NULL COMMENT '关联公司ID',
    company_name VARCHAR(128) NOT NULL COMMENT '公司名称（冗余）',
    job_name VARCHAR(128) NOT NULL COMMENT '岗位名称',
    city VARCHAR(64) NOT NULL COMMENT '城市',
    salary_min INT NOT NULL DEFAULT 0 COMMENT '最低薪资（月薪）',
    salary_max INT NOT NULL DEFAULT 0 COMMENT '最高薪资（月薪）',
    education VARCHAR(64) NOT NULL DEFAULT '不限' COMMENT '学历要求',
    experience VARCHAR(64) NOT NULL DEFAULT '不限' COMMENT '经验要求',
    skill_tags VARCHAR(512) NOT NULL DEFAULT '' COMMENT '原始技能标签',
    job_description TEXT NULL COMMENT '岗位描述',
    job_source VARCHAR(64) NULL COMMENT '来源',
    source_url VARCHAR(512) NULL COMMENT '来源链接',
    publish_time DATETIME NULL COMMENT '发布时间',
    expire_time DATETIME NULL COMMENT '过期时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0下架1上架2待审核',
    import_batch_id BIGINT NULL COMMENT '导入批次ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_job_info_city_status (city, status),
    KEY idx_job_info_salary (salary_min, salary_max),
    KEY idx_job_info_publish_time (publish_time),
    KEY idx_job_info_company_id (company_id),
    KEY idx_job_info_import_batch_id (import_batch_id),
    CONSTRAINT fk_job_company FOREIGN KEY (company_id) REFERENCES company_info (id) ON DELETE SET NULL,
    CONSTRAINT fk_job_import_batch FOREIGN KEY (import_batch_id) REFERENCES data_import_batch (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位信息表';

CREATE TABLE IF NOT EXISTS skill_dict (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    skill_name VARCHAR(128) NOT NULL COMMENT '技能名',
    skill_category VARCHAR(64) NULL COMMENT '技能分类',
    alias_names VARCHAR(512) NULL COMMENT '别名',
    description VARCHAR(512) NULL COMMENT '描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_skill_dict_skill_name (skill_name),
    KEY idx_skill_dict_category (skill_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能字典表';

CREATE TABLE IF NOT EXISTS job_skill (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    job_id BIGINT NOT NULL COMMENT '岗位ID',
    skill_id BIGINT NOT NULL COMMENT '技能ID',
    required_level VARCHAR(32) NOT NULL DEFAULT 'required' COMMENT 'required/preferred',
    weight DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT '匹配权重',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_job_skill_job_skill (job_id, skill_id),
    KEY idx_job_skill_skill_id (skill_id),
    CONSTRAINT fk_job_skill_job FOREIGN KEY (job_id) REFERENCES job_info (id) ON DELETE CASCADE,
    CONSTRAINT fk_job_skill_skill FOREIGN KEY (skill_id) REFERENCES skill_dict (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位技能关联表';

CREATE TABLE IF NOT EXISTS resume_info (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    resume_name VARCHAR(128) NOT NULL COMMENT '简历名称',
    file_url VARCHAR(512) NULL COMMENT '简历文件地址',
    file_type VARCHAR(32) NULL COMMENT '文件类型',
    resume_text LONGTEXT NULL COMMENT '简历文本',
    target_position VARCHAR(128) NULL COMMENT '目标岗位',
    parse_status TINYINT NOT NULL DEFAULT 0 COMMENT '0未解析1解析中2成功3失败',
    is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认',
    job_selection_advice LONGTEXT NULL COMMENT 'DeepSeek岗位选择建议',
    job_selection_advice_model VARCHAR(128) NULL COMMENT '生成建议所用模型',
    interest_resume_advice LONGTEXT NULL COMMENT '针对兴趣岗位的简历修改建议',
    interest_resume_advice_model VARCHAR(128) NULL COMMENT '兴趣岗位简历建议所用模型',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_resume_info_user_id (user_id),
    KEY idx_resume_info_parse_status (parse_status),
    CONSTRAINT fk_resume_info_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历信息表';

CREATE TABLE IF NOT EXISTS resume_parse_result (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    parsed_name VARCHAR(64) NULL COMMENT '解析姓名',
    parsed_education VARCHAR(64) NULL COMMENT '解析学历',
    parsed_school VARCHAR(128) NULL COMMENT '解析学校',
    parsed_major VARCHAR(128) NULL COMMENT '解析专业',
    parsed_skills_json TEXT NULL COMMENT '解析技能JSON',
    parsed_projects_json TEXT NULL COMMENT '项目JSON',
    suggestions TEXT NULL COMMENT '优化建议',
    raw_result_json LONGTEXT NULL COMMENT '原始结果JSON',
    model_name VARCHAR(128) NULL COMMENT '模型名',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_resume_parse_result_resume_id (resume_id),
    KEY idx_resume_parse_result_created_at (created_at),
    CONSTRAINT fk_parse_result_resume FOREIGN KEY (resume_id) REFERENCES resume_info (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历解析结果表';

CREATE TABLE IF NOT EXISTS resume_skill (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    skill_id BIGINT NULL COMMENT '技能ID',
    source VARCHAR(32) NOT NULL DEFAULT 'ai' COMMENT '来源 ai/manual',
    confidence DECIMAL(5,2) NULL COMMENT '置信度',
    skill_name VARCHAR(64) NULL COMMENT '兼容字段：技能名',
    skill_level TINYINT NULL COMMENT '兼容字段：技能等级',
    years_of_experience DECIMAL(4,1) NULL COMMENT '兼容字段：经验年限',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_resume_skill_resume_skill (resume_id, skill_id),
    KEY idx_resume_skill_skill_id (skill_id),
    KEY idx_resume_skill_name (skill_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历技能关联表';

CREATE TABLE IF NOT EXISTS job_favorite (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    job_id BIGINT NOT NULL COMMENT '岗位ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_job_favorite_user_job (user_id, job_id),
    KEY idx_job_favorite_job_id (job_id),
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_job FOREIGN KEY (job_id) REFERENCES job_info (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位收藏表';

CREATE TABLE IF NOT EXISTS job_application (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    job_id BIGINT NOT NULL COMMENT '岗位ID',
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    application_status VARCHAR(32) NOT NULL DEFAULT 'submitted' COMMENT '投递状态',
    apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
    remark VARCHAR(512) NULL COMMENT '备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_job_application_user_job (user_id, job_id),
    KEY idx_job_application_status (application_status),
    KEY idx_job_application_job_id (job_id),
    CONSTRAINT fk_application_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_application_job FOREIGN KEY (job_id) REFERENCES job_info (id) ON DELETE CASCADE,
    CONSTRAINT fk_application_resume FOREIGN KEY (resume_id) REFERENCES resume_info (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位投递表';

CREATE TABLE IF NOT EXISTS job_match_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    job_id BIGINT NOT NULL COMMENT '岗位ID',
    match_score INT NOT NULL COMMENT '匹配分',
    skill_score DECIMAL(10,2) NULL COMMENT '技能分',
    experience_score DECIMAL(10,2) NULL COMMENT '经验分',
    education_score DECIMAL(10,2) NULL COMMENT '学历分',
    city_score DECIMAL(10,2) NULL COMMENT '城市分',
    salary_score DECIMAL(10,2) NULL COMMENT '薪资分',
    matched_skills_json TEXT NULL COMMENT '匹配技能JSON',
    missing_skills_json TEXT NULL COMMENT '缺失技能JSON',
    suggestion TEXT NULL COMMENT '建议',
    raw_result_json LONGTEXT NULL COMMENT '原始结果JSON',
    model_name VARCHAR(128) NULL COMMENT '模型名',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_job_match_record_user_id (user_id),
    KEY idx_job_match_record_resume_job (resume_id, job_id),
    KEY idx_job_match_record_score (match_score),
    CONSTRAINT fk_match_record_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_match_record_resume FOREIGN KEY (resume_id) REFERENCES resume_info (id) ON DELETE CASCADE,
    CONSTRAINT fk_match_record_job FOREIGN KEY (job_id) REFERENCES job_info (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位匹配记录表';

CREATE TABLE IF NOT EXISTS analysis_metric_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    metric_code VARCHAR(64) NOT NULL COMMENT '指标编码',
    metric_name VARCHAR(128) NOT NULL COMMENT '指标名',
    dimension_key VARCHAR(64) NULL COMMENT '维度键',
    dimension_value VARCHAR(128) NULL COMMENT '维度值',
    metric_value DECIMAL(18,2) NULL COMMENT '指标值',
    metric_text VARCHAR(255) NULL COMMENT '文本值',
    snapshot_date DATE NOT NULL COMMENT '快照日期',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_analysis_metric_code_date (metric_code, snapshot_date),
    KEY idx_analysis_metric_dimension (dimension_key, dimension_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分析指标快照表';

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NULL COMMENT '操作人',
    module_name VARCHAR(64) NOT NULL COMMENT '模块名',
    operation_type VARCHAR(64) NOT NULL COMMENT '操作类型',
    operation_desc VARCHAR(512) NULL COMMENT '操作描述',
    request_path VARCHAR(255) NULL COMMENT '请求路径',
    request_method VARCHAR(16) NULL COMMENT '请求方法',
    request_params TEXT NULL COMMENT '请求参数',
    ip_address VARCHAR(64) NULL COMMENT 'IP',
    result_status VARCHAR(32) NOT NULL COMMENT '结果状态',
    error_message TEXT NULL COMMENT '错误信息',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_operation_log_user_id (user_id),
    KEY idx_operation_log_module_type (module_name, operation_type),
    KEY idx_operation_log_created_at (created_at),
    CONSTRAINT fk_operation_log_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
