-- ============================================================
--  设备进出库管理系统 - 完整初始化脚本
--  适配：若依框架（RuoYi-Vue） + MySQL 8.0
--  数据库：ry-vue
-- ============================================================

USE `ry-vue`;

-- ============================================================
--  一、业务表建立
-- ============================================================

-- 1. 员工表
CREATE TABLE IF NOT EXISTS `dev_employee` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(50)  NOT NULL                COMMENT '员工姓名',
  `department`  VARCHAR(100)                         COMMENT '所属部门',
  `phone_total` INT          NOT NULL DEFAULT 0      COMMENT '登记持有手机总数',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
  `del_flag`    CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志(0正常 2删除)',
  `create_by`   VARCHAR(64)           DEFAULT ''     COMMENT '创建者',
  `create_time` DATETIME              DEFAULT NULL   COMMENT '创建时间',
  `update_by`   VARCHAR(64)           DEFAULT ''     COMMENT '更新者',
  `update_time` DATETIME              DEFAULT NULL   COMMENT '更新时间',
  `remark`      VARCHAR(500)          DEFAULT NULL   COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备管理-员工表';

-- 2. 账号平台配置表
CREATE TABLE IF NOT EXISTS `dev_account_platform` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code`             VARCHAR(50)  NOT NULL                COMMENT '平台唯一编码',
  `label`            VARCHAR(50)  NOT NULL                COMMENT '平台显示名称',
  `color`            VARCHAR(20)  NOT NULL DEFAULT '#94a3b8' COMMENT '十六进制颜色值，如 #3b82f6',
  `active_aliases`   VARCHAR(500) NOT NULL DEFAULT '[]'   COMMENT '在用关键词JSON数组',
  `disabled_aliases` VARCHAR(500) NOT NULL DEFAULT '[]'   COMMENT '不能用关键词JSON数组',
  `posting_aliases`  VARCHAR(500) NOT NULL DEFAULT '[]'   COMMENT '发作品关键词JSON数组',
  `sort`             INT          NOT NULL DEFAULT 0      COMMENT '排序',
  `del_flag`         CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志',
  `create_by`        VARCHAR(64)           DEFAULT ''     COMMENT '创建者',
  `create_time`      DATETIME              DEFAULT NULL   COMMENT '创建时间',
  `update_by`        VARCHAR(64)           DEFAULT ''     COMMENT '更新者',
  `update_time`      DATETIME              DEFAULT NULL   COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备管理-账号平台配置表';

-- 3. 进出库原因分类表
CREATE TABLE IF NOT EXISTS `dev_inout_category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(50)  NOT NULL                COMMENT '原因名称',
  `type`        VARCHAR(10)  NOT NULL DEFAULT 'both' COMMENT '类型(in/out/both)',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
  `del_flag`    CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志',
  `create_by`   VARCHAR(64)           DEFAULT ''     COMMENT '创建者',
  `create_time` DATETIME              DEFAULT NULL   COMMENT '创建时间',
  `update_by`   VARCHAR(64)           DEFAULT ''     COMMENT '更新者',
  `update_time` DATETIME              DEFAULT NULL   COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备管理-进出库原因分类表';

-- 4. 库房分类表
CREATE TABLE IF NOT EXISTS `dev_warehouse_category` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(50)  NOT NULL                COMMENT '分类名称',
  `type`        VARCHAR(30)  NOT NULL DEFAULT 'custom' COMMENT '类型(work_account/intercept_account/xiaohongshu/abnormal/pending_online/maintenance/custom)',
  `color`       VARCHAR(20)  NOT NULL DEFAULT '#3b82f6' COMMENT '十六进制颜色值，如 #3b82f6',
  `sort`        INT          NOT NULL DEFAULT 0       COMMENT '排序',
  `del_flag`    CHAR(1)      NOT NULL DEFAULT '0'     COMMENT '删除标志',
  `create_by`   VARCHAR(64)           DEFAULT ''      COMMENT '创建者',
  `create_time` DATETIME              DEFAULT NULL    COMMENT '创建时间',
  `update_by`   VARCHAR(64)           DEFAULT ''      COMMENT '更新者',
  `update_time` DATETIME              DEFAULT NULL    COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备管理-库房分类表';

-- 5. 库房库存表
CREATE TABLE IF NOT EXISTS `dev_warehouse_stock` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` BIGINT       NOT NULL                COMMENT '库房分类ID',
  `quantity`    INT          NOT NULL DEFAULT 0      COMMENT '库存数量',
  `sub_type`    VARCHAR(20)           DEFAULT NULL   COMMENT '异常设备子类型(pending_verify/pending_unban/other)',
  `note`        VARCHAR(500)          DEFAULT NULL   COMMENT '备注',
  `update_by`   VARCHAR(64)           DEFAULT ''     COMMENT '更新者',
  `update_time` DATETIME              DEFAULT NULL   COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备管理-库房库存表';

-- 6. 在外设备表
CREATE TABLE IF NOT EXISTS `dev_outside_device` (
  `id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reason`               VARCHAR(200) NOT NULL                COMMENT '原因描述',
  `quantity`             INT          NOT NULL DEFAULT 1      COMMENT '数量',
  `note`                 VARCHAR(500)          DEFAULT NULL   COMMENT '备注',
  `out_date`             DATE         NOT NULL                COMMENT '出去日期',
  `expected_return_date` DATE                  DEFAULT NULL   COMMENT '预计归还日期',
  `actual_return_date`   DATE                  DEFAULT NULL   COMMENT '实际归还日期',
  `create_by`            VARCHAR(64)           DEFAULT ''     COMMENT '创建者',
  `create_time`          DATETIME              DEFAULT NULL   COMMENT '创建时间',
  `update_by`            VARCHAR(64)           DEFAULT ''     COMMENT '更新者',
  `update_time`          DATETIME              DEFAULT NULL   COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备管理-在外设备表';

-- 7. 进出库记录表
CREATE TABLE IF NOT EXISTS `dev_inout_record` (
  `id`                     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_time`            DATETIME     NOT NULL                COMMENT '操作时间',
  `type`                   VARCHAR(5)   NOT NULL                COMMENT '类型(in/out)',
  `quantity`               INT          NOT NULL DEFAULT 1      COMMENT '数量',
  `inout_category_id`      BIGINT       NOT NULL                COMMENT '进出库原因ID',
  `warehouse_category_id`  BIGINT                DEFAULT NULL   COMMENT '库房分类ID',
  `receiver_type`          VARCHAR(20)           DEFAULT NULL   COMMENT '出库接收方类型(employee/outside/warehouse)',
  `receiver_employee_id`   BIGINT                DEFAULT NULL   COMMENT '出库接收员工ID',
  `receiver_outside_note`  VARCHAR(200)          DEFAULT NULL   COMMENT '出库在外备注',
  `sender_type`            VARCHAR(20)           DEFAULT NULL   COMMENT '入库来源类型(employee/warehouse)',
  `sender_employee_id`     BIGINT                DEFAULT NULL   COMMENT '入库来源员工ID',
  `note`                   VARCHAR(500)          DEFAULT NULL   COMMENT '备注',
  `create_by`              VARCHAR(64)           DEFAULT ''     COMMENT '创建者',
  `create_time`            DATETIME              DEFAULT NULL   COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备管理-进出库记录表';

-- 8. 每日汇报表
CREATE TABLE IF NOT EXISTS `dev_daily_report` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_date`    DATE         NOT NULL                COMMENT '汇报日期',
  `employee_id`    BIGINT       NOT NULL                COMMENT '员工ID',
  `phone_count`    INT          NOT NULL DEFAULT 0      COMMENT '汇报手机数量',
  `platform_stats` TEXT                  DEFAULT NULL   COMMENT '平台统计JSON',
  `create_by`      VARCHAR(64)           DEFAULT ''     COMMENT '创建者',
  `create_time`    DATETIME              DEFAULT NULL   COMMENT '创建时间',
  `update_by`      VARCHAR(64)           DEFAULT ''     COMMENT '更新者',
  `update_time`    DATETIME              DEFAULT NULL   COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_emp` (`report_date`, `employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备管理-每日汇报表';

-- 9. 每日核对表
CREATE TABLE IF NOT EXISTS `dev_daily_check` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `check_date`  DATE         NOT NULL                COMMENT '核对日期',
  `employee_id` BIGINT       NOT NULL                COMMENT '员工ID',
  `status`      VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT '状态(pending/confirmed/absent)',
  `checked_at`  DATETIME              DEFAULT NULL   COMMENT '核对时间',
  `note`        VARCHAR(500)          DEFAULT NULL   COMMENT '备注',
  `create_by`   VARCHAR(64)           DEFAULT ''     COMMENT '创建者',
  `create_time` DATETIME              DEFAULT NULL   COMMENT '创建时间',
  `update_by`   VARCHAR(64)           DEFAULT ''     COMMENT '更新者',
  `update_time` DATETIME              DEFAULT NULL   COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_emp` (`check_date`, `employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备管理-每日核对表';

-- 10. 操作日志表
CREATE TABLE IF NOT EXISTS `dev_log` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `log_time`   DATETIME     NOT NULL                COMMENT '日志时间',
  `level`      VARCHAR(10)  NOT NULL DEFAULT 'info' COMMENT '级别(info/warning/error)',
  `module`     VARCHAR(50)  NOT NULL                COMMENT '模块',
  `message`    VARCHAR(500) NOT NULL                COMMENT '日志消息',
  `detail`     TEXT                  DEFAULT NULL   COMMENT '详情',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备管理-操作日志表';

-- 11. 系统配置表
CREATE TABLE IF NOT EXISTS `dev_config` (
  `cfg_key`   VARCHAR(50)  NOT NULL COMMENT '配置键',
  `cfg_value` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '配置值',
  PRIMARY KEY (`cfg_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备管理-系统配置表';

-- ============================================================
--  二、菜单注册（若依 sys_menu）
-- ============================================================

-- 一级菜单：设备统计（父菜单ID=0）
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('设备统计', 0, 5, 'device', NULL, NULL, 1, 0, 'M', '0', '0', '', 'phone', 'admin', NOW(), '', NULL, '设备进出库管理系统');

-- 获取刚插入的一级菜单ID（存入变量）
SET @parentId = LAST_INSERT_ID();

-- 二级菜单：总览
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('总览', @parentId, 1, 'dashboard', 'device/dashboard/index', NULL, 1, 0, 'C', '0', '0', 'device:dashboard:view', 'dashboard', 'admin', NOW(), '', NULL, '设备总览仪表盘');

-- 二级菜单：每日管理
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('每日管理', @parentId, 2, 'report', 'device/report/index', NULL, 1, 0, 'C', '0', '0', 'device:report:view', 'edit', 'admin', NOW(), '', NULL, '每日汇报与核对');

-- 二级菜单：库房库存
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('库房库存', @parentId, 3, 'warehouse', 'device/warehouse/index', NULL, 1, 0, 'C', '0', '0', 'device:warehouse:view', 'list', 'admin', NOW(), '', NULL, '库房库存管理');

-- 二级菜单：进出库
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('进出库', @parentId, 4, 'inout', 'device/inout/index', NULL, 1, 0, 'C', '0', '0', 'device:inout:view', 'swap', 'admin', NOW(), '', NULL, '进出库操作记录');

-- 二级菜单：统计榜单
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('统计榜单', @parentId, 5, 'stats', 'device/stats/index', NULL, 1, 0, 'C', '0', '0', 'device:stats:view', 'chart-bar', 'admin', NOW(), '', NULL, '时间段统计分析');

-- 二级菜单：日志记录
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('日志记录', @parentId, 6, 'logs', 'device/logs/index', NULL, 1, 0, 'C', '0', '0', 'device:log:view', 'form', 'admin', NOW(), '', NULL, '操作日志查看');

-- 二级菜单：系统设置
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('系统设置', @parentId, 7, 'settings', 'device/settings/index', NULL, 1, 0, 'C', '0', '0', 'device:settings:view', 'system', 'admin', NOW(), '', NULL, '系统配置管理');

-- 子菜单按钮权限（示例：进出库添加/删除）
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`)
SELECT '进出库新增', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'device:inout:add', '#', 'admin', NOW()
FROM `sys_menu` WHERE `path`='inout' AND `parent_id`=@parentId LIMIT 1;

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`)
SELECT '进出库删除', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'device:inout:remove', '#', 'admin', NOW()
FROM `sys_menu` WHERE `path`='inout' AND `parent_id`=@parentId LIMIT 1;

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`)
SELECT '员工新增', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'device:employee:add', '#', 'admin', NOW()
FROM `sys_menu` WHERE `path`='settings' AND `parent_id`=@parentId LIMIT 1;

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`)
SELECT '员工修改', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'device:employee:edit', '#', 'admin', NOW()
FROM `sys_menu` WHERE `path`='settings' AND `parent_id`=@parentId LIMIT 1;

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`)
SELECT '员工删除', menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'device:employee:remove', '#', 'admin', NOW()
FROM `sys_menu` WHERE `path`='settings' AND `parent_id`=@parentId LIMIT 1;

-- ============================================================
--  三、初始化配置数据
-- ============================================================

-- 系统配置：公司设备总数
INSERT INTO `dev_config` (`cfg_key`, `cfg_value`) VALUES ('company_phone_total', '0')
ON DUPLICATE KEY UPDATE `cfg_value` = `cfg_value`;

-- 默认账号平台
INSERT INTO `dev_account_platform` (`code`, `label`, `color`, `active_aliases`, `disabled_aliases`, `posting_aliases`, `sort`, `create_by`, `create_time`) VALUES
('douyin',       '抖音',  '#1a1a2e', '["抖音","在用抖音"]',     '["禁用抖音","抖音不能用"]', '["发抖音"]',  1, 'admin', NOW()),
('xiaohongshu',  '小红书','#ef4444', '["小红书","在用小红书"]', '["禁用小红书"]',            '["发小红书"]',2, 'admin', NOW()),
('kuaishou',     '快手',  '#f97316','["快手","在用快手"]',     '["禁用快手"]',              '["发快手"]',  3, 'admin', NOW()),
('other',        '其他',  '#94a3b8', '["其他","其他号","在用其他"]','["禁用其他"]',           '["发其他"]',  4, 'admin', NOW())
ON DUPLICATE KEY UPDATE `label`=VALUES(`label`);

-- 默认进出库原因
INSERT INTO `dev_inout_category` (`name`, `type`, `sort`, `create_by`, `create_time`) VALUES
('作品号出库',   'out',  1, 'admin', NOW()),
('截留号回收',   'in',   2, 'admin', NOW()),
('永久禁言',     'in',   3, 'admin', NOW()),
('新设备入库',   'in',   4, 'admin', NOW()),
('设备报废',     'both', 5, 'admin', NOW()),
('维修送修',     'out',  6, 'admin', NOW()),
('维修归还',     'in',   7, 'admin', NOW());

-- 默认库房分类（含初始库存记录）
INSERT INTO `dev_warehouse_category` (`name`, `type`, `color`, `sort`, `create_by`, `create_time`) VALUES
('作品号',     'work_account',        '#3b82f6',  1, 'admin', NOW()),
('截留号',     'intercept_account',   '#8b5cf6',  2, 'admin', NOW()),
('小红书号',   'xiaohongshu',         '#ef4444',  3, 'admin', NOW()),
('异常设备',   'abnormal',            '#f97316',  4, 'admin', NOW()),
('待上机设备', 'pending_online',      '#facc15',  5, 'admin', NOW()),
('维修设备',   'maintenance',         '#94a3b8',  6, 'admin', NOW());

-- 为每个库房分类初始化库存记录（数量为0）
INSERT INTO `dev_warehouse_stock` (`category_id`, `quantity`, `update_time`)
SELECT `id`, 0, NOW() FROM `dev_warehouse_category`
ON DUPLICATE KEY UPDATE `category_id`=`category_id`;

-- ============================================================
-- 执行完成
-- ============================================================
SELECT '设备进出库管理系统初始化完成' AS result;
