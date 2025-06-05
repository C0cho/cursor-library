<template>
  <div class="book-list-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>图书列表</span>
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
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 图书列表 -->
      <el-row :gutter="20">
        <el-col :span="6" v-for="book in bookList" :key="book.id" class="book-item">
          <el-card :body-style="{ padding: '0px' }" class="book-card">
            <div class="book-cover">
              <el-image
                :src="book.coverUrl || '/default-book-cover.jpg'"
                fit="cover"
                :preview-src-list="book.coverUrl ? [book.coverUrl] : []"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="book-info">
              <h3 class="book-title" @click="handleViewDetails(book)">{{ book.title }}</h3>
              <p class="book-author">作者：{{ book.author }}</p>
              <p class="book-category">分类：{{ book.category?.name }}</p>
              <p class="book-status">
                状态：
                <el-tag :type="getBookStatusType(book.status)" size="small">
                  {{ getBookStatusText(book.status) }}
                </el-tag>
              </p>
              <div class="book-actions">
                <el-button
                  type="primary"
                  size="small"
                  :disabled="book.status !== 'AVAILABLE'"
                  @click="handleBorrow(book)"
                >
                  借阅
                </el-button>
                <el-button
                  v-if="book.status === 'UNAVAILABLE'"
                  type="warning"
                  size="small"
                  @click="handleReserve(book)"
                >
                  预约
                </el-button>
                <el-button
                  type="info"
                  size="small"
                  @click="handleViewDetails(book)"
                >
                  详情
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.page"
        :page-sizes="[12, 24, 36, 48]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        background
        class="pagination-container"
      >
      </el-pagination>
    </el-card>

    <!-- 图书详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="图书详情"
      width="50%"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="书名">{{ selectedBook?.title }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ selectedBook?.author }}</el-descriptions-item>
        <el-descriptions-item label="ISBN">{{ selectedBook?.isbn }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ selectedBook?.category?.name }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getBookStatusType(selectedBook?.status)">
            {{ getBookStatusText(selectedBook?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ selectedBook?.description || '暂无描述' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 图书评论组件 -->
      <book-reviews v-if="selectedBook" :book-id="selectedBook.id"></book-reviews>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
          <el-button
            type="primary"
            :disabled="selectedBook?.status !== 'AVAILABLE'"
            @click="handleBorrow(selectedBook)"
          >
            借阅
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 借阅确认对话框 -->
    <el-dialog
      v-model="borrowDialogVisible"
      title="借阅确认"
      width="30%"
    >
      <el-form :model="borrowForm" label-width="100px">
        <el-form-item label="借阅天数">
          <el-input-number
            v-model="borrowForm.days"
            :min="1"
            :max="30"
            :step="1"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="borrowForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入借阅备注（选填）"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="borrowDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBorrow">确认借阅</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'
import BookReviews from '@/components/BookReviews.vue'

const bookList = ref([])
const categories = ref([])
const loading = ref(true)
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 12
})

// 搜索表单
const searchForm = reactive({
  title: '',
  author: '',
  categoryId: ''
})

// 详情对话框
const detailsDialogVisible = ref(false)
const selectedBook = ref(null)

// 借阅对话框
const borrowDialogVisible = ref(false)
const borrowForm = reactive({
  bookId: null,
  days: 7,
  remarks: ''
})

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
      console.log('BookList: 成功加载图书列表', bookList.value.length)
    } else {
      console.error('BookList: 收到意外的数据结构', responseData)
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

// 查看图书详情
const handleViewDetails = (book) => {
  selectedBook.value = book
  detailsDialogVisible.value = true
}

// 处理借阅
const handleBorrow = (book) => {
  borrowForm.bookId = book.id
  borrowForm.days = 7
  borrowForm.remarks = ''
  borrowDialogVisible.value = true
}

// 提交借阅
const submitBorrow = async () => {
  try {
    await request.post('/borrow-records', {
      bookId: borrowForm.bookId,
      days: borrowForm.days,
      remarks: borrowForm.remarks
    })
    ElMessage.success('借阅申请已提交，请等待管理员审批')
    borrowDialogVisible.value = false
    fetchBookList() // 刷新图书列表
  } catch (error) {
    console.error('提交借阅申请失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '提交失败，请稍后重试'
    ElMessage.error(errorMessage)
  }
}

// 处理预约
const handleReserve = async (book) => {
  // Optional: Check if user is logged in before attempting to reserve
  // This is also handled by backend @PreAuthorize, but can provide faster feedback
   // if (!userStore.isLoggedIn) {
   //    ElMessage.warning('请先登录以进行预约');
   //    return;
   // }

  ElMessageBox.confirm(
    `确定要预约《${book.title}》吗？`, // Confirmation message
    '确认预约',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    }
  )
  .then(async () => {
    try {
      // Assuming backend endpoint for creating reservation is POST /api/reservations
      await request.post('/reservations', null, { params: { bookId: book.id } })
      ElMessage.success('预约成功，请关注我的预约记录')
      // No need to refresh book list immediately, status won't change for this user
    } catch (error) {
      console.error('提交预约请求失败:', error)
      const errorMessage = error.response?.data?.message || error.message || '预约失败，请稍后重试';
      ElMessage.error(errorMessage)
    }
  })
  .catch(() => {
    ElMessage.info('已取消预约')
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchBookList()
  fetchCategories()
})
</script>

<style scoped>
.book-list-container {
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

.book-item {
  margin-bottom: 20px;
}

.book-card {
  height: 100%;
  transition: all 0.3s;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}

.book-cover {
  height: 200px;
  overflow: hidden;
}

.book-cover .el-image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.book-info {
  padding: 14px;
}

.book-title {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-title:hover {
  color: #409EFF;
}

.book-author,
.book-category,
.book-status {
  margin: 8px 0;
  font-size: 14px;
  color: #606266;
}

.book-actions {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
}

.pagination-container {
  margin-top: 20px;
  justify-content: center;
}
</style> 