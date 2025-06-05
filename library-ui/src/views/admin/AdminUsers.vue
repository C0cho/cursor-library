<template>
  <div class="admin-users-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" size="small" @click="handleAddUser">新增用户</el-button>
        </div>
      </template>

      <el-table :data="userList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="role" label="角色">
           <template #default="scope">
               <el-tag>{{ scope.row.role === 'ADMIN' ? '管理员' : '读者' }}</el-tag>
           </template>
        </el-table-column>
        <!-- Add other user fields as needed, e.g., email, registrationDate -->
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="handleEditUser(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteUser(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background
        class="pagination-container"
      >
      </el-pagination>
    </el-card>

    <!-- Add/Edit User Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑用户' : '新增用户'"
      width="40%"
      @close="resetForm"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEditing" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" :prop="isEditing ? '' : 'password'"> <!-- Require password only for add -->
          <el-input v-model="userForm.password" type="password" show-password :placeholder="isEditing ? '留空表示不修改密码' : '请输入密码'"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
           <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%;">
              <el-option label="管理员" value="ADMIN"></el-option>
              <el-option label="读者" value="READER"></el-option>
           </el-select>
        </el-form-item>
        <!-- Add other user fields as needed (e.g., email) -->
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitUser">确定</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const userList = ref([])
const loading = ref(true)
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 10
})

// Dialog and Form state
const dialogVisible = ref(false)
const isEditing = ref(false)
const userFormRef = ref(null)
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  role: 'READER',
  // Add other fields here as needed
})

const formRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
   role: [
     { required: true, message: '请选择角色', trigger: 'change' }
   ]
  // Add rules for other fields
})

// Watch isEditing to conditionally require password
// This is one way, another is to manually validate password only for add
// watch(isEditing, (newVal) => {
//   if (newVal) {
//     // Editing: remove password requirement
//     formRules.password[0].required = false;
//   } else {
//      // Adding: require password
//     formRules.password[0].required = true;
//   }
// }, { immediate: true });

// Fetch user list data from backend
const fetchUserList = async () => {
  loading.value = true
  try {
    // Assuming backend endpoint for listing users is /api/users with pagination
    const responseData = await request.get('/users', {
      params: {
        page: pagination.page - 1, // Adjust to 0-indexed for backend
        size: pagination.size
      }
    })
    
    // Assuming backend returns data in a structure like: { content: [...], totalElements: ... }
    if (responseData && responseData.content) {
      userList.value = responseData.content
      total.value = responseData.totalElements
      console.log('AdminUsers: Successfully loaded users', userList.value.length)
    } else {
      console.error('AdminUsers: Received unexpected data structure', responseData)
      ElMessage.error('获取用户列表失败：数据格式不正确')
      userList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取用户列表请求失败:', error)
    ElMessage.error('获取用户列表失败，请稍后重试')
    userList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// Handle page size change
const handleSizeChange = (newSize) => {
  pagination.size = newSize
  pagination.page = 1 // Reset to first page when size changes
  fetchUserList()
}

// Handle current page change
const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchUserList()
}

// Handle Add User button click
const handleAddUser = () => {
  isEditing.value = false;
  resetForm();
  dialogVisible.value = true;
  // Manually ensure password is required for add
  formRules.password[0].required = true;
  console.log('Add User clicked');
}

// Handle Edit User button click
const handleEditUser = (user) => {
  isEditing.value = true;
   // Remove password requirement for edit
  formRules.password[0].required = false;
  // Populate form with existing user data
  userForm.id = user.id;
  userForm.username = user.username;
  userForm.password = ''; // Don't populate password for security
  userForm.role = user.role;
  // Populate other fields here
  dialogVisible.value = true;
  console.log('Edit User clicked', user);
}

// Handle Delete User button click
const handleDeleteUser = (user) => {
   ElMessageBox.confirm(
    `确定要删除用户 "${user.username}" 吗?`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
  .then(async () => {
    // Call backend API to delete user
    try {
      // Assuming backend endpoint for deleting user is DELETE /api/users/{id}
      const responseData = await request.delete(`/users/${user.id}`);
      // Check backend response if needed (e.g., responseData.code === '200')
      ElMessage.success('删除成功');
      fetchUserList(); // Refresh list after deletion
    } catch (error) {
      console.error('删除用户请求失败:', error);
      ElMessage.error('删除失败，请稍后重试');
    }
  })
  .catch(() => {
    ElMessage.info('已取消删除');
  });
  console.log('Delete User clicked', user);
}

// Handle form submission (Add or Edit)
const handleSubmitUser = () => {
  userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let responseData;
        // Prepare data to send
        const userDataToSend = { ...userForm };

        if (isEditing.value) {
           // Remove password if not changed during edit
           if (userDataToSend.password === '') {
             delete userDataToSend.password;
           }
           // Remove username for edit if backend doesn't allow updating username
           // Assuming username cannot be updated, remove it from payload
           delete userDataToSend.username;

          // Call backend API to update user
          // Assuming backend endpoint is PUT /api/users/{id} with request body
          responseData = await request.put(`/users/${userForm.id}`, userDataToSend);
          ElMessage.success('更新成功');
        } else {
           // Remove id if adding a new user
           delete userDataToSend.id;

          // Call backend API to create user
          // Assuming backend endpoint is POST /api/users with request body
          responseData = await request.post('/users', userDataToSend);
          ElMessage.success('新增成功');
        }
        
        // Check backend response if needed (e.g., responseData.code === '200')

        dialogVisible.value = false; // Close dialog on success
        fetchUserList(); // Refresh list after add/edit

      } catch (error) {
        console.error('保存用户请求失败:', error);
        // Display backend error message if available
        const errorMessage = error.response?.data?.message || error.message || '保存失败，请稍后重试';
        ElMessage.error(errorMessage);
      }
    }
  });
}

// Reset form data and validation state
const resetForm = () => {
  userForm.id = null;
  userForm.username = '';
  userForm.password = '';
  userForm.role = 'READER';
   // Reset other fields
  if (userFormRef.value) {
    userFormRef.value.resetFields();
  }
}

// Fetch data on component mount
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.admin-users-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  justify-content: center;
}
</style> 