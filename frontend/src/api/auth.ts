/**
 * 身份认证API模块
 * 提供登录、注册、验证码和登出相关的API接口
 */

import { http } from "@/utils/request";

export interface LoginRequest {
  account: string;
  password: string;
  captchaCode: string;
}

export interface RegisterRequest {
  email: string;
  nickname: string;
  countryCode: string;
  phone: string;
  password: string;
  verificationCode: string;
  captchaCode: string;
}

export interface SendEmailVerificationCodeRequest {
  email: string;
  captchaCode: string;
}

export interface LoginResponse {
  token: string;
  userId: string | number;
  nickname: string;
  avatar: string;
  identity: string;
  roles: string[];
}

export const loginApi = (payload: LoginRequest): Promise<LoginResponse> => {
  return http.post<LoginResponse>("/auth/login", payload);
};

export const registerApi = (
  payload: RegisterRequest
): Promise<Record<string, unknown>> => {
  return http.post<Record<string, unknown>>("/auth/register", payload);
};

export const sendEmailVerificationCodeApi = (
  payload: SendEmailVerificationCodeRequest
): Promise<boolean> => {
  return http.post<boolean>("/auth/verification/email/send", payload);
};

export const logoutApi = (): Promise<boolean> => {
  return http.post<boolean>("/auth/logout");
};
