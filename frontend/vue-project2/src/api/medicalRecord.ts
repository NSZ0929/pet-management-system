import http from './http'

// ── 类型定义 ──────────────────────────────────────────────

export interface Disease {
  id: number
  name: string
  category?: string
  description?: string
}

export interface MedicalRecord {
  id: number
  title: string
  description?: string
  visitDate?: string
  // 注意：后端 @JsonIgnoreProperties({"pet","disease"}) 所以这两个字段不会返回
}

export interface AddMedicalRecordPayload {
  title: string
  description?: string
  visitDate?: string
}

// ── 医疗记录 API ──────────────────────────────────────────

// 获取某只宠物的所有医疗记录
export const getMedicalRecordsByPet = (petId: number) =>
  http.get<MedicalRecord[]>(`/medical-records/pet/${petId}`)

// 添加医疗记录
export const addMedicalRecord = (
  payload: AddMedicalRecordPayload,
  petId: number,
  diseaseId?: number,
) => {
  const params: Record<string, number> = { petId }
  if (diseaseId) params.diseaseId = diseaseId
  return http.post<MedicalRecord>('/medical-records', payload, { params })
}

// 更新医疗记录
export const updateMedicalRecord = (
  id: number,
  payload: AddMedicalRecordPayload,
  diseaseId?: number,
) => {
  const params: Record<string, number> = {}
  if (diseaseId) params.diseaseId = diseaseId
  return http.put<MedicalRecord>(`/medical-records/${id}`, payload, { params })
}

// 删除医疗记录
export const deleteMedicalRecord = (id: number) => http.delete(`/medical-records/${id}`)

// ── 疾病 API ──────────────────────────────────────────────

export const getAllDiseases = () => http.get<Disease[]>('/diseases')

export const searchDiseases = (name: string) =>
  http.get<Disease[]>('/diseases/search', { params: { name } })
