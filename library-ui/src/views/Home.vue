<template>
  <div class="common-layout">
    <el-container style="height: 100vh;">
      <el-header class="header">
        <div class="header-left">
           <el-icon :size="26" style="margin-right: 10px; color: white;"><Reading /></el-icon>
           <h1>图书管理系统</h1>
        </div>
        <div class="header-right">
          <span v-if="userStore.user">欢迎, {{ userStore.user.username }} ({{ userStore.user.role === 'ADMIN' ? '管理员' : '读者' }})</span>
          <el-button type="info" @click="handleLogout" size="small">退出登录</el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" class="aside">
          <el-menu
            active-text-color="#ffd04b"
            background-color="#545c64"
            class="el-menu-vertical-demo"
            :default-active="activeMenu"
            text-color="#fff"
            router
          >
            <!-- Reader Navigation items -->
            <el-menu-item index="/books">
              <el-icon><Reading /></el-icon>
              <span>图书列表</span>
            </el-menu-item>
            <el-menu-item index="/user/borrow-records">
              <el-icon><List /></el-icon>
              <span>我的借阅</span>
            </el-menu-item>
            <!-- Add Personal Profile menu item -->
            <el-menu-item index="/user/profile">
              <el-icon><Avatar /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
            <!-- Add My Reservations menu item -->
            <el-menu-item index="/user/reservations">
              <el-icon><List /></el-icon>
              <span>我的预约</span>
            </el-menu-item>

            <!-- Admin Navigation items (conditionally rendered) -->
            <el-sub-menu index="/admin" v-if="userStore.user && userStore.user.role === 'ADMIN'">
               <template #title>
                  <el-icon><Setting /></el-icon>
                  <span>管理员菜单</span>
               </template>
               <el-menu-item index="/admin/books">
                 <el-icon><Management /></el-icon>
                 <span>图书管理</span>
               </el-menu-item>
               <el-menu-item index="/admin/categories">
                 <el-icon><Menu /></el-icon>
                 <span>分类管理</span>
               </el-menu-item>
               <el-menu-item index="/admin/borrow-records">
                 <el-icon><Tickets /></el-icon>
                 <span>借阅管理</span>
               </el-menu-item>
                <!-- Add other admin menu items here (Users) -->
                 <el-menu-item index="/admin/users">
                   <el-icon><Avatar /></el-icon>
                   <span>用户管理</span>
                 </el-menu-item>
                 <!-- Add Reviews Management menu item -->
                 <el-menu-item index="/admin/reviews">
                   <el-icon><ChatDotRound /></el-icon>
                   <span>评论管理</span>
                 </el-menu-item>
                 <!-- Add Reservations Management menu item -->
                 <el-menu-item index="/admin/reservations">
                   <el-icon><Calendar /></el-icon>
                   <span>预约管理</span>
                 </el-menu-item>
            </el-sub-menu>

          </el-menu>
        </el-aside>
        <el-main class="main-content">
          <!-- Router view will render nested routes like BookList.vue or AdminBooks.vue -->
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
// Import icons for menu
import { Reading, Management, Menu, Setting, Tickets, Avatar, List, ChatDotRound, Calendar } from '@element-plus/icons-vue' 

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// Computed property to set active menu item based on current route
const activeMenu = computed(() => {
  // Handle nested routes by finding the top-level parent that matches a menu group
  const matched = route.matched;
   if (matched.length > 1 && matched[1].path.startsWith('/admin')) {
       return '/admin';
   }
  return route.path;
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// Optionally fetch user details again if needed, though store should handle persistence
// onMounted(() => {
//   // Check if user data is in store/localStorage, if not, maybe fetch user profile
//   if (!userStore.user && userStore.token) {
//     // TODO: Implement fetch user profile API call if needed
//     console.warn('User data not in store, consider fetching user profile.');
//   }
// });

</script>

<style scoped>
.common-layout {
  min-height: 100vh;
}

.el-container {
  width: 100%; /* Ensure containers take full width */
}

.header {
  background-color: #409EFF;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px; /* Add some padding */
}

.header-left {
  display: flex;
  align-items: center;
}

.header h1 {
    margin: 0;
    font-size: 20px; /* Slightly smaller font size */
}

.header-right span {
    margin-right: 10px;
    font-size: 14px;
}

.aside {
  background-color: #545c64;
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
}

.main-content {
  padding: 20px; /* Add padding to the main content area */
}
</style> 