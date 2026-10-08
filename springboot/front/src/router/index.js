import Vue from 'vue'
import VueRouter from 'vue-router'
import Layout from '@/layout'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index'),
    hidden: true
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { title: '数据概览', icon: 'el-icon-monitor' },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index'),
        meta: { title: '数据概览', icon: 'el-icon-data-line' }
      }
    ]
  },
  {
    path: '/crawler',
    component: Layout,
    hidden: true,
    meta: { title: '数据采集', icon: 'el-icon-download' },
    children: [
      {
        path: 'merchants',
        name: 'CrawlerMerchants',
        component: () => import('@/views/crawler/merchants'),
        meta: { title: '商户爬取', icon: 'el-icon-s-promotion' }
      },
      {
        path: 'dishes',
        name: 'CrawlerDishes',
        component: () => import('@/views/crawler/dishes'),
        meta: { title: '菜品爬取', icon: 'el-icon-receiving' }
      }
    ]
  },
  {
    path: '/import',
    component: Layout,
    meta: { title: '数据导入', icon: 'el-icon-upload2' },
    children: [
      {
        path: 'index',
        name: 'DataImport',
        component: () => import('@/views/import/index'),
        meta: { title: '文件导入', icon: 'el-icon-folder-opened' }
      },
      {
        path: 'logs',
        name: 'ImportLogs',
        component: () => import('@/views/import/logs'),
        meta: { title: '导入日志', icon: 'el-icon-document' }
      }
    ]
  },
  {
    path: '/cleaning',
    component: Layout,
    meta: { title: '数据清洗', icon: 'el-icon-brush' },
    children: [
      {
        path: 'index',
        name: 'DataCleaning',
        component: () => import('@/views/cleaning/index'),
        meta: { title: '清洗管理', icon: 'el-icon-setting' }
      }
    ]
  },
  {
    path: '/analysis',
    component: Layout,
    meta: { title: '数据分析', icon: 'el-icon-data-analysis' },
    children: [
      {
        path: 'merchant-sales',
        name: 'MerchantSales',
        component: () => import('@/views/analysis/merchant-sales'),
        meta: { title: '商户销量分析', icon: 'el-icon-trophy' }
      },
      {
        path: 'price-distribution',
        name: 'PriceDistribution',
        component: () => import('@/views/analysis/price-distribution'),
        meta: { title: '价格区间分布', icon: 'el-icon-coin' }
      },
      {
        path: 'hot-dishes',
        name: 'HotDishes',
        component: () => import('@/views/analysis/hot-dishes'),
        meta: { title: '热门菜品分析', icon: 'el-icon-star-on' }
      },
      {
        path: 'user-behavior',
        name: 'UserBehavior',
        component: () => import('@/views/analysis/user-behavior'),
        meta: { title: '用户行为分析', icon: 'el-icon-user' }
      },
      {
        path: 'merchant-business',
        name: 'MerchantBusiness',
        component: () => import('@/views/analysis/merchant-business'),
        meta: { title: '商户经营分析', icon: 'el-icon-office-building' }
      },
      {
        path: 'order-trend',
        name: 'OrderTrend',
        component: () => import('@/views/analysis/order-trend'),
        meta: { title: '订单趋势分析', icon: 'el-icon-s-data' }
      },
      {
        path: 'dish-category',
        name: 'DishCategory',
        component: () => import('@/views/analysis/dish-category'),
        meta: { title: '菜品类别占比', icon: 'el-icon-pie-chart' }
      }
    ]
  },
  {
    path: '/merchant',
    component: Layout,
    meta: { title: '商户管理', icon: 'el-icon-s-shop' },
    children: [
      {
        path: 'list',
        name: 'MerchantList',
        component: () => import('@/views/merchant/list'),
        meta: { title: '商户列表', icon: 'el-icon-tickets' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    meta: { title: '系统管理', icon: 'el-icon-s-tools' },
    children: [
      {
        path: 'settings',
        name: 'SystemSettings',
        component: () => import('@/views/system/settings'),
        meta: { title: '系统设置', icon: 'el-icon-setting' }
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
const whiteList = ['/login'] // 白名单

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (token) {
    // 已登录
    if (to.path === '/login') {
      next('/')
    } else {
      next()
    }
  } else {
    // 未登录
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
