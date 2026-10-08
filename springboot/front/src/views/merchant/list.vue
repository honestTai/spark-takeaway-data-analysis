<template>
  <div class="merchant-container">
    <div class="card-container">
      <h2>商户列表</h2>
      <el-form :inline="true" style="margin-bottom: 20px">
        <el-form-item label="商户名称">
          <el-input v-model="queryForm.merchantName" clearable placeholder="输入名称搜索" style="width: 180px" @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item label="商户类型">
          <el-select v-model="queryForm.merchantType" clearable placeholder="请选择" style="width: 150px">
            <el-option label="中餐" value="中餐" />
            <el-option label="西餐" value="西餐" />
            <el-option label="快餐" value="快餐" />
            <el-option label="甜品" value="甜品" />
            <el-option label="火锅" value="火锅" />
            <el-option label="烧烤" value="烧烤" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="loading"
        :data="merchantList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="merchantName" label="商户名称" width="200" />
        <el-table-column prop="merchantType" label="商户类型" width="120" />
        <el-table-column prop="rating" label="评分" width="150">
          <template slot-scope="scope">
            <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="totalSales" label="总销量" width="120" sortable>
          <template slot-scope="scope">
            <span style="color: #409EFF; font-weight: bold">{{ scope.row.totalSales }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="monthlySales" label="月销量" width="120" sortable />
        <el-table-column prop="minOrderPrice" label="起送价" width="100">
          <template slot-scope="scope">
            <span style="color: #f56c6c">¥{{ scope.row.minOrderPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryFee" label="配送费" width="100">
          <template slot-scope="scope">
            <span>¥{{ scope.row.deliveryFee }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleView(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="margin-top: 20px; text-align: right"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script>
import { merchantApi } from '@/api'

export default {
  name: 'MerchantList',
  data() {
    return {
      queryForm: {
        merchantType: '',
        merchantName: ''
      },
      loading: false,
      merchantList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await merchantApi.getMerchantList({
          page: this.currentPage,
          size: this.pageSize,
          merchantType: this.queryForm.merchantType || undefined,
          merchantName: this.queryForm.merchantName || undefined
        })

        if (res.data) {
          this.merchantList = res.data.list || []
          this.total = res.data.total || 0
        }
      } catch (error) {
        this.$message.error('加载数据失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.loadData()
    },
    handleReset() {
      this.queryForm.merchantType = ''
      this.queryForm.merchantName = ''
      this.currentPage = 1
      this.loadData()
    },
    handleView(row) {
      this.$message.info('查看商户详情：' + row.merchantName)
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadData()
    }
  }
}
</script>

<style lang="scss" scoped>
.merchant-container {
  h2 {
    margin-bottom: 20px;
    color: #333;
  }
}
</style>

