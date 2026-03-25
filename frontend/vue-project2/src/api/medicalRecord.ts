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
  diseaseId?: number | null
  diseaseName?: string | null
  diseaseCategory?: string | null
}

export interface AddMedicalRecordPayload {
  title: string
  description?: string
  visitDate?: string
}

// ── 医疗记录 API ──────────────────────────────────────────

// 获取所有医疗记录
export const getAllMedicalRecords = () =>
  http.get<MedicalRecord[]>('/medical-records')

// 获取某只宠物的所有医疗记录
export const getMedicalRecordsByPet = (petId: number) =>
  http.get<MedicalRecord[]>(`/medical-records/pet/${petId}`)

// 根据 ID 获取单条医疗记录
export const getMedicalRecordById = (id: number) =>
  http.get<MedicalRecord>(`/medical-records/${id}`)

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
export const deleteMedicalRecord = (id: number) =>
  http.delete(`/medical-records/${id}`)

// 后端关键词搜索（按当前宠物 + 标题/描述）
export const searchMedicalRecords = (petId: number, keyword: string) =>
  http.get<MedicalRecord[]>('/medical-records/search', { params: { petId, keyword } })

// ── 疾病 API ──────────────────────────────────────────────

// 获取所有疾病
export const getAllDiseases = () =>
  http.get<Disease[]>('/diseases')

// 根据 ID 获取单个疾病
export const getDiseaseById = (id: number) =>
  http.get<Disease>(`/diseases/${id}`)

// 新增疾病
export const addDisease = (disease: Omit<Disease, 'id'>) =>
  http.post<Disease>('/diseases', disease)

// 更新疾病
export const updateDisease = (id: number, disease: Omit<Disease, 'id'>) =>
  http.put<Disease>(`/diseases/${id}`, disease)

// 删除疾病
export const deleteDisease = (id: number) =>
  http.delete(`/diseases/${id}`)

// 按名称搜索疾病（调用后端 /diseases/search?name=...）
export const searchDiseases = (name: string) =>
  http.get<Disease[]>('/diseases/search', { params: { name } })