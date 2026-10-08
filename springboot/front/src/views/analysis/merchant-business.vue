<template>
  <div class="analysis-container">
    <div class="card-container">
      <h2>商户经营分析</h2>

      <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
        <!-- 评分与销量关系 -->
        <el-tab-pane label="评分与销量关系" name="rating">
          <div class="rating-summary" v-if="ratingTable.length > 0">
            <div class="summary-item">
              <div class="summary-label">商户数量</div>
              <div class="summary-value">{{ ratingStats.count }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">平均评分</div>
              <div class="summary-value">{{ ratingStats.avgRating }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">最高销量</div>
              <div class="summary-value">{{ formatNumber(ratingStats.maxSales) }}</div>
            </div>
          </div>
          <div v-loading="loadingRating" class="chart-container-large rating-chart-panel">
            <div ref="ratingChart" class="rating-chart"></div>
            <div v-if="!loadingRating && ratingTable.length === 0" class="no-data-hint">暂无评分与销量数据</div>
          </div>
          <el-table v-loading="loadingRating" :data="ratingTable" border stripe style="margin-top: 20px">
            <el-table-column prop="merchant_name" label="商户名称" />
            <el-table-column prop="rating" label="评分" sortable>
              <template slot-scope="scope">
                <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900" />
              </template>
            </el-table-column>
            <el-table-column prop="total_sales" label="总销量" sortable>
              <template slot-scope="scope">
                <span style="color: #409EFF; font-weight: bold">{{ scope.row.total_sales }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 商户类型对比 -->
        <el-tab-pane label="商户类型对比" name="type">
          <div v-loading="loadingType" class="chart-container-large business-chart-panel">
            <div v-show="typeTable.length > 0" ref="typeChart" class="business-chart"></div>
            <div v-if="!loadingType && typeTable.length === 0" class="no-data-hint">暂无商户类型数据，请先导入或爬取商户数据</div>
          </div>
          <el-table v-loading="loadingType" :data="typeTable" border stripe style="margin-top: 20px">
            <el-table-column prop="merchant_type" label="商户类型" />
            <el-table-column prop="avg_rating" label="平均评分" sortable>
              <template slot-scope="scope">
                <span style="color: #E6A23C; font-weight: bold">{{ scope.row.avg_rating }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="total_sales" label="总销量" sortable>
              <template slot-scope="scope">
                <span style="color: #409EFF; font-weight: bold">{{ scope.row.total_sales }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="merchant_count" label="商户数量" sortable>
              <template slot-scope="scope">
                <span style="color: #67c23a; font-weight: bold">{{ scope.row.merchant_count }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="avg_sales_per_merchant" label="店均销量" sortable>
              <template slot-scope="scope">
                <span style="color: #00a6a6; font-weight: bold">{{ formatNumber(scope.row.avg_sales_per_merchant) }}</span>
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
import 'echarts/lib/component/legend'
import 'echarts/lib/component/grid'

export default {
  name: 'MerchantBusiness',
  data() {
    return {
      activeTab: 'rating',
      loadingRating: false,
      loadingType: false,
      ratingChart: null,
      typeChart: null,
      ratingTable: [],
      typeTable: [],
      typeChartRows: []
    }
  },
  computed: {
    ratingStats() {
      const rows = this.ratingTable || []
      const count = rows.length
      const totalRating = rows.reduce((sum, item) => sum + (Number(item.rating) || 0), 0)
      const maxSales = Math.max(...rows.map(item => Number(item.total_sales || item.totalSales) || 0), 0)
      return {
        count,
        avgRating: count > 0 ? (totalRating / count).toFixed(1) : '0.0',
        maxSales
      }
    }
  },
  mounted() {
    this.loadRatingData()
    this.loadTypeData()
    this._resizeHandler = () => {
      this.ratingChart && this.ratingChart.resize()
      this.typeChart && this.typeChart.resize()
    }
    window.addEventListener('resize', this._resizeHandler)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this._resizeHandler)
    if (this.ratingChart) {
      this.ratingChart.dispose()
      this.ratingChart = null
    }
    if (this.typeChart) {
      this.typeChart.dispose()
      this.typeChart = null
    }
  },
  methods: {
    handleTabClick() {
      if (this.activeTab === 'type') {
        this.renderTypeChart(this.typeChartRows)
      } else {
        this.renderRatingChart(this.ratingTable)
      }
    },
    formatNumber(value) {
      return Number(value || 0).toLocaleString()
    },
    normalize(value, max) {
      if (!max) return 0
      return Math.round((Number(value || 0) / max) * 100)
    },
    renderRatingChart(data) {
      this.$nextTick(() => {
        if (!data || data.length === 0) return
        const el = this.$refs.ratingChart
        if (!el) return
        if (!this.ratingChart) {
          this.ratingChart = echarts.init(el)
        }
        const rows = data
          .map(item => ({
            merchant_name: item.merchant_name || item.merchantName || '未知商户',
            rating: Number(item.rating) || 0,
            total_sales: Number(item.total_sales || item.totalSales) || 0
          }))
          .sort((a, b) => b.total_sales - a.total_sales)
          .slice(0, 20)
        const maxSales = Math.max(...rows.map(item => item.total_sales), 1)
        const minSales = Math.min(...rows.map(item => item.total_sales), maxSales)
        const axisMin = Math.max(0, Math.floor(minSales - Math.max((maxSales - minSales) * 0.3, maxSales * 0.03)))
        const colors = ['#2f7ed8', '#00a6a6', '#f5a623', '#e15f41', '#6c5ce7', '#20bf6b']
        const option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            formatter: params => {
              const row = rows[params[0].dataIndex]
              return `${row.merchant_name}<br/>总销量：${this.formatNumber(row.total_sales)}<br/>评分：${row.rating}`
            }
          },
          grid: { top: 28, right: 130, bottom: 36, left: 150 },
          xAxis: {
            type: 'value',
            min: axisMin,
            axisLabel: { formatter: value => this.formatNumber(value) },
            splitLine: { lineStyle: { type: 'dashed', color: '#dcdfe6' } }
          },
          yAxis: {
            type: 'category',
            inverse: true,
            data: rows.map((item, index) => `No.${index + 1} ${item.merchant_name}`),
            axisTick: { show: false },
            axisLabel: {
              color: '#303133',
              width: 130,
              overflow: 'truncate'
            }
          },
          series: [{
            name: '总销量',
            type: 'bar',
            barWidth: 18,
            data: rows.map((item, index) => ({
              value: item.total_sales,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: colors[index % colors.length] },
                  { offset: 1, color: '#8fc5ff' }
                ]),
                barBorderRadius: [0, 5, 5, 0]
              }
            })),
            label: {
              show: true,
              position: 'right',
              formatter: params => {
                const row = rows[params.dataIndex]
                return `${this.formatNumber(row.total_sales)}  评分${row.rating}`
              },
              color: '#303133',
              fontWeight: 600
            }
          }]
        }
        this.ratingChart.setOption(option, true)
      })
    },
    renderTypeChart(rows) {
      this.$nextTick(() => {
        if (this.activeTab !== 'type' || !rows || rows.length === 0) return
        const el = this.$refs.typeChart
        if (!el) return
        if (!this.typeChart) {
          this.typeChart = echarts.init(el)
        }

        const sortedRows = [...rows].sort((a, b) => b.total_sales - a.total_sales)
        const maxAvgSales = Math.max(...sortedRows.map(item => item.avg_sales_per_merchant), 1)
        const colors = ['#2f7ed8', '#00a6a6', '#f5a623', '#e15f41', '#6c5ce7', '#20bf6b', '#8854d0', '#45aaf2']
        const names = sortedRows.map(item => item.merchant_type)
        const topSales = sortedRows[0] ? sortedRows[0].total_sales : 0

        this.typeChart.setOption({
          color: colors,
          legend: {
            top: 4,
            left: 'center',
            data: ['总销量', '店均销量', '商户数量']
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            formatter: params => {
              const row = sortedRows[params[0].dataIndex]
              if (!row) return params.name
              return `${row.merchant_type}<br/>总销量：${this.formatNumber(row.total_sales)}<br/>店均销量：${this.formatNumber(row.avg_sales_per_merchant)}<br/>商户数量：${row.merchant_count}<br/>平均评分：${row.avg_rating}`
            }
          },
          grid: [
            { top: 58, right: '52%', bottom: 46, left: 130 },
            { top: 58, right: 70, bottom: 46, left: '56%' }
          ],
          xAxis: [
            {
              type: 'value',
              gridIndex: 0,
              axisLabel: { formatter: value => this.formatNumber(value) },
              splitLine: { lineStyle: { type: 'dashed', color: '#dcdfe6' } }
            },
            {
              type: 'value',
              gridIndex: 1,
              axisLabel: { formatter: value => this.formatNumber(value) },
              splitLine: { lineStyle: { type: 'dashed', color: '#dcdfe6' } }
            }
          ],
          yAxis: [
            {
              type: 'category',
              gridIndex: 0,
              inverse: true,
              data: names,
              axisTick: { show: false },
              axisLabel: { color: '#303133' }
            },
            {
              type: 'category',
              gridIndex: 1,
              inverse: true,
              data: names,
              axisTick: { show: false },
              axisLabel: { show: false }
            }
          ],
          series: [
            {
              name: '总销量',
              type: 'bar',
              xAxisIndex: 0,
              yAxisIndex: 0,
              barWidth: 18,
              data: sortedRows.map((item, index) => ({
                value: item.total_sales,
                itemStyle: { color: colors[index % colors.length], barBorderRadius: [0, 5, 5, 0] }
              })),
              label: {
                show: true,
                position: 'right',
                formatter: params => {
                  const row = sortedRows[params.dataIndex]
                  const diff = topSales - row.total_sales
                  return `${this.formatNumber(params.value)}  差${this.formatNumber(diff)}`
                },
                color: '#303133',
                fontWeight: 600
              }
            },
            {
              name: '店均销量',
              type: 'bar',
              xAxisIndex: 1,
              yAxisIndex: 1,
              barWidth: 18,
              data: sortedRows.map((item, index) => ({
                value: item.avg_sales_per_merchant,
                itemStyle: { color: colors[index % colors.length], barBorderRadius: [0, 5, 5, 0] }
              })),
              label: {
                show: true,
                position: 'right',
                formatter: params => `${this.formatNumber(params.value)} / 店`,
                color: '#303133',
                fontWeight: 600
              }
            },
            {
              name: '商户数量',
              type: 'bar',
              xAxisIndex: 1,
              yAxisIndex: 1,
              barWidth: 8,
              barGap: '-75%',
              data: sortedRows.map(item => ({
                value: Math.round((item.merchant_count / Math.max(...sortedRows.map(row => row.merchant_count), 1)) * maxAvgSales),
                rawCount: item.merchant_count,
                itemStyle: { color: 'rgba(144, 147, 153, 0.35)', barBorderRadius: [0, 4, 4, 0] }
              })),
              label: {
                show: true,
                position: 'insideLeft',
                formatter: params => `${params.data.rawCount}家`,
                color: '#606266'
              }
            }
          ]
        }, true)
      })
    },
    async loadRatingData() {
      this.loadingRating = true
      try {
        const res = await analysisApi.getRatingVsSales()
        if (res.data && res.data.length > 0) {
          this.ratingTable = res.data
            .map(item => ({
              merchant_name: item.merchant_name || item.merchantName || '未知商户',
              rating: Number(item.rating) || 0,
              total_sales: Number(item.total_sales || item.totalSales) || 0
            }))
            .sort((a, b) => b.total_sales - a.total_sales)
          this.renderRatingChart(this.ratingTable)
        }
      } catch (error) {
        this.$message.error('加载评分数据失败：' + (error.message || '未知错误'))
      } finally {
        this.loadingRating = false
      }
    },
    async loadTypeData() {
      this.loadingType = true
      try {
        const res = await analysisApi.getMerchantTypeComparison()
        if (res.data && res.data.length > 0) {
          this.typeTable = res.data
          this.typeChartRows = res.data.map(item => {
            const totalSales = Number(item.total_sales || item.totalSales) || 0
            const merchantCount = Number(item.merchant_count || item.merchantCount) || 0
            return {
            merchant_type: item.merchant_type || item.merchantType || '未知类型',
            avg_rating: parseFloat(item.avg_rating || item.avgRating) || 0,
              total_sales: totalSales,
              merchant_count: merchantCount,
              avg_sales_per_merchant: merchantCount > 0 ? Math.round(totalSales / merchantCount) : 0
            }
          })
          this.typeTable = this.typeChartRows
          this.renderTypeChart(this.typeChartRows)
        } else {
          this.$message.warning('暂无商户类型数据，请先导入或爬取商户数据')
        }
      } catch (error) {
        this.$message.error('加载类型数据失败：' + (error.message || '未知错误'))
      } finally {
        this.loadingType = false
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
.no-data-hint {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
}
.rating-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin: 18px 0 8px;
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
.rating-chart-panel {
  height: 560px;
}
.rating-chart {
  width: 100%;
  height: 560px;
}
.business-chart-panel {
  height: 560px;
}
.business-chart {
  width: 100%;
  height: 560px;
}
</style>

