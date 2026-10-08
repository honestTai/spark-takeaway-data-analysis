<template>
  <div class="crawler-container">
    <div class="card-container">
      <h2>商户数据爬取</h2>
      <el-form :model="crawlerForm" label-width="120px" class="crawler-form">
        <el-form-item label="搜索关键词">
          <el-input
            v-model="crawlerForm.keyword"
            placeholder="请输入搜索关键词（如：地区、商圈）"
            style="width: 400px"
          >
            <template slot="prepend">美团</template>
          </el-input>
        </el-form-item>
        <el-form-item label="爬取数量">
          <el-input-number
            v-model="crawlerForm.pageSize"
            :min="1"
            :max="100"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #999">建议每次不超过50条</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleCrawl">
            <i class="el-icon-download"></i> 开始爬取
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 爬取结果 -->
      <el-divider content-position="left">爬取结果</el-divider>
      <el-table
        v-loading="loading"
        :data="merchantList"
        border
        stripe
        style="width: 100%"
        max-height="500"
      >
        <el-table-column prop="merchantName" label="商户名称" width="200" />
        <el-table-column prop="merchantType" label="商户类型" width="120" />
        <el-table-column prop="rating" label="评分" width="100">
          <template slot-scope="scope">
            <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="totalSales" label="总销量" width="120" />
        <el-table-column prop="minOrderPrice" label="起送价" width="100">
          <template slot-scope="scope">
            ¥{{ scope.row.minOrderPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="handleCrawlDishes(scope.row.merchantId)"
            >
              爬取菜品
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="merchantList.length > 0"
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
import { crawlerApi } from '@/api'

export default {
  name: 'CrawlerMerchants',
  data() {
    return {
      crawlerForm: {
        keyword: '北京',
        pageSize: 10
      },
      loading: false,
      merchantList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  methods: {
    async handleCrawl() {
      if (!this.crawlerForm.keyword) {
        this.$message.warning('请输入搜索关键词')
        return
      }

      this.loading = true
      try {
        const res = await crawlerApi.crawlMerchants({
          keyword: this.crawlerForm.keyword,
          pageSize: this.crawlerForm.pageSize
        })

        if (res.data) {
          this.merchantList = res.data
          this.total = res.data.length
          this.$message.success(`成功爬取 ${res.data.length} 条商户数据`)
        }
      } catch (error) {
        this.$message.error('爬取失败：' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },
    async handleCrawlDishes(merchantId) {
      this.$message.info('开始爬取该商户的菜品数据...')
      try {
        const res = await crawlerApi.crawlDishes(merchantId)
        if (res.data) {
          this.$message.success(`成功爬取 ${res.data.length} 条菜品数据`)
        }
      } catch (error) {
        this.$message.error('爬取菜品失败：' + (error.message || '未知错误'))
      }
    },
    handleReset() {
      this.crawlerForm = {
        keyword: '北京',
        pageSize: 10
      }
      this.merchantList = []
    },
    handleSizeChange(val) {
      this.pageSize = val
    },
    handleCurrentChange(val) {
      this.currentPage = val
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

