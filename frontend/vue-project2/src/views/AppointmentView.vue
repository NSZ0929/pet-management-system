<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  CalendarDays,
  Bell,
  Plus,
  Trash2,
  ChevronDown,
  ChevronUp,
  Clock,
  MapPin,
  Stethoscope,
  Syringe,
  Pill,
  HeartPulse,
  Utensils,
  CheckCircle2,
  AlertCircle,
  XCircle,
  AlarmClock,
} from 'lucide-vue-next'

defineProps<{ headerTitle: string }>()

// ─── 类型定义 ────────────────────────────────────────────────
type AppointmentStatus = 'upcoming' | 'completed' | 'cancelled'
type ReminderType = 'vaccine' | 'medication' | 'checkup' | 'feeding'
type ReminderFrequency = 'once' | 'daily' | 'weekly' | 'monthly'

interface Appointment {
  id: number
  date: string
  time: string
  hospital: string
  symptoms: string
  note: string
  status: AppointmentStatus
}

interface Reminder {
  id: number
  type: ReminderType
  title: string
  datetime: string
  frequency: ReminderFrequency
  enabled: boolean
  note: string
}

// ─── 展开/收起控制 ───────────────────────────────────────────
const expandedSection = ref<string | null>('appointments')
const toggleSection = (name: string) => {
  expandedSection.value = expandedSection.value === name ? null : name
}

const today = new Date().toISOString().split('T')[0] ?? ''
const now = new Date().toTimeString().slice(0, 5) ?? ''

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// H18. 医疗预约 Appointment
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const appointments = ref<Appointment[]>([
  {
    id: 1,
    date: '2025-07-20',
    time: '10:00',
    hospital: '阳光宠物医院 Sunshine Pet Clinic',
    symptoms: '定期体检，检查心脏和体重',
    note: '带上之前的血检报告',
    status: 'upcoming',
  },
  {
    id: 2,
    date: '2025-06-15',
    time: '14:30',
    hospital: '爱心动物医院 Care Animal Hospital',
    symptoms: '疫苗接种（狂犬疫苗）',
    note: '已完成，下次预约时间2026年6月',
    status: 'completed',
  },
])

const showAppointmentForm = ref(false)
const newAppointment = ref<Omit<Appointment, 'id'>>({
  date: today,
  time: now,
  hospital: '',
  symptoms: '',
  note: '',
  status: 'upcoming',
})

const addAppointment = () => {
  if (!newAppointment.value.hospital || !newAppointment.value.date) return
  appointments.value.unshift({
    ...newAppointment.value,
    id: Date.now(),
  })
  newAppointment.value = {
    date: today,
    time: now,
    hospital: '',
    symptoms: '',
    note: '',
    status: 'upcoming',
  }
  showAppointmentForm.value = false
}

const deleteAppointment = (id: number) => {
  appointments.value = appointments.value.filter((a) => a.id !== id)
}

const setStatus = (id: number, status: AppointmentStatus) => {
  const a = appointments.value.find((x) => x.id === id)
  if (a) a.status = status
}

const upcomingCount = computed(
  () => appointments.value.filter((a) => a.status === 'upcoming').length,
)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// H19. 提醒系统 Reminder System
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const reminders = ref<Reminder[]>([
  {
    id: 1,
    type: 'vaccine',
    title: '狂犬疫苗复打 Rabies Booster',
    datetime: '2026-06-15T09:00',
    frequency: 'once',
    enabled: true,
    note: '每年一次',
  },
  {
    id: 2,
    type: 'medication',
    title: '心丝虫预防药 Heartworm Med',
    datetime: '2025-07-01T08:00',
    frequency: 'monthly',
    enabled: true,
    note: '每月第一天喂药',
  },
  {
    id: 3,
    type: 'checkup',
    title: '年度健康检查 Annual Checkup',
    datetime: '2026-01-10T10:00',
    frequency: 'once',
    enabled: true,
    note: '包含血检和体检',
  },
  {
    id: 4,
    type: 'feeding',
    title: '早餐喂食 Morning Meal',
    datetime: `${today}T08:00`,
    frequency: 'daily',
    enabled: false,
    note: '120g 皇家成犬粮',
  },
])

const showReminderForm = ref(false)
const newReminder = ref<Omit<Reminder, 'id'>>({
  type: 'vaccine',
  title: '',
  datetime: `${today}T${now}`,
  frequency: 'once',
  enabled: true,
  note: '',
})

const addReminder = () => {
  if (!newReminder.value.title) return
  reminders.value.unshift({ ...newReminder.value, id: Date.now() })
  newReminder.value = {
    type: 'vaccine',
    title: '',
    datetime: `${today}T${now}`,
    frequency: 'once',
    enabled: true,
    note: '',
  }
  showReminderForm.value = false
}

const deleteReminder = (id: number) => {
  reminders.value = reminders.value.filter((r) => r.id !== id)
}

const toggleReminder = (id: number) => {
  const r = reminders.value.find((x) => x.id === id)
  if (r) r.enabled = !r.enabled
}

const enabledCount = computed(() => reminders.value.filter((r) => r.enabled).length)

// ─── 辅助函数 ────────────────────────────────────────────────
const reminderTypeConfig: Record<
  ReminderType,
  { label: string; color: string; bg: string; icon: unknown }
> = {
  vaccine: { label: '疫苗', color: 'text-purple-600', bg: 'bg-purple-50', icon: Syringe },
  medication: { label: '药物', color: 'text-blue-600', bg: 'bg-blue-50', icon: Pill },
  checkup: { label: '检查', color: 'text-teal-600', bg: 'bg-teal-50', icon: HeartPulse },
  feeding: { label: '喂食', color: 'text-amber-600', bg: 'bg-amber-50', icon: Utensils },
}

const frequencyLabel: Record<ReminderFrequency, string> = {
  once: '一次性',
  daily: '每天',
  weekly: '每周',
  monthly: '每月',
}

const statusConfig: Record<
  AppointmentStatus,
  { label: string; color: string; bg: string; icon: unknown }
> = {
  upcoming: { label: '即将到来', color: 'text-teal-600', bg: 'bg-teal-50', icon: Clock },
  completed: { label: '已完成', color: 'text-slate-500', bg: 'bg-slate-100', icon: CheckCircle2 },
  cancelled: { label: '已取消', color: 'text-red-400', bg: 'bg-red-50', icon: XCircle },
}

const formatDateTime = (dt: string) => {
  const d = new Date(dt)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<template>
  <div class="space-y-6">
    <!-- ── 统计卡片 ──────────────────────────────────────── -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div
        class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 flex items-center gap-3"
      >
        <div class="w-10 h-10 rounded-xl bg-teal-50 flex items-center justify-center">
          <CalendarDays :size="20" class="text-teal-500" />
        </div>
        <div>
          <p class="text-xs text-slate-400 font-medium">即将预约</p>
          <p class="text-xl font-extrabold text-slate-800">{{ upcomingCount }}</p>
        </div>
      </div>
      <div
        class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 flex items-center gap-3"
      >
        <div class="w-10 h-10 rounded-xl bg-purple-50 flex items-center justify-center">
          <Bell :size="20" class="text-purple-500" />
        </div>
        <div>
          <p class="text-xs text-slate-400 font-medium">已启用提醒</p>
          <p class="text-xl font-extrabold text-slate-800">{{ enabledCount }}</p>
        </div>
      </div>
      <div
        class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 flex items-center gap-3"
      >
        <div class="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center">
          <Syringe :size="20" class="text-amber-500" />
        </div>
        <div>
          <p class="text-xs text-slate-400 font-medium">疫苗提醒</p>
          <p class="text-xl font-extrabold text-slate-800">
            {{ reminders.filter((r) => r.type === 'vaccine' && r.enabled).length }}
          </p>
        </div>
      </div>
      <div
        class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 flex items-center gap-3"
      >
        <div class="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center">
          <Pill :size="20" class="text-blue-500" />
        </div>
        <div>
          <p class="text-xs text-slate-400 font-medium">用药提醒</p>
          <p class="text-xl font-extrabold text-slate-800">
            {{ reminders.filter((r) => r.type === 'medication' && r.enabled).length }}
          </p>
        </div>
      </div>
    </div>

    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <!-- H18. 医疗预约 Appointment                   -->
    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <div class="bg-white rounded-3xl shadow-lg border border-slate-100 overflow-hidden">
      <!-- 标题栏 -->
      <button
        @click="toggleSection('appointments')"
        class="w-full flex items-center justify-between p-6 hover:bg-slate-50 transition-colors"
      >
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-teal-50 flex items-center justify-center">
            <CalendarDays :size="20" class="text-teal-500" />
          </div>
          <div class="text-left">
            <h2 class="font-extrabold text-slate-800 text-base">医疗预约 Appointment</h2>
            <p class="text-xs text-slate-400">预约时间 · 症状 · 医院名称</p>
          </div>
        </div>
        <component
          :is="expandedSection === 'appointments' ? ChevronUp : ChevronDown"
          :size="20"
          class="text-slate-400"
        />
      </button>

      <div v-if="expandedSection === 'appointments'" class="px-6 pb-6 space-y-4">
        <!-- 添加按钮 -->
        <button
          @click="showAppointmentForm = !showAppointmentForm"
          class="flex items-center gap-2 text-sm font-bold text-teal-600 hover:text-teal-700 bg-teal-50 hover:bg-teal-100 px-4 py-2 rounded-xl transition-all"
        >
          <Plus :size="16" /> 新增预约 New Appointment
        </button>

        <!-- 新增表单 -->
        <Transition name="slide">
          <div
            v-if="showAppointmentForm"
            class="bg-slate-50 rounded-2xl p-5 space-y-3 border border-slate-200"
          >
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">日期 Date</label>
                <input
                  v-model="newAppointment.date"
                  type="date"
                  class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">时间 Time</label>
                <input
                  v-model="newAppointment.time"
                  type="time"
                  class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
                />
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">医院名称 Hospital</label>
              <input
                v-model="newAppointment.hospital"
                type="text"
                placeholder="请输入医院名称"
                class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
              />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">症状/原因 Symptoms</label>
              <textarea
                v-model="newAppointment.symptoms"
                rows="2"
                placeholder="描述宠物症状或就诊原因"
                class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all resize-none"
              />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">备注 Note</label>
              <input
                v-model="newAppointment.note"
                type="text"
                placeholder="其他备注（可选）"
                class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
              />
            </div>
            <div class="flex gap-2 pt-1">
              <button
                @click="addAppointment"
                class="flex-1 py-2.5 bg-teal-500 hover:bg-teal-600 text-white rounded-xl text-sm font-bold shadow-md shadow-teal-100 transition-all"
              >
                确认添加
              </button>
              <button
                @click="showAppointmentForm = false"
                class="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all"
              >
                取消
              </button>
            </div>
          </div>
        </Transition>

        <!-- 预约列表 -->
        <div v-if="appointments.length === 0" class="text-center py-8 text-slate-400 text-sm">
          暂无预约记录，点击上方按钮添加
        </div>

        <div class="space-y-3">
          <div
            v-for="appt in appointments"
            :key="appt.id"
            class="border border-slate-100 rounded-2xl p-4 hover:border-teal-100 hover:shadow-sm transition-all"
          >
            <div class="flex items-start justify-between gap-2 mb-3">
              <div class="flex items-center gap-2 flex-wrap">
                <span
                  :class="[
                    statusConfig[appt.status].bg,
                    statusConfig[appt.status].color,
                    'flex items-center gap-1 text-xs font-bold px-2.5 py-1 rounded-lg',
                  ]"
                >
                  <component :is="statusConfig[appt.status].icon" :size="12" />
                  {{ statusConfig[appt.status].label }}
                </span>
                <span class="flex items-center gap-1 text-xs text-slate-500 font-medium">
                  <Clock :size="12" /> {{ appt.date }} {{ appt.time }}
                </span>
              </div>
              <button
                @click="deleteAppointment(appt.id)"
                class="text-slate-300 hover:text-red-400 transition-colors flex-shrink-0"
              >
                <Trash2 :size="16" />
              </button>
            </div>

            <div class="space-y-1.5">
              <div class="flex items-start gap-2">
                <MapPin :size="14" class="text-teal-400 mt-0.5 flex-shrink-0" />
                <p class="text-sm font-bold text-slate-700">{{ appt.hospital }}</p>
              </div>
              <div class="flex items-start gap-2">
                <Stethoscope :size="14" class="text-slate-400 mt-0.5 flex-shrink-0" />
                <p class="text-sm text-slate-600">{{ appt.symptoms }}</p>
              </div>
              <p v-if="appt.note" class="text-xs text-slate-400 ml-5">📝 {{ appt.note }}</p>
            </div>

            <!-- 快捷状态按钮 -->
            <div v-if="appt.status === 'upcoming'" class="flex gap-2 mt-3">
              <button
                @click="setStatus(appt.id, 'completed')"
                class="flex items-center gap-1 text-xs font-bold text-teal-600 bg-teal-50 hover:bg-teal-100 px-3 py-1.5 rounded-lg transition-all"
              >
                <CheckCircle2 :size="12" /> 标为已完成
              </button>
              <button
                @click="setStatus(appt.id, 'cancelled')"
                class="flex items-center gap-1 text-xs font-bold text-red-400 bg-red-50 hover:bg-red-100 px-3 py-1.5 rounded-lg transition-all"
              >
                <XCircle :size="12" /> 取消预约
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <!-- H19. 提醒系统 Reminder System               -->
    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <div class="bg-white rounded-3xl shadow-lg border border-slate-100 overflow-hidden">
      <button
        @click="toggleSection('reminders')"
        class="w-full flex items-center justify-between p-6 hover:bg-slate-50 transition-colors"
      >
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-purple-50 flex items-center justify-center">
            <AlarmClock :size="20" class="text-purple-500" />
          </div>
          <div class="text-left">
            <h2 class="font-extrabold text-slate-800 text-base">提醒系统 Reminder System</h2>
            <p class="text-xs text-slate-400">疫苗 · 用药 · 健康检查 · 喂食</p>
          </div>
        </div>
        <component
          :is="expandedSection === 'reminders' ? ChevronUp : ChevronDown"
          :size="20"
          class="text-slate-400"
        />
      </button>

      <div v-if="expandedSection === 'reminders'" class="px-6 pb-6 space-y-4">
        <!-- 添加按钮 -->
        <button
          @click="showReminderForm = !showReminderForm"
          class="flex items-center gap-2 text-sm font-bold text-purple-600 hover:text-purple-700 bg-purple-50 hover:bg-purple-100 px-4 py-2 rounded-xl transition-all"
        >
          <Plus :size="16" /> 新增提醒 New Reminder
        </button>

        <!-- 新增表单 -->
        <Transition name="slide">
          <div
            v-if="showReminderForm"
            class="bg-slate-50 rounded-2xl p-5 space-y-3 border border-slate-200"
          >
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">类型 Type</label>
                <select
                  v-model="newReminder.type"
                  class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all"
                >
                  <option value="vaccine">💉 疫苗</option>
                  <option value="medication">💊 药物</option>
                  <option value="checkup">🩺 健康检查</option>
                  <option value="feeding">🍖 喂食</option>
                </select>
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">频率 Frequency</label>
                <select
                  v-model="newReminder.frequency"
                  class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all"
                >
                  <option value="once">一次性</option>
                  <option value="daily">每天</option>
                  <option value="weekly">每周</option>
                  <option value="monthly">每月</option>
                </select>
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">提醒标题 Title</label>
              <input
                v-model="newReminder.title"
                type="text"
                placeholder="请输入提醒名称"
                class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all"
              />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1"
                >提醒时间 Date & Time</label
              >
              <input
                v-model="newReminder.datetime"
                type="datetime-local"
                class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all"
              />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">备注 Note</label>
              <input
                v-model="newReminder.note"
                type="text"
                placeholder="其他备注（可选）"
                class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all"
              />
            </div>
            <div class="flex gap-2 pt-1">
              <button
                @click="addReminder"
                class="flex-1 py-2.5 bg-purple-500 hover:bg-purple-600 text-white rounded-xl text-sm font-bold shadow-md shadow-purple-100 transition-all"
              >
                确认添加
              </button>
              <button
                @click="showReminderForm = false"
                class="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all"
              >
                取消
              </button>
            </div>
          </div>
        </Transition>

        <!-- 分类展示 -->
        <div v-if="reminders.length === 0" class="text-center py-8 text-slate-400 text-sm">
          暂无提醒，点击上方按钮添加
        </div>

        <div class="space-y-3">
          <div
            v-for="reminder in reminders"
            :key="reminder.id"
            :class="[
              'border rounded-2xl p-4 transition-all',
              reminder.enabled
                ? 'border-slate-100 hover:border-purple-100 hover:shadow-sm'
                : 'border-slate-100 opacity-50',
            ]"
          >
            <div class="flex items-start justify-between gap-2">
              <div class="flex items-center gap-3 flex-1 min-w-0">
                <!-- 图标 -->
                <div
                  :class="[
                    reminderTypeConfig[reminder.type].bg,
                    'w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0',
                  ]"
                >
                  <component
                    :is="reminderTypeConfig[reminder.type].icon"
                    :size="16"
                    :class="reminderTypeConfig[reminder.type].color"
                  />
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 flex-wrap">
                    <p class="text-sm font-bold text-slate-800 truncate">{{ reminder.title }}</p>
                    <span
                      :class="[
                        reminderTypeConfig[reminder.type].bg,
                        reminderTypeConfig[reminder.type].color,
                        'text-xs font-bold px-2 py-0.5 rounded-lg flex-shrink-0',
                      ]"
                    >
                      {{ reminderTypeConfig[reminder.type].label }}
                    </span>
                  </div>
                  <div class="flex items-center gap-3 mt-1 flex-wrap">
                    <span class="text-xs text-slate-400 flex items-center gap-1">
                      <Clock :size="11" /> {{ formatDateTime(reminder.datetime) }}
                    </span>
                    <span class="text-xs text-slate-400 bg-slate-100 px-2 py-0.5 rounded-lg">
                      {{ frequencyLabel[reminder.frequency] }}
                    </span>
                  </div>
                  <p v-if="reminder.note" class="text-xs text-slate-400 mt-1">
                    {{ reminder.note }}
                  </p>
                </div>
              </div>

              <!-- 右侧操作 -->
              <div class="flex items-center gap-2 flex-shrink-0">
                <!-- 开关 -->
                <button
                  @click="toggleReminder(reminder.id)"
                  :class="[
                    'relative inline-flex h-5 w-9 items-center rounded-full transition-colors',
                    reminder.enabled ? 'bg-purple-500' : 'bg-slate-200',
                  ]"
                >
                  <span
                    :class="[
                      'inline-block h-3.5 w-3.5 rounded-full bg-white shadow transition-transform',
                      reminder.enabled ? 'translate-x-4' : 'translate-x-1',
                    ]"
                  />
                </button>
                <button
                  @click="deleteReminder(reminder.id)"
                  class="text-slate-300 hover:text-red-400 transition-colors"
                >
                  <Trash2 :size="15" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 提示说明 -->
        <div class="flex items-start gap-2 bg-amber-50 border border-amber-100 rounded-xl p-3 mt-2">
          <AlertCircle :size="15" class="text-amber-500 flex-shrink-0 mt-0.5" />
          <p class="text-xs text-amber-700 font-medium">
            提醒功能目前为前端展示，实际推送通知需配合后端定时任务（如 Spring Scheduler）实现。
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}
.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  max-height: 0;
}
.slide-enter-to,
.slide-leave-from {
  max-height: 800px;
}
</style>
