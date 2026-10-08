<template>
  <div class="settings-container">
    <div class="card-container">
      <h2>系统设置</h2>
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="爬虫设置" name="crawler">
          <el-form :model="crawlerSettings" label-width="150px" style="max-width: 600px">
            <el-form-item label="请求超时时间">
              <el-input-number v-model="crawlerSettings.timeout" :min="1000" :max="60000" />
              <span style="margin-left: 10px; color: #999">毫秒</span>
            </el-form-item>
            <el-form-item label="重试次数">
              <el-input-number v-model="crawlerSettings.retryTimes" :min="1" :max="10" />
            </el-form-item>
            <el-form-item label="请求延迟">
              <el-input-number v-model="crawlerSettings.delay" :min="0" :max="5000" />
              <span style="margin-left: 10px; color: #999">毫秒</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveCrawlerSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="分析设置" name="analysis">
          <el-form :model="analysisSettings" label-width="150px" style="max-width: 600px">
            <el-form-item label="默认显示数量">
              <el-input-number v-model="analysisSettings.defaultLimit" :min="10" :max="100" />
            </el-form-item>
            <el-form-item label="数据刷新间隔">
              <el-input-number v-model="analysisSettings.refreshInterval" :min="30" :max="3600" />
              <span style="margin-left: 10px; color: #999">秒</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveAnalysisSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="系统信息" name="info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="系统名称">外卖数据分析系统</el-descriptions-item>
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="前端框架">Vue 2.6.14</el-descriptions-item>
            <el-descriptions-item label="UI框架">Element UI 2.15.13</el-descriptions-item>
            <el-descriptions-item label="图表库">v-charts 1.19.2</el-descriptions-item>
            <el-descriptions-item label="后端框架">Spring Boot 2.7.14</el-descriptions-item>
            <el-descriptions-item label="数据分析">Apache Spark 3.2.0</el-descriptions-item>
            <el-descriptions-item label="数据库">MySQL 8.0</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SystemSettings',
  data() {
    return {
      activeTab: 'crawler',
      crawlerSettings: {
        timeout: 10000,
        retryTimes: 3,
        delay: 1000
      },
      analysisSettings: {
        defaultLimit: 10,
        refreshInterval: 300
      }
    }
  },
  methods: {
    saveCrawlerSettings() {
      this.$message.success('爬虫设置已保存')
    },
    saveAnalysisSettings() {
      this.$message.success('分析设置已保存')
    }
  }
}
</script>

<style lang="scss" scoped>
.settings-container {
  h2 {
    margin-bottom: 20px;
    color: #333;
  }
}
</style>

