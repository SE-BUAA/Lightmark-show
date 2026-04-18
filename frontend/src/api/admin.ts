import http from './http'
import type { DashboardSummary, OrderRow, ProductRow, UserRow } from '@/types/app'

export function fetchDashboardSummary(): Promise<DashboardSummary> {
  return http.get('/api/admin/dashboard/summary') as unknown as Promise<DashboardSummary>
}

export function fetchProducts(params: Record<string, unknown>): Promise<{ items: ProductRow[]; page: number; size: number; total: number }> {
  return http.get('/api/admin/products', { params }) as unknown as Promise<{ items: ProductRow[]; page: number; size: number; total: number }>
}

export function updateProductStatus(id: number, status: number): Promise<void> {
  return http.patch(`/api/admin/products/${id}/status`, null, { params: { status } }) as unknown as Promise<void>
}

export function fetchOrders(params: Record<string, unknown>): Promise<{ items: OrderRow[]; page: number; size: number; total: number }> {
  return http.get('/api/admin/orders', { params }) as unknown as Promise<{ items: OrderRow[]; page: number; size: number; total: number }>
}

export function updateOrderStatus(id: number, status: number, cancelReason?: string): Promise<void> {
  return http.patch(`/api/admin/orders/${id}/status`, null, { params: { status, cancelReason } }) as unknown as Promise<void>
}

export function fetchUsers(params: Record<string, unknown>): Promise<{ items: UserRow[]; page: number; size: number; total: number }> {
  return http.get('/api/admin/users', { params }) as unknown as Promise<{ items: UserRow[]; page: number; size: number; total: number }>
}

export function updateUserStatus(id: number, status: number): Promise<void> {
  return http.patch(`/api/admin/users/${id}/status`, null, { params: { status } }) as unknown as Promise<void>
}

export function updateUserLevel(id: number, level: number): Promise<void> {
  return http.patch(`/api/admin/users/${id}/level`, null, { params: { level } }) as unknown as Promise<void>
}


