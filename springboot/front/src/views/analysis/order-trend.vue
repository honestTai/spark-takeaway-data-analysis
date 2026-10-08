<template>
  <div class="analysis-container">
    <div class="card-container">
      <h2>订单趋势分析</h2>
      <el-form :inline="true" style="margin-bottom: 20px">
        <el-form-item label="时间范围">
          <el-select v-model="days" style="width: 150px" @change="loadData">
            <el-option label="最近7天" :value="7" />
            <el-option label="最近30天" :value="30" />
            <el-option label="最近90天" :value="90" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">刷新数据</el-button>
        </el-form-item>
      </el-form>

      <div class="summary-row" v-if="tableData.length > 0">
        <div class="summary-item">
          <div class="summary-label">订单总量</div>
          <div class="summary-value">{{ formatNumber(trendStats.total) }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-label">日均订单</div>
          <div class="summary-value">{{ formatNumber(trendStats.avg) }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-label">峰值日期</div>
          <div class="summary-value">{{ trendStats.peakDate }}</div>
        </div>
      </div>

      <!-- 趋势图 -->
      <div v-loading="loading" class="chart-container-large trend-chart-panel">
        <div ref="trendChart" class="trend-chart"></div>
        <div v-if="!loading && tableData.length === 0" class="no-data-hint">暂无订单趋势数据</div>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe style="margin-top: 20px">
        <el-table-column prop="order_date" label="日期" sortable />
        <el-table-column prop="order_count" label="订单数量" sortable>
          <template slot-scope="scope">
            <span style="color: #409EFF; font-weight: bold">{{ scope.row.order_count }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="change_text" label="较前一日变化" />
        <el-table-column label="趋势">
          <template slot-scope="scope">
            <i
              v-if="scope.row.trend === 'up'"
              class="el-icon-arrow-up"
              style="color: #67c23a"
            ></i>
            <i
              v-else-if="scope.row.trend === 'down'"
              class="el-icon-arrow-down"
              style="color: #f56c6c"
            ></i>
            <i v-else class="el-icon-minus" style="color: #909399"></i>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { analysisApi } from '@/api'
import echarts from 'echarts/lib/echarts'
import 'echarts/lib/chart/bar'
import 'echarts/lib/chart/line'
import 'echarts/lib/component/tooltip'
import 'echarts/lib/component/grid'
import 'echarts/lib/component/legend'

export default {
  name: 'OrderTrend',
  data() {
    return {
      days: 30,
      loading: false,
      trendChart: null,
      tableData: []
    }
  },
  computed: {
    trendStats() {
      const total = this.tableData.reduce((sum, item) => sum + item.order_count, 0)
      const peak = [...this.tableData].sort((a, b) => b.order_count - a.order_count)[0] || {}
      return {
        total,
        avg: this.tableData.length > 0 ? Math.round(total / this.tableData.length) : 0,
        peakDate: peak.order_date || '-'
      }
    }
  },
  mounted() {
    this.loadData()
    this._resizeHandler = () => { this.trendChart && this.trendChart.resize() }
    window.addEventListener('resize', this._resizeHandler)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this._resizeHandler)
    if (this.trendChart) {
      this.trendChart.dispose()
      this.trendChart = null
    }
  },
  methods: {
    formatNumber(value) {
      return Number(value || 0).toLocaleString()
    },
    renderTrendChart(rows) {
      this.$nextTick(() => {
        const el = this.$refs.trendChart
        if (!el || rows.length === 0) return
        if (!this.trendChart) {
          this.trendChart = echarts.init(el)
        }
        const avg = this.trendStats.avg
        this.trendChart.setOption({
          color: ['#2f7ed8', '#e15f41'],
          legend: { top: 6, data: ['订单数量', '日均线'] },
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'cross' },
            formatter: params => {
              const row = rows[params[0].dataIndex]
              return `${row.order_date}<br/>订单数量：${this.formatNumber(row.order_count)}<br/>较前一日：${row.change_text}`
            }
          },
          grid: { top: 54, right: 60, bottom: 70, left: 80 },
          xAxis: {
            type: 'category',
            data: rows.map(item => item.order_date),
            axisLabel: { rotate: rows.length > 12 ? 35 : 0 }
          },
          yAxis: {
            type: 'value',
            axisLabel: { formatter: value => this.formatNumber(value) },
            splitLine: { lineStyle: { type: 'dashed', color: '#dcdfe6' } }
          },
          series: [
            {
              name: '订单数量',
              type: 'bar',
              barMaxWidth: 26,
              data: rows.map(item => ({
                value: item.order_count,
                itemStyle: {
                  color: item.trend === 'down' ? '#e15f41' : (item.trend === 'up' ? '#20bf6b' : '#2f7ed8'),
                  barBorderRadius: [5, 5, 0, 0]
                }
              })),
              label: {
                show: rows.length <= 30,
                position: 'top',
                formatter: params => this.formatNumber(params.value),
                color: '#303133'
              }
            },
            {
              name: '日均线',
              type: 'line',
              smooth: true,
              symbolSize: 5,
              data: rows.map(() => avg),
              lineStyle: { width: 2, type: 'dashed' }
            }
          ]
        }, true)
      })
    },
    async loadData() {
      this.loading = true
      try {
        const res = await analysisApi.getOrderTrend(this.days)
        if (res.data) {
          this.tableData = res.data.map((item, index) => {
            const orderCount = Number(item.order_count || item.orderCount) || 0
            const prevRaw = index > 0 ? res.data[index - 1] : null
            const prevCount = prevRaw ? Number(prevRaw.order_count || prevRaw.orderCount) || 0 : orderCount
            const change = index > 0 ? orderCount - prevCount : 0
            let trend = 'equal'
            if (change > 0) {
              trend = 'up'
            } else if (change < 0) {
              trend = 'down'
            }
            return {
              order_date: item.order_date || item.orderDate,
              order_count: orderCount,
              change,
              change_text: index > 0 ? `${change > 0 ? '+' : ''}${this.formatNumber(change)}` : '-',
              trend
            }
          })
          this.renderTrendChart(this.tableData)
        }
      } catch (error) {
        this.$message.error('加载数据失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
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
  margin-bottom: 14px;
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
.trend-chart-panel {
  height: 520px;
}
.trend-chart {
  width: 100%;
  height: 520px;
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
