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
  Pencil,
  X,
} from 'lucide-vue-next'
import {
  getAppointmentsByPet,
  addAppointment,
  updateAppointment,
  deleteAppointment,
  getAllVets,
  addVet,
  deleteVet,
  type Appointment,
  type Vet,
  type AddAppointmentPayload,
} from '../api/appointment'
import { currentPet, allPets, loadPets, selectPet } from '../composables/usePetData'

defineProps<{ headerTitle: string }>()

// ─── Pet selection ─────────────────────────────────────────
onMounted(async () => {
  await loadPets()
  await Promise.all([loadAppointments(), loadVets()])
})

watch(() => currentPet.value, async () => {
  await loadAppointments()
})

// ─── Expand / collapse ─────────────────────────────────────
const expandedSection = ref<string | null>('appointments')
const toggleSection = (name: string) => {
  expandedSection.value = expandedSection.value === name ? null : name
}

const today = new Date().toISOString().split('T')[0] ?? ''
const now = new Date().toTimeString().slice(0, 5) ?? ''

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Medical Appointments (backend)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const appointments = ref<Appointment[]>([])
const loading = ref(false)
const error = ref('')

const loadAppointments = async () => {
  if (!currentPet.value) return
  loading.value = true
  error.value = ''
  try {
    const res = await getAppointmentsByPet(currentPet.value.id)
    appointments.value = res.data
  } catch (err) {
    error.value = 'Failed to load appointments. Please check backend connection.'
    console.error(err)
  } finally {
    loading.value = false
  }
}

// ── Add appointment ────────────────────────────────────────
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
    submitError.value = 'Please select a date and a vet.'
    return
  }
  if (!currentPet.value) {
    submitError.value = 'Please select a pet first.'
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
    newAppointmentForm.value = { date: today, time: now, vetId: '', description: '' }
    showAppointmentForm.value = false
  } catch (err) {
    submitError.value = 'Failed to add appointment. Please try again.'
    console.error(err)
  } finally {
    isSubmitting.value = false
  }
}

// ── Edit appointment ───────────────────────────────────────
const editingAppointment = ref<Appointment | null>(null)
const editForm = ref({
  date: today,
  time: now,
  vetId: '',
  description: '',
})
const isEditSubmitting = ref(false)
const editError = ref('')

const openEditForm = (appt: Appointment) => {
  editingAppointment.value = appt
  const d = new Date(appt.appointmentTime)
  editForm.value = {
    date: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`,
    time: `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`,
    vetId: String(appt.vet.id),
    description: appt.description ?? '',
  }
  editError.value = ''
  // 关闭新增表单，避免两个表单同时展开
  showAppointmentForm.value = false
}

const closeEditForm = () => {
  editingAppointment.value = null
  editError.value = ''
}

const handleEditAppointment = async () => {
  if (!editingAppointment.value) return
  if (!editForm.value.date || !editForm.value.vetId) {
    editError.value = 'Please select a date and a vet.'
    return
  }
  isEditSubmitting.value = true
  editError.value = ''
  try {
    const appointmentTime = `${editForm.value.date}T${editForm.value.time}:00`
    const payload: AddAppointmentPayload = {
      appointmentTime,
      description: editForm.value.description || undefined,
    }
    await updateAppointment(editingAppointment.value.id, payload)
    await loadAppointments()
    closeEditForm()
  } catch (err) {
    editError.value = 'Failed to update appointment. Please try again.'
    console.error(err)
  } finally {
    isEditSubmitting.value = false
  }
}

// ── Delete appointment ─────────────────────────────────────
const handleDeleteAppointment = async (id: number) => {
  try {
    await deleteAppointment(id)
    if (editingAppointment.value?.id === id) closeEditForm()
    await loadAppointments()
  } catch (err) {
    console.error('Failed to delete appointment', err)
  }
}

const upcomingCount = computed(() =>
  appointments.value.filter(a => new Date(a.appointmentTime) >= new Date()).length
)

const formatTime = (iso: string) => {
  const d = new Date(iso)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

const isUpcoming = (iso: string) => new Date(iso) >= new Date()

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Vet Management (backend)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const vets = ref<Vet[]>([])
const vetLoading = ref(false)
const showVetForm = ref(false)
const newVet = ref({ name: '', specialty: '' })
const vetSubmitting = ref(false)
const vetError = ref('')
const vetDeleteError = ref('')

const loadVets = async () => {
  vetLoading.value = true
  try {
    const res = await getAllVets()
    vets.value = res.data
  } catch (err) {
    console.error('Failed to load vet list', err)
  } finally {
    vetLoading.value = false
  }
}

const handleAddVet = async () => {
  if (!newVet.value.name.trim()) {
    vetError.value = 'Vet name cannot be empty.'
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
    vetError.value = 'Failed to add vet.'
    console.error(err)
  } finally {
    vetSubmitting.value = false
  }
}

const handleDeleteVet = async (id: number) => {
  vetDeleteError.value = ''
  try {
    await deleteVet(id)
    await loadVets()
  } catch (err: unknown) {
  let msg = ''

  if (typeof err === 'object' && err !== null) {
    const e = err as {
      response?: { data?: { message?: string } }
      message?: string
    }

    msg = e.response?.data?.message || e.message || ''
  }

  vetDeleteError.value =
    msg || 'Delete failed. This vet may have existing appointments.'

  console.error('Failed to delete vet', err)
}
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Reminder System (frontend local state)
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
  { id: 1, type: 'vaccine',    title: 'Rabies Booster',         datetime: '2026-06-15T09:00', frequency: 'once',    enabled: true,  note: 'Once per year' },
  { id: 2, type: 'medication', title: 'Heartworm Prevention',   datetime: '2025-07-01T08:00', frequency: 'monthly', enabled: true,  note: 'Give on the 1st of each month' },
  { id: 3, type: 'checkup',   title: 'Annual Health Checkup',  datetime: '2026-01-10T10:00', frequency: 'once',    enabled: true,  note: 'Includes blood test & physical exam' },
  { id: 4, type: 'feeding',   title: 'Morning Meal',           datetime: `${today}T08:00`,   frequency: 'daily',   enabled: false, note: '120g Royal Canin Adult' },
])

const showReminderForm = ref(false)
const newReminder = ref<Omit<Reminder, 'id'>>({
  type: 'vaccine', title: '', datetime: `${today}T${now}`, frequency: 'once', enabled: true, note: '',
})

const addReminder = () => {
  if (!newReminder.value.title) return
  reminders.value.unshift({ ...newReminder.value, id: Date.now() })
  newReminder.value = { type: 'vaccine', title: '', datetime: `${today}T${now}`, frequency: 'once', enabled: true, note: '' }
  showReminderForm.value = false
}

const deleteReminder  = (id: number) => { reminders.value = reminders.value.filter(r => r.id !== id) }
const toggleReminder  = (id: number) => { const r = reminders.value.find(x => x.id === id); if (r) r.enabled = !r.enabled }
const enabledCount    = computed(() => reminders.value.filter(r => r.enabled).length)

const reminderTypeConfig: Record<ReminderType, { label: string; color: string; bg: string; icon: unknown }> = {
  vaccine:    { label: 'Vaccine',    color: 'text-purple-600', bg: 'bg-purple-50', icon: Syringe    },
  medication: { label: 'Medication', color: 'text-blue-600',   bg: 'bg-blue-50',   icon: Pill       },
  checkup:    { label: 'Checkup',    color: 'text-teal-600',   bg: 'bg-teal-50',   icon: HeartPulse },
  feeding:    { label: 'Feeding',    color: 'text-amber-600',  bg: 'bg-amber-50',  icon: Utensils   },
}
const frequencyLabel: Record<ReminderFrequency, string> = {
  once: 'One-time', daily: 'Daily', weekly: 'Weekly', monthly: 'Monthly',
}

const formatDateTime = (dt: string) => {
  const d = new Date(dt)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}
</script>

<template>
  <div class="space-y-6">

    <!-- ── Pet Selector ──────────────────────────────────────── -->
    <div v-if="allPets.length > 1" class="flex items-center gap-2 flex-wrap">
      <span class="text-xs font-bold text-slate-500">Current Pet:</span>
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

    <!-- ── Stats Cards ──────────────────────────────────────── -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-teal-50 flex items-center justify-center">
          <CalendarDays :size="20" class="text-teal-500" />
        </div>
        <div>
          <p class="text-xs text-slate-400 font-medium">Upcoming</p>
          <p class="text-xl font-extrabold text-slate-800">{{ upcomingCount }}</p>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-purple-50 flex items-center justify-center">
          <Bell :size="20" class="text-purple-500" />
        </div>
        <div>
          <p class="text-xs text-slate-400 font-medium">Active Reminders</p>
          <p class="text-xl font-extrabold text-slate-800">{{ enabledCount }}</p>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center">
          <UserRound :size="20" class="text-blue-500" />
        </div>
        <div>
          <p class="text-xs text-slate-400 font-medium">Registered Vets</p>
          <p class="text-xl font-extrabold text-slate-800">{{ vets.length }}</p>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center">
          <Syringe :size="20" class="text-amber-500" />
        </div>
        <div>
          <p class="text-xs text-slate-400 font-medium">Total Appointments</p>
          <p class="text-xl font-extrabold text-slate-800">{{ appointments.length }}</p>
        </div>
      </div>
    </div>

    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <!-- Medical Appointments                       -->
    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <div class="bg-white rounded-3xl shadow-lg border border-slate-100 overflow-hidden">
      <button @click="toggleSection('appointments')" class="w-full flex items-center justify-between p-6 hover:bg-slate-50 transition-colors">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-teal-50 flex items-center justify-center">
            <CalendarDays :size="20" class="text-teal-500" />
          </div>
          <div class="text-left">
            <h2 class="font-extrabold text-slate-800 text-base">Medical Appointments</h2>
            <p class="text-xs text-slate-400">Connected to backend · Live sync</p>
          </div>
        </div>
        <component :is="expandedSection === 'appointments' ? ChevronUp : ChevronDown" :size="20" class="text-slate-400" />
      </button>

      <div v-if="expandedSection === 'appointments'" class="px-6 pb-6 space-y-4">
        <!-- Error -->
        <div v-if="error" class="flex items-center gap-2 bg-red-50 text-red-600 text-sm px-4 py-3 rounded-xl">
          <AlertCircle :size="16" />
          {{ error }}
          <button @click="loadAppointments" class="ml-auto text-xs underline">Retry</button>
        </div>

        <!-- Toolbar -->
        <div class="flex items-center gap-2">
          <button @click="showAppointmentForm = !showAppointmentForm; closeEditForm()" class="flex items-center gap-2 text-sm font-bold text-teal-600 hover:text-teal-700 bg-teal-50 hover:bg-teal-100 px-4 py-2 rounded-xl transition-all">
            <Plus :size="16" /> New Appointment
          </button>
          <button @click="loadAppointments" class="flex items-center gap-1 text-xs text-slate-400 hover:text-slate-600 px-3 py-2 rounded-xl hover:bg-slate-100 transition-all">
            <RefreshCw :size="13" /> Refresh
          </button>
        </div>

        <!-- Add Form -->
        <Transition name="slide">
          <div v-if="showAppointmentForm" class="bg-slate-50 rounded-2xl p-5 space-y-3 border border-slate-200">
            <p class="text-sm font-extrabold text-slate-700">New Appointment</p>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">Date</label>
                <input v-model="newAppointmentForm.date" type="date" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all" />
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">Time</label>
                <input v-model="newAppointmentForm.time" type="time" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all" />
              </div>
            </div>

            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">Select Vet</label>
              <div v-if="vets.length === 0" class="flex items-start gap-3 bg-amber-50 border border-amber-200 rounded-xl px-4 py-3 mb-2">
                <AlertCircle :size="16" class="text-amber-500 flex-shrink-0 mt-0.5" />
                <div>
                  <p class="text-sm font-bold text-amber-700">No vets added yet</p>
                  <p class="text-xs text-amber-600 mt-0.5 leading-snug">
                    You need to add a vet before booking an appointment.
                    Scroll down to <strong>Vet Management</strong> and add one first.
                  </p>
                </div>
              </div>
              <select
                v-model="newAppointmentForm.vetId"
                :disabled="vets.length === 0"
                class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all disabled:bg-slate-100 disabled:text-slate-400 disabled:cursor-not-allowed"
              >
                <option value="">-- Select a Vet --</option>
                <option v-for="vet in vets" :key="vet.id" :value="vet.id">
                  {{ vet.name }}{{ vet.specialty ? ` · ${vet.specialty}` : '' }}
                </option>
              </select>
            </div>

            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">Description / Symptoms</label>
              <textarea v-model="newAppointmentForm.description" rows="2" placeholder="Describe symptoms or reason for visit (optional)" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all resize-none" />
            </div>
            <p v-if="submitError" class="text-xs text-red-500">{{ submitError }}</p>
            <div class="flex gap-2 pt-1">
              <button @click="handleAddAppointment" :disabled="isSubmitting || vets.length === 0" class="flex-1 py-2.5 bg-teal-500 hover:bg-teal-600 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-xl text-sm font-bold shadow-md shadow-teal-100 transition-all flex items-center justify-center gap-2">
                <Loader2 v-if="isSubmitting" :size="14" class="animate-spin" />
                Confirm
              </button>
              <button @click="showAppointmentForm = false" class="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all">Cancel</button>
            </div>
          </div>
        </Transition>

        <!-- Edit Form -->
        <Transition name="slide">
          <div v-if="editingAppointment" class="bg-blue-50 rounded-2xl p-5 space-y-3 border border-blue-200">
            <p class="text-sm font-extrabold text-blue-700">
              Edit Appointment <span class="font-normal text-blue-400">#{{ editingAppointment.id }}</span>
            </p>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">Date</label>
                <input v-model="editForm.date" type="date" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-blue-400 transition-all" />
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">Time</label>
                <input v-model="editForm.time" type="time" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-blue-400 transition-all" />
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">Select Vet</label>
              <select
                v-model="editForm.vetId"
                class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-blue-400 transition-all"
              >
                <option value="">-- Select a Vet --</option>
                <option v-for="vet in vets" :key="vet.id" :value="vet.id">
                  {{ vet.name }}{{ vet.specialty ? ` · ${vet.specialty}` : '' }}
                </option>
              </select>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">Description / Symptoms</label>
              <textarea v-model="editForm.description" rows="2" placeholder="Describe symptoms or reason for visit (optional)" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-blue-400 transition-all resize-none" />
            </div>
            <p v-if="editError" class="text-xs text-red-500">{{ editError }}</p>
            <div class="flex gap-2 pt-1">
              <button @click="handleEditAppointment" :disabled="isEditSubmitting" class="flex-1 py-2.5 bg-blue-500 hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-xl text-sm font-bold shadow-md shadow-blue-100 transition-all flex items-center justify-center gap-2">
                <Loader2 v-if="isEditSubmitting" :size="14" class="animate-spin" />
                Save Changes
              </button>
              <button @click="closeEditForm" class="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all flex items-center justify-center">
                <X :size="14" />
              </button>
            </div>
          </div>
        </Transition>

        <!-- Loading -->
        <div v-if="loading" class="flex justify-center py-8">
          <Loader2 :size="24" class="animate-spin text-teal-400" />
        </div>

        <!-- Empty state -->
        <div v-else-if="appointments.length === 0 && !error" class="text-center py-8 text-slate-400 text-sm">
          No appointments yet. Click the button above to add one.
        </div>

        <!-- Appointment list -->
        <div v-else class="space-y-3">
          <div
            v-for="appt in appointments"
            :key="appt.id"
            :class="[
              'border rounded-2xl p-4 transition-all',
              editingAppointment?.id === appt.id
                ? 'border-blue-300 bg-blue-50/40'
                : 'border-slate-100 hover:border-teal-100 hover:shadow-sm',
            ]"
          >
            <div class="flex items-start justify-between gap-2 mb-3">
              <div class="flex items-center gap-2 flex-wrap">
                <span :class="[
                  isUpcoming(appt.appointmentTime) ? 'bg-teal-50 text-teal-600' : 'bg-slate-100 text-slate-500',
                  'flex items-center gap-1 text-xs font-bold px-2.5 py-1 rounded-lg'
                ]">
                  <component :is="isUpcoming(appt.appointmentTime) ? Clock : CheckCircle2" :size="12" />
                  {{ isUpcoming(appt.appointmentTime) ? 'Upcoming' : 'Past' }}
                </span>
                <span class="flex items-center gap-1 text-xs text-slate-500 font-medium">
                  <Clock :size="12" /> {{ formatTime(appt.appointmentTime) }}
                </span>
              </div>
              <div class="flex items-center gap-1 flex-shrink-0">
                <button @click="openEditForm(appt)" class="p-1 text-slate-300 hover:text-blue-400 transition-colors" title="Edit">
                  <Pencil :size="15" />
                </button>
                <button @click="handleDeleteAppointment(appt.id)" class="p-1 text-slate-300 hover:text-red-400 transition-colors" title="Delete">
                  <Trash2 :size="16" />
                </button>
              </div>
            </div>
            <div class="space-y-1.5">
              <div class="flex items-start gap-2">
                <Stethoscope :size="14" class="text-teal-400 mt-0.5 flex-shrink-0" />
                <p class="text-sm font-bold text-slate-700">Vet: {{ appt.vet.name }}{{ appt.vet.specialty ? ` · ${appt.vet.specialty}` : '' }}</p>
              </div>
              <div class="flex items-start gap-2">
                <MapPin :size="14" class="text-slate-400 mt-0.5 flex-shrink-0" />
                <p class="text-sm text-slate-600">Pet: {{ appt.pet.name }}</p>
              </div>
              <p v-if="appt.description" class="text-xs text-slate-400 ml-5">📝 {{ appt.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <!-- Vet Management                             -->
    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <div class="bg-white rounded-3xl shadow-lg border border-slate-100 overflow-hidden">
      <button @click="toggleSection('vets')" class="w-full flex items-center justify-between p-6 hover:bg-slate-50 transition-colors">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-blue-50 flex items-center justify-center">
            <UserRound :size="20" class="text-blue-500" />
          </div>
          <div class="text-left">
            <h2 class="font-extrabold text-slate-800 text-base">Vet Management</h2>
            <p class="text-xs text-slate-400">Connected to backend · Live sync</p>
          </div>
        </div>
        <component :is="expandedSection === 'vets' ? ChevronUp : ChevronDown" :size="20" class="text-slate-400" />
      </button>

      <div v-if="expandedSection === 'vets'" class="px-6 pb-6 space-y-4">
        <button @click="showVetForm = !showVetForm" class="flex items-center gap-2 text-sm font-bold text-blue-600 hover:text-blue-700 bg-blue-50 hover:bg-blue-100 px-4 py-2 rounded-xl transition-all">
          <Plus :size="16" /> Add Vet
        </button>

        <!-- Add Vet Form -->
        <Transition name="slide">
          <div v-if="showVetForm" class="bg-slate-50 rounded-2xl p-5 space-y-3 border border-slate-200">
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">Vet Name *</label>
              <input v-model="newVet.name" type="text" placeholder="Enter vet name" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-blue-400 transition-all" />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">Specialty</label>
              <input v-model="newVet.specialty" type="text" placeholder="e.g. Dermatology, Surgery, Dentistry (optional)" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-blue-400 transition-all" />
            </div>
            <p v-if="vetError" class="text-xs text-red-500">{{ vetError }}</p>
            <div class="flex gap-2 pt-1">
              <button @click="handleAddVet" :disabled="vetSubmitting" class="flex-1 py-2.5 bg-blue-500 hover:bg-blue-600 disabled:opacity-50 text-white rounded-xl text-sm font-bold shadow-md shadow-blue-100 transition-all flex items-center justify-center gap-2">
                <Loader2 v-if="vetSubmitting" :size="14" class="animate-spin" />
                Confirm
              </button>
              <button @click="showVetForm = false" class="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all">Cancel</button>
            </div>
          </div>
        </Transition>

        <!-- Delete error -->
        <div v-if="vetDeleteError" class="flex items-center gap-2 bg-red-50 text-red-600 text-sm px-4 py-3 rounded-xl">
          <AlertCircle :size="16" />
          {{ vetDeleteError }}
          <button @click="vetDeleteError = ''" class="ml-auto text-xs underline">Dismiss</button>
        </div>

        <!-- Loading -->
        <div v-if="vetLoading" class="flex justify-center py-6">
          <Loader2 :size="20" class="animate-spin text-blue-400" />
        </div>

        <div v-else-if="vets.length === 0" class="text-center py-6 text-slate-400 text-sm">No vets found. Add one above.</div>

        <div v-else class="space-y-2">
          <div v-for="vet in vets" :key="vet.id" class="flex items-center justify-between px-4 py-3 bg-slate-50 rounded-xl border border-slate-100">
            <div>
              <p class="text-sm font-bold text-slate-800">{{ vet.name }}</p>
              <p v-if="vet.specialty" class="text-xs text-slate-400">{{ vet.specialty }}</p>
            </div>
            <div class="flex items-center gap-3">
              <span class="text-xs text-slate-400">#{{ vet.id }}</span>
              <button @click="handleDeleteVet(vet.id)" class="text-slate-300 hover:text-red-400 transition-colors" title="Delete vet">
                <Trash2 :size="16" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <!-- Reminder System (local state)              -->
    <!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
    <div class="bg-white rounded-3xl shadow-lg border border-slate-100 overflow-hidden">
      <button @click="toggleSection('reminders')" class="w-full flex items-center justify-between p-6 hover:bg-slate-50 transition-colors">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-purple-50 flex items-center justify-center">
            <AlarmClock :size="20" class="text-purple-500" />
          </div>
          <div class="text-left">
            <h2 class="font-extrabold text-slate-800 text-base">Reminder System</h2>
            <p class="text-xs text-slate-400">Vaccine · Medication · Checkup · Feeding</p>
          </div>
        </div>
        <component :is="expandedSection === 'reminders' ? ChevronUp : ChevronDown" :size="20" class="text-slate-400" />
      </button>

      <div v-if="expandedSection === 'reminders'" class="px-6 pb-6 space-y-4">
        <button @click="showReminderForm = !showReminderForm" class="flex items-center gap-2 text-sm font-bold text-purple-600 hover:text-purple-700 bg-purple-50 hover:bg-purple-100 px-4 py-2 rounded-xl transition-all">
          <Plus :size="16" /> New Reminder
        </button>

        <!-- Add Reminder Form -->
        <Transition name="slide">
          <div v-if="showReminderForm" class="bg-slate-50 rounded-2xl p-5 space-y-3 border border-slate-200">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">Type</label>
                <select v-model="newReminder.type" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all">
                  <option value="vaccine">💉 Vaccine</option>
                  <option value="medication">💊 Medication</option>
                  <option value="checkup">🩺 Checkup</option>
                  <option value="feeding">🍖 Feeding</option>
                </select>
              </div>
              <div>
                <label class="block text-xs font-bold text-slate-500 mb-1">Frequency</label>
                <select v-model="newReminder.frequency" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all">
                  <option value="once">One-time</option>
                  <option value="daily">Daily</option>
                  <option value="weekly">Weekly</option>
                  <option value="monthly">Monthly</option>
                </select>
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">Title</label>
              <input v-model="newReminder.title" type="text" placeholder="Enter reminder name" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all" />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">Date & Time</label>
              <input v-model="newReminder.datetime" type="datetime-local" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all" />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">Notes</label>
              <input v-model="newReminder.note" type="text" placeholder="Additional notes (optional)" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-slate-800 text-sm font-medium focus:outline-none focus:border-purple-400 transition-all" />
            </div>
            <div class="flex gap-2 pt-1">
              <button @click="addReminder" class="flex-1 py-2.5 bg-purple-500 hover:bg-purple-600 text-white rounded-xl text-sm font-bold shadow-md shadow-purple-100 transition-all">Confirm</button>
              <button @click="showReminderForm = false" class="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all">Cancel</button>
            </div>
          </div>
        </Transition>

        <!-- Reminder list -->
        <div class="space-y-3">
          <div
            v-for="reminder in reminders"
            :key="reminder.id"
            :class="['border rounded-2xl p-4 transition-all', reminder.enabled ? 'border-slate-100 hover:border-purple-100 hover:shadow-sm' : 'border-slate-100 opacity-50']"
          >
            <div class="flex items-start justify-between gap-2">
              <div class="flex items-center gap-3 flex-1 min-w-0">
                <div :class="[reminderTypeConfig[reminder.type].bg, 'w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0']">
                  <component :is="reminderTypeConfig[reminder.type].icon" :size="16" :class="reminderTypeConfig[reminder.type].color" />
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 flex-wrap">
                    <p class="text-sm font-bold text-slate-800 truncate">{{ reminder.title }}</p>
                    <span :class="[reminderTypeConfig[reminder.type].bg, reminderTypeConfig[reminder.type].color, 'text-xs font-bold px-2 py-0.5 rounded-lg flex-shrink-0']">
                      {{ reminderTypeConfig[reminder.type].label }}
                    </span>
                  </div>
                  <div class="flex items-center gap-3 mt-1 flex-wrap">
                    <span class="text-xs text-slate-400 flex items-center gap-1"><Clock :size="11" /> {{ formatDateTime(reminder.datetime) }}</span>
                    <span class="text-xs text-slate-400 bg-slate-100 px-2 py-0.5 rounded-lg">{{ frequencyLabel[reminder.frequency] }}</span>
                  </div>
                  <p v-if="reminder.note" class="text-xs text-slate-400 mt-1">{{ reminder.note }}</p>
                </div>
              </div>
              <div class="flex items-center gap-2 flex-shrink-0">
                <button
                  @click="toggleReminder(reminder.id)"
                  :class="['relative inline-flex h-5 w-9 items-center rounded-full transition-colors', reminder.enabled ? 'bg-purple-500' : 'bg-slate-200']"
                >
                  <span :class="['inline-block h-3.5 w-3.5 rounded-full bg-white shadow transition-transform', reminder.enabled ? 'translate-x-4' : 'translate-x-1']" />
                </button>
                <button @click="deleteReminder(reminder.id)" class="text-slate-300 hover:text-red-400 transition-colors">
                  <Trash2 :size="15" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Info notice -->
        <div class="flex items-start gap-2 bg-amber-50 border border-amber-100 rounded-xl p-3 mt-2">
          <AlertCircle :size="15" class="text-amber-500 flex-shrink-0 mt-0.5" />
          <p class="text-xs text-amber-700 font-medium">
            Reminders are stored locally in the frontend. Push notifications require a backend scheduler (e.g. Spring Scheduler).
          </p>
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