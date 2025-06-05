<template>
  <div class="login-container main-content-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>登录</h2>
        </div>
      </template>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        label-width="80px"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>

        <div class="form-footer">
          <router-link to="/register">还没有账号？立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request' // Import the request utility

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const loginFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        console.log('发送登录请求，数据:', loginForm)
        const responseData = await request.post('/auth/login', loginForm)
        console.log('收到响应数据:', responseData)
        
        // 确保 responseData.data 存在并从中提取用户信息和 token
        if (responseData && responseData.data) {
          const { id, username, role, token } = responseData.data
          console.log('提取的用户数据:', { id, username, role })
          console.log('提取的token:', token)

          if (id && username && role && token) {
            // Use the new loginSuccess function which handles persistence
            userStore.loginSuccess({ id, username, role }, token);
            ElMessage.success('登录成功')
            router.push('/')
          } else {
            // responseData.data 存在但内部字段缺失
            console.error('responseData.data 结构不正确:', responseData.data)
            ElMessage.error('登录失败：后端返回数据格式错误')
          }
        } else {
          // responseData 或 responseData.data 不存在
          console.error('响应数据或其data字段缺失:', responseData)
          ElMessage.error('登录失败：后端返回数据格式错误')
        }
      } catch (error) {
        console.error('登录请求失败:', error)
        console.error('错误详情:', error.response?.data)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.login-card {
  width: 480px;
  padding: 20px;
}

.card-header {
  text-align: center;
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0;
  font-size: 28px;
  color: #303133;
}

.form-footer {
  text-align: center;
  margin-top: 24px;
}

.form-footer a {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.form-footer a:hover {
  text-decoration: underline;
}

.el-form-item {
  margin-bottom: 24px;
}

.el-input {
  width: 100%;
}

.el-button--primary {
  width: 100%;
  height: 40px;
  font-size: 16px;
}
</style> 