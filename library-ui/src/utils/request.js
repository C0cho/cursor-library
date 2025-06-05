import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端API的基础URL
  timeout: 5000 // 请求超时时间
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    // 如果存在token，则在请求头中添加Authorization
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    ElMessage.error('请求发送失败')
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 获取后端返回的数据
    const res = response.data
    console.log('响应拦截器收到的原始数据:', res)
    
    // 检查业务状态码 (如果后端返回 Result 结构)
    if (res && res.code !== undefined) { // Check if 'code' field exists
       if (res.code !== '200') {
         console.error('业务状态码错误:', res.code, res.message)
         ElMessage.error(res.message || '请求失败')
         // 如果是认证失败（例如token过期），可以重定向到登录页
         if (res.code === 'UNAUTHORIZED' || res.code === 'FORBIDDEN') {
           useUserStore().logout() // 清除本地用户状态
           router.push('/login')
         }
         // 返回一个rejected的Promise中断Promise链
         return Promise.reject(new Error(res.message || '请求失败'))
       } else {
          // 如果业务状态码是200，返回data字段的内容
          console.log('响应拦截器返回 Result.data:', res.data)
          return res.data
       }
    }

    // 如果后端没有返回 Result 结构，直接返回整个响应数据
    console.log('响应拦截器返回原始数据:', res)
    return res
  },
  error => {
    console.error('响应错误:', error)
    console.error('错误响应数据:', error.response?.data)
    // 处理HTTP状态码错误，例如401, 403, 404, 500等
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          useUserStore().logout()
          router.push('/login')
          break
        case 403:
          ElMessage.error('无权限访问')
          break
        case 404:
          ElMessage.error('请求资源不存在')
          break
        case 500:
             // Attempt to get error message from backend response body
             const backendErrorMessage = error.response.data?.message || '系统异常，请联系管理员';
             ElMessage.error(`请求错误: ${error.response.status} - ${backendErrorMessage}`);
             break;
        default:
          ElMessage.error(error.response.data?.message || `请求错误: ${error.response.status}`)
      }
    } else if (error.message.includes('timeout')) {
      ElMessage.error('请求超时')
    } else {
      ElMessage.error('网络或服务器错误')
    }
    return Promise.reject(error)
  }
)

export default request