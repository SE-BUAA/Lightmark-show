/**
 * 用户中心API模块
 *
 * 对应新版 API 文档第 4 节「用户中心」
 * 所有接口需携带 Authorization: Bearer <token>
 */

import { http } from "@/utils/request";

// ─── 数据类型 ─────────────────────────────────────────

/** 用户DTO — GET /api/user/current 返回 */
export interface UserCurrentDTO {
  id: string;
  phone: string;
  email: string;
  nickname: string;
  avatar: string;
  points: number;
  level: number;
  identity: string;
  roles: string[];
  permissions: string[];
}

/** 用户资料更新请求体 — PUT /api/user/current */
export interface UserUpdateRequest {
  nickname?: string;
  phone?: string;
  email?: string;
}

/** 出行人DTO */
export interface TravelerDTO {
  id?: number;
  name: string;
  idCard: string;
  phone?: string;
}

/** 积分记录DTO */
export interface PointsLogDTO {
  id: number;
  points: number;
  type: string;
  remark: string;
  createTime: string;
}

/** 等级升级信息DTO */
export interface UserLevelUpgradeInfoDTO {
  currentLevel: number;
  currentPoints: number;
  nextLevel: number;
  pointsToNext: number;
}

/** 订单DTO（概要） */
export interface OrderDTO {
  orderNo: string;
  orderType: string;
  productName: string;
  payAmount: number;
  status: number;
  createTime: string;
}

/** 分页响应 */
export interface PageResponse<T> {
  total: number;
  list: T[];
}

// ─── 个人信息 ─────────────────────────────────────────

/**
 * 获取当前用户信息
 * GET /api/user/current
 */
export const getCurrentUser = (): Promise<UserCurrentDTO> => {
  return http.get<UserCurrentDTO>("/user/current");
};

/**
 * 更新当前用户个人信息
 * PUT /api/user/current
 */
export const updateCurrentUser = (
  data: UserUpdateRequest
): Promise<UserCurrentDTO> => {
  return http.put<UserCurrentDTO>("/user/current", data);
};

/**
 * 更新头像
 * POST /api/user/avatar
 */
export const updateAvatar = (
  avatarUrl: string
): Promise<{ avatarUrl: string }> => {
  return http.post<{ avatarUrl: string }>("/user/avatar", { avatarUrl });
};

/**
 * 修改密码
 * PUT /api/user/password
 */
export const changePassword = (data: {
  oldPassword: string;
  newPassword: string;
}): Promise<boolean> => {
  return http.put<boolean>("/user/password", data);
};

// ─── 出行人管理 ──────────────────────────────────────

/**
 * 获取出行人列表
 * GET /api/user/travelers
 */
export const getTravelers = (): Promise<TravelerDTO[]> => {
  return http.get<TravelerDTO[]>("/user/travelers");
};

/**
 * 新增出行人
 * POST /api/user/travelers
 */
export const addTraveler = (data: TravelerDTO): Promise<TravelerDTO> => {
  return http.post<TravelerDTO>("/user/travelers", data);
};

/**
 * 修改出行人
 * PUT /api/user/travelers/{id}
 */
export const updateTraveler = (
  id: number,
  data: TravelerDTO
): Promise<TravelerDTO> => {
  return http.put<TravelerDTO>(`/user/travelers/${id}`, data);
};

/**
 * 删除出行人
 * DELETE /api/user/travelers/{id}
 */
export const deleteTraveler = (id: number): Promise<boolean> => {
  return http.delete<boolean>(`/user/travelers/${id}`);
};

// ─── 积分与等级 ──────────────────────────────────────

/**
 * 获取积分明细
 * GET /api/user/points/logs
 */
export const getPointsLogs = (): Promise<PageResponse<PointsLogDTO>> => {
  return http.get<PageResponse<PointsLogDTO>>("/user/points/logs");
};

/**
 * 获取等级升级信息
 * GET /api/user/level/upgrade-info
 */
export const getLevelUpgradeInfo = (): Promise<UserLevelUpgradeInfoDTO> => {
  return http.get<UserLevelUpgradeInfoDTO>("/user/level/upgrade-info");
};

// ─── 订单 ────────────────────────────────────────────

/**
 * 获取我的订单
 * GET /api/user/orders
 */
export const getUserOrders = (): Promise<PageResponse<OrderDTO>> => {
  return http.get<PageResponse<OrderDTO>>("/user/orders");
};

/**
 * 获取订单详情
 * GET /api/user/orders/{orderNo}
 */
export const getOrderDetail = (orderNo: string): Promise<OrderDTO> => {
  return http.get<OrderDTO>(`/user/orders/${orderNo}`);
};
