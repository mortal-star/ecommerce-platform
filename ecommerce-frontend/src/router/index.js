import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

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
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/DashboardView.vue'),
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true }
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
  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  document.title = to.meta.title ? `${to.meta.title} - 电商平台` : '电商平台'
  next()
})

export default router
