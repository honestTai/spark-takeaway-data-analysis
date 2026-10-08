<template>
  <div class="cleaning-container">
    <div class="card-container">
      <h2>数据清洗</h2>
      <p style="color: #999; margin-bottom: 20px">对导入的数据进行去重、异常值处理、数据标准化等清洗操作</p>

      <el-tabs v-model="activeTab" type="card">
        <!-- 清洗规则配置 -->
        <el-tab-pane label="清洗规则" name="rules">
          <el-card>
            <div slot="header">
              <span>清洗规则配置</span>
            </div>
            <el-form :model="cleaningRules" label-width="200px" v-loading="loading">
              <el-form-item label="评分范围">
                <el-input-number v-model="cleaningRules.ratingMin" :min="0" :max="5" :precision="1" />
                <span style="margin: 0 10px">-</span>
                <el-input-number v-model="cleaningRules.ratingMax" :min="0" :max="5" :precision="1" />
                <span style="margin-left: 10px; color: #999">（0-5分）</span>
              </el-form-item>
              <el-form-item label="价格范围">
                <el-input-number v-model="cleaningRules.priceMin" :min="0" :precision="2" />
                <span style="margin: 0 10px">-</span>
                <el-input-number v-model="cleaningRules.priceMax" :min="0" :precision="2" />
                <span style="margin-left: 10px; color: #999">（元）</span>
              </el-form-item>
              <el-form-item label="销量范围">
                <el-input-number v-model="cleaningRules.salesMin" :min="0" />
                <span style="margin: 0 10px">-</span>
                <el-input-number v-model="cleaningRules.salesMax" :min="0" />
                <span style="margin-left: 10px; color: #999">（次）</span>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="loading" @click="saveRules">保存规则</el-button>
                <el-button @click="resetRules">重置为默认值</el-button>
                <el-button @click="loadRules">重新加载</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>

        <!-- 清洗统计 -->
        <el-tab-pane label="清洗统计" name="stats">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="6">
              <div class="stat-card">
                <div class="stat-title">总清洗次数</div>
                <div class="stat-value">{{ stats.totalCleaning }}</div>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <div class="stat-card stat-card-success">
                <div class="stat-title">清洗成功</div>
                <div class="stat-value">{{ stats.successCleaning }}</div>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <div class="stat-card stat-card-warning">
                <div class="stat-title">去重数据</div>
                <div class="stat-value">{{ stats.duplicateCount }}</div>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <div class="stat-card stat-card-info">
                <div class="stat-title">无效数据</div>
                <div class="stat-value">{{ stats.invalidCount }}</div>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>

      <!-- 清洗说明 -->
      <el-card style="margin-top: 20px">
        <div slot="header">
          <span>清洗功能说明</span>
        </div>
        <el-collapse>
          <el-collapse-item title="1. 数据去重" name="1">
            <div>
              <p><strong>商户数据：</strong>基于商户名称进行去重（不区分大小写）</p>
              <p><strong>菜品数据：</strong>基于商户ID+菜品名称进行去重</p>
              <p><strong>订单数据：</strong>基于订单编号进行去重</p>
            </div>
          </el-collapse-item>
          <el-collapse-item title="2. 异常值处理" name="2">
            <div>
              <p><strong>评分：</strong>超出0-5范围的值会被修正到边界值</p>
              <p><strong>价格：</strong>超出配置范围的值会被修正到边界值</p>
              <p><strong>销量：</strong>超出配置范围的值会被修正到边界值</p>
            </div>
          </el-collapse-item>
          <el-collapse-item title="3. 数据标准化" name="3">
            <div>
              <p><strong>去除空格：</strong>自动去除字段前后空格</p>
              <p><strong>格式统一：</strong>统一电话号码格式，去除特殊字符</p>
              <p><strong>默认值：</strong>为空字段设置合理的默认值</p>
            </div>
          </el-collapse-item>
          <el-collapse-item title="4. 数据验证" name="4">
            <div>
              <p><strong>必填字段：</strong>验证必填字段是否为空</p>
              <p><strong>数据类型：</strong>验证数据类型是否正确</p>
              <p><strong>范围检查：</strong>验证数值是否在有效范围内</p>
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataCleaning',
  data() {
    return {
      activeTab: 'rules',
      cleaningRules: {
        ratingMin: 0,
        ratingMax: 5,
        priceMin: 0,
        priceMax: 10000,
        salesMin: 0,
        salesMax: 10000000
      },
      stats: {
        totalCleaning: 0,
        successCleaning: 0,
        duplicateCount: 0,
        invalidCount: 0
      },
      loading: false
    }
  },
  mounted() {
    this.loadRules()
  },
  methods: {
    async loadRules() {
      this.loading = true
      try {
        const { cleaningApi } = await import('@/api')
        const res = await cleaningApi.getRules()
        if (res.data) {
          this.cleaningRules = {
            ratingMin: res.data.ratingMin || 0,
            ratingMax: res.data.ratingMax || 5,
            priceMin: res.data.priceMin || 0,
            priceMax: res.data.priceMax || 10000,
            salesMin: res.data.salesMin || 0,
            salesMax: res.data.salesMax || 10000000
          }
        }
      } catch (error) {
        this.$message.warning('加载清洗规则失败，使用默认值')
        console.error('加载规则失败', error)
      } finally {
        this.loading = false
      }
    },
    async saveRules() {
      // 验证规则
      if (this.cleaningRules.ratingMin >= this.cleaningRules.ratingMax) {
        this.$message.error('评分最小值必须小于最大值')
        return
      }
      if (this.cleaningRules.priceMin >= this.cleaningRules.priceMax) {
        this.$message.error('价格最小值必须小于最大值')
        return
      }
      if (this.cleaningRules.salesMin >= this.cleaningRules.salesMax) {
        this.$message.error('销量最小值必须小于最大值')
        return
      }

      this.loading = true
      try {
        const { cleaningApi } = await import('@/api')
        const res = await cleaningApi.saveRules(this.cleaningRules)
        if (res.code === 200) {
          this.$message.success('清洗规则已保存')
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存清洗规则失败：' + (error.message || '未知错误'))
        console.error('保存规则失败', error)
      } finally {
        this.loading = false
      }
    },
    async resetRules() {
      this.$confirm('确定要重置为默认规则吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.loading = true
        try {
          const { cleaningApi } = await import('@/api')
          const res = await cleaningApi.resetRules()
          if (res.code === 200) {
            this.$message.success('规则已重置为默认值')
            // 重新加载规则
            await this.loadRules()
          } else {
            this.$message.error(res.message || '重置失败')
          }
        } catch (error) {
          this.$message.error('重置规则失败：' + (error.message || '未知错误'))
        } finally {
          this.loading = false
        }
      }).catch(() => {
        // 用户取消
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.cleaning-container {
  h2 {
    margin-bottom: 10px;
    color: #333;
  }
}
</style>

