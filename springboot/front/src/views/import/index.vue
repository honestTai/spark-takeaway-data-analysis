<template>
  <div class="import-container">
    <div class="card-container">
      <h2>数据导入</h2>
      <el-tabs v-model="activeTab" type="card">
        <!-- 商户数据导入 -->
        <el-tab-pane label="商户数据" name="merchants">
          <el-upload
            ref="merchantUpload"
            :auto-upload="false"
            :on-change="handleMerchantChange"
            :file-list="merchantFileList"
            accept=".csv,.json"
            drag
            style="width: 100%"
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <div slot="tip" class="el-upload__tip">只能上传CSV或JSON文件</div>
          </el-upload>
          <el-button
            type="primary"
            :loading="merchantLoading"
            style="margin-top: 20px"
            @click="handleImportMerchants"
          >
            开始导入
          </el-button>
        </el-tab-pane>

        <!-- 菜品数据导入 -->
        <el-tab-pane label="菜品数据" name="dishes">
          <el-upload
            ref="dishUpload"
            :auto-upload="false"
            :on-change="handleDishChange"
            :file-list="dishFileList"
            accept=".csv,.json"
            drag
            style="width: 100%"
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <div slot="tip" class="el-upload__tip">只能上传CSV或JSON文件</div>
          </el-upload>
          <el-button
            type="primary"
            :loading="dishLoading"
            style="margin-top: 20px"
            @click="handleImportDishes"
          >
            开始导入
          </el-button>
        </el-tab-pane>

        <!-- 订单数据导入 -->
        <el-tab-pane label="订单数据" name="orders">
          <el-upload
            ref="orderUpload"
            :auto-upload="false"
            :on-change="handleOrderChange"
            :file-list="orderFileList"
            accept=".csv,.json"
            drag
            style="width: 100%"
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <div slot="tip" class="el-upload__tip">只能上传CSV或JSON文件</div>
          </el-upload>
          <el-button
            type="primary"
            :loading="orderLoading"
            style="margin-top: 20px"
            @click="handleImportOrders"
          >
            开始导入
          </el-button>
        </el-tab-pane>
      </el-tabs>

      <!-- 导入说明 -->
      <el-card style="margin-top: 20px">
        <div slot="header">
          <span>导入说明</span>
        </div>
        <div>
          <h4>CSV格式示例：</h4>
          <pre>商户名称,商户类型,评分,总销量,起送价
川味小厨,中餐,4.5,8560,20
西式简餐,西餐,4.2,7420,25</pre>
          <h4 style="margin-top: 20px">JSON格式示例：</h4>
          <pre>[{
  "merchantName": "川味小厨",
  "merchantType": "中餐",
  "rating": 4.5,
  "totalSales": 8560,
  "minOrderPrice": 20
}]</pre>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { importApi } from '@/api'

export default {
  name: 'DataImport',
  data() {
    return {
      activeTab: 'merchants',
      merchantFileList: [],
      dishFileList: [],
      orderFileList: [],
      merchantLoading: false,
      dishLoading: false,
      orderLoading: false
    }
  },
  methods: {
    handleMerchantChange(file, fileList) {
      this.merchantFileList = fileList
    },
    handleDishChange(file, fileList) {
      this.dishFileList = fileList
    },
    handleOrderChange(file, fileList) {
      this.orderFileList = fileList
    },
    async handleImportMerchants() {
      if (this.merchantFileList.length === 0) {
        this.$message.warning('请先选择文件')
        return
      }

      this.merchantLoading = true
      try {
        const file = this.merchantFileList[0].raw
        const res = await importApi.importMerchants(file)
        if (res.data) {
          this.$message.success(
            `导入完成！成功：${res.data.successCount}，失败：${res.data.failCount}`
          )
          this.merchantFileList = []
        }
      } catch (error) {
        this.$message.error('导入失败：' + (error.message || '未知错误'))
      } finally {
        this.merchantLoading = false
      }
    },
    async handleImportDishes() {
      if (this.dishFileList.length === 0) {
        this.$message.warning('请先选择文件')
        return
      }

      this.dishLoading = true
      try {
        const file = this.dishFileList[0].raw
        const res = await importApi.importDishes(file)
        if (res.data) {
          this.$message.success(
            `导入完成！成功：${res.data.successCount}，失败：${res.data.failCount}`
          )
          this.dishFileList = []
        }
      } catch (error) {
        this.$message.error('导入失败：' + (error.message || '未知错误'))
      } finally {
        this.dishLoading = false
      }
    },
    async handleImportOrders() {
      if (this.orderFileList.length === 0) {
        this.$message.warning('请先选择文件')
        return
      }

      this.orderLoading = true
      try {
        const file = this.orderFileList[0].raw
        const res = await importApi.importOrders(file)
        if (res.data) {
          this.$message.success(
            `导入完成！成功：${res.data.successCount}，失败：${res.data.failCount}`
          )
          this.orderFileList = []
        }
      } catch (error) {
        this.$message.error('导入失败：' + (error.message || '未知错误'))
      } finally {
        this.orderLoading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.import-container {
  pre {
    background: #f5f7fa;
    padding: 15px;
    border-radius: 4px;
    overflow-x: auto;
    font-size: 12px;
    line-height: 1.5;
  }
}
</style>

