# 更新日志

所有重要更改均会记录在此文件中。

## [3.10.2] - 2026-05-17

### 修复

- 上报接口异常保护：添加 try-catch 全包裹，防止 500 错误暴露堆栈信息
- 上报参数校验：app_token/table_key 空值返回 400，body 空值返回 400
- OOM 保护：数据量过大时返回 413 友好提示
- 表格多余"发布时间"列：删除固定列，动态字段列已包含该信息
- 排序功能修复：数字字段按数值排序（新增 sortType 参数，JSON_EXTRACT +0 隐式转换）
- 字段类型自动推断增强：含"量/数/count"关键词自动识别为数字类型，含"时间/date"识别为日期类型

### 优化

- 上报去重性能优化：HashMap record_id 快速查找替代逐条查询，HashSet 哈希去重替代全量 JSON 比对
- 现有数字字段类型修正：点赞量、子评论数等字段从 type=1 改为 type=2

### 构建

- 版本号从 3.10.1 升级至 3.10.2

## [3.10.1] - 2026-05-15

### 修复

- 级联删除 Bug：deleteTableById 修复后重新编译未生效，导致字段/记录残留
- BitableTableMapper.java 语法错误修复（接口末尾多余括号、缺失 @Param import、缺失方法声明）
- AddressUtils.java 回滚：IP 接口 myip.ipip.net 不接受参数，回滚所有修改
- 删除应用后遗留孤儿数据问题

### 清理

- 数据库残留数据清理：39条 bitable_field、122条 bitable_record、2张 bitable_table（软删除）
- 3张孤儿表清理（app 已不存在但 table 仍在）

### 构建

- 版本号从 3.10.0 升级至 3.10.1

## [3.10.0] - 2026-05-11

### 新增

- Bitable 多维表格模块（应用管理、数据表管理、字段配置、记录 CRUD）
- 数据上报 API（`/api/bitable/reporting/*`），支持匿名访问
- 上报去重逻辑：基于 cid 字段先查后更，避免重复数据
- 多条件筛选：文本/数字字段（等于/不等于/包含/不包含/为空/不为空），日期字段（等于/不等于/为空/不为空/晚于/早于）
- CSV 导出：支持嵌套字段导出，限制 10000 条防止内存溢出
- 列显隐控制：localStorage 持久化，页面刷新后保留配置
- 骨架屏加载效果
- 批量删除操作
- 字段类型可视化标签
- 空状态设计

### 修复

- BitableAppMapper SELECT * 改为明确字段 + 子查询
- 软删除恢复时唯一键冲突问题（检查 del_flag 后恢复而非新增）
- 验证码配置：CaptchaController 硬编码 false，DB sys_config 同步 false
- XSS 过滤排除上报接口白名单（application.yml xss.excludes）
- Druid 连接池属性跨 profile 解析失败（application.yml 补充 spring.datasource.druid 属性）
- 序号格式简化为纯序号
- 删除应用后自动跳转回列表页
- loadOverview 空值重定向

### 变更

- 版本号从 3.9.2 升级至 3.10.0
- SecurityConfig 添加上报接口匿名访问权限
- vue.config.js 添加 /api/ 代理转发到后端 8080

## [0.0.1] - 2026-05-01

### 新增

- 项目初始化，基于 RuoYi-Vue 框架
- 设备分类管理（入/出/通）
- 设备出入记录管理
- 统计榜单页面（员工排行、分类排行、日期筛选）
- 前端聚合计算，切换分类即时响应
- 展开行详情，显示分类/员工占比进度条
- 项目推送至 GitHub 私有仓库

### 修复

- 统计页面日期范围筛选不生效
