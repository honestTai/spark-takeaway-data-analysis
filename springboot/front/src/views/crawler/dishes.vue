<template>
  <div class="crawler-container">
    <div class="card-container">
      <h2>菜品数据爬取</h2>
      <el-form :model="crawlerForm" label-width="120px" class="crawler-form">
        <el-form-item label="商户ID">
          <el-input
            v-model="crawlerForm.merchantId"
            placeholder="请输入商户ID"
            style="width: 300px"
          />
          <el-button
            type="primary"
            style="margin-left: 10px"
            :loading="loading"
            @click="handleCrawl"
          >
            <i class="el-icon-download"></i> 开始爬取
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 爬取结果 -->
      <el-divider content-position="left">爬取结果</el-divider>
      <el-table
        v-loading="loading"
        :data="dishList"
        border
        stripe
        style="width: 100%"
        max-height="500"
      >
        <el-table-column prop="dishName" label="菜品名称" width="200" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="100">
          <template slot-scope="scope">
            <span style="color: #f56c6c; font-weight: bold">¥{{ scope.row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原价" width="100">
          <template slot-scope="scope">
            <span v-if="scope.row.originalPrice" style="text-decoration: line-through; color: #999">
              ¥{{ scope.row.originalPrice }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="salesCount" label="销量" width="120" />
        <el-table-column prop="monthlySales" label="月销量" width="120" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>

<script>
import { crawlerApi } from '@/api'

export default {
  name: 'CrawlerDishes',
  data() {
    return {
      crawlerForm: {
        merchantId: ''
      },
      loading: false,
      dishList: []
    }
  },
  methods: {
    async handleCrawl() {
      if (!this.crawlerForm.merchantId) {
        this.$message.warning('请输入商户ID')
        return
      }

      this.loading = true
      try {
        const res = await crawlerApi.crawlDishes(this.crawlerForm.merchantId)

        if (res.data) {
          this.dishList = res.data
          this.$message.success(`成功爬取 ${res.data.length} 条菜品数据`)
        }
      } catch (error) {
        this.$message.error('爬取失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.crawler-container {
  .crawler-form {
    margin: 20px 0;
  }
}
</style>

