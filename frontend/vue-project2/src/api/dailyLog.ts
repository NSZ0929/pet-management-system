import http from './http'

// ── 类型定义（完全匹配后端 DailyLog 实体）────────────────

export type DailyLogType = 'FEEDING' | 'EXCRETION' | 'SLEEP' | 'ACTIVITY' | 'MOOD'

export interface DailyLog {
  id?: number
  petId: number
  type: DailyLogType
  content: string // ← 后端字段名是 content（JSON 字符串）
  createTime?: string // ← 后端自动生成
}

// ── 日常记录 API ──────────────────────────────────────────

// 添加日常记录
export const addDailyLog = (log: DailyLog) => http.post<DailyLog>('/daily-logs', log)

// 获取某只宠物某类型的日常记录
export const getDailyLogs = (petId: number, type: DailyLogType) =>
  http.get<DailyLog[]>('/daily-logs', { params: { petId, type } })

// 删除日常记录
export const deleteDailyLog = (id: number) => http.delete(`/daily-logs/${id}`)

// ── 生命体征 API ──────────────────────────────────────────

export interface VitalSign {
  id?: number
  petId: number
  temperature?: number
  weight?: number
  recordTime?: string // ← 和后端字段名一致
}

export const addVitalSign = (vs: VitalSign) => http.post<VitalSign>('/vital-signs', vs)

export const getVitalSigns = (petId: number) =>
  http.get<VitalSign[]>('/vital-signs', { params: { petId } })

export const deleteVitalSign = (id: number) =>
  http.delete(`/vital-signs/${id}`)
