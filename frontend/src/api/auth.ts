import http from './http'
import type { LoginRequest, LoginResponse, RegisterRequest } from '@/types/app'

export function login(payload: LoginRequest): Promise<LoginResponse> {
  return http.post('/api/auth/login', payload) as unknown as Promise<LoginResponse>
}

export function register(payload: RegisterRequest): Promise<void> {
  return http.post('/api/auth/register', payload) as unknown as Promise<void>
}


export function logout(): Promise<void> {
  return http.post('/api/auth/logout') as unknown as Promise<void>
}


