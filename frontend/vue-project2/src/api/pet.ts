import http from './http'

// ── 类型定义（匹配后端实体）──────────────────────────────

export interface Owner {
  id: number
  name: string
  contact: string
  address: string
  environment?: string // 居住环境
  homeTemperature?: string // 环境气温
  otherPets?: string // 其他宠物
  members?: number // 家庭成员数
}

export interface Pet {
  id: number
  name: string
  breed: string
  age: number
  owner: Owner
  avatar?: string // ← 新增头像字段
}

export interface AddPetPayload {
  name?: string
  breed?: string
  age?: number
}

// ── 宠物 API ─────────────────────────────────────────────

export const getAllPets = () => http.get<Pet[]>('/pets')

export const getPetById = (id: number) => http.get<Pet>(`/pets/${id}`)

export const addPet = (pet: AddPetPayload, ownerId: number) =>
  http.post<Pet>(`/pets?ownerId=${ownerId}`, pet)

export const updatePet = (id: number, pet: Partial<Omit<Pet, 'id' | 'owner'>>) =>
  http.put<Pet>(`/pets/${id}`, pet)

export const deletePet = (id: number) => http.delete(`/pets/${id}`)

// ── 头像上传 API ──────────────────────────────────────────
// POST /upload/avatar/{petId}，返回 { avatarUrl: string }
export const uploadAvatar = (petId: number, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<{ avatarUrl: string }>(`/upload/avatar/${petId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// ── 主人 API ─────────────────────────────────────────────

export const getAllOwners = () => http.get<Owner[]>('/owners')

export const getOwnerById = (id: number) => http.get<Owner>(`/owners/${id}`)

export const addOwner = (owner: Partial<Owner>) => http.post<Owner>('/owners', owner)

export const updateOwner = (id: number, owner: Partial<Owner>) =>
  http.put<Owner>(`/owners/${id}`, owner)