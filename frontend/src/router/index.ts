import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken, isAdmin } from '@/utils/auth'

// @ts-ignore
import HomeView from '@/views/home/HomeView.vue'
// @ts-ignore
import LoginView from '@/views/auth/LoginView.vue'
// @ts-ignore
import AdminLayout from '@/layouts/AdminLayout.vue'
// @ts-ignore
import DashboardView from '@/views/admin/DashboardView.vue'
// @ts-ignore
import ProductManageView from '@/views/admin/ProductManageView.vue'
// @ts-ignore
import OrderManageView from '@/views/admin/OrderManageView.vue'
// @ts-ignore
import UserManageView from '@/views/admin/UserManageView.vue'
// @ts-ignore
import ModulePlaceholderView from '@/views/placeholders/ModulePlaceholderView.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { guestOnly: true },
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, adminOnly: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: DashboardView },
      { path: 'products', name: 'products', component: ProductManageView },
      { path: 'orders', name: 'orders', component: OrderManageView },
      { path: 'users', name: 'users', component: UserManageView },
      { path: 'm2', name: 'm2', component: ModulePlaceholderView, props: { moduleId: 'm2', title: '模块二：用户中心与 AI 智能助手' } },
      { path: 'm3', name: 'm3', component: ModulePlaceholderView, props: { moduleId: 'm3', title: '模块三：机票预订模块' } },
      { path: 'm4', name: 'm4', component: ModulePlaceholderView, props: { moduleId: 'm4', title: '模块四：酒店预订模块' } },
      { path: 'm5', name: 'm5', component: ModulePlaceholderView, props: { moduleId: 'm5', title: '模块五：火车票与旅游度假模块' } },
      { path: 'm6', name: 'm6', component: ModulePlaceholderView, props: { moduleId: 'm6', title: '模块六：智能行程与社区模块' } },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const token = getToken()

  if (to.meta.guestOnly && token) {
    return '/admin/dashboard'
  }

  if (to.meta.requiresAuth && !token) {
    return '/login'
  }

  if (to.meta.adminOnly && !isAdmin()) {
    return '/'
  }

  return true
})

export default router


