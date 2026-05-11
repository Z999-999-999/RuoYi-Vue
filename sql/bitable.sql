-- =============================================
-- 多维表格 (Bitable) - 兼容飞书多维表格API
-- =============================================

-- ----------------------------
-- 1. 多维表格应用
-- ----------------------------
DROP TABLE IF EXISTS bitable_app;
CREATE TABLE bitable_app (
  id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
  app_token    VARCHAR(50)  NOT NULL UNIQUE COMMENT '应用唯一标识',
  name         VARCHAR(200) NOT NULL COMMENT '应用名称',
  description  VARCHAR(500) DEFAULT '' COMMENT '应用描述',
  api_key      VARCHAR(100) DEFAULT '' COMMENT 'API访问密钥',
  created_by   VARCHAR(64)  DEFAULT '' COMMENT '创建者',
  created_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_by   VARCHAR(64)  DEFAULT '' COMMENT '更新者',
  updated_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag     CHAR(1)      DEFAULT '0' COMMENT '删除标志(0存在 1删除)'
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='多维表格应用';

-- ----------------------------
-- 2. 多维表格-数据表
-- ----------------------------
DROP TABLE IF EXISTS bitable_table;
CREATE TABLE bitable_table (
  id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
  app_id       BIGINT       NOT NULL COMMENT '应用ID',
  app_token    VARCHAR(50)  NOT NULL COMMENT '应用标识',
  table_id     VARCHAR(50)  NOT NULL COMMENT '数据表标识',
  name         VARCHAR(200) NOT NULL COMMENT '数据表名称',
  created_by   VARCHAR(64)  DEFAULT '' COMMENT '创建者',
  created_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_by   VARCHAR(64)  DEFAULT '' COMMENT '更新者',
  updated_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag     CHAR(1)      DEFAULT '0' COMMENT '删除标志',
  UNIQUE KEY uk_app_table (app_token, table_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='多维表格-数据表';

-- ----------------------------
-- 3. 多维表格-字段
-- ----------------------------
DROP TABLE IF EXISTS bitable_field;
CREATE TABLE bitable_field (
  id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
  table_pk     BIGINT       NOT NULL COMMENT '数据表主键ID',
  app_token    VARCHAR(50)  NOT NULL COMMENT '应用标识',
  table_id     VARCHAR(50)  NOT NULL COMMENT '数据表标识',
  field_id     VARCHAR(50)  NOT NULL COMMENT '字段标识',
  field_name   VARCHAR(200) NOT NULL COMMENT '字段名称',
  type         INT          NOT NULL DEFAULT 1 COMMENT '字段类型: 1多行文本 2数字 3单选 4多选 5日期 7复选框 13电话 15链接 1005自动编号',
  property_json TEXT        DEFAULT NULL COMMENT '字段属性JSON',
  ui_type      VARCHAR(50)  DEFAULT NULL COMMENT 'UI类型',
  created_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag     CHAR(1)      DEFAULT '0' COMMENT '删除标志',
  UNIQUE KEY uk_table_field (app_token, table_id, field_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='多维表格-字段';

-- ----------------------------
-- 4. 多维表格-记录
-- ----------------------------
DROP TABLE IF EXISTS bitable_record;
CREATE TABLE bitable_record (
  id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
  table_pk     BIGINT       NOT NULL COMMENT '数据表主键ID',
  app_token    VARCHAR(50)  NOT NULL COMMENT '应用标识',
  table_id     VARCHAR(50)  NOT NULL COMMENT '数据表标识',
  record_id    VARCHAR(50)  NOT NULL COMMENT '记录标识',
  fields_json  MEDIUMTEXT   NOT NULL COMMENT '字段值JSON',
  created_by   VARCHAR(64)  DEFAULT '' COMMENT '创建者',
  created_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_by   VARCHAR(64)  DEFAULT '' COMMENT '更新者',
  updated_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag     CHAR(1)      DEFAULT '0' COMMENT '删除标志',
  UNIQUE KEY uk_table_record (app_token, table_id, record_id),
  INDEX idx_app_table (app_token, table_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='多维表格-记录';
