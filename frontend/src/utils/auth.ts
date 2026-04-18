import type { SessionUser } from '@/types/app'

const TOKEN_KEY = 'timemark_token'
const USER_KEY = 'timemark_user'

export function saveSession(token: string, user: SessionUser) {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function getUser(): SessionUser | null {
  const value = localStorage.getItem(USER_KEY)
  if (!value) {
    return null
  }
  try {
    return JSON.parse(value) as SessionUser
  } catch {
    return null
  }
}

export function isAdmin(): boolean {
  const user = getUser()
  return Boolean(user?.roles?.includes('ADMIN'))
}


