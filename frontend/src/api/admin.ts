/**
 * 管理员API模块
 *
 * 对应新版 API 文档第 8 节「管理后台」
 * 所有接口需要 ADMIN 角色 + Authorization: Bearer <token>
 */

import { http } from "@/utils/request";

// ─── 通用类型 ─────────────────────────────────────────

/** 新版分页响应：data.list 为数组，非 items */
export interface PageResponse<T> {
  total: number;
  page: number;
  size: number;
  list: T[];
}

// ─── 仪表盘 ──────────────────────────────────────────

export interface DashboardSummary {
  totalUsers: number;
  totalOrders: number;
  totalRevenue: number;
}

export interface DashboardTrendDTO {
  date: string;
  orderCount: number;
  revenue: number;
}

export interface HotProductDTO {
  id: number;
  name: string;
  productType: string;
  soldCount: number;
  revenue: number;
}

// ─── 产品 ────────────────────────────────────────────

export interface AdminProductDTO {
  id: number;
  product_type: string;
  name: string;
  price: number;
  stock: number;
  sold_count: number;
  status: number;
}

export interface CreateProductRequest {
  productType: string;
  name: string;
  price: number;
  stock: number;
  soldCount: number;
  status: number;
  categoryTags: string;
  extra: string;
}

// ─── 订单 ────────────────────────────────────────────

export interface AdminOrderDTO {
  id: number;
  order_no: string;
  user_id: number;
  order_type: string;
  pay_amount: number;
  payment_method: string;
  status: number;
  create_time: string;
}

// ─── 用户 ────────────────────────────────────────────

export interface UserDTO {
  id: number;
  phone: string;
  email: string;
  nickname: string;
  level: number;
  status: number;
  register_source: string;
  create_time: string;
}

// ─── 日志 ────────────────────────────────────────────

export interface AdminLogDTO {
  id: number;
  adminId: number;
  operation: string;
  target: string;
  result: string;
  createTime: string;
}

// ═══════════════════════════════════════════════════════
//  仪表盘
// ═══════════════════════════════════════════════════════

/** GET /api/admin/dashboard/summary — 核心指标 */
export const getDashboardSummary = (): Promise<DashboardSummary> => {
  return http.get<DashboardSummary>("/admin/dashboard/summary");
};

/** GET /api/admin/dashboard/trends — 近 7 天交易趋势 */
export const getDashboardTrends = (): Promise<DashboardTrendDTO[]> => {
  return http.get<DashboardTrendDTO[]>("/admin/dashboard/trends");
};

/** GET /api/admin/dashboard/hot-products — 热门产品 Top10 */
export const getHotProducts = (): Promise<HotProductDTO[]> => {
  return http.get<HotProductDTO[]>("/admin/dashboard/hot-products");
};

// ═══════════════════════════════════════════════════════
//  用户管理
// ═══════════════════════════════════════════════════════

/** GET /api/admin/users — 用户列表 */
export const getAdminUsers = (params?: {
  keyword?: string;
  status?: number;
}): Promise<PageResponse<UserDTO>> => {
  return http.get<PageResponse<UserDTO>>("/admin/users", { params });
};

/** PUT /api/admin/users/{id}/status — 封禁/解封 */
export const updateUserStatus = (
  id: number,
  status: number
): Promise<boolean> => {
  return http.put<boolean>(`/admin/users/${id}/status`, { status });
};

/** PUT /api/admin/users/{id}/level — 调整会员等级 */
export const updateUserLevel = (
  id: number,
  level: number
): Promise<boolean> => {
  return http.put<boolean>(`/admin/users/${id}/level`, { level });
};

// ═══════════════════════════════════════════════════════
//  产品管理
// ═══════════════════════════════════════════════════════

/** GET /api/admin/products — 产品列表 */
export const getAdminProducts = (params?: {
  productType?: string;
  name?: string;
  status?: number;
}): Promise<PageResponse<AdminProductDTO>> => {
  return http.get<PageResponse<AdminProductDTO>>("/admin/products", { params });
};

/** PUT /api/admin/products/{id}/status — 上架/下架 */
export const updateProductStatus = (
  id: number,
  status: number
): Promise<boolean> => {
  return http.put<boolean>(`/admin/products/${id}/status`, { status });
};

/** PUT /api/admin/products/{id}/price — 调价 */
export const updateProductPrice = (
  id: number,
  price: number
): Promise<boolean> => {
  return http.put<boolean>(`/admin/products/${id}/price`, { price });
};

/** PUT /api/admin/products/{id}/stock — 调库存 */
export const updateProductStock = (
  id: number,
  stock: number
): Promise<boolean> => {
  return http.put<boolean>(`/admin/products/${id}/stock`, { stock });
};

/** POST /api/admin/products — 新增产品 */
export const createProduct = (
  data: CreateProductRequest
): Promise<AdminProductDTO> => {
  return http.post<AdminProductDTO>("/admin/products", data);
};

/** DELETE /api/admin/products/{id} — 逻辑删除 */
export const deleteProduct = (id: number): Promise<boolean> => {
  return http.delete<boolean>(`/admin/products/${id}`);
};

// ═══════════════════════════════════════════════════════
//  订单管理
// ═══════════════════════════════════════════════════════

/** GET /api/admin/orders — 订单列表 */
export const getAdminOrders = (
  status?: number
): Promise<PageResponse<AdminOrderDTO>> => {
  return http.get<PageResponse<AdminOrderDTO>>("/admin/orders", {
    params: { status },
  });
};

/** PUT /api/admin/orders/{orderNo}/status — 强制修改订单状态 */
export const updateOrderStatus = (
  orderNo: string,
  status: number,
  remark?: string
): Promise<boolean> => {
  return http.put<boolean>(`/admin/orders/${orderNo}/status`, {
    status,
    ...(remark ? { remark } : {}),
  });
};

/** POST /api/admin/orders/{orderNo}/refund — 强制退款 */
export const refundOrder = (
  orderNo: string,
  remark?: string
): Promise<boolean> => {
  return http.post<boolean>(`/admin/orders/${orderNo}/refund`, {
    ...(remark ? { remark } : {}),
  });
};

// ═══════════════════════════════════════════════════════
//  操作日志
// ═══════════════════════════════════════════════════════

/** GET /api/admin/logs — 管理员操作日志 */
export const getAdminLogs = (params?: {
  admin_id?: number;
  operation?: string;
  result?: string;
}): Promise<PageResponse<AdminLogDTO>> => {
  return http.get<PageResponse<AdminLogDTO>>("/admin/logs", { params });
};

// ═══════════════════════════════════════════════════════
//  问答 & 评论
// ═══════════════════════════════════════════════════════

/** POST /api/admin/questions/{id}/answer — 官方回答问题 */
export const answerQuestion = (
  id: number,
  answer: string
): Promise<boolean> => {
  return http.post<boolean>(`/admin/questions/${id}/answer`, { answer });
};

/** PUT /api/admin/comments/{id}/approve — 审核评论 */
export const approveComment = (
  id: number,
  isApproved: number
): Promise<boolean> => {
  return http.put<boolean>(`/admin/comments/${id}/approve`, { isApproved });
};
