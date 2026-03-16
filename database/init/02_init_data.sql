-- ============================================================
-- 初始化数据脚本
-- ============================================================
USE djbh_system;

-- ------------------------------------------------------------
-- 初始化字典类型
-- ------------------------------------------------------------
INSERT INTO t_dict (dict_code, dict_name, is_system, sort_order) VALUES
('PROJECT_TYPE', '项目类型', 1, 1),
('INDUSTRY',     '所属行业', 1, 2),
('SYS_LEVEL',    '系统等级', 1, 3),
('EVAL_INDEX',   '测评指标', 1, 4),
('STAFF_LEVEL',  '人员角色级别', 1, 5),
('PROJECT_STATUS','项目状态', 1, 6);

-- ------------------------------------------------------------
-- 初始化字典项 - 项目类型
-- ------------------------------------------------------------
INSERT INTO t_dict_item (dict_id, item_value, item_label, sort_order) VALUES
((SELECT id FROM t_dict WHERE dict_code='PROJECT_TYPE'), 'ELECTRIC', '电力', 1),
((SELECT id FROM t_dict WHERE dict_code='PROJECT_TYPE'), 'PERIPHERAL', '外围', 2);

-- ------------------------------------------------------------
-- 初始化字典项 - 所属行业
-- ------------------------------------------------------------
INSERT INTO t_dict_item (dict_id, item_value, item_label, sort_order) VALUES
((SELECT id FROM t_dict WHERE dict_code='INDUSTRY'), 'ELECTRIC',    '电力',   1),
((SELECT id FROM t_dict WHERE dict_code='INDUSTRY'), 'MEDICAL',     '医疗',   2),
((SELECT id FROM t_dict WHERE dict_code='INDUSTRY'), 'GOVERNMENT',  '政府',   3),
((SELECT id FROM t_dict WHERE dict_code='INDUSTRY'), 'FINANCE',     '金融',   4),
((SELECT id FROM t_dict WHERE dict_code='INDUSTRY'), 'TRANSPORT',   '交通',   5),
((SELECT id FROM t_dict WHERE dict_code='INDUSTRY'), 'EDUCATION',   '教育',   6),
((SELECT id FROM t_dict WHERE dict_code='INDUSTRY'), 'OTHER',       '其他',   7);

-- ------------------------------------------------------------
-- 初始化字典项 - 系统等级
-- ------------------------------------------------------------
INSERT INTO t_dict_item (dict_id, item_value, item_label, sort_order) VALUES
((SELECT id FROM t_dict WHERE dict_code='SYS_LEVEL'), '2', '二级', 1),
((SELECT id FROM t_dict WHERE dict_code='SYS_LEVEL'), '3', '三级', 2),
((SELECT id FROM t_dict WHERE dict_code='SYS_LEVEL'), '4', '四级', 3);

-- ------------------------------------------------------------
-- 初始化字典项 - 人员角色级别
-- ------------------------------------------------------------
INSERT INTO t_dict_item (dict_id, item_value, item_label, sort_order) VALUES
((SELECT id FROM t_dict WHERE dict_code='STAFF_LEVEL'), 'SENIOR',    '高级测评师', 1),
((SELECT id FROM t_dict WHERE dict_code='STAFF_LEVEL'), 'MIDDLE',    '中级测评师', 2),
((SELECT id FROM t_dict WHERE dict_code='STAFF_LEVEL'), 'JUNIOR',    '初级测评师', 3),
((SELECT id FROM t_dict WHERE dict_code='STAFF_LEVEL'), 'PENTEST',   '渗透师',     4);

-- ------------------------------------------------------------
-- 初始化系统配置（密钥类初始为空，首次启动强制设置）
-- ------------------------------------------------------------
INSERT INTO t_sys_config (config_key, config_value, config_desc, is_encrypted) VALUES
('SM4_KEY',                 '',       'SM4加密主密钥（32位十六进制，首次启动必须设置）', 1),
('HMAC_KEY',                '',       'HMAC-SM3密钥（首次启动必须设置）', 1),
('PASSWORD_MIN_LENGTH',     '8',      '密码最小长度', 0),
('PASSWORD_REQUIRE_UPPER',  'true',   '密码必须含大写字母', 0),
('PASSWORD_REQUIRE_LOWER',  'true',   '密码必须含小写字母', 0),
('PASSWORD_REQUIRE_NUMBER', 'true',   '密码必须含数字', 0),
('PASSWORD_REQUIRE_SPECIAL','false',  '密码必须含特殊字符', 0),
('PASSWORD_EXPIRE_DAYS',    '90',     '密码有效期（天），0=永不过期', 0),
('LOGIN_MAX_FAIL',          '5',      '最大登录失败次数', 0),
('LOGIN_LOCK_MINUTES',      '30',     '账号锁定时长（分钟），0=需手动解锁', 0),
('LOGIN_CAPTCHA_ENABLED',   'true',   '是否启用图形验证码', 0),
('LOGIN_CAPTCHA_FAIL_COUNT','3',      'N次失败后显示验证码', 0),
('SESSION_TIMEOUT_MINUTES', '30',     '会话超时时长（分钟）', 0),
('SESSION_WARN_MINUTES',    '5',      '超时前弹窗提醒时间（分钟）', 0),
('ADMIN_IP_WHITELIST',      '',       '管理员登录IP白名单（逗号分隔，空=不限制）', 0),
('SYSTEM_NAME',             '网络安全等级保护测评项目管理系统', '系统名称', 0),
('LOG_RETAIN_DAYS',         '180',    '本地日志保留天数', 0),
('SYSLOG_ENABLED',          'false',  '是否启用远程日志同步', 0),
('SYSLOG_SERVER',           '',       '日志服务器地址', 0),
('SYSLOG_PORT',             '514',    '日志服务器端口', 0),
('SYSLOG_PROTOCOL',         'UDP',    '日志服务器协议: UDP/TCP/TLS', 0),
('SYSLOG_FORMAT',           'RFC5424','日志格式', 0);

-- ------------------------------------------------------------
-- 初始化角色
-- ------------------------------------------------------------
INSERT INTO t_role (role_code, role_name, role_desc, is_system, status) VALUES
('SUPER_ADMIN',     '超级管理员', '系统全部权限，不可删除', 1, 1),
('PROJECT_MANAGER', '项目管理员', '可新增项目、编辑所有项目、归档管理', 1, 1),
('NORMAL',          '普通角色',   '可新增项目、查看所有项目、编辑自己创建的项目', 1, 1),
('SECURITY_ADMIN',  '安全管理员', '可查看项目、配置系统安全设置', 1, 1),
('LOG_ADMIN',       '日志管理员', '可查看日志、配置日志服务器', 1, 1);

-- ------------------------------------------------------------
-- 初始化权限数据
-- ------------------------------------------------------------
INSERT INTO t_permission (perm_code, perm_name, module, action, sort_order) VALUES
-- 项目管理
('project:view:all',      '查看所有项目',       'project', 'view',   1),
('project:view:own',      '查看参与的项目',     'project', 'view',   2),
('project:create',        '新增项目',           'project', 'create', 3),
('project:update:all',    '编辑所有项目',       'project', 'update', 4),
('project:update:own',    '编辑自己创建的项目', 'project', 'update', 5),
('project:delete',        '删除项目',           'project', 'delete', 6),
('project:export',        '导出项目',           'project', 'export', 7),
('project:stats',         '项目统计',           'project', 'view',   8),
-- 归档管理
('archive:create',        '生成归档材料',       'archive', 'create', 10),
('archive:download',      '下载归档材料',       'archive', 'export', 11),
('archive:template',      '管理归档模板',       'archive', 'update', 12),
-- 系统设置
('system:config',         '系统参数设置',       'system',  'update', 20),
('system:user',           '管理员账号管理',     'system',  'update', 21),
('system:role',           '角色权限管理',       'system',  'update', 22),
('system:staff',          '项目人员清单管理',   'system',  'update', 23),
('system:device',         '测评工具清单管理',   'system',  'update', 24),
('system:dict',           '字典管理',           'system',  'update', 25),
('system:key',            '密钥管理',           'system',  'update', 26),
-- 日志管理
('log:login:view',        '查看登录日志',       'log',     'view',   30),
('log:operation:view',    '查看操作日志',       'log',     'view',   31),
('log:server:config',     '日志服务器配置',     'log',     'update', 32);

-- ------------------------------------------------------------
-- 分配权限给各角色（超级管理员拥有全部权限）
-- ------------------------------------------------------------
-- 超级管理员 - 全部权限
INSERT INTO t_role_permission (role_id, permission_id)
SELECT (SELECT id FROM t_role WHERE role_code='SUPER_ADMIN'), id FROM t_permission;

-- 项目管理员
INSERT INTO t_role_permission (role_id, permission_id)
SELECT (SELECT id FROM t_role WHERE role_code='PROJECT_MANAGER'), id FROM t_permission
WHERE perm_code IN ('project:view:all','project:create','project:update:all','project:delete',
                    'project:export','project:stats','archive:create','archive:download',
                    'archive:template','system:staff','system:device');

-- 普通角色
INSERT INTO t_role_permission (role_id, permission_id)
SELECT (SELECT id FROM t_role WHERE role_code='NORMAL'), id FROM t_permission
WHERE perm_code IN ('project:view:own','project:create','project:update:own');

-- 安全管理员
INSERT INTO t_role_permission (role_id, permission_id)
SELECT (SELECT id FROM t_role WHERE role_code='SECURITY_ADMIN'), id FROM t_permission
WHERE perm_code IN ('project:view:all','system:config','system:user','system:role',
                    'system:dict','system:key','log:login:view','log:operation:view');

-- 日志管理员
INSERT INTO t_role_permission (role_id, permission_id)
SELECT (SELECT id FROM t_role WHERE role_code='LOG_ADMIN'), id FROM t_permission
WHERE perm_code IN ('log:login:view','log:operation:view','log:server:config');

-- ------------------------------------------------------------
-- 初始化超级管理员账号（密码: Admin@123456，BCrypt哈希）
-- 首次登录必须修改密码
-- ------------------------------------------------------------
INSERT INTO t_user (username, real_name, password_hash, role_id, status, first_login, created_at)
VALUES (
    'admin',
    '85TWKWSVvwFmLdGJwkjiLw==',  -- SM4加密的"超级管理员"（默认开发密钥djbhdefaultkey00）
    '$2a$10$k3x.4c6z8eKrJhOSGRmmCezdVMFvyoJmRxlOqebirthBMiMDE3ZHa',  -- Admin@123456
    (SELECT id FROM t_role WHERE role_code='SUPER_ADMIN'),
    1, 1, NOW()
);

-- ------------------------------------------------------------
-- 初始化测评设备（从Excel导入的真实数据）
-- ------------------------------------------------------------
INSERT INTO t_eval_device (device_no, device_name, device_model, device_type, owner_staff_id) VALUES
('CL-CP002', '绿盟远程安全评估系统',         'RSAS NX3',              2, NULL),
('CL-CP003', '铱迅漏洞扫描系统',             'Yxlink NVS-H7900(P)',   2, NULL),
('CL-CP004', '绿盟工控漏洞扫描系统',         'ICSScan NX3',           2, NULL),
('CL-CP005', '天融信脆弱性扫描与管理系统',   'TopScanner7000',        2, NULL),
('CL-CP006', '绿盟虚拟远程安全评估系统',     'RSAS NX3-VM',           2, NULL),
('CL-CP007', '启明星辰天镜脆弱性扫描与管理系统', 'TJCS-UVS3000P',    2, NULL);
-- 注：测评专用机(CL-CP011~CL-CP044)在人员导入后通过系统界面录入，owner_staff_id关联具体人员

-- ------------------------------------------------------------
-- 初始化渗透软件工具
-- ------------------------------------------------------------
INSERT INTO t_pentest_tool (tool_no, tool_name, tool_version) VALUES
('CL-RJ-07', 'Burpsuite',  '社区版2020.9'),
('CL-RJ-08', 'nmap',       '7.91'),
('CL-RJ-09', 'sqlmap',     '1.5'),
('CL-RJ-10', '中国蚁剑',   '2.1.8.1'),
('CL-RJ-11', 'Goby',       'V2.7.1beta'),
('CL-RJ-12', 'hydra',      '9.5'),
('CL-RJ-13', 'MSF',        '6.3.34'),
('CL-RJ-14', 'Godzila',    '4.01'),
('CL-RJ-15', 'Behinder',   '4.1'),
('CL-RJ-16', 'Xray',       '1.9.11'),
('CL-RJ-17', 'Yakit',      '1.2.5');

-- ------------------------------------------------------------
-- 初始化测评专用机（个人配备，CL-CP011~CL-CP044）
-- ------------------------------------------------------------
INSERT INTO t_eval_device (device_no, device_name, device_model, device_type, status, data_hmac) VALUES
('CL-CP011', '测评专用机', 'Lenovo ThinkPad E570', 1, 1, ''),
('CL-CP012', '测评专用机', 'Lenovo ThinkPad E570', 1, 1, ''),
('CL-CP013', '测评专用机', 'Lenovo ThinkPad E570', 1, 1, ''),
('CL-CP014', '测评专用机', 'Lenovo ThinkPad E570', 1, 1, ''),
('CL-CP015', '测评专用机', 'Lenovo ThinkPad E570', 1, 1, ''),
('CL-CP016', '测评专用机', 'Lenovo ThinkPad E570', 1, 1, ''),
('CL-CP017', '测评专用机', 'Lenovo ThinkPad E570', 1, 1, ''),
('CL-CP018', '测评专用机', 'Huawei MatebookD15', 1, 1, ''),
('CL-CP019', '测评专用机', 'Huawei MatebookD15', 1, 1, ''),
('CL-CP020', '测评专用机', 'Huawei MatebookD15', 1, 1, ''),
('CL-CP021', '测评专用机', 'Huawei MatebookD15', 1, 1, ''),
('CL-CP022', '测评专用机', 'Huawei MatebookD15', 1, 1, ''),
('CL-CP024', '测评专用机', 'Huawei MatebookD15', 1, 1, ''),
('CL-CP025', '测评专用机', 'Huawei MatebookD15', 1, 1, ''),
('CL-CP026', '测评专用机', 'ThinkBook16+', 1, 1, ''),
('CL-CP027', '测评专用机', 'ThinkBook16+', 1, 1, ''),
('CL-CP028', '测评专用机', 'ThinkBook16+', 1, 1, ''),
('CL-CP029', '测评专用机', 'ThinkBook16+', 1, 1, ''),
('CL-CP030', '测评专用机', 'ThinkBook16+', 1, 1, ''),
('CL-CP031', '测评专用机', 'ThinkBook16+', 1, 1, ''),
('CL-CP032', '测评专用机', 'ThinkBook16+', 1, 1, ''),
('CL-CP033', '测评专用机', 'ThinkBook16G7+ IAH', 1, 1, ''),
('CL-CP034', '测评专用机', 'ThinkBook16G7+ IAH', 1, 1, ''),
('CL-CP035', '测评专用机', 'ThinkBook16G7+ IAH', 1, 1, ''),
('CL-CP036', '测评专用机', 'ThinkBook16G7+ IAH', 1, 1, ''),
('CL-CP037', '测评专用机', 'ThinkBook16G7+ IAH', 1, 1, ''),
('CL-CP038', '测评专用机', 'ThinkBook16G7+ IAH', 1, 1, ''),
('CL-CP039', '测评专用机', 'ThinkBook16G7+ IAH', 1, 1, ''),
('CL-CP040', '测评专用机', 'ThinkBook16G7+ IAH', 1, 1, ''),
('CL-CP041', '测评专用机', 'ThinkBook16G7+ IAH', 1, 1, ''),
('CL-CP042', '测评专用机', 'ThinkBook16G7+ IAH', 1, 1, ''),
('CL-CP043', '测评专用机', 'ThinkBook16G7+ IAH', 1, 1, ''),
('CL-CP044', '测评专用机', 'ThinkBook16G7+ IAH', 1, 1, '');

-- ------------------------------------------------------------
-- 初始化归档模板记录（文件需部署到 ./data/templates/ 目录）
-- ------------------------------------------------------------
INSERT INTO t_archive_template (template_code, template_name, template_category, file_original_name, file_path, file_size, version, is_default, status, upload_by, upload_at, data_hmac)
VALUES
('project_plan_electric_2', '项目计划书-电力二级', '项目计划书', '2-xmbh-项目计划书-电力二级.docx', './data/templates/tpl_project_plan_electric_2.docx', 68730, 'v1.0', 1, 1, 1, NOW(), ''),
('project_plan_electric_3', '项目计划书-电力三级', '项目计划书', '2-xmbh-项目计划书-电力三级.docx', './data/templates/tpl_project_plan_electric_3.docx', 69020, 'v1.0', 1, 1, 1, NOW(), ''),
('project_plan_peripheral_2', '项目计划书-外围二级', '项目计划书', '2-xmbh-项目计划书-外围二级.docx', './data/templates/tpl_project_plan_peripheral_2.docx', 68675, 'v1.0', 1, 1, 1, NOW(), ''),
('project_plan_peripheral_3', '项目计划书-外围三级', '项目计划书', '2-xmbh-项目计划书-外围三级.docx', './data/templates/tpl_project_plan_peripheral_3.docx', 69239, 'v1.0', 1, 1, 1, NOW(), ''),
('task_dengbao', '项目任务书-等保测评服务', '项目任务书', '2-xmbh-项目任务书-等保测评服务.docx', './data/templates/tpl_task_dengbao.docx', 23004, 'v1.0', 1, 1, 1, NOW(), ''),
('task_dengbao_eval', '项目任务书-等保及评估服务', '项目任务书', '2-xmbh-项目任务书-等保及评估服务.docx', './data/templates/tpl_task_dengbao_eval.docx', 23146, 'v1.0', 1, 1, 1, NOW(), ''),
('pm_appointment', '项目经理任命审批表', '审批表', '2-xmbh-项目经理任命审批表.docx', './data/templates/tpl_pm_appointment.docx', 23281, 'v1.0', 1, 1, 1, NOW(), ''),
('tool_list', '测评工具清单', '工具清单', '2-xmbh-测评工具清单.docx', './data/templates/tpl_tool_list.docx', 26741, 'v1.0', 1, 1, 1, NOW(), ''),
('meeting_minutes', '会议纪要及相关文件', '会议纪要', '5-xmbh会议纪要等.docx', './data/templates/tpl_meeting_minutes.docx', 26859, 'v1.0', 1, 1, 1, NOW(), ''),
('confidential_agreement', '保密协议及相关文件', '保密协议', '5-xmbh保密协议等.docx', './data/templates/tpl_confidential_agreement.docx', 61837, 'v1.0', 1, 1, 1, NOW(), '');
