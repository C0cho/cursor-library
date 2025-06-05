<template>
  <div class="admin-borrow-records-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>借阅记录管理</span>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable></el-input>
          </el-form-item>
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
        <el-table-column label="用户" prop="user.username"></el-table-column>
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
        <el-table-column label="操作" width="200">
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
              v-if="scope.row.status === 'BORROWED'"
              size="small"
              type="primary"
              @click="handleReturn(scope.row)"
            >归还</el-button>
            <el-button
              size="small"
              type="info"
              @click="handleViewDetails(scope.row)"
            >详情</el-button>
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
        <el-descriptions-item label="用户名">{{ selectedRecord?.user?.username }}</el-descriptions-item>
        <el-descriptions-item label="图书名">{{ selectedRecord?.book?.title }}</el-descriptions-item>
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

const borrowRecords = ref([])
const loading = ref(true)
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 10
})

// 搜索表单
const searchForm = reactive({
  username: '',
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
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      username: searchForm.username,
      bookTitle: searchForm.bookTitle,
      status: searchForm.status
    }

    // 添加日期范围参数
    if (searchForm.borrowDateRange && searchForm.borrowDateRange.length === 2) {
      params.startDate = searchForm.borrowDateRange[0]
      params.endDate = searchForm.borrowDateRange[1]
    }

    const responseData = await request.get('/borrow-records', { params })
    
    if (responseData && responseData.content) {
      borrowRecords.value = responseData.content
      total.value = responseData.totalElements
      console.log('AdminBorrowRecords: 成功加载借阅记录', borrowRecords.value.length)
    } else {
      console.error('AdminBorrowRecords: 收到意外的数据结构', responseData)
      ElMessage.error('获取借阅记录列表失败：数据格式不正确')
      borrowRecords.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取借阅记录列表请求失败:', error)
    ElMessage.error('获取借阅记录列表失败，请稍后重试')
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

// 处理批准借阅
const handleApprove = (record) => {
  ElMessageBox.confirm(
    `确定要批准用户 "${record.user.username}" 借阅《${record.book.title}》吗？`,
    '确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    }
  )
  .then(async () => {
    try {
      await request.put(`/borrow-records/${record.id}/approve`)
      ElMessage.success('已批准借阅')
      fetchBorrowRecords()
    } catch (error) {
      console.error('批准借阅请求失败:', error)
      ElMessage.error('批准失败，请稍后重试')
    }
  })
  .catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 处理拒绝借阅
const handleReject = (record) => {
  ElMessageBox.confirm(
    `确定要拒绝用户 "${record.user.username}" 借阅《${record.book.title}》吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
  .then(async () => {
    try {
      await request.put(`/borrow-records/${record.id}/reject`)
      ElMessage.success('已拒绝借阅')
      fetchBorrowRecords()
    } catch (error) {
      console.error('拒绝借阅请求失败:', error)
      ElMessage.error('拒绝失败，请稍后重试')
    }
  })
  .catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 处理归还图书
const handleReturn = (record) => {
  ElMessageBox.confirm(
    `确定要将《${record.book.title}》标记为已归还吗？`,
    '确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    }
  )
  .then(async () => {
    try {
      await request.put(`/borrow-records/${record.id}/return`)
      ElMessage.success('已标记为归还')
      fetchBorrowRecords()
    } catch (error) {
      console.error('归还图书请求失败:', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  })
  .catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 查看借阅记录详情
const handleViewDetails = (record) => {
  selectedRecord.value = record
  detailsDialogVisible.value = true
}

// 组件挂载时获取数据
onMounted(() => {
  fetchBorrowRecords()
})
</script>

<style scoped>
.admin-borrow-records-container {
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