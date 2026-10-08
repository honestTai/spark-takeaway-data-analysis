<template>
  <div class="analysis-container">
    <div class="card-container">
      <h2>不同价格区间订单分布分析</h2>

      <div class="summary-row" v-if="tableData.length > 0">
        <div class="summary-item">
          <div class="summary-label">订单总量</div>
          <div class="summary-value">{{ formatNumber(totalOrders) }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-label">最高区间</div>
          <div class="summary-value">{{ topRange.price_range }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-label">最高占比</div>
          <div class="summary-value">{{ topRange.percent }}%</div>
        </div>
      </div>

      <!-- 价格区间分布图 -->
      <div v-loading="loading" class="chart-container-large distribution-chart-panel">
        <div ref="distributionChart" class="distribution-chart"></div>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe style="margin-top: 20px">
        <el-table-column prop="price_range" label="价格区间" />
        <el-table-column prop="order_count" label="订单数量" sortable>
          <template slot-scope="scope">
            <span style="color: #409EFF; font-weight: bold">{{ scope.row.order_count }}</span>
          </template>
        </el-table-column>
        <el-table-column label="占比">
          <template slot-scope="scope">
            <el-progress
              :percentage="Number(scope.row.percent)"
              :color="getProgressColor(scope.row.price_range)"
            />
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
import 'echarts/lib/component/tooltip'
import 'echarts/lib/component/grid'

export default {
  name: 'PriceDistribution',
  data() {
    return {
      loading: false,
      distributionChart: null,
      tableData: [],
      totalOrders: 0
    }
  },
  computed: {
    topRange() {
      const row = [...this.tableData].sort((a, b) => b.order_count - a.order_count)[0] || {}
      const percent = this.totalOrders > 0 ? ((row.order_count || 0) / this.totalOrders * 100).toFixed(1) : '0.0'
      return {
        price_range: row.price_range || '-',
        percent
      }
    }
  },
  mounted() {
    this.loadData()
    this._resizeHandler = () => { this.distributionChart && this.distributionChart.resize() }
    window.addEventListener('resize', this._resizeHandler)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this._resizeHandler)
    if (this.distributionChart) {
      this.distributionChart.dispose()
      this.distributionChart = null
    }
  },
  methods: {
    formatNumber(value) {
      return Number(value || 0).toLocaleString()
    },
    renderDistributionChart(rows) {
      this.$nextTick(() => {
        const el = this.$refs.distributionChart
        if (!el || rows.length === 0) return
        if (!this.distributionChart) {
          this.distributionChart = echarts.init(el)
        }
        const sortedRows = [...rows].sort((a, b) => b.order_count - a.order_count)
        const colors = ['#2f7ed8', '#00a6a6', '#f5a623', '#e15f41', '#6c5ce7']
        this.distributionChart.setOption({
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            formatter: params => {
              const row = sortedRows[params[0].dataIndex]
              return `${row.price_range}<br/>订单数量：${this.formatNumber(row.order_count)}<br/>占比：${row.percent}%`
            }
          },
          grid: { top: 30, right: 120, bottom: 36, left: 110 },
          xAxis: {
            type: 'value',
            axisLabel: { formatter: value => this.formatNumber(value) },
            splitLine: { lineStyle: { type: 'dashed', color: '#dcdfe6' } }
          },
          yAxis: {
            type: 'category',
            inverse: true,
            data: sortedRows.map(item => item.price_range),
            axisTick: { show: false },
            axisLabel: { color: '#303133' }
          },
          series: [{
            name: '订单数量',
            type: 'bar',
            barWidth: 28,
            data: sortedRows.map((item, index) => ({
              value: item.order_count,
              itemStyle: { color: colors[index % colors.length], barBorderRadius: [0, 6, 6, 0] }
            })),
            label: {
              show: true,
              position: 'right',
              formatter: params => {
                const row = sortedRows[params.dataIndex]
                return `${this.formatNumber(row.order_count)}  ${row.percent}%`
              },
              color: '#303133',
              fontWeight: 600
            }
          }]
        }, true)
      })
    },
    async loadData() {
      this.loading = true
      try {
        const res = await analysisApi.getPriceDistribution()
        if (res.data) {
          const rows = res.data.map(item => ({
            price_range: item.price_range || item.priceRange,
            order_count: Number(item.order_count || item.orderCount) || 0
          }))
          this.totalOrders = rows.reduce((sum, item) => sum + item.order_count, 0)
          this.tableData = rows.map(item => ({
            ...item,
            percent: this.totalOrders > 0 ? (item.order_count / this.totalOrders * 100).toFixed(2) : '0.00'
          }))
          this.renderDistributionChart(this.tableData)
        }
      } catch (error) {
        this.$message.error('加载数据失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    getProgressColor(range) {
      const colors = {
        '0-30元': '#67c23a',
        '30-50元': '#409EFF',
        '50-100元': '#E6A23C',
        '100元以上': '#F56C6C'
      }
      return colors[range] || '#409EFF'
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
.distribution-chart-panel {
  height: 460px;
}
.distribution-chart {
  width: 100%;
  height: 460px;
}
</style>

