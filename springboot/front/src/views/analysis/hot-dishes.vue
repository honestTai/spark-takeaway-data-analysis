<template>
  <div class="analysis-container">
    <div class="card-container">
      <h2>热门菜品分析</h2>
      <el-form :inline="true" style="margin-bottom: 20px">
        <el-form-item label="显示数量">
          <el-select v-model="limit" style="width: 150px" @change="loadData">
            <el-option label="Top 10" :value="10" />
            <el-option label="Top 20" :value="20" />
            <el-option label="Top 50" :value="50" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">刷新数据</el-button>
        </el-form-item>
      </el-form>

      <!-- 销量排名图 -->
      <div v-loading="loading" class="chart-container-large hot-chart-panel">
        <div ref="hotChart" class="hot-chart"></div>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe style="margin-top: 20px">
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="dish_name" label="菜品名称" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="sales_count" label="销量" sortable>
          <template slot-scope="scope">
            <span style="color: #F56C6C; font-weight: bold">{{ scope.row.sales_count }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" sortable>
          <template slot-scope="scope">
            <span style="color: #409EFF">¥{{ scope.row.price }}</span>
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
  name: 'HotDishes',
  data() {
    return {
      limit: 10,
      loading: false,
      hotChart: null,
      tableData: [],
      chartRows: []
    }
  },
  mounted() {
    this.loadData()
    this._resizeHandler = () => { this.hotChart && this.hotChart.resize() }
    window.addEventListener('resize', this._resizeHandler)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this._resizeHandler)
    if (this.hotChart) {
      this.hotChart.dispose()
      this.hotChart = null
    }
  },
  methods: {
    formatNumber(value) {
      return Number(value || 0).toLocaleString()
    },
    renderHotChart(rows) {
      this.$nextTick(() => {
        const el = this.$refs.hotChart
        if (!el || rows.length === 0) return
        if (!this.hotChart) {
          this.hotChart = echarts.init(el)
        }
        const sortedRows = [...rows].sort((a, b) => b.sales_count - a.sales_count)
        const topSales = sortedRows[0] ? sortedRows[0].sales_count : 0
        const max = Math.max(...sortedRows.map(item => item.sales_count), 1)
        const min = Math.min(...sortedRows.map(item => item.sales_count), max)
        const axisMin = Math.max(0, Math.floor(min - Math.max((max - min) * 0.25, max * 0.01)))
        const colors = ['#e15f41', '#f5a623', '#2f7ed8', '#00a6a6', '#6c5ce7', '#20bf6b']
        this.hotChart.setOption({
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            formatter: params => {
              const row = sortedRows[params[0].dataIndex]
              return `${row.dish_name}<br/>销量：${this.formatNumber(row.sales_count)}<br/>分类：${row.category || '-'}<br/>价格：¥${row.price || 0}`
            }
          },
          grid: { top: 28, right: 125, bottom: 36, left: 160 },
          xAxis: {
            type: 'value',
            min: axisMin,
            axisLabel: { formatter: value => this.formatNumber(value) },
            splitLine: { lineStyle: { type: 'dashed', color: '#dcdfe6' } }
          },
          yAxis: {
            type: 'category',
            inverse: true,
            data: sortedRows.map((item, index) => `No.${index + 1} ${item.dish_name}`),
            axisTick: { show: false },
            axisLabel: { color: '#303133', width: 145, overflow: 'truncate' }
          },
          series: [{
            name: '销量',
            type: 'bar',
            barWidth: 20,
            data: sortedRows.map((item, index) => ({
              value: item.sales_count,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: colors[index % colors.length] },
                  { offset: 1, color: '#ffc08a' }
                ]),
                barBorderRadius: [0, 5, 5, 0]
              }
            })),
            label: {
              show: true,
              position: 'right',
              formatter: params => {
                const diff = topSales - sortedRows[params.dataIndex].sales_count
                return `${this.formatNumber(params.value)}  差${this.formatNumber(diff)}`
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
        const res = await analysisApi.getHotDishes(this.limit)
        if (res.data) {
          const rows = res.data.map(item => ({
            dish_name: item.dish_name || item.dishName,
            category: item.category || '未分类',
            sales_count: Number(item.sales_count || item.salesCount) || 0,
            price: item.price
          }))
          this.tableData = rows
          this.chartRows = rows
          this.renderHotChart(rows)
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
.hot-chart-panel {
  height: 560px;
}
.hot-chart {
  width: 100%;
  height: 560px;
}
</style>

