-- ============================================================
--  多维表格模块 - 菜单注册
--  适配：若依框架（RuoYi-Vue） + MySQL
-- ============================================================

USE `ry-vue`;

-- 一级菜单：多维表格（父菜单ID=0，order_num=6排在设备统计后面）
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('多维表格', 0, 6, 'bitable', NULL, NULL, 1, 0, 'M', '0', '0', '', 'excel', 'admin', NOW(), '', NULL, '多维表格数据管理');

SET @parentId = LAST_INSERT_ID();

-- 二级菜单：应用管理
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES ('应用管理', @parentId, 1, 'index', 'bitable/index', NULL, 1, 0, 'C', '0', '0', 'bitable:app:list', 'list', 'admin', NOW(), '', NULL, '多维表格应用管理');

-- 获取"应用管理"菜单ID
SET @appMenuId = LAST_INSERT_ID();

-- 按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`)
VALUES ('应用新增', @appMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'bitable:app:add', '#', 'admin', NOW());

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`)
VALUES ('应用修改', @appMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'bitable:app:edit', '#', 'admin', NOW());

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`)
VALUES ('应用删除', @appMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'bitable:app:remove', '#', 'admin', NOW());

-- 给admin角色(role_id=1)分配所有多维表格权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu_id FROM `sys_menu` WHERE `perms` LIKE 'bitable:%'
ON DUPLICATE KEY UPDATE `role_id`=`role_id`;

-- ============================================================
SELECT '多维表格菜单注册完成' AS result;
