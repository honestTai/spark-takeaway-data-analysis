<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>外卖数据分析系统</h2>
        <p>基于Spark的大数据分析平台</p>
      </div>
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            prefix-icon="el-icon-user"
            placeholder="请输入用户名"
            size="large"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            prefix-icon="el-icon-lock"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-tip">
        <p>默认账号：admin / admin123</p>
      </div>
    </div>
  </div>
</template>

<script>
import { authApi } from '@/api/auth'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: 'admin',
        password: 'CHANGE_ME_BEFORE_RUNNING'
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (!valid) {
          return false
        }

        this.loading = true
        try {
          const res = await authApi.login({
            username: this.loginForm.username,
            password: this.loginForm.password
          })

          if (res.data && res.data.token) {
            // 保存token和用户信息
            localStorage.setItem('token', res.data.token)
            localStorage.setItem('userInfo', JSON.stringify(res.data.user))
            
            this.$message.success('登录成功')
            
            // 跳转到首页
            this.$router.push('/')
          } else {
            this.$message.error('登录失败，请检查用户名和密码')
          }
        } catch (error) {
          this.$message.error(error.message || '登录失败')
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;

  .login-box {
    width: 400px;
    background: #fff;
    border-radius: 12px;
    padding: 40px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);

    .login-header {
      text-align: center;
      margin-bottom: 30px;

      h2 {
        color: #333;
        font-size: 24px;
        margin-bottom: 10px;
      }

      p {
        color: #999;
        font-size: 14px;
      }
    }

    .login-form {
      .el-form-item {
        margin-bottom: 20px;
      }
    }
  }
}
</style>

