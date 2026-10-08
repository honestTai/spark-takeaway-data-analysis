<template>
  <div class="analysis-container">
    <div class="card-container">
      <h2>菜品类别占比分析</h2>

      <div class="summary-row" v-if="tableData.length > 0">
        <div class="summary-item">
          <div class="summary-label">菜品总数</div>
          <div class="summary-value">{{ formatNumber(totalDishes) }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-label">类别数量</div>
          <div class="summary-value">{{ tableData.length }}</div>
        </div>
        <div class="summary-item">
          <div class="summary-label">最多类别</div>
          <div class="summary-value">{{ topCategory.category }}</div>
        </div>
      </div>

      <!-- 类别占比图 -->
      <div v-loading="loading" class="chart-container-large category-chart-panel">
        <div ref="categoryChart" class="category-chart"></div>
        <div v-if="!loading && tableData.length === 0" class="no-data-hint">暂无菜品类别数据</div>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe style="margin-top: 20px">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="category" label="菜品类别" />
        <el-table-column prop="dish_count" label="菜品数量" sortable>
          <template slot-scope="scope">
            <span style="color: #409EFF; font-weight: bold">{{ scope.row.dish_count }}</span>
          </template>
        </el-table-column>
        <el-table-column label="占比">
          <template slot-scope="scope">
            <el-progress
              :percentage="Number(scope.row.percent)"
              :color="getProgressColor(scope.$index)"
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
  name: 'DishCategory',
  data() {
    return {
      loading: false,
      categoryChart: null,
      tableData: [],
      totalDishes: 0
    }
  },
  computed: {
    topCategory() {
      return [...this.tableData].sort((a, b) => b.dish_count - a.dish_count)[0] || { category: '-' }
    }
  },
  mounted() {
    this.loadData()
    this._resizeHandler = () => { this.categoryChart && this.categoryChart.resize() }
    window.addEventListener('resize', this._resizeHandler)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this._resizeHandler)
    if (this.categoryChart) {
      this.categoryChart.dispose()
      this.categoryChart = null
    }
  },
  methods: {
    formatNumber(value) {
      return Number(value || 0).toLocaleString()
    },
    renderCategoryChart(rows) {
      this.$nextTick(() => {
        const el = this.$refs.categoryChart
        if (!el || rows.length === 0) return
        if (!this.categoryChart) {
          this.categoryChart = echarts.init(el)
        }
        const sortedRows = [...rows].sort((a, b) => b.dish_count - a.dish_count)
        const colors = ['#2f7ed8', '#00a6a6', '#f5a623', '#e15f41', '#6c5ce7', '#20bf6b', '#8854d0', '#45aaf2']
        this.categoryChart.setOption({
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            formatter: params => {
              const row = sortedRows[params[0].dataIndex]
              return `${row.category}<br/>菜品数量：${this.formatNumber(row.dish_count)}<br/>占比：${row.percent}%`
            }
          },
          grid: { top: 28, right: 120, bottom: 36, left: 150 },
          xAxis: {
            type: 'value',
            axisLabel: { formatter: value => this.formatNumber(value) },
            splitLine: { lineStyle: { type: 'dashed', color: '#dcdfe6' } }
          },
          yAxis: {
            type: 'category',
            inverse: true,
            data: sortedRows.map((item, index) => `No.${index + 1} ${item.category}`),
            axisTick: { show: false },
            axisLabel: { color: '#303133', width: 130, overflow: 'truncate' }
          },
          series: [{
            name: '菜品数量',
            type: 'bar',
            barWidth: 20,
            data: sortedRows.map((item, index) => ({
              value: item.dish_count,
              itemStyle: { color: colors[index % colors.length], barBorderRadius: [0, 5, 5, 0] }
            })),
            label: {
              show: true,
              position: 'right',
              formatter: params => {
                const row = sortedRows[params.dataIndex]
                return `${this.formatNumber(row.dish_count)}  ${row.percent}%`
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
        const res = await analysisApi.getDishCategoryDistribution()
        const list = Array.isArray(res && res.data) ? res.data : ((res && res.data && res.data.list) ? res.data.list : []) || []
        if (list.length > 0) {
          const rows = list.map(item => ({
            category: item.category || item.categoryName || '未分类',
            dish_count: Number(item.dish_count != null ? item.dish_count : (item.dishCount || 0)) || 0
          }))
          this.totalDishes = rows.reduce((sum, item) => sum + item.dish_count, 0)
          this.tableData = rows.map(item => ({
            ...item,
            percent: this.totalDishes > 0 ? (item.dish_count / this.totalDishes * 100).toFixed(2) : '0.00'
          }))
          this.renderCategoryChart(this.tableData)
        }
      } catch (error) {
        this.$message.error('加载数据失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    getProgressColor(index) {
      const colors = ['#2f7ed8', '#00a6a6', '#f5a623', '#e15f41', '#6c5ce7', '#20bf6b', '#8854d0', '#45aaf2']
      return colors[index % colors.length]
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
.category-chart-panel {
  height: 520px;
}
.category-chart {
  width: 100%;
  height: 520px;
}
.no-data-hint {
  height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
}
</style>
