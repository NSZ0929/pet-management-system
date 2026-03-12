import http from './http'

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  password: string
}

export interface AuthResponse {
  message: string
  token: string
  tokenType: string
  expiresIn: number
  user: {
    id: number
    username: string
    role: string
  }
}

export interface RegisterResponse {
  message: string
  userId: number
  username: string
}

// 登录
export const login = (payload: LoginPayload) => http.post<AuthResponse>('/auth/login', payload)

// 注册
export const register = (payload: RegisterPayload) =>
  http.post<RegisterResponse>('/auth/register', payload)

// 验证 Token
export const checkToken = () => http.get('/auth/check-token')
