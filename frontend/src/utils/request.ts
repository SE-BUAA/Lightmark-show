/**
 * HTTP请求工具类
 * 基于Axios封装的HTTP客户端，提供统一的请求处理、认证拦截和错误处理
 */

import axios, { AxiosError, AxiosInstance, AxiosRequestConfig } from "axios";
import { ElMessage } from "element-plus";
import { getAuthSnapshot } from "@/utils/auth";

/**
 * API响应数据结构
 * @template T - 响应数据的具体类型
 */
interface ApiResponse<T> {
  /** 业务状态码，0表示成功 */
  code: number;
  /** 状态描述，成功时为 "success" */
  msg: string;
  /** 响应数据体 */
  data: T;
}

// 创建Axios实例，配置基础URL和超时时间
const request: AxiosInstance = axios.create({
  // 使用环境变量中的API基础URL，如果未设置则使用默认值
  baseURL: process.env.VUE_APP_API_BASE_URL || "/api",
  // 请求超时时间，单位毫秒
  timeout: 15000,
});

// 请求拦截器：在发送请求前自动添加认证令牌
request.interceptors.request.use((config) => {
  // 从认证快照中获取令牌
  const { token } = getAuthSnapshot();
  if (token) {
    // 确保headers对象存在
    config.headers = config.headers ?? {};
    // 在请求头中添加Bearer认证令牌
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：处理响应数据和错误
request.interceptors.response.use(
  // 成功响应处理
  (response) => {
    // 将响应数据转换为ApiResponse类型
    const payload = response.data as ApiResponse<unknown>;

    // 如果响应数据不是预期的ApiResponse格式，则直接返回原始数据
    if (typeof payload?.code !== "number") {
      return response.data;
    }

    // 检查业务状态码，如果不是0（表示失败）则显示错误消息
    if (payload.code !== 0) {
      ElMessage.error(payload.msg || "请求失败");
      return Promise.reject(new Error(payload.msg || "Request failed"));
    }

    // 返回成功数据
    return payload.data;
  },
  // 错误响应处理
  (error: AxiosError) => {
    // 从错误响应中提取错误消息，优先使用API返回的消息，否则使用通用错误消息
    const message =
      (error.response?.data as { msg?: string } | undefined)?.msg ||
      error.message ||
      "网络异常";
    // 显示错误消息给用户
    // 返回拒绝的Promise，让调用方可以处理错误
    return Promise.reject(new Error(message));
  }
);

/**
 * HTTP请求客户端
 * 提供常用的HTTP方法封装
 */
export const http = {
  /**
   * GET请求
   * @param url - 请求地址
   * @param config - 请求配置
   * @returns Promise<T> - 响应数据
   */
  get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return request.get(url, config) as Promise<T>;
  },
  /**
   * POST请求
   * @param url - 请求地址
   * @param data - 请求数据
   * @param config - 请求配置
   * @returns Promise<T> - 响应数据
   */
  post<T>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<T> {
    return request.post(url, data, config) as Promise<T>;
  },
  /**
   * PUT请求
   * @param url - 请求地址
   * @param data - 请求数据
   * @param config - 请求配置
   * @returns Promise<T> - 响应数据
   */
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return request.put(url, data, config) as Promise<T>;
  },
  /**
   * PATCH请求
   * @param url - 请求地址
   * @param data - 请求数据
   * @param config - 请求配置
   * @returns Promise<T> - 响应数据
   */
  patch<T>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<T> {
    return request.patch(url, data, config) as Promise<T>;
  },
  /**
   * DELETE请求
   * @param url - 请求地址
   * @param config - 请求配置
   * @returns Promise<T> - 响应数据
   */
  delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return request.delete(url, config) as Promise<T>;
  },
};
