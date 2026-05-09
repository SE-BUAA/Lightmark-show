/**
 * 身份认证API模块
 * 提供登录、注册和登出相关的API接口
 *
 * 对应新版 API 文档第 3 节「认证接口」
 */

import { http } from "@/utils/request";

/**
 * 登录请求数据结构
 */
export interface LoginRequest {
  /** 账户名（手机号或邮箱） */
  account: string;
  /** 密码 */
  password: string;
}

/**
 * 注册请求数据结构
 */
export interface RegisterRequest {
  /** 账户名（手机号或邮箱） */
  account: string;
  /** 密码 */
  password: string;
  /** 昵称 */
  nickname: string;
}

/**
 * 登录响应数据结构
 * POST /api/auth/login
 */
export interface LoginResponse {
  /** 认证令牌 */
  token: string;
  /** 用户ID */
  userId: string | number;
  /** 昵称 */
  nickname: string;
  /** 头像URL */
  avatar: string;
  /** 身份枚举：ADMIN | USER */
  identity: string;
  /** 角色数组，与 identity 一致，如 ["USER"] 或 ["ADMIN"] */
  roles: string[];
}

/**
 * 用户登录
 * POST /api/auth/login
 */
export const loginApi = (payload: LoginRequest): Promise<LoginResponse> => {
  return http.post<LoginResponse>("/auth/login", payload);
};

/**
 * 用户注册
 * POST /api/auth/register
 */
export const registerApi = (
  payload: RegisterRequest
): Promise<Record<string, unknown>> => {
  return http.post<Record<string, unknown>>("/auth/register", payload);
};

/**
 * 用户登出
 * POST /api/auth/logout
 */
export const logoutApi = (): Promise<boolean> => {
  return http.post<boolean>("/auth/logout");
};
