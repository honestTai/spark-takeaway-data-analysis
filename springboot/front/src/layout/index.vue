<template>
  <div class="app-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar-container">
        <div class="logo-container">
          <i class="el-icon-data-line" style="font-size: 24px; margin-right: 10px;"></i>
          <span v-if="!isCollapse" class="logo-text">外卖数据分析</span>
          <span v-else class="logo-text-mini">数据</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :unique-opened="true"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          class="sidebar-menu"
        >
          <template v-for="route in routes">
            <el-submenu v-if="route.children && route.children.length > 1" :key="route.path" :index="route.path">
              <template slot="title">
                <i :class="route.meta.icon"></i>
                <span>{{ route.meta.title }}</span>
              </template>
              <el-menu-item
                v-for="child in route.children"
                :key="child.path"
                :index="route.path === '/' ? '/' + child.path : route.path + '/' + child.path"
              >
                <i :class="child.meta ? child.meta.icon : ''"></i>
                <span>{{ child.meta ? child.meta.title : '' }}</span>
              </el-menu-item>
            </el-submenu>
            <el-menu-item v-else-if="route.children && route.children.length === 1" :key="route.path" :index="route.path === '/' ? '/' + route.children[0].path : route.path + '/' + route.children[0].path">
              <i :class="route.meta ? route.meta.icon : (route.children[0].meta ? route.children[0].meta.icon : '')"></i>
              <span>{{ route.meta ? route.meta.title : (route.children[0].meta ? route.children[0].meta.title : '') }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="navbar">
          <div class="navbar-left">
            <i
              :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"
              class="collapse-icon"
              @click="toggleSidebar"
            ></i>
            <el-breadcrumb separator="/" class="breadcrumb">
              <el-breadcrumb-item v-for="item in breadcrumbList" :key="item.path" :to="item.path">
                {{ item.meta.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="navbar-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <i class="el-icon-user-solid"></i>
                <span>{{ userInfo.nickname || userInfo.username || '用户' }}</span>
                <i class="el-icon-arrow-down"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 内容区域 -->
        <el-main class="app-main">
          <transition name="fade-transform" mode="out-in">
            <router-view :key="key" />
          </transition>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import router from '@/router'

export default {
  name: 'Layout',
  data() {
    return {
      isCollapse: false,
      routes: [],
      userInfo: {}
    }
  },
  computed: {
    ...mapGetters(['sidebar']),
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
    breadcrumbList() {
      const matched = this.$route.matched.filter(item => item.meta && item.meta.title)
      return matched
    },
    key() {
      return this.$route.path
    }
  },
  created() {
    this.routes = router.options.routes.filter(route => route.path !== '/login' && !route.hidden)
    this.loadUserInfo()
    
    // 检查登录状态
    const token = localStorage.getItem('token')
    if (!token) {
      this.$router.push('/login')
    }
  },
  methods: {
    loadUserInfo() {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        try {
          this.userInfo = JSON.parse(userInfoStr)
        } catch (e) {
          this.userInfo = {}
        }
      }
    },
    toggleSidebar() {
      this.isCollapse = !this.isCollapse
    },
    async handleCommand(command) {
      if (command === 'logout') {
        try {
          const { authApi } = await import('@/api/auth')
          await authApi.logout()
        } catch (e) {
          console.error('登出失败', e)
        }
        
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        this.$message.success('已退出登录')
        this.$router.push('/login')
      } else if (command === 'profile') {
        this.$message.info('个人中心功能开发中...')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  height: 100vh;
  overflow: hidden;
}

.sidebar-container {
  background-color: #304156;
  transition: width 0.28s;
  overflow: hidden;

  .logo-container {
    height: 60px;
    line-height: 60px;
    padding: 0 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    font-weight: bold;


    .logo-text {
      font-size: 18px;
    }

    .logo-text-mini {
      font-size: 16px;
    }
  }

  .sidebar-menu {
    border: none;
    height: calc(100vh - 60px);
    overflow-y: auto;
  }
}

.navbar {
  height: 60px;
  line-height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;

  .navbar-left {
    display: flex;
    align-items: center;

    .collapse-icon {
      font-size: 20px;
      cursor: pointer;
      margin-right: 20px;
      color: #606266;
      transition: color 0.3s;

      &:hover {
        color: #409EFF;
      }
    }

    .breadcrumb {
      font-size: 14px;
    }
  }

  .navbar-right {
    .user-info {
      cursor: pointer;
      color: #606266;
      display: flex;
      align-items: center;

      i {
        margin: 0 5px;
      }

      &:hover {
        color: #409EFF;
      }
    }
  }
}

.app-main {
  background: #f0f2f5;
  padding: 20px;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>

