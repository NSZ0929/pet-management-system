<script setup lang="ts">
import { ref, watch } from 'vue'
import { Bell, X, Check, Trash2, Loader2 } from 'lucide-vue-next'
import {
  getNotificationsByPet,
  getUnreadCount,
  markAsRead,
  markAllAsRead,
  deleteNotification,
  type Notification,
} from '../api/notification'

const props = defineProps<{ petId: number | null }>()

const notifications = ref<Notification[]>([])
const unreadCount = ref(0)
const loading = ref(false)
const panelOpen = ref(false)

const load = async () => {
  if (!props.petId) return
  loading.value = true
  try {
    const [notiRes, countRes] = await Promise.all([
      getNotificationsByPet(props.petId),
      getUnreadCount(props.petId),
    ])
    notifications.value = notiRes.data
    unreadCount.value = countRes.data.count
  } catch (e) {
    console.error('加载通知失败', e)
  } finally {
    loading.value = false
  }
}

const handleMarkRead = async (id: number) => {
  await markAsRead(id)
  const item = notifications.value.find((n) => n.id === id)
  if (item) {
    item.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

const handleMarkAll = async () => {
  if (!props.petId) return
  await markAllAsRead(props.petId)
  notifications.value.forEach((n) => (n.isRead = true))
  unreadCount.value = 0
}

const handleDelete = async (id: number) => {
  const item = notifications.value.find((n) => n.id === id)
  await deleteNotification(id)
  notifications.value = notifications.value.filter((n) => n.id !== id)
  if (item && !item.isRead) unreadCount.value = Math.max(0, unreadCount.value - 1)
}

const typeIcon: Record<string, string> = {
  APPOINTMENT: '📅',
  VACCINE: '💉',
  MEDICATION: '💊',
}

watch(() => props.petId, load, { immediate: true })
</script>

<template>
  <div class="relative">
    <button
      @click="panelOpen = !panelOpen"
      class="relative p-2 rounded-full hover:bg-slate-100 text-slate-500 transition-colors"
    >
      <Bell :size="22" />
      <span
        v-if="unreadCount > 0"
        class="absolute top-0.5 right-0.5 w-4 h-4 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center"
      >
        {{ unreadCount > 9 ? '9+' : unreadCount }}
      </span>
    </button>

    <div
      v-if="panelOpen"
      class="absolute right-0 top-12 w-80 bg-white rounded-2xl shadow-2xl border border-slate-100 z-50 overflow-hidden"
    >
      <div class="flex items-center justify-between px-4 py-3 border-b border-slate-100">
        <span class="font-bold text-slate-800 text-sm">通知</span>
        <div class="flex items-center gap-2">
          <button
            v-if="unreadCount > 0"
            @click="handleMarkAll"
            class="text-xs text-teal-600 hover:text-teal-700 font-medium flex items-center gap-1"
          >
            <Check :size="13" /> 全部已读
          </button>
          <button @click="panelOpen = false" class="text-slate-400 hover:text-slate-600">
            <X :size="16" />
          </button>
        </div>
      </div>

      <div class="max-h-96 overflow-y-auto">
        <div v-if="loading" class="flex justify-center py-8">
          <Loader2 :size="20" class="animate-spin text-slate-400" />
        </div>
        <div
          v-else-if="notifications.length === 0"
          class="py-10 text-center text-sm text-slate-400"
        >
          暂无通知
        </div>
        <div
          v-else
          v-for="n in notifications"
          :key="n.id"
          :class="[
            'flex items-start gap-3 px-4 py-3 border-b border-slate-50 hover:bg-slate-50 transition-colors',
            !n.isRead ? 'bg-teal-50/40' : '',
          ]"
        >
          <span class="text-lg flex-shrink-0 mt-0.5">{{ typeIcon[n.type] ?? '🔔' }}</span>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-bold text-slate-700 truncate">{{ n.title }}</p>
            <p class="text-xs text-slate-500 mt-0.5 leading-snug">{{ n.message }}</p>
            <p class="text-xs text-slate-300 mt-1">
              {{ new Date(n.createdAt).toLocaleString('zh-CN') }}
            </p>
          </div>
          <div class="flex flex-col gap-1 flex-shrink-0">
            <button
              v-if="!n.isRead"
              @click="handleMarkRead(n.id)"
              class="text-teal-500 hover:text-teal-600"
              title="标记已读"
            >
              <Check :size="14" />
            </button>
            <button
              @click="handleDelete(n.id)"
              class="text-slate-300 hover:text-red-400"
              title="删除"
            >
              <Trash2 :size="14" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="panelOpen" class="fixed inset-0 z-40" @click="panelOpen = false" />
  </div>
</template>