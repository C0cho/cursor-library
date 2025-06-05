<template>
  <div class="admin-categories-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>分类管理</span>
          <el-button type="primary" size="small" @click="handleAddCategory">新增分类</el-button>
        </div>
      </template>

      <el-table :data="categoryList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="分类名称"></el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="handleEditCategory(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteCategory(scope.row)">删除</el-button>
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

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑分类' : '新增分类'"
      width="40%"
      @close="resetForm"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitCategory">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const categoryList = ref([])
const loading = ref(true)
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 10
})

// 对话框和表单状态
const dialogVisible = ref(false)
const isEditing = ref(false)
const categoryFormRef = ref(null)
const categoryForm = reactive({
  id: null,
  name: '',
  description: ''
})

const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const responseData = await request.get('/categories', {
      params: {
        page: pagination.page - 1,
        size: pagination.size
      }
    })
    
    if (responseData && responseData.content) {
      categoryList.value = responseData.content
      total.value = responseData.totalElements
      console.log('AdminCategories: 成功加载分类列表', categoryList.value.length)
    } else {
      console.error('AdminCategories: 收到意外的数据结构', responseData)
      ElMessage.error('获取分类列表失败：数据格式不正确')
      categoryList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取分类列表请求失败:', error)
    ElMessage.error('获取分类列表失败，请稍后重试')
    categoryList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理页码变化
const handleSizeChange = (newSize) => {
  pagination.size = newSize
  pagination.page = 1
  fetchCategoryList()
}

// 处理当前页变化
const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchCategoryList()
}

// 处理添加分类
const handleAddCategory = () => {
  isEditing.value = false
  resetForm()
  dialogVisible.value = true
}

// 处理编辑分类
const handleEditCategory = (category) => {
  isEditing.value = true
  categoryForm.id = category.id
  categoryForm.name = category.name
  categoryForm.description = category.description
  dialogVisible.value = true
}

// 处理删除分类
const handleDeleteCategory = (category) => {
  ElMessageBox.confirm(
    `确定要删除分类 "${category.name}" 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
  .then(async () => {
    try {
      await request.delete(`/categories/${category.id}`)
      ElMessage.success('删除成功')
      fetchCategoryList()
    } catch (error) {
      console.error('删除分类请求失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  })
  .catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 处理表单提交
const handleSubmitCategory = () => {
  categoryFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let responseData
        const categoryData = { ...categoryForm }

        if (isEditing.value) {
          responseData = await request.put(`/categories/${categoryForm.id}`, categoryData)
          ElMessage.success('更新成功')
        } else {
          delete categoryData.id
          responseData = await request.post('/categories', categoryData)
          ElMessage.success('新增成功')
        }

        dialogVisible.value = false
        fetchCategoryList()
      } catch (error) {
        console.error('保存分类请求失败:', error)
        const errorMessage = error.response?.data?.message || error.message || '保存失败，请稍后重试'
        ElMessage.error(errorMessage)
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  categoryForm.id = null
  categoryForm.name = ''
  categoryForm.description = ''
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCategoryList()
})
</script>

<style scoped>
.admin-categories-container {
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