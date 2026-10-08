<template>
  <div class="logs-container">
    <div class="card-container">
      <h2>导入日志</h2>
      <el-table :data="logList" border stripe style="width: 100%">
        <el-table-column prop="logId" label="日志ID" width="100" />
        <el-table-column prop="importType" label="导入类型" width="120">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.importType === 'merchant'" type="success">商户</el-tag>
            <el-tag v-else-if="scope.row.importType === 'dish'" type="warning">菜品</el-tag>
            <el-tag v-else-if="scope.row.importType === 'order'" type="info">订单</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名" show-overflow-tooltip />
        <el-table-column prop="totalCount" label="总记录数" width="100" />
        <el-table-column prop="successCount" label="成功数" width="100">
          <template slot-scope="scope">
            <span style="color: #67c23a">{{ scope.row.successCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="failCount" label="失败数" width="100">
          <template slot-scope="scope">
            <span style="color: #f56c6c">{{ scope.row.failCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="importStatus" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.importStatus === 1" type="info">进行中</el-tag>
            <el-tag v-else-if="scope.row.importStatus === 2" type="success">成功</el-tag>
            <el-tag v-else-if="scope.row.importStatus === 3" type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleViewDetail(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 详情对话框 -->
    <el-dialog title="导入日志详情" :visible.sync="detailVisible" width="600px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志ID">{{ currentLog.logId }}</el-descriptions-item>
        <el-descriptions-item label="导入类型">{{ currentLog.importType }}</el-descriptions-item>
        <el-descriptions-item label="文件名">{{ currentLog.fileName }}</el-descriptions-item>
        <el-descriptions-item label="总记录数">{{ currentLog.totalCount }}</el-descriptions-item>
        <el-descriptions-item label="成功数">
          <span style="color: #67c23a">{{ currentLog.successCount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="失败数">
          <span style="color: #f56c6c">{{ currentLog.failCount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentLog.importStatus === 1" type="info">进行中</el-tag>
          <el-tag v-else-if="currentLog.importStatus === 2" type="success">成功</el-tag>
          <el-tag v-else-if="currentLog.importStatus === 3" type="danger">失败</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentLog.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间" :span="2">{{ currentLog.endTime }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMessage">
          <pre style="color: #f56c6c">{{ currentLog.errorMessage }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { importApi } from '@/api'

export default {
  name: 'ImportLogs',
  data() {
    return {
      logList: [],
      detailVisible: false,
      currentLog: null
    }
  },
  mounted() {
    this.loadLogs()
  },
  methods: {
    async loadLogs() {
      try {
        const res = await importApi.getImportLogs()
        if (res.data) {
          this.logList = res.data
        }
      } catch (error) {
        this.$message.error('加载日志失败：' + (error.message || '未知错误'))
      }
    },
    async handleViewDetail(row) {
      try {
        const res = await importApi.getImportLog(row.logId)
        if (res.data) {
          this.currentLog = res.data
          this.detailVisible = true
        }
      } catch (error) {
        this.$message.error('加载详情失败：' + (error.message || '未知错误'))
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.logs-container {
  pre {
    background: #f5f7fa;
    padding: 10px;
    border-radius: 4px;
    font-size: 12px;
  }
}
</style>

