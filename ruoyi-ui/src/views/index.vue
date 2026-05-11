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
      version: "3.10.0"
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

