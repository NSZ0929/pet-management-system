import http from './http'

export interface Notification {
  id: number
  petId: number
  title: string
  message: string
  type: string
  isRead: boolean
  createdAt: string
  appointmentId?: number
}

export const getNotificationsByPet = (petId: number) =>
  http.get<Notification[]>('/notifications', { params: { petId } })

export const getUnreadCount = (petId: number) =>
  http.get<{ count: number }>('/notifications/unread-count', { params: { petId } })

export const markAsRead = (id: number) =>
  http.put(`/notifications/${id}/read`)

export const markAllAsRead = (petId: number) =>
  http.put('/notifications/read-all', null, { params: { petId } })

export const deleteNotification = (id: number) =>
  http.delete(`/notifications/${id}`)