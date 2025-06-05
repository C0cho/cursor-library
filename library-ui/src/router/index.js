import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user' // Import the user store
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/',
      name: 'Home',
      component: () => import('../views/Home.vue'), // Home.vue will be the layout
      meta: { requiresAuth: true },
      children: [
        {
          path: '', // Default child path for '/'
          redirect: '/books' // Redirect root to /books for now
        },
        {
          path: 'books', // Full path will be /books
          name: 'BookList',
          component: () => import('../views/BookList.vue'),
          meta: { requiresAuth: true } // Ensure BookList also requires auth
        },
        {
          path: 'admin/categories', // Full path will be /admin/categories
          name: 'AdminCategories',
          component: () => import('../views/admin/AdminCategories.vue'),
          meta: { requiresAuth: true, requiresAdmin: true } // Requires auth and admin role
        },
        {
          path: 'admin/books', // Full path will be /admin/books
          name: 'AdminBooks',
          component: () => import('../views/admin/AdminBooks.vue'),
          meta: { requiresAuth: true, requiresAdmin: true } // Requires auth and admin role
        },
        {
          path: 'admin/borrow-records', // Full path will be /admin/borrow-records
          name: 'AdminBorrowRecords',
          component: () => import('../views/admin/AdminBorrowRecords.vue'),
          meta: { requiresAuth: true, requiresAdmin: true } // Requires auth and admin role
        },
        {
          path: '/user/borrow-records',
          name: 'UserBorrowRecords',
          component: () => import('@/views/UserBorrowRecords.vue'),
          meta: {
            requiresAuth: true,
            title: '我的借阅记录'
          }
        },
        {
          path: 'user/profile', // Full path will be /user/profile
          name: 'UserProfile',
          component: () => import('@/views/UserProfile.vue'),
          meta: { requiresAuth: true, title: '个人中心' } // Requires authentication
        },
        {
          path: 'admin/reviews', // Full path will be /admin/reviews
          name: 'AdminReviews',
          component: () => import('../views/admin/AdminReviews.vue'),
          meta: { requiresAuth: true, requiresAdmin: true } // Requires auth and admin role
        },
        {
          path: 'admin/users', // Full path will be /admin/users
          name: 'AdminUsers',
          component: () => import('../views/admin/AdminUsers.vue'),
          meta: { requiresAuth: true, requiresAdmin: true } // Requires auth and admin role
        },
        {
          path: 'user/reservations', // Full path will be /user/reservations
          name: 'UserReservations',
          component: () => import('@/views/UserReservations.vue'),
          meta: { requiresAuth: true, title: '我的预约' }
        },
        {
          path: 'admin/reservations', // Full path will be /admin/reservations
          name: 'AdminReservations',
          component: () => import('../views/admin/AdminReservations.vue'),
          meta: { requiresAuth: true, requiresAdmin: true } // Requires auth and admin role
        }
        // Add other authenticated routes here as children
      ]
    }
  ]
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin);

  console.log('Router Guard: Navigating to', to.path);
  console.log('Router Guard: requiresAuth', requiresAuth);
  console.log('Router Guard: requiresAdmin', requiresAdmin);
  console.log('Router Guard: userStore.isLoggedIn', userStore.isLoggedIn);
  console.log('Router Guard: userStore.user', userStore.user);
  console.log('Router Guard: userStore.user?.role', userStore.user?.role);

  if (requiresAuth && !userStore.isLoggedIn) {
    // If the route requires authentication and the user is not logged in, redirect to login
    console.log('Router Guard: Redirecting to login (requires auth but not logged in)');
    next('/login');
  } else if (requiresAdmin && (!userStore.user || userStore.user.role !== 'ADMIN')) {
    // If the route requires admin role and the user is not admin, redirect to home or show error
    console.log('Router Guard: Blocking access (requires admin but user is not)', userStore.user?.role);
    ElMessage.error('无权限访问，只有管理员可以访问此页面');
    next(from.path || '/'); // Stay on current page or go to home
  } else {
    // Otherwise, allow navigation
    console.log('Router Guard: Allowing access');
    next();
  }
});

export default router 