<template>
  <div class="admin-books-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>图书管理</span>
          <el-button type="primary" size="small" @click="handleAddBook">新增图书</el-button>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="书名">
            <el-input v-model="searchForm.title" placeholder="请输入书名" clearable></el-input>
          </el-form-item>
          <el-form-item label="作者">
            <el-input v-model="searchForm.author" placeholder="请输入作者" clearable></el-input>
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="可借阅" value="AVAILABLE"></el-option>
              <el-option label="已借出" value="BORROWED"></el-option>
              <el-option label="维护中" value="MAINTENANCE"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="bookList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="书名"></el-table-column>
        <el-table-column prop="author" label="作者"></el-table-column>
        <el-table-column prop="category.name" label="分类"></el-table-column>
        <el-table-column prop="isbn" label="ISBN"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getBookStatusType(scope.row.status)">
              {{ getBookStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEditBook(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteBook(scope.row)">删除</el-button>
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

    <!-- 添加/编辑图书对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑图书' : '新增图书'"
      width="50%"
      @close="resetForm"
    >
      <el-form
        ref="bookFormRef"
        :model="bookForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="书名" prop="title">
          <el-input v-model="bookForm.title" placeholder="请输入书名"></el-input>
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="bookForm.author" placeholder="请输入作者"></el-input>
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="bookForm.isbn" placeholder="请输入ISBN"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="bookForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="bookForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="可借阅" value="AVAILABLE"></el-option>
            <el-option label="已借出" value="BORROWED"></el-option>
            <el-option label="维护中" value="MAINTENANCE"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="bookForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入图书描述"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitBook">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const bookList = ref([])
const categories = ref([])
const loading = ref(true)
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 10
})

// 搜索表单
const searchForm = reactive({
  title: '',
  author: '',
  categoryId: '',
  status: ''
})

// 对话框和表单状态
const dialogVisible = ref(false)
const isEditing = ref(false)
const bookFormRef = ref(null)
const bookForm = reactive({
  id: null,
  title: '',
  author: '',
  isbn: '',
  categoryId: '',
  status: 'AVAILABLE',
  description: ''
})

const formRules = {
  title: [
    { required: true, message: '请输入书名', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  isbn: [
    { required: true, message: '请输入ISBN', trigger: 'blur' },
    { pattern: /^(?:\d[- ]?){9}[\dX]$/, message: '请输入正确的ISBN格式', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 获取图书列表
const fetchBookList = async () => {
  loading.value = true
  try {
    const responseData = await request.get('/books', {
      params: {
        page: pagination.page - 1,
        size: pagination.size,
        ...searchForm
      }
    })
    
    if (responseData && responseData.content) {
      bookList.value = responseData.content
      total.value = responseData.totalElements
      console.log('AdminBooks: 成功加载图书列表', bookList.value.length)
    } else {
      console.error('AdminBooks: 收到意外的数据结构', responseData)
      ElMessage.error('获取图书列表失败：数据格式不正确')
      bookList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取图书列表请求失败:', error)
    ElMessage.error('获取图书列表失败，请稍后重试')
    bookList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const responseData = await request.get('/categories')
    if (responseData && responseData.content) {
      categories.value = responseData.content
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.page = 1
  fetchBookList()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

// 处理页码变化
const handleSizeChange = (newSize) => {
  pagination.size = newSize
  pagination.page = 1
  fetchBookList()
}

// 处理当前页变化
const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchBookList()
}

// 获取图书状态标签类型
const getBookStatusType = (status) => {
  switch (status) {
    case 'AVAILABLE':
      return 'success'
    case 'BORROWED':
      return 'warning'
    case 'MAINTENANCE':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取图书状态文本
const getBookStatusText = (status) => {
  switch (status) {
    case 'AVAILABLE':
      return '可借阅'
    case 'BORROWED':
      return '已借出'
    case 'MAINTENANCE':
      return '维护中'
    default:
      return status
  }
}

// 处理添加图书
const handleAddBook = () => {
  isEditing.value = false
  resetForm()
  dialogVisible.value = true
}

// 处理编辑图书
const handleEditBook = (book) => {
  isEditing.value = true
  bookForm.id = book.id
  bookForm.title = book.title
  bookForm.author = book.author
  bookForm.isbn = book.isbn
  bookForm.categoryId = book.category.id
  bookForm.status = book.status
  bookForm.description = book.description
  dialogVisible.value = true
}

// 处理删除图书
const handleDeleteBook = (book) => {
  ElMessageBox.confirm(
    `确定要删除图书 "${book.title}" 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
  .then(async () => {
    try {
      await request.delete(`/books/${book.id}`)
      ElMessage.success('删除成功')
      fetchBookList()
    } catch (error) {
      console.error('删除图书请求失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  })
  .catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 处理表单提交
const handleSubmitBook = () => {
  bookFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let responseData
        const bookData = { ...bookForm }

        if (isEditing.value) {
          responseData = await request.put(`/books/${bookForm.id}`, bookData)
          ElMessage.success('更新成功')
        } else {
          delete bookData.id
          responseData = await request.post('/books', bookData)
          ElMessage.success('新增成功')
        }

        dialogVisible.value = false
        fetchBookList()
      } catch (error) {
        console.error('保存图书请求失败:', error)
        const errorMessage = error.response?.data?.message || error.message || '保存失败，请稍后重试'
        ElMessage.error(errorMessage)
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  bookForm.id = null
  bookForm.title = ''
  bookForm.author = ''
  bookForm.isbn = ''
  bookForm.categoryId = ''
  bookForm.status = 'AVAILABLE'
  bookForm.description = ''
  if (bookFormRef.value) {
    bookFormRef.value.resetFields()
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchBookList()
  fetchCategories()
})
</script>

<style scoped>
.admin-books-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.search-bar {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  justify-content: center;
}
</style> 