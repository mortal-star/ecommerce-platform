import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/products',
    name: 'Products',
    component: () => import('@/views/ProductListView.vue'),
    meta: { title: '商品列表', public: true }
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetailView.vue'),
    meta: { title: '商品详情', public: true }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/CartView.vue'),
    meta: { title: '购物车', requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/OrderListView.vue'),
    meta: { title: '我的订单', requiresAuth: true }
  },
  {
    path: '/order/confirm',
    name: 'OrderConfirm',
    component: () => import('@/views/order/OrderConfirmView.vue'),
    meta: { title: '订单确认', requiresAuth: true }
  },
  {
    path: '/order/pay',
    name: 'OrderPay',
    component: () => import('@/views/order/OrderPayView.vue'),
    meta: { title: '订单支付', requiresAuth: true }
  },
  {
    path: '/review',
    name: 'Review',
    component: () => import('@/views/order/ReviewView.vue'),
    meta: { title: '商品评价', requiresAuth: true }
  },
  {
    path: '/notices',
    name: 'NoticeList',
    component: () => import('@/views/misc/NoticeListView.vue'),
    meta: { title: '系统公告', public: true }
  },
  {
    path: '/feedback',
    name: 'Feedback',
    component: () => import('@/views/misc/FeedbackView.vue'),
    meta: { title: '意见反馈', requiresAuth: true }
  },
  {
    path: '/user',
    component: () => import('@/views/user/UserLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/user/profile' },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'favorites',
        name: 'UserFavorites',
        component: () => import('@/views/user/FavoritesView.vue'),
        meta: { title: '我的收藏', requiresAuth: true }
      },
      {
        path: 'addresses',
        name: 'UserAddresses',
        component: () => import('@/views/user/AddressesView.vue'),
        meta: { title: '收货地址', requiresAuth: true }
      },
      {
        path: 'account',
        name: 'UserAccount',
        component: () => import('@/views/user/AccountView.vue'),
        meta: { title: '账号设置', requiresAuth: true }
      },
      {
        path: 'password',
        name: 'UserPassword',
        component: () => import('@/views/user/PasswordView.vue'),
        meta: { title: '修改密码', requiresAuth: true }
      }
    ]
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLoginView.vue'),
    meta: { title: '管理员登录', layout: 'blank', public: true }
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { layout: 'admin', requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '数据看板', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/PlaceholderView.vue'),
        meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/PlaceholderView.vue'),
        meta: { title: '商品列表', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/PlaceholderView.vue'),
        meta: { title: '分类管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/PlaceholderView.vue'),
        meta: { title: '订单管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'banners',
        name: 'AdminBanners',
        component: () => import('@/views/admin/PlaceholderView.vue'),
        meta: { title: '轮播图', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'notices',
        name: 'AdminNotices',
        component: () => import('@/views/admin/PlaceholderView.vue'),
        meta: { title: '系统公告', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'feedback',
        name: 'AdminFeedback',
        component: () => import('@/views/admin/PlaceholderView.vue'),
        meta: { title: '用户反馈', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/PlaceholderView.vue'),
        meta: { title: '系统设置', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/PlaceholderView.vue'),
        meta: { title: '个人信息', requiresAuth: true, requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  const isAdminRoute = to.path.startsWith('/admin') && to.name !== 'AdminLogin'

  if (to.meta.requiresAuth && !token) {
    if (isAdminRoute) {
      next({ name: 'AdminLogin', query: { redirect: to.fullPath } })
    } else {
      next({ name: 'Login', query: { redirect: to.fullPath } })
    }
    return
  }

  if (to.meta.requiresAdmin && token) {
    const userStore = useUserStore()
    if (!userStore.isAdmin) {
      ElMessage.warning('该页面仅管理员可访问')
      next({ name: 'AdminLogin', query: { redirect: to.fullPath } })
      return
    }
  }

  const suffix = isAdminRoute || to.name === 'AdminLogin' ? '管理后台' : '电商平台'
  document.title = to.meta.title ? `${to.meta.title} - ${suffix}` : suffix
  next()
})

export default router
