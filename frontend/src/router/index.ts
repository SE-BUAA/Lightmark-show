/**
 * 路由配置文件
 * 定义应用的所有路由及其访问权限控制
 */

import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import HomeView from "../views/HomeView.vue";
import { getAuthSnapshot } from "@/utils/auth";

/**
 * 路由配置数组
 * 定义所有页面路由及其元数据
 */
const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "home",
    component: HomeView,
  },
  {
    path: "/login",
    name: "user-login",
    component: () => import("@/views/auth/UserLoginView.vue"),
    // 仅允许未登录用户访问（访客专用）
    meta: { guestOnly: true },
  },
  {
    path: "/flights",
    name: "flights",
    component: () => import("@/views/module/FlightsView.vue"),
    // 需要登录后访问
    meta: { requiresAuth: true },
  },
  {
    path: "/hotels",
    name: "hotels",
    component: () => import("@/views/module/HotelsView.vue"),
    // 需要登录后访问
    meta: { requiresAuth: true },
  },
  {
    path: "/hotels/search",
    name: "HotelSearch",
    component: () => import("@/views/hotel/HotelSearch.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/hotel/search",
    redirect: "/hotels/search",
  },
  {
    path: "/hotels/detail/:hotelId",
    name: "HotelDetail",
    component: () => import("@/views/hotel/HotelDetail.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/hotels/payment/:orderId",
    name: "HotelPayment",
    component: () => import("@/views/hotel/HotelPayment.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/hotels/orders",
    name: "HotelOrders",
    component: () => import("@/views/hotel/HotelOrders.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/hotel/detail/:hotelId",
    redirect: (to) => ({
      path: `/hotels/detail/${to.params.hotelId}`,
      query: to.query,
    }),
  },
  {
    path: "/trains",
    name: "trains",
    component: () => import("@/views/module/TrainsView.vue"),
    // 需要登录后访问
    meta: { requiresAuth: true },
  },
  {
    path: "/vacations",
    name: "vacations",
    component: () => import("@/views/module/VacationsView.vue"),
    // 需要登录后访问
    meta: { requiresAuth: true },
  },
  {
    path: "/itinerary",
    name: "itinerary",
    component: () => import("@/views/module/ItineraryView.vue"),
    // 需要登录后访问
    meta: { requiresAuth: true },
  },
  {
    path: "/community",
    name: "community",
    component: () => import("@/views/module/CommunityView.vue"),
    // 需要登录后访问
    meta: { requiresAuth: true },
  },
  {
    path: "/user-center",
    name: "user-center",
    component: () => import("@/views/module/UserCenterView.vue"),
    // 需要登录后访问
    meta: { requiresAuth: true },
  },
  {
    path: "/admin/login",
    name: "admin-login",
    component: () => import("@/views/admin/AdminLoginView.vue"),
    // 仅允许未登录用户访问（访客专用）
    meta: { guestOnly: true },
  },
  {
    path: "/admin",
    component: () => import("@/layouts/AdminLayout.vue"),
    // 需要认证且仅限管理员访问
    meta: { requiresAuth: true, adminOnly: true },
    children: [
      {
        path: "",
        redirect: "/admin/dashboard",
      },
      {
        path: "dashboard",
        name: "admin-dashboard",
        component: () => import("@/views/admin/DashboardView.vue"),
        // 需要认证且仅限管理员访问
        meta: { requiresAuth: true, adminOnly: true },
      },
      {
        path: "products",
        name: "admin-products",
        component: () => import("@/views/admin/ProductManageView.vue"),
        // 需要认证且仅限管理员访问
        meta: { requiresAuth: true, adminOnly: true },
      },
      {
        path: "orders",
        name: "admin-orders",
        component: () => import("@/views/admin/OrderManageView.vue"),
        // 需要认证且仅限管理员访问
        meta: { requiresAuth: true, adminOnly: true },
      },
      {
        path: "users",
        name: "admin-users",
        component: () => import("@/views/admin/UserManageView.vue"),
        // 需要认证且仅限管理员访问
        meta: { requiresAuth: true, adminOnly: true },
      },
      {
        path: "tables",
        name: "admin-tables",
        component: () => import("@/views/admin/TableBrowserView.vue"),
        // 需要认证且仅限管理员访问
        meta: { requiresAuth: true, adminOnly: true },
      },
    ],
  },
  {
    path: "/about",
    name: "about",
    // 路由级别的代码分割
    // 这会为此路由生成一个单独的代码块 (about.[hash].js)
    // 当访问此路由时才懒加载
    component: () =>
      import(/* webpackChunkName: "about" */ "../views/AboutView.vue"),
  },
  {
    path: "/privacy-policy",
    name: "privacy-policy",
    component: () => import("@/views/PrivacyPolicyView.vue"),
  },
];

// 创建路由实例，使用HTML5 History模式
const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

// 全局前置守卫：处理路由权限验证
router.beforeEach((to) => {
  // 获取当前认证状态
  const auth = getAuthSnapshot();
  // 检查目标路由是否需要认证
  const requiresAuth = Boolean(to.meta.requiresAuth);
  // 检查目标路由是否仅限管理员访问
  const adminOnly = Boolean(to.meta.adminOnly);
  // 检查目标路由是否仅限访客访问
  const guestOnly = Boolean(to.meta.guestOnly);

  // 如果需要认证但用户未登录
  if (requiresAuth && !auth.token) {
    // 如果是管理员专属页面，重定向到管理员登录
    if (adminOnly) {
      return { path: "/admin/login", query: { redirect: to.fullPath } };
    }
    // 否则重定向到普通用户登录
    return { path: "/login", query: { redirect: to.fullPath } };
  }

  // 如果是管理员专属页面但用户不是管理员
  if (adminOnly && !auth.isAdmin) {
    // 重定向到普通用户登录页面
    return { path: "/login" };
  }

  // 如果是访客专用页面但用户已登录
  if (guestOnly && auth.token) {
    // 如果尝试访问管理员页面
    if (to.path.startsWith("/admin")) {
      // 如果用户是管理员则重定向到管理员面板，否则重定向到登录
      return auth.isAdmin ? { path: "/admin/dashboard" } : { path: "/login" };
    }
    // 根据用户角色进行相应重定向
    return auth.isAdmin ? { path: "/admin/dashboard" } : { path: "/" };
  }

  // 允许导航继续
  return true;
});

export default router;
