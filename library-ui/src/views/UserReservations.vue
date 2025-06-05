<template>
  <div class="user-reservations-container main-content-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的预约记录</span>
        </div>
      </template>

      <!-- 搜索和筛选区域 (可选，如果预约记录很多的话) -->
      <!--
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="图书名">
            <el-input v-model="searchForm.bookTitle" placeholder="请输入图书名" clearable></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="待处理" value="PENDING"></el-option>
              <el-option label="已履行" value="FULFILLED"></el-option>
              <el-option label="已取消" value="CANCELLED"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      -->

      <el-table :data="reservations" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column label="图书" prop="book.title"></el-table-column>
        <el-table-column prop="reservationDate" label="预约日期">
          <template #default="scope">
            {{ formatDate(scope.row.reservationDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="fulfillmentDate" label="履行日期">
          <template #default="scope">
            {{ scope.row.fulfillmentDate ? formatDate(scope.row.fulfillmentDate) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="expirationDate" label="过期日期">
          <template #default="scope">
            {{ scope.row.expirationDate ? formatDate(scope.row.expirationDate) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getReservationStatusTagType(scope.row.status)">
              {{ getReservationStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'PENDING'"
              size="small"
              type="danger"
              @click="handleCancel(scope.row)"
            >取消预约</el-button>
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

const reservations = ref([])
const loading = ref(true)
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 10
})

// 搜索表单 (Optional)
// const searchForm = reactive({
//   bookTitle: '',
//   status: '',
// });

// 获取预约记录列表
const fetchReservations = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
       // ...searchForm // Uncomment if search is added
    }

    // Assuming backend endpoint for user reservations is GET /api/user/reservations
    const responseData = await request.get('/user/reservations', { params })
    
    // Add more robust data structure check
    if (responseData && Array.isArray(responseData.content)) {
      reservations.value = responseData.content
      total.value = responseData.totalElements || 0 // Ensure total is a number, default to 0
      console.log('UserReservations: 成功加载预约记录', reservations.value.length)
    } else {
      console.error('UserReservations: 收到意外的数据结构', responseData)
      ElMessage.error('获取预约记录列表失败：数据格式不正确')
      reservations.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取预约记录列表请求失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '获取预约记录列表失败，请稍后重试';
    ElMessage.error(errorMessage)
    reservations.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索 (Optional)
// const handleSearch = () => {
//   pagination.page = 1;
//   fetchReservations();
// };

// 重置搜索 (Optional)
// const resetSearch = () => {
//   Object.keys(searchForm).forEach(key => {
//     searchForm[key] = '';
//   });
//   handleSearch();
// };

// 处理页码变化
const handleSizeChange = (newSize) => {
  pagination.size = newSize
  pagination.page = 1
  fetchReservations()
}

// 处理当前页变化
const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchReservations()
}

// 获取预约状态标签类型
const getReservationStatusTagType = (status) => {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'FULFILLED':
      return 'success'
    case 'CANCELLED':
      return 'info'
    default:
      return 'info'
  }
}

// 获取预约状态文本
const getReservationStatusText = (status) => {
  switch (status) {
    case 'PENDING':
      return '待处理'
    case 'FULFILLED':
      return '已履行'
    case 'CANCELLED':
      return '已取消'
    default:
      return status
  }
}

// 处理取消预约
const handleCancel = (reservation) => {
   ElMessageBox.confirm(
    `确定要取消对《${reservation.book.title}》的预约吗？`,
    '确认取消',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
  .then(async () => {
    try {
      // Assuming backend endpoint for cancelling reservation is DELETE /api/reservations/{reservationId}
      await request.delete(`/reservations/${reservation.id}`)
      ElMessage.success('预约已取消')
      fetchReservations() // Refresh list
    } catch (error) {
      console.error('取消预约请求失败:', error)
      const errorMessage = error.response?.data?.message || error.message || '取消失败，请稍后重试';
      ElMessage.error(errorMessage)
    }
  })
  .catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 在 script setup 部分添加日期格式化函数
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchReservations()
})
</script>

<style scoped>
.reservations-container {
  padding: 20px;
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

/* Make the card take full width */
.el-card {
  width: 100%;
}

/* Ensure table takes full width */
.el-table {
  width: 100% !important;
}

/* Adjust form layout */
.el-form {
  width: 100%;
}

.el-form-item__content {
  width: 100%;
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