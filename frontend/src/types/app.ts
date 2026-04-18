export interface SessionUser {
  userId: number
  nickname: string
  roles: string[]
}

export interface LoginRequest {
  account: string
  password: string
}

export interface RegisterRequest {
  account: string
  password: string
  nickname: string
}

export interface LoginResponse extends SessionUser {
  token: string
}

export interface DestinationCard {
  city: string
  route: string
  price: string
  highlight: string
  tone: string
}

export interface ModuleCard {
  id: 'm2' | 'm3' | 'm4' | 'm5' | 'm6'
  title: string
  description: string
  status: string
}

export interface StoryCard {
  title: string
  content: string
  author: string
  accent: string
}

export interface DashboardItem {
  name: string
  sold_count: number
}

export interface DashboardSummary {
  totalUsers: number
  totalOrders: number
  paidOrders: number
  gmv: number | string
  activeProducts: number
  hotDestinations: DashboardItem[]
}

export interface ProductRow {
  id: number
  product_type: string
  name: string
  price: number
  stock: number
  sold_count: number
  status: number
  update_time: string
}

export interface OrderRow {
  id: number
  order_no: string
  user_id: number
  order_type: string
  pay_amount: number
  payment_method: string
  status: number
  create_time: string
}

export interface UserRow {
  id: number
  phone: string
  email: string
  nickname: string
  points: number
  level: number
  status: number
  create_time: string
}

