-- ============================================================
-- 网络安全等级保护测评项目管理系统 - 数据库建表脚本
-- 数据库版本: MySQL 8.0
-- 字符集: utf8mb4
-- 创建时间: 2026-03-03
-- ============================================================

CREATE DATABASE IF NOT EXISTS djbh_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE djbh_system;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 1. 数据字典类型表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_dict (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    dict_code   VARCHAR(50)     NOT NULL COMMENT '字典编码，如PROJECT_TYPE',
    dict_name   VARCHAR(100)    NOT NULL COMMENT '字典名称',
    is_system   TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否系统内置: 1是/0否',
    sort_order  INT             NOT NULL DEFAULT 0 COMMENT '排序',
    remark      VARCHAR(300)    DEFAULT NULL COMMENT '备注',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    data_hmac   VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典类型表';

-- ------------------------------------------------------------
-- 2. 数据字典项表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_dict_item (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    dict_id     BIGINT          NOT NULL COMMENT '字典类型ID',
    item_value  VARCHAR(100)    NOT NULL COMMENT '字典项值',
    item_label  VARCHAR(100)    NOT NULL COMMENT '字典项显示文本',
    sort_order  INT             NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0停用',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data_hmac   VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    KEY idx_dict_id (dict_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典项表';

-- ------------------------------------------------------------
-- 3. 系统配置表（含密钥、系统参数）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_sys_config (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    config_key      VARCHAR(100)    NOT NULL COMMENT '配置键名',
    config_value    TEXT            DEFAULT NULL COMMENT '配置值（密钥类脱敏展示）',
    config_desc     VARCHAR(300)    DEFAULT NULL COMMENT '配置说明',
    is_encrypted    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '值是否加密存储: 1是/0否',
    updated_by      BIGINT          DEFAULT NULL COMMENT '最后修改人ID',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data_hmac       VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ------------------------------------------------------------
-- 4. 角色表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_role (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_code   VARCHAR(50)     NOT NULL COMMENT '角色编码，如SUPER_ADMIN',
    role_name   VARCHAR(100)    NOT NULL COMMENT '角色名称',
    role_desc   VARCHAR(300)    DEFAULT NULL COMMENT '角色描述',
    is_system   TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否系统内置: 1是/0否',
    status      TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0停用',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data_hmac   VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ------------------------------------------------------------
-- 5. 权限表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    perm_code       VARCHAR(100)    NOT NULL COMMENT '权限编码，如project:view',
    perm_name       VARCHAR(100)    NOT NULL COMMENT '权限名称',
    module          VARCHAR(50)     NOT NULL COMMENT '所属模块',
    action          VARCHAR(50)     NOT NULL COMMENT '操作类型: view/create/update/delete/export',
    sort_order      INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ------------------------------------------------------------
-- 6. 角色权限关联表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_role_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id         BIGINT          NOT NULL COMMENT '角色ID',
    permission_id   BIGINT          NOT NULL COMMENT '权限ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ------------------------------------------------------------
-- 7. 系统用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_user (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    username            VARCHAR(50)     NOT NULL COMMENT '登录账号，唯一',
    real_name           VARCHAR(500)    NOT NULL COMMENT '真实姓名(SM4加密)',
    password_hash       VARCHAR(200)    NOT NULL COMMENT 'BCrypt哈希密码',
    role_id             BIGINT          DEFAULT NULL COMMENT '角色ID',
    phone               VARCHAR(200)    DEFAULT NULL COMMENT '手机号(SM4加密)',
    email               VARCHAR(300)    DEFAULT NULL COMMENT '邮箱(SM4加密)',
    totp_secret         VARCHAR(300)    DEFAULT NULL COMMENT 'TOTP密钥(SM4加密)',
    totp_enabled        TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否启用双因子: 0否/1是',
    status              TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0禁用',
    login_fail_count    TINYINT(4)      NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    locked_until        DATETIME        DEFAULT NULL COMMENT '账号锁定到期时间',
    last_login_at       DATETIME        DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip       VARCHAR(50)     DEFAULT NULL COMMENT '最后登录IP',
    password_changed_at DATETIME        DEFAULT NULL COMMENT '密码最后修改时间',
    first_login         TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否首次登录: 1是/0否',
    created_by          BIGINT          DEFAULT NULL COMMENT '创建人ID',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data_hmac           VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ------------------------------------------------------------
-- 8. 项目人员清单表（测评人员库）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_staff (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    staff_no        VARCHAR(50)     NOT NULL COMMENT '工号，唯一',
    real_name       VARCHAR(200)    NOT NULL COMMENT '姓名(SM4加密)',
    department      VARCHAR(200)    DEFAULT NULL COMMENT '所属部门',
    position        VARCHAR(100)    DEFAULT NULL COMMENT '人员岗位: 项目经理/项目组成员',
    role_level      VARCHAR(50)     NOT NULL COMMENT '人员角色级别: 高级测评师/中级测评师/初级测评师/渗透师',
    cert_no         VARCHAR(300)    DEFAULT NULL COMMENT '证书编号(SM4加密)',
    cert_expire     DATE            DEFAULT NULL COMMENT '证书有效期',
    phone           VARCHAR(200)    DEFAULT NULL COMMENT '联系电话(SM4加密)',
    email           VARCHAR(300)    DEFAULT NULL COMMENT '邮箱(SM4加密)',
    status          TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态: 1在职/0离职',
    created_by      BIGINT          DEFAULT NULL COMMENT '创建人',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data_hmac       VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    UNIQUE KEY uk_staff_no (staff_no),
    KEY idx_staff_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目人员清单表';

-- ------------------------------------------------------------
-- 9. 测评设备表（硬件，含使用人）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_eval_device (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    device_no       VARCHAR(50)     NOT NULL COMMENT '设备编号，如CL-CP011',
    device_name     VARCHAR(200)    NOT NULL COMMENT '设备名称',
    device_model    VARCHAR(200)    DEFAULT NULL COMMENT '型号',
    device_type     TINYINT(4)      NOT NULL COMMENT '设备类型: 1=测评专用机/2=扫描设备',
    owner_staff_id  BIGINT          DEFAULT NULL COMMENT '使用人ID，扫描设备为NULL',
    status          TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0停用',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data_hmac       VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_no (device_no),
    KEY idx_device_type (device_type),
    KEY idx_owner_staff (owner_staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评设备表';

-- ------------------------------------------------------------
-- 10. 渗透软件工具表（软件，无固定使用人）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_pentest_tool (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    tool_no         VARCHAR(50)     NOT NULL COMMENT '工具编号，如CL-RJ-07',
    tool_name       VARCHAR(200)    NOT NULL COMMENT '工具名称',
    tool_version    VARCHAR(100)    DEFAULT NULL COMMENT '版本号',
    status          TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0停用',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data_hmac       VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tool_no (tool_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渗透软件工具表';

-- ------------------------------------------------------------
-- 11. 项目主表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_project (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    project_no          VARCHAR(50)     NOT NULL COMMENT '项目编号，如CP26-0101-01',
    project_name        VARCHAR(500)    NOT NULL COMMENT '项目名称(SM4加密)',
    customer_name       VARCHAR(500)    NOT NULL COMMENT '客户名称(SM4加密)',
    customer_address    VARCHAR(1000)   DEFAULT NULL COMMENT '客户地址(SM4加密)',
    customer_contact    VARCHAR(200)    DEFAULT NULL COMMENT '联系人姓名(SM4加密)',
    customer_phone      VARCHAR(200)    DEFAULT NULL COMMENT '联系人电话(SM4加密)',
    system_name_merged  VARCHAR(1000)   NOT NULL COMMENT '系统名称合并，用于模板xtmc(SM4加密)',
    task_no             VARCHAR(500)    NOT NULL COMMENT '任务编号',
    project_type_id     BIGINT          DEFAULT NULL COMMENT '项目类型字典项ID',
    industry_id         BIGINT          DEFAULT NULL COMMENT '所属行业字典项ID',
    project_manager_id  BIGINT          DEFAULT NULL COMMENT '项目经理(人员表ID)',
    project_leader_id   BIGINT          DEFAULT NULL COMMENT '项目负责人(人员表ID)',
    contract_date       DATE            DEFAULT NULL COMMENT '合同签订日期',
    contract_amount     VARCHAR(500)    DEFAULT NULL COMMENT '合同金额(SM4加密)',
    project_status      TINYINT(4)      NOT NULL DEFAULT 0 COMMENT '状态:0待启动/1已分配/2进行中/3已完成/4电子归档',
    paper_archived      TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '纸质归档: 0否/1是',
    phase_prepare       VARCHAR(200)    DEFAULT NULL COMMENT '测评准备阶段时间段',
    phase_plan          VARCHAR(200)    DEFAULT NULL COMMENT '方案编制阶段时间段',
    phase_onsite        VARCHAR(200)    DEFAULT NULL COMMENT '现场测评阶段时间段',
    phase_report        VARCHAR(200)    DEFAULT NULL COMMENT '报告编制阶段时间段',
    task_appoint_date   DATE            DEFAULT NULL COMMENT '任务书任命表时间',
    year_belong         CHAR(4)         DEFAULT NULL COMMENT '所属年份',
    business_person     VARCHAR(200)    DEFAULT NULL COMMENT '业务人员(SM4加密)',
    project_region      VARCHAR(200)    DEFAULT NULL COMMENT '项目地区',
    remark              TEXT            DEFAULT NULL COMMENT '项目备注(SM4加密)',
    report_mail_date    DATE            DEFAULT NULL COMMENT '报告邮寄日期',
    report_mail_no      VARCHAR(200)    DEFAULT NULL COMMENT '报告邮寄单号',
    created_by          BIGINT          DEFAULT NULL COMMENT '创建人ID',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data_hmac           VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_no (project_no),
    KEY idx_project_status (project_status),
    KEY idx_project_manager (project_manager_id),
    KEY idx_project_type (project_type_id),
    KEY idx_project_industry (industry_id),
    KEY idx_year_belong (year_belong),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目主表';

-- ------------------------------------------------------------
-- 12. 被测系统子表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_project_system (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    project_id  BIGINT          NOT NULL COMMENT '关联项目ID',
    sys_seq     TINYINT(4)      NOT NULL COMMENT '系统序号: 1/2/3...',
    sys_name    VARCHAR(500)    NOT NULL COMMENT '系统名称(SM4加密)',
    sys_level   TINYINT(4)      NOT NULL COMMENT '系统等级: 2/3/4',
    eval_index  VARCHAR(50)     NOT NULL COMMENT '测评指标，如S3A3G3',
    record_no   VARCHAR(200)    DEFAULT NULL COMMENT '备案号',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data_hmac   VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    KEY idx_sys_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='被测系统子表';

-- ------------------------------------------------------------
-- 13. 项目人员关联表（项目-人员-角色）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_project_member (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    project_id  BIGINT          NOT NULL COMMENT '关联项目ID',
    member_id   BIGINT          NOT NULL COMMENT '关联人员清单ID',
    role_type   VARCHAR(50)     NOT NULL COMMENT '角色类型枚举',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_hmac   VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_member_role (project_id, member_id, role_type),
    KEY idx_member_project (project_id),
    KEY idx_member_staff (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目人员关联表';

-- ------------------------------------------------------------
-- 14. 归档模板表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_archive_template (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    template_code       VARCHAR(100)    NOT NULL COMMENT '模板唯一编码，如PLAN_ELECTRIC_L3',
    template_name       VARCHAR(300)    NOT NULL COMMENT '模板名称',
    template_category   VARCHAR(50)     NOT NULL COMMENT '分类: 项目启动/调研信息/现场测评/报告编制/验收总结',
    applicable_type     VARCHAR(50)     DEFAULT NULL COMMENT '适用项目类型: 电力/外围/通用',
    applicable_level    TINYINT(4)      DEFAULT NULL COMMENT '适用等级: 2/3/4, NULL=通用',
    file_path           VARCHAR(500)    NOT NULL COMMENT '服务器存储路径',
    file_original_name  VARCHAR(300)    NOT NULL COMMENT '原始文件名',
    file_size           BIGINT          DEFAULT NULL COMMENT '文件大小(字节)',
    version             VARCHAR(20)     NOT NULL DEFAULT 'v1.0' COMMENT '版本号',
    is_default          TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '默认选中: 1是/0否',
    status              TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '状态: 1启用/0停用',
    upload_by           BIGINT          DEFAULT NULL COMMENT '上传人ID',
    upload_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    data_hmac           VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    UNIQUE KEY uk_template_code (template_code),
    KEY idx_template_status (status),
    KEY idx_template_category (template_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归档模板表';

-- ------------------------------------------------------------
-- 15. 归档记录表（记录每次归档操作）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_archive_record (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    project_id      BIGINT          NOT NULL COMMENT '关联项目ID',
    archive_name    VARCHAR(300)    NOT NULL COMMENT 'ZIP文件名',
    file_path       VARCHAR(500)    NOT NULL COMMENT '归档文件存储路径',
    templates_used  TEXT            DEFAULT NULL COMMENT '使用的模板ID列表(JSON)',
    created_by      BIGINT          DEFAULT NULL COMMENT '操作人ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    expire_at       DATETIME        DEFAULT NULL COMMENT '下载链接过期时间',
    data_hmac       VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    KEY idx_archive_project (project_id),
    KEY idx_archive_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='归档记录表';

-- ------------------------------------------------------------
-- 16. 登录日志表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_login_log (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    username        VARCHAR(50)     NOT NULL COMMENT '登录账号',
    real_name       VARCHAR(500)    DEFAULT NULL COMMENT '用户姓名(SM4加密)',
    login_ip        VARCHAR(50)     DEFAULT NULL COMMENT '登录IP',
    login_location  VARCHAR(200)    DEFAULT NULL COMMENT 'IP解析地理位置',
    browser         VARCHAR(200)    DEFAULT NULL COMMENT '浏览器信息',
    login_status    TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '登录状态: 1成功/0失败',
    fail_reason     VARCHAR(200)    DEFAULT NULL COMMENT '失败原因',
    login_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    logout_at       DATETIME        DEFAULT NULL COMMENT '退出时间',
    session_duration INT            DEFAULT NULL COMMENT '会话时长(秒)',
    data_hmac       VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    KEY idx_login_username (username),
    KEY idx_login_at (login_at),
    KEY idx_login_status (login_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ------------------------------------------------------------
-- 17. 操作日志表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_operation_log (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT          DEFAULT NULL COMMENT '操作人ID',
    username        VARCHAR(50)     DEFAULT NULL COMMENT '操作人账号',
    module          VARCHAR(50)     DEFAULT NULL COMMENT '功能模块',
    action_type     VARCHAR(50)     DEFAULT NULL COMMENT '操作类型: CREATE/UPDATE/DELETE/VIEW/EXPORT/IMPORT/ARCHIVE',
    action_desc     VARCHAR(500)    DEFAULT NULL COMMENT '操作描述',
    target_id       BIGINT          DEFAULT NULL COMMENT '操作对象ID',
    target_type     VARCHAR(50)     DEFAULT NULL COMMENT '操作对象类型',
    request_params  TEXT            DEFAULT NULL COMMENT '请求参数(脱敏)',
    op_result       TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '操作结果: 1成功/0失败',
    fail_reason     VARCHAR(500)    DEFAULT NULL COMMENT '失败原因',
    op_ip           VARCHAR(50)     DEFAULT NULL COMMENT '操作IP',
    op_at           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    data_hmac       VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'HMAC-SM3完整性校验值',
    PRIMARY KEY (id),
    KEY idx_op_user (user_id),
    KEY idx_op_at (op_at),
    KEY idx_op_module (module),
    KEY idx_op_type (action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ------------------------------------------------------------
-- 18. 用户个人偏好表（自定义列配置）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_user_preference (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT          NOT NULL COMMENT '用户ID',
    pref_key    VARCHAR(100)    NOT NULL COMMENT '配置键，如project_list_columns',
    pref_value  TEXT            DEFAULT NULL COMMENT '配置值(JSON)',
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_pref (user_id, pref_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户个人偏好表';

SET FOREIGN_KEY_CHECKS = 1;
