<template>
  <div class="user-borrow-records-container main-content-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的借阅记录</span>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="图书名">
            <el-input v-model="searchForm.bookTitle" placeholder="请输入图书名" clearable></el-input>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="待审批" value="PENDING"></el-option>
              <el-option label="已借阅" value="BORROWED"></el-option>
              <el-option label="已归还" value="RETURNED"></el-option>
              <el-option label="已拒绝" value="REJECTED"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="借阅日期">
            <el-date-picker
              v-model="searchForm.borrowDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="borrowRecords" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column label="图书" prop="book.title"></el-table-column>
        <el-table-column prop="borrowDate" label="借阅日期"></el-table-column>
        <el-table-column prop="dueDate" label="应还日期"></el-table-column>
        <el-table-column prop="returnDate" label="归还日期"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getBorrowStatusTagType(scope.row.status)">
              {{ getBorrowStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              size="small"
              type="info"
              @click="handleViewDetails(scope.row)"
            >详情</el-button>
            <el-button
              v-if="scope.row.status === 'BORROWED'"
              size="small"
              type="primary"
              @click="handleReturn(scope.row)"
            >归还</el-button>
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

    <!-- 借阅记录详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="借阅记录详情"
      width="50%"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="借阅ID">{{ selectedRecord?.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getBorrowStatusTagType(selectedRecord?.status)">
            {{ getBorrowStatusText(selectedRecord?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="图书名">{{ selectedRecord?.book?.title }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ selectedRecord?.book?.author }}</el-descriptions-item>
        <el-descriptions-item label="借阅日期">{{ selectedRecord?.borrowDate }}</el-descriptions-item>
        <el-descriptions-item label="应还日期">{{ selectedRecord?.dueDate }}</el-descriptions-item>
        <el-descriptions-item label="归还日期">{{ selectedRecord?.returnDate || '未归还' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ selectedRecord?.remarks || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const borrowRecords = ref([])
const loading = ref(true)
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 10
})

const userStore = useUserStore()

// 搜索表单
const searchForm = reactive({
  bookTitle: '',
  status: '',
  borrowDateRange: []
})

// 详情对话框
const detailsDialogVisible = ref(false)
const selectedRecord = ref(null)

// 获取借阅记录列表
const fetchBorrowRecords = async () => {
  loading.value = true
  try {
    // Get current user ID from store
    const userId = userStore.user?.id;
    if (!userId) {
        ElMessage.error('无法获取当前用户信息，请重新登录');
        loading.value = false;
        return;
    }

    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      bookTitle: searchForm.bookTitle,
      status: searchForm.status
    }

    // 添加日期范围参数
    if (searchForm.borrowDateRange && searchForm.borrowDateRange.length === 2) {
      params.startDate = searchForm.borrowDateRange[0]
      params.endDate = searchForm.borrowDateRange[1]
    }

    // Corrected API endpoint
    const responseData = await request.get(`/borrow-records/user/${userId}`, { params })
    
    if (responseData && responseData.content) {
      borrowRecords.value = responseData.content
      total.value = responseData.totalElements
      console.log('UserBorrowRecords: 成功加载借阅记录', borrowRecords.value.length)
    } else {
      console.error('UserBorrowRecords: 收到意外的数据结构', responseData)
      ElMessage.error('获取借阅记录列表失败：数据格式不正确')
      borrowRecords.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取借阅记录列表请求失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '获取借阅记录列表失败，请稍后重试';
    ElMessage.error(errorMessage);
    borrowRecords.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.page = 1
  fetchBorrowRecords()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'borrowDateRange' ? [] : ''
  })
  handleSearch()
}

// 处理页码变化
const handleSizeChange = (newSize) => {
  pagination.size = newSize
  pagination.page = 1
  fetchBorrowRecords()
}

// 处理当前页变化
const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchBorrowRecords()
}

// 获取借阅状态标签类型
const getBorrowStatusTagType = (status) => {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'BORROWED':
      return 'info'
    case 'RETURNED':
      return 'success'
    case 'REJECTED':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取借阅状态文本
const getBorrowStatusText = (status) => {
  switch (status) {
    case 'PENDING':
      return '待审批'
    case 'BORROWED':
      return '已借阅'
    case 'RETURNED':
      return '已归还'
    case 'REJECTED':
      return '已拒绝'
    default:
      return status
  }
}

// 查看借阅记录详情
const handleViewDetails = (record) => {
  selectedRecord.value = record
  detailsDialogVisible.value = true
}

// 处理归还图书
const handleReturn = (record) => {
  ElMessageBox.confirm(
    `确定要归还《${record.book.title}》吗？`,
    '确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    }
  )
  .then(async () => {
    try {
      // Assuming backend endpoint for returning a book is PUT /borrow-records/{id}/return
      await request.put(`/borrow-records/${record.id}/return`);
      ElMessage.success('归还成功！');
      fetchBorrowRecords(); // Refresh list
    } catch (error) {
      console.error('归还图书失败:', error);
      const errorMessage = error.response?.data?.message || error.message || '归还失败，请稍后重试';
      ElMessage.error(errorMessage);
    }
  })
  .catch(() => {
    ElMessage.info('已取消归还操作');
  });
}

// 组件挂载时获取数据
onMounted(() => {
  fetchBorrowRecords()
})
</script>

<style scoped>
.user-borrow-records-container {
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

.borrow-records-container {
  padding: 20px;
  width: 100%;
}

.el-card {
  width: 100%;
}

.el-table {
  width: 100% !important;
}

.el-form {
  width: 100%;
}

.el-form-item__content {
  width: 100%;
}
</style>