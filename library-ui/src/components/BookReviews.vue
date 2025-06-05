<template>
  <div class="book-reviews-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>图书评论与评分</span>
          <span v-if="averageRating > 0" class="average-rating">
            平均评分: <el-rate v-model="averageRating" disabled show-score text-color="#ff9900"></el-rate>
          </span>
        </div>
      </template>

      <!-- 提交评论区域 -->
      <div class="submit-review-area">
        <h3>留下你的评论</h3>
        <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="80px">
          <el-form-item label="评分" prop="rating">
            <el-rate v-model="reviewForm.rating" allow-half show-text :texts="rateTexts"></el-rate>
          </el-form-item>
          <el-form-item label="评论" prop="comment">
            <el-input
              type="textarea"
              v-model="reviewForm.comment"
              :rows="3"
              placeholder="请输入评论内容"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitReviewForm">提交评论</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-divider></el-divider>

      <!-- 评论列表 -->
      <div class="reviews-list">
        <h3>所有评论 ({{ reviews.length }})</h3>
        <div v-if="reviews.length === 0" class="no-reviews">
          暂无评论，快来发表第一条评论吧！
        </div>
        <div v-else>
          <div v-for="review in reviews" :key="review.id" class="review-item">
            <div class="review-header">
              <span class="reviewer-name">{{ review.user?.username || '匿名用户' }}</span>
              <el-rate v-model="review.rating" disabled></el-rate>
              <span class="review-date">{{ review.createdAt }}</span>
            </div>
            <div class="review-content">
              <p>{{ review.comment }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 评论列表分页 (可选，如果评论很多的话) -->
      <!-- <el-pagination
        v-if="reviewTotal > 0"
        @size-change="handleReviewSizeChange"
        @current-change="handleReviewCurrentChange"
        :current-page="reviewPagination.page"
        :page-sizes="[5, 10, 20]"
        :page-size="reviewPagination.size"
        layout="total, prev, pager, next"
        :total="reviewTotal"
        background
        class="pagination-container"
      >
      </el-pagination> -->

    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  bookId: {
    type: Number,
    required: true
  }
})

const reviews = ref([])
const averageRating = ref(0)
// const reviewTotal = ref(0)
// const reviewPagination = reactive({ page: 1, size: 10 })

const reviewFormRef = ref(null)
const reviewForm = reactive({
  rating: 0,
  comment: '',
  bookId: props.bookId
})

const rateTexts = ref(['极差', '失望', '一般', '推荐', '力荐'])

const reviewRules = {
  rating: [
    { required: true, type: 'number', min: 0.5, message: '请选择评分', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入评论内容', trigger: 'blur' },
    { min: 5, message: '评论内容不能少于5个字符', trigger: 'blur' }
  ]
}

// 获取图书评论列表
const fetchBookReviews = async (id) => {
  if (!id) return
  try {
    const responseData = await request.get(`/books/${id}/reviews`)
    if (responseData) {
      reviews.value = responseData
      calculateAverageRating()
      console.log(`BookReviews: 成功加载图书 ${id} 的评论`, reviews.value.length)
    } else {
      console.error(`BookReviews: 收到图书 ${id} 评论意外的数据结构`, responseData)
      reviews.value = []
    }
  } catch (error) {
    console.error('获取评论请求失败:', error)
    reviews.value = []
  }
}

// 计算平均评分
const calculateAverageRating = () => {
  if (reviews.value.length === 0) {
    averageRating.value = 0
    return
  }
  const totalRating = reviews.value.reduce((sum, review) => sum + review.rating, 0)
  averageRating.value = totalRating / reviews.value.length
}

// 提交评论表单
const submitReviewForm = () => {
  reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await request.post('/reviews', reviewForm)
        ElMessage.success('评论提交成功！')
        
        // Reset form and refresh reviews
        reviewFormRef.value.resetFields()
        reviewForm.rating = 0
        reviewForm.comment = ''
        
        // Refresh reviews
        fetchBookReviews(props.bookId)
      } catch (error) {
        console.error('提交评论失败:', error)
        const errorMessage = error.response?.data?.message || error.message || '提交失败，请稍后重试'
        ElMessage.error(errorMessage)
      }
    }
  })
}

// 监听 bookId 变化
watch(() => props.bookId, (newBookId) => {
  if (newBookId) {
    fetchBookReviews(newBookId)
  }
}, { immediate: true })
</script>

<style scoped>
.book-reviews-container {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.average-rating {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.average-rating .el-rate {
  margin-left: 5px;
}

.submit-review-area {
  margin-bottom: 20px;
}

.submit-review-area h3, .reviews-list h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 18px;
  color: #303133;
}

.reviews-list .no-reviews {
  text-align: center;
  color: #909399;
  margin-top: 20px;
}

.review-item {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.reviewer-name {
  font-weight: bold;
  margin-right: 10px;
  color: #606266;
}

.review-date {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.review-content p {
  margin: 0;
  color: #303133;
  line-height: 1.5;
}
</style> 