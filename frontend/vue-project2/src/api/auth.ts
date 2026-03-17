import http from './http'

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  password: string
}

export interface AuthUser {
  id: number
  username: string
  role: string
}

export interface LoginResponse {
  message: string
  token: string
  tokenType: string
  expiresIn: number
  user: AuthUser
}

export const login = (data: LoginPayload) => http.post<LoginResponse>('/auth/login', data)

export const register = (data: RegisterPayload) => http.post('/auth/register', data)

export const checkToken = () => http.get('/auth/check-token')

// 修改密码 PUT /auth/change-password
// Body: { oldPassword, newPassword }
export const changePassword = (oldPassword: string, newPassword: string) =>
  http.put('/auth/change-password', { oldPassword, newPassword })

// 修改用户名 PUT /auth/update-username
// Body: { newUsername }
export const updateUsername = (newUsername: string) =>
  http.put<{ message: string; username: string }>('/auth/update-username', { newUsername })
