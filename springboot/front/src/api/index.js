import request from '@/utils/request'
import { authApi } from './auth'

// 导出认证API
export { authApi }

// 首页统计API
export const dashboardApi = {
  getStatistics() {
    return request({ url: '/dashboard/statistics', method: 'get' })
  },
  getMerchantTypeDist() {
    return request({ url: '/dashboard/merchant-type-dist', method: 'get' })
  },
  getTopMerchants(limit = 10) {
    return request({ url: '/dashboard/top-merchants', method: 'get', params: { limit } })
  }
}

// 爬虫API
export const crawlerApi = {
  // 爬取商户
  crawlMerchants(params) {
    return request({
      url: '/crawler/merchants',
      method: 'post',
      params: params // POST请求使用params，后端使用@RequestParam接收
    })
  },
  // 爬取菜品
  crawlDishes(merchantId) {
    return request({
      url: `/crawler/dishes/${merchantId}`,
      method: 'post'
    })
  },
  // 批量爬取
  batchCrawl(params) {
    return request({
      url: '/crawler/batch',
      method: 'post',
      params: params // POST请求使用params，后端使用@RequestParam接收
    })
  }
}

// 数据分析API
export const analysisApi = {
  // 商户销量排名
  getMerchantSales(limit = 10) {
    return request({
      url: '/analysis/merchant-sales',
      method: 'get',
      params: { limit }
    })
  },
  // 价格区间分布
  getPriceDistribution() {
    return request({
      url: '/analysis/price-distribution',
      method: 'get'
    })
  },
  // 热门菜品
  getHotDishes(limit = 10) {
    return request({
      url: '/analysis/hot-dishes',
      method: 'get',
      params: { limit }
    })
  },
  // 用户消费频次
  getUserConsumptionFrequency() {
    return request({
      url: '/analysis/user-frequency',
      method: 'get'
    })
  },
  // 用户消费金额
  getUserConsumptionAmount() {
    return request({
      url: '/analysis/user-amount',
      method: 'get'
    })
  },
  // 评分与销量关系
  getRatingVsSales() {
    return request({
      url: '/analysis/rating-sales',
      method: 'get'
    })
  },
  // 商户类型对比
  getMerchantTypeComparison() {
    return request({
      url: '/analysis/merchant-type',
      method: 'get'
    })
  },
  // 订单趋势
  getOrderTrend(days = 30) {
    return request({
      url: '/analysis/order-trend',
      method: 'get',
      params: { days }
    })
  },
  // 菜品类别占比
  getDishCategoryDistribution() {
    return request({
      url: '/analysis/dish-category',
      method: 'get'
    })
  }
}

// 数据导入API
export const importApi = {
  // 导入商户
  importMerchants(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/import/merchants',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  // 导入菜品
  importDishes(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/import/dishes',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  // 导入订单
  importOrders(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/import/orders',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  // 获取导入日志
  getImportLogs() {
    return request({
      url: '/import/logs',
      method: 'get'
    })
  },
  // 获取导入日志详情
  getImportLog(logId) {
    return request({
      url: `/import/logs/${logId}`,
      method: 'get'
    })
  }
}

// 数据清洗API
export const cleaningApi = {
  // 清洗商户
  cleanMerchants(merchants) {
    return request({
      url: '/cleaning/merchants',
      method: 'post',
      data: merchants
    })
  },
  // 清洗菜品
  cleanDishes(dishes) {
    return request({
      url: '/cleaning/dishes',
      method: 'post',
      data: dishes
    })
  },
  // 清洗订单
  cleanOrders(orders) {
    return request({
      url: '/cleaning/orders',
      method: 'post',
      data: orders
    })
  },
  // 验证数据
  validateData(data, dataType) {
    return request({
      url: '/cleaning/validate',
      method: 'post',
      data: { data, dataType }
    })
  },
  // 获取清洗规则
  getRules() {
    return request({
      url: '/cleaning/rules',
      method: 'get'
    })
  },
  // 保存清洗规则
  saveRules(rules) {
    return request({
      url: '/cleaning/rules',
      method: 'post',
      data: rules
    })
  },
  // 重置清洗规则
  resetRules() {
    return request({
      url: '/cleaning/rules/reset',
      method: 'post'
    })
  }
}

// 商户API
export const merchantApi = {
  // 获取商户列表
  getMerchantList(params) {
    return request({
      url: '/merchants',
      method: 'get',
      params
    })
  },
  // 获取商户详情
  getMerchantById(id) {
    return request({
      url: `/merchants/${id}`,
      method: 'get'
    })
  },
  // 获取销量排行
  getTopMerchants(limit = 10) {
    return request({
      url: '/merchants/top',
      method: 'get',
      params: { limit }
    })
  },
  // 按类型获取
  getMerchantsByType(type) {
    return request({
      url: `/merchants/type/${type}`,
      method: 'get'
    })
  }
}

