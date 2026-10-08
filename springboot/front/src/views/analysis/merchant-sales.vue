<template>
  <div class="analysis-container">
    <div class="card-container">
      <h2>商户销量统计与排名</h2>
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

      <!-- 差异化排名图 -->
      <div v-loading="loading" class="chart-container-large sales-chart-panel">
        <div ref="salesChart" class="sales-chart"></div>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe style="margin-top: 20px">
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="merchant_name" label="商户名称" />
        <el-table-column prop="total_sales" label="总销量" sortable>
          <template slot-scope="scope">
            <span style="color: #409EFF; font-weight: bold">{{ scope.row.total_sales }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" sortable>
          <template slot-scope="scope">
            <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900" />
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
import 'echarts/lib/component/legend'

export default {
  name: 'MerchantSales',
  data() {
    return {
      limit: 10,
      loading: false,
      salesChart: null,
      tableData: [],
      chartRows: []
    }
  },
  mounted() {
    this.loadData()
    this._resizeHandler = () => { this.salesChart && this.salesChart.resize() }
    window.addEventListener('resize', this._resizeHandler)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this._resizeHandler)
    if (this.salesChart) {
      this.salesChart.dispose()
      this.salesChart = null
    }
  },
  methods: {
    formatNumber(value) {
      return Number(value || 0).toLocaleString()
    },
    renderSalesChart(rows) {
      this.$nextTick(() => {
        const el = this.$refs.salesChart
        if (!el) return
        if (!this.salesChart) {
          this.salesChart = echarts.init(el)
        }

        const values = rows.map(item => Number(item.total_sales) || 0)
        const max = Math.max(...values, 1)
        const min = Math.min(...values, max)
        const axisMin = Math.max(0, Math.floor(min - Math.max((max - min) * 0.25, max * 0.001)))
        const topValue = values[0] || max
        const names = rows.map((item, index) => `No.${index + 1} ${item.merchant_name}`)
        const rankingColors = ['#2f7ed8', '#00a6a6', '#f5a623', '#e15f41', '#6c5ce7', '#4b7bec', '#20bf6b', '#eb3b5a', '#8854d0', '#45aaf2']

        this.salesChart.setOption({
          color: rankingColors,
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            formatter: params => {
              const item = params[0]
              const row = rows[item.dataIndex]
              const diff = topValue - row.total_sales
              return `${row.merchant_name}<br/>总销量：${this.formatNumber(row.total_sales)}<br/>距第1名：${this.formatNumber(diff)}<br/>评分：${row.rating || '-'}`
            }
          },
          grid: { top: 28, right: 120, bottom: 38, left: 150 },
          xAxis: {
            type: 'value',
            min: axisMin,
            max: Math.ceil(max + Math.max((max - min) * 0.15, 20)),
            axisLabel: { formatter: value => this.formatNumber(value) },
            splitLine: { lineStyle: { type: 'dashed', color: '#dcdfe6' } }
          },
          yAxis: {
            type: 'category',
            inverse: true,
            data: names,
            axisTick: { show: false },
            axisLabel: { color: '#303133', fontSize: 13 }
          },
          series: [{
            name: '总销量',
            type: 'bar',
            barWidth: 22,
            data: rows.map((item, index) => ({
              value: item.total_sales,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: rankingColors[index % rankingColors.length] },
                  { offset: 1, color: '#8fb8ff' }
                ]),
                barBorderRadius: [0, 6, 6, 0]
              }
            })),
            label: {
              show: true,
              position: 'right',
              formatter: params => {
                const row = rows[params.dataIndex]
                const diff = topValue - row.total_sales
                return `${this.formatNumber(row.total_sales)}  差${this.formatNumber(diff)}`
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
        const res = await analysisApi.getMerchantSales(this.limit)
        if (res.data) {
          const rows = res.data.map(item => ({
            merchant_name: item.merchant_name || item.merchantName,
            total_sales: Number(item.total_sales || item.totalSales) || 0,
            rating: Number(item.rating) || 0
          }))
          this.tableData = rows
          this.chartRows = rows
          this.renderSalesChart(rows)
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
.sales-chart-panel {
  height: 560px;
}
.sales-chart {
  width: 100%;
  height: 560px;
}
</style>

