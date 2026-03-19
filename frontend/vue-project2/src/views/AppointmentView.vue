<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
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
  AlarmClock,
  Loader2,
  RefreshCw,
  UserRound,
} from 'lucide-vue-next'
import {
  getAllAppointments,
  addAppointment,
  deleteAppointment,
  getAllVets,
  addVet,
  type Appointment,
  type Vet,
  type AddAppointmentPayload,
} from '../api/appointment'
import { currentPet, allPets, loadPets, selectPet } from '../composables/usePetData'

defineProps<{ headerTitle: string }>()

// ─── 宠物选择 ──────────────────────────────────────────────
onMounted(async () => {
  await loadPets()
  await Promise.all([loadAppointments(), loadVets()])
})

watch(() => currentPet.value, async () => {
  await loadAppointments()
})

// ─── 展开/收起 ─────────────────────────────────────────────
const expandedSection = ref<string | null>('appointments')
const toggleSection = (name: string) => {
  expandedSection.value = expandedSection.value === name ? null : name
}

const today = new Date().toISOString().split('T')[0] ?? ''
const now = new Date().toTimeString().slice(0, 5) ?? ''

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 医疗预约 Appointment（对接后端）
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const appointments = ref<Appointment[]>([])
const loading = ref(false)
const error = ref('')

// 加载预约列表
const loadAppointments = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await getAllAppointments()
    // 只展示当前宠物的预约
    if (currentPet.value) {
      appointments.value = res.data.filter(a => a.pet.id === currentPet.value!.id)
    } else {
      appointments.value = res.data
    }
  } catch (err) {
    error.value = '加载预约失败，请检查后端连接'
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 新增预约表单
const showAppointmentForm = ref(false)
const isSubmitting = ref(false)
const submitError = ref('')

const newAppointmentForm = ref({
  date: today,
  time: now,
  vetId: '',
  description: '',
})

const handleAddAppointment = async () => {
  if (!newAppointmentForm.value.date || !newAppointmentForm.value.vetId) {
    submitError.value = '请选择预约日期和兽医'
    return
  }
  if (!currentPet.value) {
    submitError.value = '请先选择宠物'
    return
  }

  isSubmitting.value = true
  submitError.value = ''
  try {
    const appointmentTime = `${newAppointmentForm.value.date}T${newAppointmentForm.value.time}:00`
    const payload: AddAppointmentPayload = {
      appointmentTime,
      description: newAppointmentForm.value.description || undefined,
    }
    await addAppointment(payload, currentPet.value.id, Number(newAppointmentForm.value.vetId))
    await loadAppointments()
    // 重置表单
    newAppointmentForm.value = { date: today, time: now, vetId: '', description: '' }
    showAppointmentForm.value = false
  } catch (err) {
    submitError.value = '添加预约失败，请稍后重试'
    console.error(err)
  } finally {
    isSubmitting.value = false
  }
}

const handleDeleteAppointment = async (id: number) => {
  try {
    await deleteAppointment(id)
    await loadAppointments()
  } catch (err) {
    console.error('删除预约失败', err)
  }
}

// 统计
const upcomingCount = computed(() =>
  appointments.value.filter(a => {
    const t = new Date(a.appointmentTime)
    return t >= new Date()
  }).length
)

// 格式化时间
const formatTime = (iso: string) => {
  const d = new Date(iso)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

const isUpcoming = (iso: string) => new Date(iso) >= new Date()

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 兽医管理 Vet Management（对接后端）
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const vets = ref<Vet[]>([])
const vetLoading = ref(false)
const showVetForm = ref(false)
const newVet = ref({ name: '', specialty: '' })
const vetSubmitting = ref(false)
const vetError = ref('')

const loadVets = async () => {
  vetLoading.value = true
  try {
    const res = await getAllVets()
    vets.value = res.data
  } catch (err) {
    console.error('加载兽医列表失败', err)
  } finally {
    vetLoading.value = false
  }
}

const handleAddVet = async () => {
  if (!newVet.value.name.trim()) {
    vetError.value = '兽医姓名不能为空'
    return
  }
  vetSubmitting.value = true
  vetError.value = ''
  try {
    await addVet({ name: newVet.value.name.trim(), specialty: newVet.value.specialty || undefined })
    await loadVets()
    newVet.value = { name: '', specialty: '' }
    showVetForm.value = false
  } catch (err) {
    vetError.value = '添加兽医失败'
    console.error(err)
  } finally {
    vetSubmitting.value = false
  }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 提醒系统（前端本地状态，如文档中说明）
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
type ReminderType = 'vaccine' | 'medication' | 'checkup' | 'feeding'
type ReminderFrequency = 'once' | 'daily' | 'weekly' | 'monthly'

interface Reminder {
  id: number
  type: ReminderType
  title: string
  datetime: string
  frequency: ReminderFrequency
  enabled: boolean
  note: string
}

const reminders = ref<Reminder[]>([
  { id: 1, type: 'vaccine', title: '狂犬疫苗复打 Rabies Booster', datetime: '2026-06-15T09:00', frequency: 'once', enabled: true, note: '每年一次' },
  { id: 2, type: 'medication', title: '心丝虫预防药 Heartworm Med', datetime: '2025-07-01T08:00', frequency: 'monthly', enabled: true, note: '每月第一天喂药' },
  { id: 3, type: 'checkup', title: '年度健康检查 Annual Checkup', datetime: '2026-01-10T10:00', frequency: 'once', enabled: true, note: '包含血检和体检' },
  { id: 4, type: 'feeding', title: '早餐喂食 Morning Meal', datetime: `${today}T08:00`, frequency: 'daily', enabled: false, note: '120g 皇家成犬粮' },
])

const showReminderForm = ref(false)
const newReminder = ref<Omit<Reminder, 'id'>>({ type: 'vaccine', title: '', datetime: `${today}T${now}`, frequency: 'once', enabled: true, note: '' })

const addReminder = () => {
  if (!newReminder.value.title) return
  reminders.value.unshift({ ...newReminder.value, id: Date.now() })
  newReminder.value = { type: 'vaccine', title: '', datetime: `${today}T${now}`, frequency: 'once', enabled: true, note: '' }
  showReminderForm.value = false
}

const deleteReminder = (id: number) => { reminders.value = reminders.value.filter(r => r.id !== id) }
const toggleReminder = (id: number) => { const r = reminders.value.find(x => x.id === id); if (r) r.enabled = !r.enabled }
const enabledCount = computed(() => reminders.value.filter(r => r.enabled).length)

const reminderTypeConfig: Record<ReminderType, { label: string; color: string; bg: string; icon: unknown }> = {
  vaccine:    { label: '疫苗', color: 'text-purple-600', bg: 'bg-purple-50', icon: Syringe },
  medication: { label: '药物', color: 'text-blue-600',   bg: 'bg-blue-50',   icon: Pill },
  checkup:    { label: '检查', color: 'text-teal-600',   bg: 'bg-teal-50',   icon: HeartPulse },
  feeding:    { label: '喂食', color: 'text-amber-600',  bg: 'bg-amber-50',  icon: Utensils },
}
const frequencyLabel: Record<ReminderFrequency, string> = { once: '一次性', daily: '每天', weekly: '每周', monthly: '每月' }

const formatDateTime = (dt: string) => {
  const d = new Date(dt)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}
</script>

<template>
  <div class="space-y-6">
    <!-- ── 宠物选择器 ──────────────────────────────────────── -->
    <div v-if="allPets.length > 1" class="flex items-center gap-2 flex-wrap">
      <span class="text-xs font-bold text-slate-500">当前宠物：</span>
      <button
        v-for="pet in allPets"
        :key="pet.id"
        @click="selectPet(pet)"
        :class="[
          'px-3 py-1.5 rounded-xl text-xs font-bold transition-all',
          currentPet?.id === pet.id
            ? 'bg-teal-500 text-white shadow-md shadow-teal-100'
            : 'bg-slate-100 text-slate-600 hover:bg-teal-50 hover:text-teal-600',
        ]"
      >
        {{ pet.name }}
      </button>
    </div>

    <!-- ── 统计卡片 ──────────────────────────────────────── -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-teal-50 flex items-center justify-center">
          <CalendarDays :size="20" class="text-teal-500" />
        </div>
        <div>
          <p class="text-xs text-slate-400 font-medium">即将预约</p>
          <p class="text-xl font-extrabold text-slate-800">{{ upcomingCount }}</p>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-purple-50 flex items-center justify-center">
          <Bell :size="20" class="text-purple-500" />
        </div>
        <div>
          <p class="text-xs text-slate-400 font-medium">已启用提醒</p>
          <p class="text-xl font-extrabold text-slate-800">{{ enabledCount }}</p>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center">
          <UserRound :size="20" class="text-blue-500" />
        </div>
        <div>
          <p class="text-xs text-slate-400 font-medium">注册兽医</p>
          <p class="text-xl font-extrabold text-slate-800">{{ vets.length }}</p>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center">
          <Syringe :size="20" class="text-amber-500" />
        </div>
        <div>
          <p class="text-xs text-slate-400 font-medium">总预约数</p>
          <p class="text-xl font-extrabold text-slate-800">{{ appointments.length }}</p>
        </div>
      </div>
    </div>

    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <!-- 医疗预约 Appointment                        -->
    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <div class="bg-white rounded-3xl shadow-lg border border-slate-100 overflow-hidden">
      <button @click="toggleSection('appointments')" class="w-full flex items-center justify-between p-6 hover:bg-slate-50 transition-colors">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-teal-50 flex items-center justify-center">
            <CalendarDays :size="20" class="text-teal-500" />
          </div>
          <div class="text-left">
            <h2 class="font-extrabold text-slate-800 text-base">医疗预约 Appointment</h2>
            <p class="text-xs text-slate-400">已对接后端 · 数据实时同步</p>
          </div>
        </div>
        <component :is="expandedSection === 'appointments' ? ChevronUp : ChevronDown" :size="20" class="text-slate-400" />
      </button>

      <div v-if="expandedSection === 'appointments'" class="px-6 pb-6 space-y-4">
        <!-- 错误提示 -->
        <div v-if="error" class="flex items-center gap-2 bg-red-50 text-red-600 text-sm px-4 py-3 rounded-xl">
          <AlertCircle :size="16" />
          {{ error }}
          <button @click="loadAppointments" class="ml-auto text-xs underline">重试</button>
        </div>

        <!-- 操作栏 -->
        <div class="flex items-center gap-2">
          <button @click="showAppointmentForm = !showAppointmentForm" class="flex items-center gap-2 text-sm font-bold text-teal-600 hover:text-teal-700 bg-teal-50 hover:bg-teal-100 px-4 py-2 rounded-xl transition-all">
            <Plus :size="16" /> 新增预约
          </button>
          <button @click="loadAppointments" class="flex items-center gap-1 text-xs text-slate-400 hover:text-slate-600 px-3 py-2 rounded-xl hover:bg-slate-100 transition-all">
            <RefreshCw :size="13" /> 刷新
          </button>
        </div>

        <!-- 新增表单 -->
        <Transition name="slide">
          <div v-if="showAppointmentForm" class="bg-slate-50 rounded-2xl p-5 space-y-3 border border-slate-200">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">日期 Date</label>
                <input v-model="newAppointmentForm.date" type="date" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all" />
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">时间 Time</label>
                <input v-model="newAppointmentForm.time" type="time" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all" />
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">选择兽医 Vet</label>
              <select v-model="newAppointmentForm.vetId" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all">
                <option value="">-- 请选择兽医 --</option>
                <option v-for="vet in vets" :key="vet.id" :value="vet.id">
                  {{ vet.name }}{{ vet.specialty ? ` · ${vet.specialty}` : '' }}
                </option>
              </select>
              <p v-if="vets.length === 0" class="text-xs text-amber-500 mt-1">暂无兽医，请先在下方"兽医管理"中添加兽医</p>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">描述/症状 Description</label>
              <textarea v-model="newAppointmentForm.description" rows="2" placeholder="描述宠物症状或就诊原因（可选）" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all resize-none" />
            </div>
            <p v-if="submitError" class="text-xs text-red-500">{{ submitError }}</p>
            <div class="flex gap-2 pt-1">
              <button @click="handleAddAppointment" :disabled="isSubmitting" class="flex-1 py-2.5 bg-teal-500 hover:bg-teal-600 disabled:opacity-50 text-white rounded-xl text-sm font-bold shadow-md shadow-teal-100 transition-all flex items-center justify-center gap-2">
                <Loader2 v-if="isSubmitting" :size="14" class="animate-spin" />
                确认添加
              </button>
              <button @click="showAppointmentForm = false" class="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all">取消</button>
            </div>
          </div>
        </Transition>

        <!-- 加载中 -->
        <div v-if="loading" class="flex justify-center py-8">
          <Loader2 :size="24" class="animate-spin text-teal-400" />
        </div>

        <!-- 空状态 -->
        <div v-else-if="appointments.length === 0 && !error" class="text-center py-8 text-slate-400 text-sm">
          暂无预约记录，点击上方按钮添加
        </div>

        <!-- 预约列表 -->
        <div v-else class="space-y-3">
          <div
            v-for="appt in appointments"
            :key="appt.id"
            class="border border-slate-100 rounded-2xl p-4 hover:border-teal-100 hover:shadow-sm transition-all"
          >
            <div class="flex items-start justify-between gap-2 mb-3">
              <div class="flex items-center gap-2 flex-wrap">
                <span :class="[
                  isUpcoming(appt.appointmentTime) ? 'bg-teal-50 text-teal-600' : 'bg-slate-100 text-slate-500',
                  'flex items-center gap-1 text-xs font-bold px-2.5 py-1 rounded-lg'
                ]">
                  <component :is="isUpcoming(appt.appointmentTime) ? Clock : CheckCircle2" :size="12" />
                  {{ isUpcoming(appt.appointmentTime) ? '即将到来' : '已过期' }}
                </span>
                <span class="flex items-center gap-1 text-xs text-slate-500 font-medium">
                  <Clock :size="12" /> {{ formatTime(appt.appointmentTime) }}
                </span>
              </div>
              <button @click="handleDeleteAppointment(appt.id)" class="text-slate-300 hover:text-red-400 transition-colors flex-shrink-0">
                <Trash2 :size="16" />
              </button>
            </div>

            <div class="space-y-1.5">
              <div class="flex items-start gap-2">
                <Stethoscope :size="14" class="text-teal-400 mt-0.5 flex-shrink-0" />
                <p class="text-sm font-bold text-slate-700">兽医：{{ appt.vet.name }}{{ appt.vet.specialty ? ` · ${appt.vet.specialty}` : '' }}</p>
              </div>
              <div class="flex items-start gap-2">
                <MapPin :size="14" class="text-slate-400 mt-0.5 flex-shrink-0" />
                <p class="text-sm text-slate-600">宠物：{{ appt.pet.name }}</p>
              </div>
              <p v-if="appt.description" class="text-xs text-slate-400 ml-5">📝 {{ appt.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <!-- 兽医管理 Vet Management                    -->
    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <div class="bg-white rounded-3xl shadow-lg border border-slate-100 overflow-hidden">
      <button @click="toggleSection('vets')" class="w-full flex items-center justify-between p-6 hover:bg-slate-50 transition-colors">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-blue-50 flex items-center justify-center">
            <UserRound :size="20" class="text-blue-500" />
          </div>
          <div class="text-left">
            <h2 class="font-extrabold text-slate-800 text-base">兽医管理 Vet Management</h2>
            <p class="text-xs text-slate-400">已对接后端 · 数据实时同步</p>
          </div>
        </div>
        <component :is="expandedSection === 'vets' ? ChevronUp : ChevronDown" :size="20" class="text-slate-400" />
      </button>

      <div v-if="expandedSection === 'vets'" class="px-6 pb-6 space-y-4">
        <button @click="showVetForm = !showVetForm" class="flex items-center gap-2 text-sm font-bold text-blue-600 hover:text-blue-700 bg-blue-50 hover:bg-blue-100 px-4 py-2 rounded-xl transition-all">
          <Plus :size="16" /> 添加兽医
        </button>

        <!-- 添加兽医表单 -->
        <Transition name="slide">
          <div v-if="showVetForm" class="bg-slate-50 rounded-2xl p-5 space-y-3 border border-slate-200">
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">兽医姓名 Name *</label>
              <input v-model="newVet.name" type="text" placeholder="请输入兽医姓名" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-blue-400 transition-all" />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">专业领域 Specialty</label>
              <input v-model="newVet.specialty" type="text" placeholder="如：皮肤科、外科、牙科（可选）" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-blue-400 transition-all" />
            </div>
            <p v-if="vetError" class="text-xs text-red-500">{{ vetError }}</p>
            <div class="flex gap-2 pt-1">
              <button @click="handleAddVet" :disabled="vetSubmitting" class="flex-1 py-2.5 bg-blue-500 hover:bg-blue-600 disabled:opacity-50 text-white rounded-xl text-sm font-bold shadow-md shadow-blue-100 transition-all flex items-center justify-center gap-2">
                <Loader2 v-if="vetSubmitting" :size="14" class="animate-spin" />
                确认添加
              </button>
              <button @click="showVetForm = false" class="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all">取消</button>
            </div>
          </div>
        </Transition>

        <!-- 加载中 -->
        <div v-if="vetLoading" class="flex justify-center py-6">
          <Loader2 :size="20" class="animate-spin text-blue-400" />
        </div>

        <div v-else-if="vets.length === 0" class="text-center py-6 text-slate-400 text-sm">暂无兽医记录</div>

        <div v-else class="space-y-2">
          <div v-for="vet in vets" :key="vet.id" class="flex items-center justify-between px-4 py-3 bg-slate-50 rounded-xl border border-slate-100">
            <div>
              <p class="text-sm font-bold text-slate-800">{{ vet.name }}</p>
              <p v-if="vet.specialty" class="text-xs text-slate-400">{{ vet.specialty }}</p>
            </div>
            <span class="text-xs text-slate-400">#{{ vet.id }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <!-- 提醒系统（前端本地状态）                   -->
    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <div class="bg-white rounded-3xl shadow-lg border border-slate-100 overflow-hidden">
      <button @click="toggleSection('reminders')" class="w-full flex items-center justify-between p-6 hover:bg-slate-50 transition-colors">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-purple-50 flex items-center justify-center">
            <AlarmClock :size="20" class="text-purple-500" />
          </div>
          <div class="text-left">
            <h2 class="font-extrabold text-slate-800 text-base">提醒系统 Reminder System</h2>
            <p class="text-xs text-slate-400">疫苗 · 用药 · 健康检查 · 喂食</p>
          </div>
        </div>
        <component :is="expandedSection === 'reminders' ? ChevronUp : ChevronDown" :size="20" class="text-slate-400" />
      </button>

      <div v-if="expandedSection === 'reminders'" class="px-6 pb-6 space-y-4">
        <button @click="showReminderForm = !showReminderForm" class="flex items-center gap-2 text-sm font-bold text-purple-600 hover:text-purple-700 bg-purple-50 hover:bg-purple-100 px-4 py-2 rounded-xl transition-all">
          <Plus :size="16" /> 新增提醒
        </button>

        <Transition name="slide">
          <div v-if="showReminderForm" class="bg-slate-50 rounded-2xl p-5 space-y-3 border border-slate-200">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">类型</label>
                <select v-model="newReminder.type" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all">
                  <option value="vaccine">💉 疫苗</option>
                  <option value="medication">💊 药物</option>
                  <option value="checkup">🩺 健康检查</option>
                  <option value="feeding">🍖 喂食</option>
                </select>
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">频率</label>
                <select v-model="newReminder.frequency" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all">
                  <option value="once">一次性</option>
                  <option value="daily">每天</option>
                  <option value="weekly">每周</option>
                  <option value="monthly">每月</option>
                </select>
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">提醒标题</label>
              <input v-model="newReminder.title" type="text" placeholder="请输入提醒名称" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all" />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">提醒时间</label>
              <input v-model="newReminder.datetime" type="datetime-local" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all" />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">备注</label>
              <input v-model="newReminder.note" type="text" placeholder="其他备注（可选）" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all" />
            </div>
            <div class="flex gap-2 pt-1">
              <button @click="addReminder" class="flex-1 py-2.5 bg-purple-500 hover:bg-purple-600 text-white rounded-xl text-sm font-bold shadow-md shadow-purple-100 transition-all">确认添加</button>
              <button @click="showReminderForm = false" class="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all">取消</button>
            </div>
          </div>
        </Transition>

        <div class="space-y-3">
          <div v-for="reminder in reminders" :key="reminder.id" :class="['border rounded-2xl p-4 transition-all', reminder.enabled ? 'border-slate-100 hover:border-purple-100 hover:shadow-sm' : 'border-slate-100 opacity-50']">
            <div class="flex items-start justify-between gap-2">
              <div class="flex items-center gap-3 flex-1 min-w-0">
                <div :class="[reminderTypeConfig[reminder.type].bg, 'w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0']">
                  <component :is="reminderTypeConfig[reminder.type].icon" :size="16" :class="reminderTypeConfig[reminder.type].color" />
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 flex-wrap">
                    <p class="text-sm font-bold text-slate-800 truncate">{{ reminder.title }}</p>
                    <span :class="[reminderTypeConfig[reminder.type].bg, reminderTypeConfig[reminder.type].color, 'text-xs font-bold px-2 py-0.5 rounded-lg flex-shrink-0']">{{ reminderTypeConfig[reminder.type].label }}</span>
                  </div>
                  <div class="flex items-center gap-3 mt-1 flex-wrap">
                    <span class="text-xs text-slate-400 flex items-center gap-1"><Clock :size="11" /> {{ formatDateTime(reminder.datetime) }}</span>
                    <span class="text-xs text-slate-400 bg-slate-100 px-2 py-0.5 rounded-lg">{{ frequencyLabel[reminder.frequency] }}</span>
                  </div>
                  <p v-if="reminder.note" class="text-xs text-slate-400 mt-1">{{ reminder.note }}</p>
                </div>
              </div>
              <div class="flex items-center gap-2 flex-shrink-0">
                <button @click="toggleReminder(reminder.id)" :class="['relative inline-flex h-5 w-9 items-center rounded-full transition-colors', reminder.enabled ? 'bg-purple-500' : 'bg-slate-200']">
                  <span :class="['inline-block h-3.5 w-3.5 rounded-full bg-white shadow transition-transform', reminder.enabled ? 'translate-x-4' : 'translate-x-1']" />
                </button>
                <button @click="deleteReminder(reminder.id)" class="text-slate-300 hover:text-red-400 transition-colors">
                  <Trash2 :size="15" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="flex items-start gap-2 bg-amber-50 border border-amber-100 rounded-xl p-3 mt-2">
          <AlertCircle :size="15" class="text-amber-500 flex-shrink-0 mt-0.5" />
          <p class="text-xs text-amber-700 font-medium">提醒功能目前为前端本地状态，实际推送通知需配合后端定时任务（如 Spring Scheduler）实现。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.slide-enter-active, .slide-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}
.slide-enter-from, .slide-leave-to {
  opacity: 0;
  max-height: 0;
}
.slide-enter-to, .slide-leave-from {
  max-height: 900px;
}
</style>