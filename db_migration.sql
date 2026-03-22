-- ============================================================
-- 数据库增量迁移脚本
-- 适用于：将您自己的环境同步到最新沙盒状态
-- 执行方式：mysql -u root -p您的密码 您的数据库名 --default-character-set=utf8mb4 < db_migration.sql
-- 说明：所有操作均为幂等操作（重复执行不会报错）
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================================
-- 变更1：t_project_system 新增4个字段
-- （编写人、审核人员、报告结论、质量审核得分）
-- ============================================================

-- 新增 writer_id 字段（编写人，关联t_staff.id）
ALTER TABLE `t_project_system`
  ADD COLUMN IF NOT EXISTS `writer_id` bigint(20) DEFAULT NULL COMMENT '编写人ID，关联t_staff.id';

-- 新增 reviewer_id 字段（审核人员，关联t_staff.id）
ALTER TABLE `t_project_system`
  ADD COLUMN IF NOT EXISTS `reviewer_id` bigint(20) DEFAULT NULL COMMENT '审核人员ID，关联t_staff.id';

-- 新增 report_conclusion 字段（报告结论）
ALTER TABLE `t_project_system`
  ADD COLUMN IF NOT EXISTS `report_conclusion` varchar(50) DEFAULT NULL COMMENT '报告结论，字典值：FH/JBFH/BFH';

-- 新增 quality_score 字段（质量审核得分）
ALTER TABLE `t_project_system`
  ADD COLUMN IF NOT EXISTS `quality_score` int(11) DEFAULT NULL COMMENT '质量审核得分，0-100';

-- ============================================================
-- 变更2：新增 REPORT_CONCLUSION 数据字典
-- ============================================================

-- 新增字典类型（幂等：若已存在则跳过）
INSERT INTO `t_dict` (`dict_code`, `dict_name`, `is_system`, `sort_order`, `remark`)
SELECT 'REPORT_CONCLUSION', '报告结论', 1, 50, NULL
WHERE NOT EXISTS (
  SELECT 1 FROM `t_dict` WHERE `dict_code` = 'REPORT_CONCLUSION'
);

-- 新增字典项：符合
INSERT INTO `t_dict_item` (`dict_id`, `item_value`, `item_label`, `sort_order`, `status`)
SELECT id, 'FH', '符合', 1, 1
FROM `t_dict` WHERE `dict_code` = 'REPORT_CONCLUSION'
  AND NOT EXISTS (
    SELECT 1 FROM `t_dict_item` di
    JOIN `t_dict` d ON di.dict_id = d.id
    WHERE d.dict_code = 'REPORT_CONCLUSION' AND di.item_value = 'FH'
  );

-- 新增字典项：基本符合
INSERT INTO `t_dict_item` (`dict_id`, `item_value`, `item_label`, `sort_order`, `status`)
SELECT id, 'JBFH', '基本符合', 2, 1
FROM `t_dict` WHERE `dict_code` = 'REPORT_CONCLUSION'
  AND NOT EXISTS (
    SELECT 1 FROM `t_dict_item` di
    JOIN `t_dict` d ON di.dict_id = d.id
    WHERE d.dict_code = 'REPORT_CONCLUSION' AND di.item_value = 'JBFH'
  );

-- 新增字典项：不符合
INSERT INTO `t_dict_item` (`dict_id`, `item_value`, `item_label`, `sort_order`, `status`)
SELECT id, 'BFH', '不符合', 3, 1
FROM `t_dict` WHERE `dict_code` = 'REPORT_CONCLUSION'
  AND NOT EXISTS (
    SELECT 1 FROM `t_dict_item` di
    JOIN `t_dict` d ON di.dict_id = d.id
    WHERE d.dict_code = 'REPORT_CONCLUSION' AND di.item_value = 'BFH'
  );

-- ============================================================
-- 变更3：新增 SYS_ADMIN（系统管理员）角色
-- ============================================================

-- 新增角色（幂等：若 role_code 已存在则跳过）
INSERT INTO `t_role` (`role_code`, `role_name`, `role_desc`, `is_system`, `status`)
SELECT 'SYS_ADMIN', '系统管理员', '系统管理员，仅管理系统设置相关功能', 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `t_role` WHERE `role_code` = 'SYS_ADMIN'
);

-- 确保角色权限关联表存在（如不存在则创建）
CREATE TABLE IF NOT EXISTS `t_role_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色权限关联表';

-- 为 SYS_ADMIN 分配5个权限（幂等插入）
-- 权限：system:user（管理员账号管理）
INSERT INTO `t_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `t_role` r, `t_permission` p
WHERE r.role_code = 'SYS_ADMIN' AND p.perm_code = 'system:user'
  AND NOT EXISTS (
    SELECT 1 FROM `t_role_permission` rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 权限：system:staff（项目人员清单管理）
INSERT INTO `t_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `t_role` r, `t_permission` p
WHERE r.role_code = 'SYS_ADMIN' AND p.perm_code = 'system:staff'
  AND NOT EXISTS (
    SELECT 1 FROM `t_role_permission` rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 权限：system:dict（字典管理）
INSERT INTO `t_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `t_role` r, `t_permission` p
WHERE r.role_code = 'SYS_ADMIN' AND p.perm_code = 'system:dict'
  AND NOT EXISTS (
    SELECT 1 FROM `t_role_permission` rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 权限：archive:template（管理归档模板）
INSERT INTO `t_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `t_role` r, `t_permission` p
WHERE r.role_code = 'SYS_ADMIN' AND p.perm_code = 'archive:template'
  AND NOT EXISTS (
    SELECT 1 FROM `t_role_permission` rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 权限：system:role（角色权限管理）
INSERT INTO `t_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `t_role` r, `t_permission` p
WHERE r.role_code = 'SYS_ADMIN' AND p.perm_code = 'system:role'
  AND NOT EXISTS (
    SELECT 1 FROM `t_role_permission` rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- ============================================================
-- 变更4：修复 t_permission.perm_name 中文乱码
-- （将所有权限名称更新为正确的UTF-8中文）
-- ============================================================

UPDATE `t_permission` SET `perm_name` = '查看所有项目'        WHERE `perm_code` = 'project:view:all';
UPDATE `t_permission` SET `perm_name` = '查看参与的项目'      WHERE `perm_code` = 'project:view:own';
UPDATE `t_permission` SET `perm_name` = '新增项目'            WHERE `perm_code` = 'project:create';
UPDATE `t_permission` SET `perm_name` = '编辑所有项目'        WHERE `perm_code` = 'project:update:all';
UPDATE `t_permission` SET `perm_name` = '编辑自己创建的项目'  WHERE `perm_code` = 'project:update:own';
UPDATE `t_permission` SET `perm_name` = '删除项目'            WHERE `perm_code` = 'project:delete';
UPDATE `t_permission` SET `perm_name` = '导出项目'            WHERE `perm_code` = 'project:export';
UPDATE `t_permission` SET `perm_name` = '项目统计'            WHERE `perm_code` = 'project:stats';
UPDATE `t_permission` SET `perm_name` = '创建归档材料'        WHERE `perm_code` = 'archive:create';
UPDATE `t_permission` SET `perm_name` = '下载归档材料'        WHERE `perm_code` = 'archive:download';
UPDATE `t_permission` SET `perm_name` = '管理归档模板'        WHERE `perm_code` = 'archive:template';
UPDATE `t_permission` SET `perm_name` = '系统参数设置'        WHERE `perm_code` = 'system:config';
UPDATE `t_permission` SET `perm_name` = '管理员账号管理'      WHERE `perm_code` = 'system:user';
UPDATE `t_permission` SET `perm_name` = '角色权限管理'        WHERE `perm_code` = 'system:role';
UPDATE `t_permission` SET `perm_name` = '项目人员清单管理'    WHERE `perm_code` = 'system:staff';
UPDATE `t_permission` SET `perm_name` = '测评工具管理'        WHERE `perm_code` = 'system:device';
UPDATE `t_permission` SET `perm_name` = '字典管理'            WHERE `perm_code` = 'system:dict';
UPDATE `t_permission` SET `perm_name` = '密钥管理'            WHERE `perm_code` = 'system:key';
UPDATE `t_permission` SET `perm_name` = '查看登录日志'        WHERE `perm_code` = 'log:login:view';
UPDATE `t_permission` SET `perm_name` = '查看操作日志'        WHERE `perm_code` = 'log:operation:view';
UPDATE `t_permission` SET `perm_name` = '日志服务器配置'      WHERE `perm_code` = 'log:server:config';

-- ============================================================
-- 变更5：确保 t_role_permission 表存在（兼容旧环境）
-- （若您环境中已有此表，以下语句无影响）
-- ============================================================
-- 已在变更3中包含 CREATE TABLE IF NOT EXISTS

-- ============================================================
-- 验证查询（执行后请检查输出是否符合预期）
-- ============================================================

SELECT '=== t_project_system 新字段验证 ===' AS info;
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 't_project_system'
  AND COLUMN_NAME IN ('writer_id','reviewer_id','report_conclusion','quality_score');

SELECT '=== REPORT_CONCLUSION 字典验证 ===' AS info;
SELECT d.dict_code, d.dict_name, di.item_value, di.item_label
FROM t_dict d JOIN t_dict_item di ON d.id = di.dict_id
WHERE d.dict_code = 'REPORT_CONCLUSION';

SELECT '=== SYS_ADMIN 角色及权限验证 ===' AS info;
SELECT r.role_code, r.role_name, p.perm_code, p.perm_name
FROM t_role r
JOIN t_role_permission rp ON r.id = rp.role_id
JOIN t_permission p ON rp.permission_id = p.id
WHERE r.role_code = 'SYS_ADMIN'
ORDER BY p.perm_code;

SELECT '=== t_permission 权限名称验证（前5条） ===' AS info;
SELECT perm_code, perm_name FROM t_permission ORDER BY id LIMIT 5;

SELECT '=== 迁移完成 ===' AS info;
