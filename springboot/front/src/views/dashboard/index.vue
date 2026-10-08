<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h1>数据概览</h1>
      <p>实时监控外卖数据分析系统运行状态</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-title">商户总数</div>
          <div class="stat-value">{{ statistics.merchantCount }}</div>
          <div class="stat-desc">
            <i class="el-icon-arrow-up"></i> 较上月增长 12%
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card stat-card-success">
          <div class="stat-title">订单总数</div>
          <div class="stat-value">{{ statistics.orderCount }}</div>
          <div class="stat-desc">
            <i class="el-icon-arrow-up"></i> 较上月增长 8%
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card stat-card-warning">
          <div class="stat-title">菜品总数</div>
          <div class="stat-value">{{ statistics.dishCount }}</div>
          <div class="stat-desc">
            <i class="el-icon-arrow-up"></i> 较上月增长 15%
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card stat-card-info">
          <div class="stat-title">用户总数</div>
          <div class="stat-value">{{ statistics.userCount }}</div>
          <div class="stat-desc">
            <i class="el-icon-arrow-up"></i> 较上月增长 20%
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="24">
        <div class="card-container">
          <h3>订单趋势</h3>
          <ve-line
            v-if="orderTrendChartData.rows.length > 0"
            :data="orderTrendChartData"
            :settings="orderTrendSettings"
            :extend="orderTrendExtend"
            height="300px"
          />
          <div v-else class="no-data">暂无订单趋势数据</div>
        </div>
      </el-col>
      <!-- <el-col :xs="24" :sm="24" :md="12">
        <div class="card-container">
          <h3>商户类型分布</h3>
          <ve-pie
            v-if="merchantTypeChartData.rows.length > 0"
            :data="merchantTypeChartData"
            :settings="pieSettings"
            :extend="pieExtend"
            height="300px"
          />
          <div v-else class="no-data">暂无商户类型数据</div>
        </div>
      </el-col> -->
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="24">
        <div class="card-container">
          <h3>热门商户排行</h3>
          <ve-bar
            v-if="merchantRankChartData.rows.length > 0"
            :data="merchantRankChartData"
            :settings="barSettings"
            :extend="barExtend"
            height="350px"
          />
          <div v-else class="no-data">暂无商户排行数据</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { analysisApi, dashboardApi } from '@/api'

export default {
  name: 'Dashboard',
  data() {
    return {
      statistics: {
        merchantCount: '-',
        orderCount: '-',
        dishCount: '-',
        userCount: '-'
      },
      orderTrendData: {
        columns: ['日期', '订单数'],
        rows: []
      },
      orderTrendSettings: {
        area: true
      },
      orderTrendExtend: {
        color: ['#667eea', '#764ba2'],
        grid: {
          top: 40,
          right: 40,
          bottom: 40,
          left: 40
        }
      },
      merchantTypeData: {
        columns: ['类型', '数量'],
        rows: []
      },
      pieSettings: {
        radius: [40, 80],
        offsetY: 200
      },
      pieExtend: {
        color: ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#00f2fe']
      },
      merchantRankData: {
        columns: ['商户名称', '销量'],
        rows: []
      },
      barSettings: {
        type: 'bar'
      },
      barExtend: {
        color: ['#667eea'],
        grid: {
          top: 40,
          right: 40,
          bottom: 40,
          left: 80
        }
      }
    }
  },
  computed: {
    orderTrendChartData() {
      return {
        columns: this.orderTrendData.columns || [],
        rows: Array.isArray(this.orderTrendData.rows) ? this.orderTrendData.rows : []
      }
    },
    merchantTypeChartData() {
      return {
        columns: this.merchantTypeData.columns || [],
        rows: Array.isArray(this.merchantTypeData.rows) ? this.merchantTypeData.rows : []
      }
    },
    merchantRankChartData() {
      return {
        columns: this.merchantRankData.columns || [],
        rows: Array.isArray(this.merchantRankData.rows) ? this.merchantRankData.rows : []
      }
    }
  },
  mounted() {
    this.loadStatistics()
    this.loadOrderTrend()
    this.loadMerchantTypeDist()
    this.loadTopMerchants()
  },
  methods: {
    async loadStatistics() {
      try {
        const res = await dashboardApi.getStatistics()
        if (res.data) {
          this.statistics = {
            merchantCount: res.data.merchantCount || 0,
            orderCount: res.data.orderCount || 0,
            dishCount: res.data.dishCount || 0,
            userCount: res.data.userCount || 0
          }
        }
      } catch (error) {
        console.error('加载统计数据失败', error)
      }
    },
    async loadOrderTrend() {
      try {
        const res = await analysisApi.getOrderTrend(7)
        if (res.data && res.data.length > 0) {
          this.orderTrendData.rows = res.data.map(item => ({
            日期: item.order_date || item.orderDate,
            订单数: item.order_count || item.orderCount || 0
          }))
        }
      } catch (error) {
        console.error('加载订单趋势失败', error)
      }
    },
    async loadMerchantTypeDist() {
      try {
        const res = await dashboardApi.getMerchantTypeDist()
        const list = res && res.data
        const arr = Array.isArray(list) ? list : (list && list.list) ? list.list : []
        if (arr.length > 0) {
          const rows = arr.map(item => ({
            类型: item.merchant_type || item.merchantType || item.type || '未知',
            数量: item.count != null ? item.count : (item.merchantCount || 0)
          }))
          this.$set(this.merchantTypeData, 'rows', rows)
        }
      } catch (error) {
        console.error('加载商户类型分布失败', error)
      }
    },
    async loadTopMerchants() {
      try {
        const res = await dashboardApi.getTopMerchants(10)
        if (res.data && res.data.length > 0) {
          this.merchantRankData.rows = res.data.map(item => ({
            商户名称: item.merchantName || item.merchant_name || '',
            销量: item.totalSales || item.total_sales || 0
          }))
        }
      } catch (error) {
        console.error('加载热门商户失败', error)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  .dashboard-header {
    margin-bottom: 20px;

    h1 {
      font-size: 28px;
      color: #333;
      margin-bottom: 10px;
    }

    p {
      color: #999;
      font-size: 14px;
    }
  }

  .stat-cards {
    margin-bottom: 20px;
  }

  .no-data {
    height: 260px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 14px;
  }
}
</style>

