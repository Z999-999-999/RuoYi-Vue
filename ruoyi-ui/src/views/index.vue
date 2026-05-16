<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :sm="24" :lg="12" style="padding-left: 20px">
        <h2>设备出入管理系统</h2>
        <p>
          基于 RuoYi-Vue 框架开发的设备出入管理系统，支持设备出入记录管理、分类统计、员工排行等功能。提供直观的统计榜单和灵活的日期筛选，帮助高效管理设备流转。
        </p>
        <p>
          <b>当前版本:</b> <span>v{{ version }}</span>
        </p>
        <p>
          <el-tag type="danger">&yen;免费开源</el-tag>
        </p>
        <p>
          <el-button
            type="primary"
            size="mini"
            icon="el-icon-cloudy"
            plain
            @click="goTarget('https://github.com/Z999-999-999/RuoYi-Vue')"
            >访问GitHub</el-button
          >
        </p>
      </el-col>

      <el-col :sm="24" :lg="12" style="padding-left: 50px">
        <el-row>
          <el-col :span="12">
            <h2>技术选型</h2>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="6">
            <h4>后端技术</h4>
            <ul>
              <li>SpringBoot</li>
              <li>Spring Security</li>
              <li>JWT</li>
              <li>MyBatis</li>
              <li>Druid</li>
              <li>Fastjson</li>
              <li>...</li>
            </ul>
          </el-col>
          <el-col :span="6">
            <h4>前端技术</h4>
            <ul>
              <li>Vue</li>
              <li>Vuex</li>
              <li>Element-ui</li>
              <li>Axios</li>
              <li>Sass</li>
              <li>Quill</li>
              <li>...</li>
            </ul>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
    <el-divider />
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="update-log">
          <div slot="header" class="clearfix">
            <span>联系信息</span>
          </div>
          <div class="body">
            <p>
              <i class="el-icon-s-promotion"></i> GitHub：<el-link
                href="https://github.com/Z999-999-999/RuoYi-Vue"
                target="_blank"
                >Z999-999-999/RuoYi-Vue</el-link
              >
            </p>
            <p>
              <i class="el-icon-user-solid"></i> 开发者：ww999177
            </p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="update-log">
          <div slot="header" class="clearfix">
            <span>更新日志</span>
          </div>
          <el-collapse accordion>
            <el-collapse-item title="v3.10.3 - 2026-05-17">
              <ol>
                <li>修复重置 API Key 无效：前端改用 RuoYi request 封装调用重置接口</li>
                <li>修复上报接口错误返回 HTTP 200：验证失败现返回正确状态码（400/401/403/413/500）</li>
                <li>修复 curl 示例和上报地址缺端口号：补充 :8080 端口</li>
                <li>安全加固：API Key AES 加密存储、常量时间比较防时序攻击、删除测试接口</li>
                <li>版本号从 3.10.2 升级至 3.10.3</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v3.10.2 - 2026-05-17">
              <ol>
                <li>上报接口异常保护：添加 try-catch 全包裹 + 参数校验 + OOM 处理</li>
                <li>上报去重性能优化：HashMap 快速查找 + HashSet 哈希去重，避免全量加载内存</li>
                <li>修复表格多余"发布时间"列：删除固定列，动态字段列已包含该信息</li>
                <li>排序功能修复：数字字段按数值排序（sortType 参数 + JSON_EXTRACT +0）</li>
                <li>字段类型自动推断增强：含"量/数/count"关键词自动识别为数字类型</li>
                <li>版本号从 3.10.1 升级至 3.10.2</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v3.10.1 - 2026-05-15">
              <ol>
                <li>修复级联删除 Bug：deleteTableById 改用 selectTableByIdForDelete 直接查询软删除表</li>
                <li>BitableTableMapper.java 语法错误修复（多余括号、@Param import、缺失方法声明）</li>
                <li>数据库残留数据清理（39条 bitable_field、122条 bitable_record、2张 bitable_table）</li>
                <li>版本号从 3.10.0 升级至 3.10.1</li>
              </ol>
            </el-collapse-item>
<el-collapse-item title="v3.10.0 - 2026-05-11">
              <ol>
                <li>新增 Bitable 多维表格模块（应用管理、数据表管理、字段配置、记录管理）</li>
                <li>新增数据上报 API，支持匿名访问与 cid 去重逻辑</li>
                <li>新增多条件筛选（文本/数字/日期类型分别支持不同操作符）</li>
                <li>新增 CSV 导出功能（支持嵌套字段，限制 10000 条）</li>
                <li>新增列显隐控制（localStorage 持久化）</li>
                <li>新增骨架屏加载、批量操作、字段类型可视化、空状态设计</li>
                <li>修复 BitableAppMapper SELECT * 问题</li>
                <li>修复软删除恢复唯一键冲突问题</li>
                <li>修复验证码配置与 XSS 过滤白名单</li>
                <li>修复 Druid 连接池属性跨 profile 解析失败</li>
                <li>版本号从 3.9.2 升级至 3.10.0</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v0.0.1 - 2026-05-01">
              <ol>
                <li>项目初始化，基于 RuoYi-Vue 框架搭建设备出入管理系统</li>
                <li>新增设备分类管理（入/出/通）</li>
                <li>新增设备出入记录管理</li>
                <li>新增统计榜单页面（员工排行、分类排行、日期筛选）</li>
                <li>统计榜单支持前端聚合计算，切换分类即时响应</li>
                <li>统计榜单支持展开行详情，显示分类/员工占比进度条</li>
                <li>修复统计页面日期范围筛选不生效的问题</li>
                <li>项目推送至 GitHub 私有仓库</li>
              </ol>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>
      
    </el-row>
  </div>
</template>

<script>
export default {
  name: "Index",
  data() {
    return {
      // 版本号
      version: "3.10.2"
    }
  },
  methods: {
    goTarget(href) {
      window.open(href, "_blank")
    }
  }
}
</script>

<style scoped lang="scss">
.home {
  blockquote {
    padding: 10px 20px;
    margin: 0 0 20px;
    font-size: 17.5px;
    border-left: 5px solid #eee;
  }
  hr {
    margin-top: 20px;
    margin-bottom: 20px;
    border: 0;
    border-top: 1px solid #eee;
  }
  .col-item {
    margin-bottom: 20px;
  }

  ul {
    padding: 0;
    margin: 0;
  }

  font-family: "open sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 13px;
  color: #676a6c;
  overflow-x: hidden;

  ul {
    list-style-type: none;
  }

  h4 {
    margin-top: 0px;
  }

  h2 {
    margin-top: 10px;
    font-size: 26px;
    font-weight: 100;
  }

  p {
    margin-top: 10px;

    b {
      font-weight: 700;
    }
  }

  .update-log {
    ol {
      display: block;
      list-style-type: decimal;
      margin-block-start: 1em;
      margin-block-end: 1em;
      margin-inline-start: 0;
      margin-inline-end: 0;
      padding-inline-start: 40px;
    }
  }
}
</style>

