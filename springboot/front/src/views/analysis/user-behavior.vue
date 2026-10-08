<template>
  <div class="analysis-container">
    <div class="card-container">
      <h2>用户行为分析</h2>

      <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
        <!-- 消费频次 -->
        <el-tab-pane label="消费频次统计" name="frequency">
          <div class="summary-row" v-if="frequencyTable.length > 0">
            <div class="summary-item">
              <div class="summary-label">用户总数</div>
              <div class="summary-value">{{ formatNumber(totalUsers) }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">最多频次段</div>
              <div class="summary-value">{{ topFrequency.frequency_range }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">最高占比</div>
              <div class="summary-value">{{ topFrequency.percent }}%</div>
            </div>
          </div>

          <div v-loading="loadingFrequency" class="chart-container-large behavior-chart-panel">
            <div ref="frequencyChart" class="behavior-chart"></div>
            <div v-if="!loadingFrequency && frequencyTable.length === 0" class="no-data-hint">暂无消费频次数据</div>
          </div>

          <el-table :data="frequencyTable" border stripe style="margin-top: 20px">
            <el-table-column prop="frequency_range" label="消费频次" />
            <el-table-column prop="user_count" label="用户数量" sortable>
              <template slot-scope="scope">
                <span style="color: #409EFF; font-weight: bold">{{ scope.row.user_count }}</span>
              </template>
            </el-table-column>
            <el-table-column label="占比">
              <template slot-scope="scope">
                <el-progress
                  :percentage="Number(scope.row.percent)"
                  color="#409EFF"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="消费金额区间" name="amount">
          <div class="summary-row" v-if="amountTable.length > 0">
            <div class="summary-item">
              <div class="summary-label">用户总数</div>
              <div class="summary-value">{{ formatNumber(amountTotalUsers) }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">最多金额段</div>
              <div class="summary-value">{{ topAmount.amount_range }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">最高占比</div>
              <div class="summary-value">{{ topAmount.percent }}%</div>
            </div>
          </div>

          <div v-loading="loadingAmount" class="chart-container-large behavior-chart-panel">
            <div ref="amountChart" class="behavior-chart"></div>
            <div v-if="!loadingAmount && amountTable.length === 0" class="no-data-hint">暂无消费金额区间数据</div>
          </div>

          <el-table :data="amountTable" border stripe style="margin-top: 20px">
            <el-table-column prop="amount_range" label="消费金额区间" />
            <el-table-column prop="user_count" label="用户数量" sortable>
              <template slot-scope="scope">
                <span style="color: #67c23a; font-weight: bold">{{ scope.row.user_count }}</span>
              </template>
            </el-table-column>
            <el-table-column label="占比">
              <template slot-scope="scope">
                <el-progress
                  :percentage="Number(scope.row.percent)"
                  color="#67c23a"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import { analysisApi } from '@/api'
import echarts from 'echarts/lib/echarts'
import 'echarts/lib/chart/bar'
import 'echarts/lib/component/tooltip'
import 'echarts/lib/component/grid'

export default {
  name: 'UserBehavior',
  data() {
    return {
      activeTab: 'frequency',
      loadingFrequency: false,
      loadingAmount: false,
      frequencyChart: null,
      amountChart: null,
      frequencyTable: [],
      amountTable: [],
      totalUsers: 0
    }
  },
  computed: {
    amountTotalUsers() {
      return this.amountTable.reduce((sum, item) => sum + (item.user_count || 0), 0)
    },
    topFrequency() {
      const row = [...this.frequencyTable].sort((a, b) => b.user_count - a.user_count)[0] || {}
      return {
        frequency_range: row.frequency_range || '-',
        percent: row.percent || '0.00'
      }
    },
    topAmount() {
      const row = [...this.amountTable].sort((a, b) => b.user_count - a.user_count)[0] || {}
      return {
        amount_range: row.amount_range || '-',
        percent: row.percent || '0.00'
      }
    }
  },
  mounted() {
    this.loadFrequencyData()
    this.loadAmountData()
    this._resizeHandler = () => {
      this.frequencyChart && this.frequencyChart.resize()
      this.amountChart && this.amountChart.resize()
    }
    window.addEventListener('resize', this._resizeHandler)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this._resizeHandler)
    if (this.frequencyChart) {
      this.frequencyChart.dispose()
      this.frequencyChart = null
    }
    if (this.amountChart) {
      this.amountChart.dispose()
      this.amountChart = null
    }
  },
  methods: {
    handleTabClick() {
      if (this.activeTab === 'frequency') {
        this.renderBehaviorChart('frequency')
      } else {
        this.renderBehaviorChart('amount')
      }
    },
    formatNumber(value) {
      return Number(value || 0).toLocaleString()
    },
    renderBehaviorChart(type) {
      this.$nextTick(() => {
        const isFrequency = type === 'frequency'
        const rows = isFrequency ? this.frequencyTable : this.amountTable
        const el = isFrequency ? this.$refs.frequencyChart : this.$refs.amountChart
        if (!el || rows.length === 0) return
        const chartName = isFrequency ? 'frequencyChart' : 'amountChart'
        if (!this[chartName]) {
          this[chartName] = echarts.init(el)
        }
        const nameKey = isFrequency ? 'frequency_range' : 'amount_range'
        const sortedRows = [...rows].sort((a, b) => b.user_count - a.user_count)
        const colors = isFrequency
          ? ['#2f7ed8', '#00a6a6', '#f5a623', '#e15f41']
          : ['#20bf6b', '#00a6a6', '#f5a623', '#6c5ce7']
        this[chartName].setOption({
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            formatter: params => {
              const row = sortedRows[params[0].dataIndex]
              return `${row[nameKey]}<br/>用户数量：${this.formatNumber(row.user_count)}<br/>占比：${row.percent}%`
            }
          },
          grid: { top: 28, right: 120, bottom: 36, left: 130 },
          xAxis: {
            type: 'value',
            axisLabel: { formatter: value => this.formatNumber(value) },
            splitLine: { lineStyle: { type: 'dashed', color: '#dcdfe6' } }
          },
          yAxis: {
            type: 'category',
            inverse: true,
            data: sortedRows.map(item => item[nameKey]),
            axisTick: { show: false },
            axisLabel: { color: '#303133' }
          },
          series: [{
            name: '用户数量',
            type: 'bar',
            barWidth: 28,
            data: sortedRows.map((item, index) => ({
              value: item.user_count,
              itemStyle: { color: colors[index % colors.length], barBorderRadius: [0, 6, 6, 0] }
            })),
            label: {
              show: true,
              position: 'right',
              formatter: params => {
                const row = sortedRows[params.dataIndex]
                return `${this.formatNumber(row.user_count)}  ${row.percent}%`
              },
              color: '#303133',
              fontWeight: 600
            }
          }]
        }, true)
      })
    },
    async loadFrequencyData() {
      this.loadingFrequency = true
      try {
        const res = await analysisApi.getUserConsumptionFrequency()
        const list = Array.isArray(res && res.data) ? res.data : []
        if (list.length > 0) {
          const rows = list.map(item => ({
            frequency_range: item.frequency_range || item.frequencyRange || '未知',
            user_count: Number(item.user_count || item.userCount) || 0
          }))
          this.totalUsers = rows.reduce((sum, item) => sum + item.user_count, 0)
          this.frequencyTable = rows.map(item => ({
            ...item,
            percent: this.totalUsers > 0 ? (item.user_count / this.totalUsers * 100).toFixed(2) : '0.00'
          }))
          this.renderBehaviorChart('frequency')
        }
      } catch (error) {
        this.$message.error('加载数据失败：' + (error.message || '未知错误'))
      } finally {
        this.loadingFrequency = false
      }
    },
    async loadAmountData() {
      this.loadingAmount = true
      try {
        const res = await analysisApi.getUserConsumptionAmount()
        const list = Array.isArray(res && res.data) ? res.data : ((res && res.data && res.data.list) ? res.data.list : []) || []
        if (list.length > 0) {
          const rows = list.map(item => ({
            amount_range: item.amount_range || item.amountRange || '未知',
            user_count: Number(item.user_count != null ? item.user_count : (item.userCount || 0)) || 0
          }))
          const total = rows.reduce((sum, item) => sum + item.user_count, 0)
          this.amountTable = rows.map(item => ({
            ...item,
            percent: total > 0 ? (item.user_count / total * 100).toFixed(2) : '0.00'
          }))
          if (this.activeTab === 'amount') {
            this.renderBehaviorChart('amount')
          }
        }
      } catch (error) {
        this.$message.error('加载数据失败：' + (error.message || '未知错误'))
      } finally {
        this.loadingAmount = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.analysis-container {
  h2 {
    margin-bottom: 20px;
    color: #333;
  }
}
.summary-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin: 18px 0 12px;
}
.summary-item {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 14px 18px;
  background: #f8fafc;
}
.summary-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}
.summary-value {
  color: #303133;
  font-size: 24px;
  line-height: 1;
  font-weight: 700;
}
.behavior-chart-panel {
  height: 460px;
}
.behavior-chart {
  width: 100%;
  height: 460px;
}
.no-data-hint {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
}
</style>
