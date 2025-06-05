<template>
  <div class="admin-reviews-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>评论管理</span>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="图书名">
            <el-input v-model="searchForm.bookTitle" placeholder="请输入图书名" clearable></el-input>
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="待审批" value="PENDING"></el-option>
              <el-option label="已批准" value="APPROVED"></el-option>
              <el-option label="已拒绝" value="REJECTED"></el-option>
            </el-select>
          </el-form-item>
           <el-form-item label="评分">
            <el-input-number v-model="searchForm.rating" :min="0" :max="5" :step="0.5" placeholder="评分" clearable></el-input-number>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="reviews" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column label="图书" prop="book.title" width="150"></el-table-column>
        <el-table-column label="用户" prop="user.username" width="100"></el-table-column>
        <el-table-column prop="rating" label="评分" width="120">
           <template #default="scope">
             <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900"></el-rate>
           </template>
        </el-table-column>
        <el-table-column prop="comment" label="评论内容"></el-table-column>
        <el-table-column prop="createdAt" label="评论日期" width="160"></el-table-column>
         <el-table-column prop="status" label="状态" width="100">
           <template #default="scope">
             <el-tag :type="getReviewStatusTagType(scope.row.status)">
               {{ getReviewStatusText(scope.row.status) }}
             </el-tag>
           </template>
         </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'PENDING'"
              size="small"
              type="success"
              @click="handleApprove(scope.row)"
            >批准</el-button>
            <el-button
              v-if="scope.row.status === 'PENDING'"
              size="small"
              type="danger"
              @click="handleReject(scope.row)"
            >拒绝</el-button>
             <el-button
               v-if="scope.row.status !== 'PENDING'"
               size="small"
               type="danger"
               @click="handleDelete(scope.row)"
             >删除</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const reviews = ref([])
const loading = ref(true)
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 10
})

// 搜索表单
const searchForm = reactive({
  bookTitle: '',
  username: '',
  status: '',
  rating: undefined, // Use undefined for optional number input
})

// 获取评论列表
const fetchReviews = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...searchForm
    }
     // Convert rating to string if needed by backend, or ensure backend handles number/null
     if (params.rating !== undefined && params.rating !== null) {
       params.rating = params.rating.toString();
     }

    // Assuming backend endpoint for admin is GET /admin/reviews
    const responseData = await request.get('/admin/reviews', { params })
    
    if (responseData && responseData.content) {
      reviews.value = responseData.content
      total.value = responseData.totalElements
      console.log('AdminReviews: 成功加载评论列表', reviews.value.length)
    } else {
      console.error('AdminReviews: 收到意外的数据结构', responseData)
      ElMessage.error('获取评论列表失败：数据格式不正确')
      reviews.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取评论列表请求失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '获取评论列表失败，请稍后重试';
    ElMessage.error(errorMessage)
    reviews.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.page = 1
  fetchReviews()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'rating' ? undefined : ''
  })
  handleSearch()
}

// 处理页码变化
const handleSizeChange = (newSize) => {
  pagination.size = newSize
  pagination.page = 1
  fetchReviews()
}

// 处理当前页变化
const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchReviews()
}

// 获取评论状态标签类型
const getReviewStatusTagType = (status) => {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'APPROVED':
      return 'success'
    case 'REJECTED':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取评论状态文本
const getReviewStatusText = (status) => {
  switch (status) {
    case 'PENDING':
      return '待审批'
    case 'APPROVED':
      return '已批准'
    case 'REJECTED':
      return '已拒绝'
    default:
      return status
  }
}

// 处理批准评论
const handleApprove = (review) => {
  ElMessageBox.confirm(
    `确定要批准这条评论吗？`,
    '确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    }
  )
  .then(async () => {
    try {
      // Assuming backend endpoint is PUT /admin/reviews/{reviewId}/approve
      await request.put(`/admin/reviews/${review.id}/approve`)
      ElMessage.success('评论已批准')
      fetchReviews() // Refresh list
    } catch (error) {
      console.error('批准评论请求失败:', error)
      const errorMessage = error.response?.data?.message || error.message || '操作失败，请稍后重试';
      ElMessage.error(errorMessage)
    }
  })
  .catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 处理拒绝评论
const handleReject = (review) => {
   ElMessageBox.confirm(
    `确定要拒绝这条评论吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
  .then(async () => {
    try {
      // Assuming backend endpoint is PUT /admin/reviews/{reviewId}/reject
      await request.put(`/admin/reviews/${review.id}/reject`)
      ElMessage.success('评论已拒绝')
      fetchReviews() // Refresh list
    } catch (error) {
      console.error('拒绝评论请求失败:', error)
      const errorMessage = error.response?.data?.message || error.message || '操作失败，请稍后重试';
      ElMessage.error(errorMessage)
    }
  })
  .catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 处理删除评论 (Optional: If delete is allowed for admin)
const handleDelete = (review) => {
    ElMessageBox.confirm(
    `确定要删除这条评论吗？此操作不可逆！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger',
    }
  )
  .then(async () => {
    try {
      // Assuming backend endpoint is DELETE /admin/reviews/{reviewId}
      await request.delete(`/admin/reviews/${review.id}`)
      ElMessage.success('评论已删除')
      fetchReviews() // Refresh list
    } catch (error) {
      console.error('删除评论请求失败:', error)
      const errorMessage = error.response?.data?.message || error.message || '删除失败，请稍后重试';
      ElMessage.error(errorMessage)
    }
  })
  .catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.admin-reviews-container {
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

.average-rating .el-rate {
    margin-left: 5px;
}
</style> 