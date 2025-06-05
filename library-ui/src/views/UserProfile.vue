<template>
  <div class="user-profile-container main-content-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="profile">
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="120px"
            class="profile-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileForm.username" disabled></el-input>
            </el-form-item>
            <el-form-item label="角色" prop="role">
              <el-input v-model="profileForm.role" disabled></el-input>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitProfileForm">保存信息</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
            class="password-form"
          >
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input type="password" v-model="passwordForm.oldPassword" show-password placeholder="请输入旧密码"></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input type="password" v-model="passwordForm.newPassword" show-password placeholder="请输入新密码"></el-input>
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input type="password" v-model="passwordForm.confirmPassword" show-password placeholder="请确认新密码"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitPasswordForm">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const activeTab = ref('profile')

// 基本信息表单
const profileFormRef = ref(null)
const profileForm = reactive({
  username: '', // Username is disabled for editing, fetch from store or API
  role: '', // Role is disabled for editing, fetch from store or API
  email: ''
})

const profileRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

// 修改密码表单
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致!'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ]
}

// 获取用户基本信息
const fetchUserProfile = async () => {
  // Fetch user info from store initially
  if (userStore.user) {
    profileForm.username = userStore.user.username
    profileForm.role = userStore.user.role === 'ADMIN' ? '管理员' : '读者'
    // Assuming email is part of the user object in the store, or fetch from backend
    profileForm.email = userStore.user.email || '' // Assuming email field exists
  }

  // Optional: Fetch from backend for freshest data
  try {
    const responseData = await request.get('/users/profile')
    if (responseData) {
       profileForm.username = responseData.username;
       profileForm.role = responseData.role === 'ADMIN' ? '管理员' : '读者';
       profileForm.email = responseData.email;
       // Update store if necessary
       userStore.setUser(responseData);
       console.log('UserProfile: 成功获取用户基本信息', responseData);
    } else {
       console.error('UserProfile: 获取用户基本信息失败，数据格式不正确', responseData);
       // Use data from store if backend fetch fails
       ElMessage.warning('获取最新用户信息失败，显示本地信息。');
    }
  } catch (error) {
    console.error('获取用户基本信息请求失败:', error);
     // Use data from store if backend fetch fails
    ElMessage.error('获取用户基本信息失败，请稍后重试');
  }
}

// 提交基本信息表单
const submitProfileForm = () => {
  profileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // Assuming backend expects PUT to /users/profile with updated fields
        const updateData = { email: profileForm.email }; // Send only editable fields
        await request.put('/users/profile', updateData);
        ElMessage.success('个人信息更新成功');
        // Optionally refresh user data in store/page
         fetchUserProfile();
      } catch (error) {
        console.error('更新个人信息请求失败:', error);
        const errorMessage = error.response?.data?.message || error.message || '更新失败，请稍后重试';
        ElMessage.error(errorMessage);
      }
    }
  })
}

// 提交修改密码表单
const submitPasswordForm = () => {
  passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // Assuming backend expects PUT or POST to /users/change-password
        // with oldPassword and newPassword
        await request.post('/users/change-password', {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        });
        ElMessage.success('密码修改成功，请重新登录');
        // Clear password form
        passwordFormRef.value.resetFields();
        // Optionally log out user for security
         userStore.logout();
         router.push('/login'); // Redirect to login page
      } catch (error) {
        console.error('修改密码请求失败:', error);
        const errorMessage = error.response?.data?.message || error.message || '修改失败，请检查旧密码是否正确或稍后重试';
        ElMessage.error(errorMessage);
      }
    }
  })
}

// 组件挂载时获取用户基本信息
onMounted(() => {
  fetchUserProfile()
})
</script>

<style scoped>
.user-profile-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.profile-form, .password-form {
  width: 100%;
  margin: 20px auto;
  padding: 0;
}

/* Optional: Adjust tab style if needed */
.el-tabs__header {
  margin-bottom: 20px;
}

/* Make the card take full width */
.el-card {
  width: 100%;
}

/* Ensure form items have enough space */
.el-form-item {
  margin-bottom: 22px;
  width: 100%; /* Explicitly set form item width */
}

/* Make input fields wider */
.el-input {
  width: 100%;
}

/* Adjust form layout */
.el-form {
  width: 100%;
}

.el-form-item__content {
  width: 100%;
}
</style> 