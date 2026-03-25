import http from './http'

// ── 类型定义（匹配后端 Appointment 实体）────────────────────

export interface Vet {
  id: number
  name: string
  specialty?: string
}

export interface AppointmentPet {
  id: number
  name: string
  breed?: string
}

// 后端返回的预约对象
export interface Appointment {
  id: number
  pet: AppointmentPet
  vet: Vet
  appointmentTime: string   // ISO格式，如 "2026-07-20T10:00:00"
  description?: string
}

// 新增预约时传给后端的请求体
export interface AddAppointmentPayload {
  appointmentTime: string   // ISO格式，如 "2026-07-20T10:00:00"
  description?: string
}

// ── 预约 API（对应后端 /appointments）────────────────────────

// 获取所有预约
export const getAllAppointments = () =>
  http.get<Appointment[]>('/appointments')

// 获取某只宠物的所有预约
export const getAppointmentsByPet = (petId: number) =>
  http.get<Appointment[]>('/appointments', { params: { petId } })

// 根据 ID 获取单条预约
export const getAppointmentById = (id: number) =>
  http.get<Appointment>(`/appointments/${id}`)

// 新增预约（需要同时传 petId 和 vetId 作为 query 参数）
export const addAppointment = (
  payload: AddAppointmentPayload,
  petId: number,
  vetId: number,
) =>
  http.post<Appointment>('/appointments', payload, {
    params: { petId, vetId },
  })

// 更新预约
export const updateAppointment = (id: number, payload: AddAppointmentPayload) =>
  http.put<Appointment>(`/appointments/${id}`, payload)

// 删除预约
export const deleteAppointment = (id: number) =>
  http.delete(`/appointments/${id}`)

// ── 兽医 API（对应后端 /api/v1/vets）────────────────────────

// 获取所有兽医
export const getAllVets = () =>
  http.get<Vet[]>('/api/v1/vets')

// 根据 ID 获取单个兽医
export const getVetById = (id: number) =>
  http.get<Vet>(`/api/v1/vets/${id}`)

// 新增兽医
export const addVet = (vet: Omit<Vet, 'id'>) =>
  http.post<Vet>('/api/v1/vets', vet)

// 删除兽医
export const deleteVet = (id: number) =>
  http.delete(`/api/v1/vets/${id}`)