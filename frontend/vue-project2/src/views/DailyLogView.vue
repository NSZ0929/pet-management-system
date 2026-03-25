<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import {
  Utensils,
  Droplets,
  Moon,
  Activity,
  Heart,
  Plus,
  Trash2,
  ChevronDown,
  ChevronUp,
  Clock,
  CheckCircle2,
  AlertCircle,
  Loader2,
  RefreshCw,
} from 'lucide-vue-next'
import {
  addDailyLog,
  getDailyLogs,
  deleteDailyLog,
  type DailyLog,
  type DailyLogType,
} from '../api/dailyLog'
import { currentPet } from '../composables/usePetData'

defineProps<{ headerTitle: string }>()

const petId = ref<number | null>(currentPet.value?.id ?? null)
watch(
  () => currentPet.value,
  (pet) => {
    if (pet) {
      petId.value = pet.id
      loadAllLogs()
    }
  },
)

const loading = ref(false)
const error = ref('')
const expandedSection = ref<string | null>('feeding')
const toggleSection = (name: string) => {
  expandedSection.value = expandedSection.value === name ? null : name
}

const today = new Date().toISOString().split('T')[0] ?? ''
const now = new Date().toTimeString().slice(0, 5) ?? ''

const feedingLogs = ref<DailyLog[]>([])
const excretionLogs = ref<DailyLog[]>([])
const sleepLogs = ref<DailyLog[]>([])
const activityLogs = ref<DailyLog[]>([])
const moodLogs = ref<DailyLog[]>([])

const parseData = (log: DailyLog) => {
  try {
    return JSON.parse(log.content)
  } catch {
    return {}
  }
}

const loadAllLogs = async () => {
  if (!petId.value) return
  loading.value = true
  error.value = ''
  try {
    const [f, e, s, a, m] = await Promise.all([
      getDailyLogs(petId.value, 'FEEDING'),
      getDailyLogs(petId.value, 'EXCRETION'),
      getDailyLogs(petId.value, 'SLEEP'),
      getDailyLogs(petId.value, 'ACTIVITY'),
      getDailyLogs(petId.value, 'MOOD'),
    ])
    feedingLogs.value = f.data
    excretionLogs.value = e.data
    sleepLogs.value = s.data
    activityLogs.value = a.data
    moodLogs.value = m.data
  } catch {
    error.value = 'Failed to load logs. Please check backend connection.'
  } finally {
    loading.value = false
  }
}

onMounted(loadAllLogs)

const reloadType = async (type: DailyLogType) => {
  if (!petId.value) return
  const r = await getDailyLogs(petId.value, type)
  if (type === 'FEEDING') feedingLogs.value = r.data
  if (type === 'EXCRETION') excretionLogs.value = r.data
  if (type === 'SLEEP') sleepLogs.value = r.data
  if (type === 'ACTIVITY') activityLogs.value = r.data
  if (type === 'MOOD') moodLogs.value = r.data
}

const deleteLog = async (id: number, type: DailyLogType) => {
  try {
    await deleteDailyLog(id)
    await reloadType(type)
  } catch {
    error.value = 'Delete failed.'
  }
}

const failValidation = (message: string) => {
  error.value = message
  return false
}

const validateFeeding = () => {
  if (!newFeeding.value.foodType.trim()) return failValidation('Food type is required.')
  if (newFeeding.value.amount <= 0) return failValidation('Feeding amount must be greater than 0.')
  if (newFeeding.value.water < 0) return failValidation('Water intake cannot be negative.')
  return true
}

const validateExcretion = () => {
  if (!newExcretion.value.color.trim()) return failValidation('Please enter excretion color.')
  if (!newExcretion.value.consistency.trim()) return failValidation('Please enter excretion consistency.')
  return true
}

const validateSleep = () => {
  if (!newSleep.value.bedtime) return failValidation('Bedtime is required.')
  if (!newSleep.value.wakeTime) return failValidation('Wake time is required.')
  return true
}

const validateActivity = () => {
  if (!newActivity.value.type.trim()) return failValidation('Activity type is required.')
  if (newActivity.value.duration <= 0) return failValidation('Activity duration must be greater than 0.')
  return true
}

const validateMood = () => {
  if (!newMood.value.mood.trim()) return failValidation('Mood is required.')
  if (newMood.value.intensity < 1 || newMood.value.intensity > 5) {
    return failValidation('Mood intensity must be between 1 and 5.')
  }
  return true
}

// Feeding
const showFeedingForm = ref(false)
const feedingSubmitting = ref(false)
const newFeeding = ref({
  time: now,
  foodType: '',
  amount: 0,
  unit: 'g',
  water: 0,
  appetite: 'normal',
  note: '',
})
const addFeeding = async () => {
  if (!petId.value) return
  error.value = ''
  if (!validateFeeding()) return
  feedingSubmitting.value = true
  try {
    await addDailyLog({
      petId: petId.value,
      type: 'FEEDING',
      content: JSON.stringify(newFeeding.value),
    })
    await reloadType('FEEDING')
    showFeedingForm.value = false
    newFeeding.value = {
      time: now,
      foodType: '',
      amount: 0,
      unit: 'g',
      water: 0,
      appetite: 'normal',
      note: '',
    }
  } catch {
    error.value = 'Failed to add feeding record.'
  } finally {
    feedingSubmitting.value = false
  }
}
const appetiteLabel: Record<string, { label: string; color: string }> = {
  great: { label: 'Great Appetite', color: 'text-emerald-600 bg-emerald-50' },
  normal: { label: 'Normal', color: 'text-blue-600 bg-blue-50' },
  poor: { label: 'Poor Appetite', color: 'text-amber-600 bg-amber-50' },
}

// Excretion
const showExcretionForm = ref(false)
const excretionSubmitting = ref(false)
const newExcretion = ref({
  time: now,
  type: 'urine',
  color: '',
  consistency: '',
  abnormal: false,
  note: '',
})
const addExcretion = async () => {
  if (!petId.value) return
  error.value = ''
  if (!validateExcretion()) return
  excretionSubmitting.value = true
  try {
    await addDailyLog({
      petId: petId.value,
      type: 'EXCRETION',
      content: JSON.stringify(newExcretion.value),
    })
    await reloadType('EXCRETION')
    showExcretionForm.value = false
    newExcretion.value = {
      time: now,
      type: 'urine',
      color: '',
      consistency: '',
      abnormal: false,
      note: '',
    }
  } catch {
    error.value = 'Failed to add excretion record.'
  } finally {
    excretionSubmitting.value = false
  }
}

// Sleep
const showSleepForm = ref(false)
const sleepSubmitting = ref(false)
const newSleep = ref({ date: today, bedtime: '', wakeTime: '', quality: 3, note: '' })
const addSleep = async () => {
  if (!petId.value) return
  error.value = ''
  if (!validateSleep()) return
  sleepSubmitting.value = true
  try {
    await addDailyLog({ petId: petId.value, type: 'SLEEP', content: JSON.stringify(newSleep.value) })
    await reloadType('SLEEP')
    showSleepForm.value = false
    newSleep.value = { date: today, bedtime: '', wakeTime: '', quality: 3, note: '' }
  } catch {
    error.value = 'Failed to add sleep record.'
  } finally {
    sleepSubmitting.value = false
  }
}
const sleepDuration = (bedtime: string, wakeTime: string): string => {
  const [bh = 0, bm = 0] = (bedtime || '').split(':').map(Number)
  const [wh = 0, wm = 0] = (wakeTime || '').split(':').map(Number)
  let mins = wh * 60 + wm - (bh * 60 + bm)
  if (mins < 0) mins += 1440
  return `${Math.floor(mins / 60)}h ${mins % 60}m`
}
const qualityStars = (q: number) => '★'.repeat(q) + '☆'.repeat(5 - q)

// Activity
const showActivityForm = ref(false)
const activitySubmitting = ref(false)
const newActivity = ref({
  date: today,
  type: '',
  duration: 0,
  energyLevel: 3,
  behaviors: [] as string[],
  note: '',
})
const behaviorOptions = ['Scratching', 'Excessive Licking', 'Whimpering', 'Trembling', 'Aggression', 'Pica']
const toggleBehavior = (b: string) => {
  const idx = newActivity.value.behaviors.indexOf(b)
  if (idx === -1) newActivity.value.behaviors.push(b)
  else newActivity.value.behaviors.splice(idx, 1)
}
const addActivity = async () => {
  if (!petId.value) return
  error.value = ''
  if (!validateActivity()) return
  activitySubmitting.value = true
  try {
    await addDailyLog({
      petId: petId.value,
      type: 'ACTIVITY',
      content: JSON.stringify({ ...newActivity.value, behaviors: [...newActivity.value.behaviors] }),
    })
    await reloadType('ACTIVITY')
    showActivityForm.value = false
    newActivity.value = {
      date: today,
      type: '',
      duration: 0,
      energyLevel: 3,
      behaviors: [],
      note: '',
    }
  } catch {
    error.value = 'Failed to add activity record.'
  } finally {
    activitySubmitting.value = false
  }
}
const energyLabel: Record<number, string> = {
  1: 'Very Low',
  2: 'Low',
  3: 'Normal',
  4: 'Active',
  5: 'Very Active',
}

// Mood
const showMoodForm = ref(false)
const moodSubmitting = ref(false)
const newMood = ref({ date: today, mood: 'neutral', intensity: 2, triggers: '', note: '' })
const addMood = async () => {
  if (!petId.value) return
  error.value = ''
  if (!validateMood()) return
  moodSubmitting.value = true
  try {
    await addDailyLog({ petId: petId.value, type: 'MOOD', content: JSON.stringify(newMood.value) })
    await reloadType('MOOD')
    showMoodForm.value = false
    newMood.value = { date: today, mood: 'neutral', intensity: 2, triggers: '', note: '' }
  } catch {
    error.value = 'Failed to add mood record.'
  } finally {
    moodSubmitting.value = false
  }
}
const moodConfig: Record<string, { emoji: string; label: string; color: string; bg: string }> = {
  happy: {
    emoji: '😊',
    label: 'Happy',
    color: 'text-yellow-600',
    bg: 'bg-yellow-50 border-yellow-200',
  },
  nervous: {
    emoji: '😰',
    label: 'Nervous',
    color: 'text-purple-600',
    bg: 'bg-purple-50 border-purple-200',
  },
  stressed: {
    emoji: '😖',
    label: 'Stressed',
    color: 'text-red-600',
    bg: 'bg-red-50 border-red-200',
  },
  low: {
    emoji: '😔',
    label: 'Low Mood',
    color: 'text-slate-600',
    bg: 'bg-slate-50 border-slate-200',
  },
  neutral: {
    emoji: '😐',
    label: 'Calm / Neutral',
    color: 'text-teal-600',
    bg: 'bg-teal-50 border-teal-200',
  },
}

const sections = [
  {
    key: 'feeding',
    icon: Utensils,
    sublabel: 'Feeding Log',
    count: () => feedingLogs.value.length,
  },
  {
    key: 'excretion',
    icon: Droplets,
    sublabel: 'Excretion Log',
    count: () => excretionLogs.value.length,
  },
  { key: 'sleep', icon: Moon, sublabel: 'Sleep Tracking', count: () => sleepLogs.value.length },
  { key: 'activity', icon: Activity, sublabel: 'Activity', count: () => activityLogs.value.length },
  { key: 'mood', icon: Heart, sublabel: 'Emotional State', count: () => moodLogs.value.length },
]
</script>

<template>
  <div class="space-y-4 max-w-4xl mx-auto">
    <div
      v-if="!petId"
      class="text-center py-16 bg-white rounded-3xl shadow-lg border border-slate-100"
    >
      <span class="text-5xl">🐾</span>
      <p class="font-bold text-slate-600 mt-4">Please add a pet in the Profile page first.</p>
    </div>

    <template v-else>
      <div
        v-if="error"
        class="flex items-center gap-3 bg-red-50 border border-red-100 text-red-500 px-5 py-3 rounded-2xl text-sm"
      >
        <AlertCircle :size="16" />{{ error }}
        <button @click="loadAllLogs" class="ml-auto flex items-center gap-1 font-bold">
          <RefreshCw :size="13" /> Retry
        </button>
      </div>

      <!-- Header -->
      <div
        class="bg-gradient-to-r from-teal-500 to-cyan-500 rounded-3xl p-6 text-white shadow-lg shadow-teal-200"
      >
        <div class="flex items-center justify-between">
          <div>
            <h2 class="text-2xl font-extrabold">🐾 Daily Log</h2>
            <p class="text-teal-100 text-sm mt-1">Daily Life Tracking · {{ today }}</p>
          </div>
          <div class="flex items-center gap-3">
            <button
              @click="loadAllLogs"
              :disabled="loading"
              class="bg-white/20 hover:bg-white/30 px-3 py-1.5 rounded-xl text-sm font-bold flex items-center gap-1 transition-all"
            >
              <Loader2 v-if="loading" :size="14" class="animate-spin" /><RefreshCw
                v-else
                :size="14"
              />
              Refresh
            </button>
            <div class="text-right">
              <p class="text-3xl font-black">{{ sections.length }}</p>
              <p class="text-teal-100 text-xs">Modules</p>
            </div>
          </div>
        </div>
        <div class="grid grid-cols-5 gap-2 mt-4">
          <div
            v-for="s in sections"
            :key="s.key"
            @click="toggleSection(s.key)"
            class="bg-white/20 backdrop-blur rounded-2xl p-2 text-center cursor-pointer hover:bg-white/30 transition-all"
            :class="expandedSection === s.key ? 'ring-2 ring-white' : ''"
          >
            <component :is="s.icon" :size="16" class="mx-auto mb-1" />
            <p class="text-lg font-black">{{ s.count() }}</p>
            <p class="text-[10px] text-teal-100">{{ s.sublabel }}</p>
          </div>
        </div>
      </div>

      <!-- Feeding -->
      <div class="bg-white rounded-3xl shadow-lg border border-slate-100 overflow-hidden">
        <button
          @click="toggleSection('feeding')"
          class="w-full flex items-center justify-between p-5 hover:bg-teal-50 transition-colors"
        >
          <div class="flex items-center gap-3">
            <div class="bg-teal-100 p-2.5 rounded-2xl">
              <Utensils :size="20" class="text-teal-600" />
            </div>
            <div class="text-left">
              <p class="font-bold text-slate-800">Feeding Log</p>
              <p class="text-xs text-slate-400">Feeding Log · Today: {{ feedingLogs.length }} entries</p>
            </div>
          </div>
          <component
            :is="expandedSection === 'feeding' ? ChevronUp : ChevronDown"
            :size="20"
            class="text-slate-400"
          />
        </button>
        <div v-if="expandedSection === 'feeding'" class="px-5 pb-5 space-y-3">
          <div
            v-for="log in feedingLogs"
            :key="log.id"
            class="bg-slate-50 rounded-2xl p-4 flex items-start justify-between gap-3"
          >
            <div class="flex-1 space-y-1">
              <div class="flex items-center gap-2 flex-wrap">
                <span class="font-semibold text-slate-800">{{
                  parseData(log).foodType || 'Food not specified'
                }}</span>
                <span
                  class="text-xs px-2 py-0.5 rounded-full font-medium"
                  :class="appetiteLabel[parseData(log).appetite]?.color"
                  >{{ appetiteLabel[parseData(log).appetite]?.label }}</span
                >
              </div>
              <div class="flex gap-4 text-sm text-slate-500">
                <span class="flex items-center gap-1"
                  ><Clock :size="12" />{{ parseData(log).time }}</span
                >
                <span>🍖 {{ parseData(log).amount }}{{ parseData(log).unit }}</span>
                <span>💧 {{ parseData(log).water }}ml</span>
              </div>
              <p v-if="parseData(log).note" class="text-xs text-slate-400 italic">
                {{ parseData(log).note }}
              </p>
            </div>
            <button
              @click="deleteLog(log.id!, 'FEEDING')"
              class="text-slate-300 hover:text-red-400 transition-colors mt-1"
            >
              <Trash2 :size="16" />
            </button>
          </div>
          <div
            v-if="showFeedingForm"
            class="bg-teal-50 rounded-2xl p-4 space-y-3 border border-teal-100"
          >
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="text-xs font-semibold text-slate-600 mb-1 block">Time</label
                ><input
                  type="time"
                  v-model="newFeeding.time"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-300 bg-white"
                />
              </div>
              <div>
                <label class="text-xs font-semibold text-slate-600 mb-1 block">Food Type</label
                ><input
                  type="text"
                  v-model="newFeeding.foodType"
                  placeholder="e.g. Royal Canin Adult"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-300 bg-white"
                />
              </div>
              <div>
                <label class="text-xs font-semibold text-slate-600 mb-1 block">Amount (g)</label
                ><input
                  type="number"
                  v-model.number="newFeeding.amount"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-300 bg-white"
                />
              </div>
              <div>
                <label class="text-xs font-semibold text-slate-600 mb-1 block">Water Intake (ml)</label
                ><input
                  type="number"
                  v-model.number="newFeeding.water"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-300 bg-white"
                />
              </div>
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block">Appetite</label>
              <div class="flex gap-2">
                <button
                  v-for="opt in ['great', 'normal', 'poor']"
                  :key="opt"
                  @click="newFeeding.appetite = opt"
                  class="flex-1 py-2 rounded-xl text-xs font-semibold border-2 transition-all"
                  :class="
                    newFeeding.appetite === opt
                      ? 'border-teal-500 bg-teal-500 text-white'
                      : 'border-slate-200 text-slate-600 bg-white'
                  "
                >
                  {{ opt === 'great' ? '😋 Great' : opt === 'normal' ? '😊 Normal' : '😞 Poor' }}
                </button>
              </div>
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block">Notes</label
              ><input
                type="text"
                v-model="newFeeding.note"
                placeholder="Optional notes..."
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-300 bg-white"
              />
            </div>
            <div class="flex gap-2">
              <button
                @click="addFeeding"
                :disabled="feedingSubmitting"
                class="flex-1 bg-teal-500 hover:bg-teal-600 disabled:bg-teal-300 text-white font-semibold py-2 rounded-xl text-sm flex items-center justify-center gap-2"
              >
                <Loader2 v-if="feedingSubmitting" :size="14" class="animate-spin" />{{
                  feedingSubmitting ? 'Saving...' : '✓ Save'
                }}
              </button>
              <button
                @click="showFeedingForm = false"
                class="px-4 py-2 rounded-xl border border-slate-200 text-slate-500 text-sm hover:bg-slate-100"
              >
                Cancel
              </button>
            </div>
          </div>
          <button
            v-if="!showFeedingForm"
            @click="showFeedingForm = true"
            class="w-full flex items-center justify-center gap-2 py-3 border-2 border-dashed border-teal-200 rounded-2xl text-teal-500 hover:bg-teal-50 text-sm font-semibold"
          >
            <Plus :size="16" /> Add Entry
          </button>
        </div>
      </div>

      <!-- Excretion -->
      <div class="bg-white rounded-3xl shadow-lg border border-slate-100 overflow-hidden">
        <button
          @click="toggleSection('excretion')"
          class="w-full flex items-center justify-between p-5 hover:bg-blue-50 transition-colors"
        >
          <div class="flex items-center gap-3">
            <div class="bg-blue-100 p-2.5 rounded-2xl">
              <Droplets :size="20" class="text-blue-600" />
            </div>
            <div class="text-left">
              <p class="font-bold text-slate-800">Excretion Log</p>
              <p class="text-xs text-slate-400">
                Excretion Log · Today: {{ excretionLogs.length }} entries
              </p>
            </div>
          </div>
          <component
            :is="expandedSection === 'excretion' ? ChevronUp : ChevronDown"
            :size="20"
            class="text-slate-400"
          />
        </button>
        <div v-if="expandedSection === 'excretion'" class="px-5 pb-5 space-y-3">
          <div
            v-for="log in excretionLogs"
            :key="log.id"
            class="bg-slate-50 rounded-2xl p-4 flex items-start justify-between gap-3"
          >
            <div class="flex-1 space-y-1">
              <div class="flex items-center gap-2">
                <span class="text-lg">{{ parseData(log).type === 'urine' ? '💧' : '💩' }}</span>
                <span class="font-semibold text-slate-800">{{
                  parseData(log).type === 'urine' ? 'Urination' : 'Defecation'
                }}</span>
                <span
                  v-if="parseData(log).abnormal"
                  class="text-xs px-2 py-0.5 rounded-full bg-red-50 text-red-500 font-medium flex items-center gap-1"
                  ><AlertCircle :size="10" />Abnormal</span
                >
                <span
                  v-else
                  class="text-xs px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-500 font-medium flex items-center gap-1"
                  ><CheckCircle2 :size="10" />Normal</span
                >
              </div>
              <div class="flex gap-4 text-sm text-slate-500">
                <span class="flex items-center gap-1"
                  ><Clock :size="12" />{{ parseData(log).time }}</span
                >
                <span v-if="parseData(log).color">Color: {{ parseData(log).color }}</span>
              </div>
            </div>
            <button
              @click="deleteLog(log.id!, 'EXCRETION')"
              class="text-slate-300 hover:text-red-400 mt-1"
            >
              <Trash2 :size="16" />
            </button>
          </div>
          <div
            v-if="showExcretionForm"
            class="bg-blue-50 rounded-2xl p-4 space-y-3 border border-blue-100"
          >
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="text-xs font-semibold text-slate-600 mb-1 block">Time</label
                ><input
                  type="time"
                  v-model="newExcretion.time"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300 bg-white"
                />
              </div>
              <div>
                <label class="text-xs font-semibold text-slate-600 mb-1 block">Type</label>
                <div class="flex gap-2 h-[38px]">
                  <button
                    @click="newExcretion.type = 'urine'"
                    class="flex-1 rounded-xl text-sm font-semibold border-2 transition-all"
                    :class="
                      newExcretion.type === 'urine'
                        ? 'border-blue-400 bg-blue-400 text-white'
                        : 'border-slate-200 bg-white text-slate-600'
                    "
                  >
                    💧 Urination
                  </button>
                  <button
                    @click="newExcretion.type = 'defecation'"
                    class="flex-1 rounded-xl text-sm font-semibold border-2 transition-all"
                    :class="
                      newExcretion.type === 'defecation'
                        ? 'border-blue-400 bg-blue-400 text-white'
                        : 'border-slate-200 bg-white text-slate-600'
                    "
                  >
                    💩 Defecation
                  </button>
                </div>
              </div>
              <div>
                <label class="text-xs font-semibold text-slate-600 mb-1 block">Color</label
                ><input
                  type="text"
                  v-model="newExcretion.color"
                  placeholder="e.g. light yellow"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300 bg-white"
                />
              </div>
              <div class="flex items-end">
                <button
                  @click="newExcretion.abnormal = !newExcretion.abnormal"
                  class="w-full flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl text-sm font-semibold border-2 transition-all"
                  :class="
                    newExcretion.abnormal
                      ? 'border-red-400 bg-red-50 text-red-500'
                      : 'border-slate-200 text-slate-500 bg-white'
                  "
                >
                  <component
                    :is="newExcretion.abnormal ? AlertCircle : CheckCircle2"
                    :size="14"
                  />{{ newExcretion.abnormal ? 'Abnormal' : 'Normal' }}
                </button>
              </div>
            </div>
            <div class="flex gap-2">
              <button
                @click="addExcretion"
                :disabled="excretionSubmitting"
                class="flex-1 bg-blue-500 hover:bg-blue-600 disabled:bg-blue-300 text-white font-semibold py-2 rounded-xl text-sm flex items-center justify-center gap-2"
              >
                <Loader2 v-if="excretionSubmitting" :size="14" class="animate-spin" />{{
                  excretionSubmitting ? 'Saving...' : '✓ Save'
                }}
              </button>
              <button
                @click="showExcretionForm = false"
                class="px-4 py-2 rounded-xl border border-slate-200 text-slate-500 text-sm hover:bg-slate-100"
              >
                Cancel
              </button>
            </div>
          </div>
          <button
            v-if="!showExcretionForm"
            @click="showExcretionForm = true"
            class="w-full flex items-center justify-center gap-2 py-3 border-2 border-dashed border-blue-200 rounded-2xl text-blue-500 hover:bg-blue-50 text-sm font-semibold"
          >
            <Plus :size="16" /> Add Entry
          </button>
        </div>
      </div>

      <!-- Sleep -->
      <div class="bg-white rounded-3xl shadow-lg border border-slate-100 overflow-hidden">
        <button
          @click="toggleSection('sleep')"
          class="w-full flex items-center justify-between p-5 hover:bg-indigo-50 transition-colors"
        >
          <div class="flex items-center gap-3">
            <div class="bg-indigo-100 p-2.5 rounded-2xl">
              <Moon :size="20" class="text-indigo-600" />
            </div>
            <div class="text-left">
              <p class="font-bold text-slate-800">Sleep Log</p>
              <p class="text-xs text-slate-400">Sleep Tracking · Today: {{ sleepLogs.length }} entries</p>
            </div>
          </div>
          <component
            :is="expandedSection === 'sleep' ? ChevronUp : ChevronDown"
            :size="20"
            class="text-slate-400"
          />
        </button>
        <div v-if="expandedSection === 'sleep'" class="px-5 pb-5 space-y-3">
          <div
            v-for="log in sleepLogs"
            :key="log.id"
            class="bg-slate-50 rounded-2xl p-4 flex items-start justify-between gap-3"
          >
            <div class="flex-1 space-y-1">
              <div class="flex items-center gap-3">
                <span class="font-semibold text-slate-800"
                  >🌙 {{ parseData(log).bedtime }} → ☀️ {{ parseData(log).wakeTime }}</span
                >
                <span
                  class="text-xs px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-600 font-medium"
                  >{{ sleepDuration(parseData(log).bedtime, parseData(log).wakeTime) }}</span
                >
              </div>
              <span class="text-amber-400 text-sm">{{ qualityStars(parseData(log).quality) }}</span>
              <p v-if="parseData(log).note" class="text-xs text-slate-400 italic">
                {{ parseData(log).note }}
              </p>
            </div>
            <button
              @click="deleteLog(log.id!, 'SLEEP')"
              class="text-slate-300 hover:text-red-400 mt-1"
            >
              <Trash2 :size="16" />
            </button>
          </div>
          <div
            v-if="showSleepForm"
            class="bg-indigo-50 rounded-2xl p-4 space-y-3 border border-indigo-100"
          >
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="text-xs font-semibold text-slate-600 mb-1 block">Bedtime</label
                ><input
                  type="time"
                  v-model="newSleep.bedtime"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 bg-white"
                />
              </div>
              <div>
                <label class="text-xs font-semibold text-slate-600 mb-1 block">Wake Time</label
                ><input
                  type="time"
                  v-model="newSleep.wakeTime"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 bg-white"
                />
              </div>
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-2 block"
                >Sleep Quality ({{ newSleep.quality }}/5)</label
              ><input
                type="range"
                v-model.number="newSleep.quality"
                min="1"
                max="5"
                step="1"
                class="w-full accent-indigo-500"
              />
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block">Notes</label
              ><input
                type="text"
                v-model="newSleep.note"
                placeholder="Optional notes..."
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 bg-white"
              />
            </div>
            <div class="flex gap-2">
              <button
                @click="addSleep"
                :disabled="sleepSubmitting"
                class="flex-1 bg-indigo-500 hover:bg-indigo-600 disabled:bg-indigo-300 text-white font-semibold py-2 rounded-xl text-sm flex items-center justify-center gap-2"
              >
                <Loader2 v-if="sleepSubmitting" :size="14" class="animate-spin" />{{
                  sleepSubmitting ? 'Saving...' : '✓ Save'
                }}
              </button>
              <button
                @click="showSleepForm = false"
                class="px-4 py-2 rounded-xl border border-slate-200 text-slate-500 text-sm hover:bg-slate-100"
              >
                Cancel
              </button>
            </div>
          </div>
          <button
            v-if="!showSleepForm"
            @click="showSleepForm = true"
            class="w-full flex items-center justify-center gap-2 py-3 border-2 border-dashed border-indigo-200 rounded-2xl text-indigo-500 hover:bg-indigo-50 text-sm font-semibold"
          >
            <Plus :size="16" /> Add Entry
          </button>
        </div>
      </div>

      <!-- Activity -->
      <div class="bg-white rounded-3xl shadow-lg border border-slate-100 overflow-hidden">
        <button
          @click="toggleSection('activity')"
          class="w-full flex items-center justify-between p-5 hover:bg-amber-50 transition-colors"
        >
          <div class="flex items-center gap-3">
            <div class="bg-amber-100 p-2.5 rounded-2xl">
              <Activity :size="20" class="text-amber-600" />
            </div>
            <div class="text-left">
              <p class="font-bold text-slate-800">Activity & Behavior</p>
              <p class="text-xs text-slate-400">
                Activity & Behavior · Today: {{ activityLogs.length }} entries
              </p>
            </div>
          </div>
          <component
            :is="expandedSection === 'activity' ? ChevronUp : ChevronDown"
            :size="20"
            class="text-slate-400"
          />
        </button>
        <div v-if="expandedSection === 'activity'" class="px-5 pb-5 space-y-3">
          <div
            v-for="log in activityLogs"
            :key="log.id"
            class="bg-slate-50 rounded-2xl p-4 flex items-start justify-between gap-3"
          >
            <div class="flex-1 space-y-1">
              <div class="flex items-center gap-2 flex-wrap">
                <span class="font-semibold text-slate-800"
                  >🏃 {{ parseData(log).type || 'Activity not specified' }}</span
                >
                <span
                  class="text-xs px-2 py-0.5 rounded-full bg-amber-50 text-amber-600 font-medium"
                  >{{ parseData(log).duration }} min</span
                >
                <span
                  class="text-xs px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 font-medium"
                  >Energy: {{ energyLabel[parseData(log).energyLevel] }}</span
                >
              </div>
              <p v-if="parseData(log).note" class="text-xs text-slate-400 italic">
                {{ parseData(log).note }}
              </p>
            </div>
            <button
              @click="deleteLog(log.id!, 'ACTIVITY')"
              class="text-slate-300 hover:text-red-400 mt-1"
            >
              <Trash2 :size="16" />
            </button>
          </div>
          <div
            v-if="showActivityForm"
            class="bg-amber-50 rounded-2xl p-4 space-y-3 border border-amber-100"
          >
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="text-xs font-semibold text-slate-600 mb-1 block">Activity Type</label
                ><input
                  type="text"
                  v-model="newActivity.type"
                  placeholder="e.g. Walking"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-300 bg-white"
                />
              </div>
              <div>
                <label class="text-xs font-semibold text-slate-600 mb-1 block">Duration (min)</label
                ><input
                  type="number"
                  v-model.number="newActivity.duration"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-300 bg-white"
                />
              </div>
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-2 block"
                >Energy Level: {{ energyLabel[newActivity.energyLevel] }}</label
              ><input
                type="range"
                v-model.number="newActivity.energyLevel"
                min="1"
                max="5"
                step="1"
                class="w-full accent-amber-500"
              />
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-2 block">Abnormal Behaviors</label>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="b in behaviorOptions"
                  :key="b"
                  @click="toggleBehavior(b)"
                  class="text-xs px-3 py-1.5 rounded-xl border-2 font-medium transition-all"
                  :class="
                    newActivity.behaviors.includes(b)
                      ? 'border-red-400 bg-red-50 text-red-500'
                      : 'border-slate-200 bg-white text-slate-500'
                  "
                >
                  {{ b }}
                </button>
              </div>
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block">Notes</label
              ><input
                type="text"
                v-model="newActivity.note"
                placeholder="Optional notes..."
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-300 bg-white"
              />
            </div>
            <div class="flex gap-2">
              <button
                @click="addActivity"
                :disabled="activitySubmitting"
                class="flex-1 bg-amber-500 hover:bg-amber-600 disabled:bg-amber-300 text-white font-semibold py-2 rounded-xl text-sm flex items-center justify-center gap-2"
              >
                <Loader2 v-if="activitySubmitting" :size="14" class="animate-spin" />{{
                  activitySubmitting ? 'Saving...' : '✓ Save'
                }}
              </button>
              <button
                @click="showActivityForm = false"
                class="px-4 py-2 rounded-xl border border-slate-200 text-slate-500 text-sm hover:bg-slate-100"
              >
                Cancel
              </button>
            </div>
          </div>
          <button
            v-if="!showActivityForm"
            @click="showActivityForm = true"
            class="w-full flex items-center justify-center gap-2 py-3 border-2 border-dashed border-amber-200 rounded-2xl text-amber-500 hover:bg-amber-50 text-sm font-semibold"
          >
            <Plus :size="16" /> Add Entry
          </button>
        </div>
      </div>

      <!-- Mood -->
      <div class="bg-white rounded-3xl shadow-lg border border-slate-100 overflow-hidden">
        <button
          @click="toggleSection('mood')"
          class="w-full flex items-center justify-between p-5 hover:bg-rose-50 transition-colors"
        >
          <div class="flex items-center gap-3">
            <div class="bg-rose-100 p-2.5 rounded-2xl">
              <Heart :size="20" class="text-rose-600" />
            </div>
            <div class="text-left">
              <p class="font-bold text-slate-800">Mood & Emotions</p>
              <p class="text-xs text-slate-400">Emotional State · Today: {{ moodLogs.length }} entries</p>
            </div>
          </div>
          <component
            :is="expandedSection === 'mood' ? ChevronUp : ChevronDown"
            :size="20"
            class="text-slate-400"
          />
        </button>
        <div v-if="expandedSection === 'mood'" class="px-5 pb-5 space-y-3">
          <div
            v-for="log in moodLogs"
            :key="log.id"
            class="rounded-2xl p-4 flex items-start justify-between gap-3 border"
            :class="moodConfig[parseData(log).mood]?.bg"
          >
            <div class="flex-1 space-y-1">
              <div class="flex items-center gap-2">
                <span class="text-2xl">{{ moodConfig[parseData(log).mood]?.emoji }}</span>
                <span class="font-semibold" :class="moodConfig[parseData(log).mood]?.color">{{
                  moodConfig[parseData(log).mood]?.label
                }}</span>
                <span class="text-xs text-slate-400">Intensity {{ parseData(log).intensity }}/3</span>
              </div>
              <p v-if="parseData(log).note" class="text-xs text-slate-500 italic">
                {{ parseData(log).note }}
              </p>
            </div>
            <button
              @click="deleteLog(log.id!, 'MOOD')"
              class="text-slate-300 hover:text-red-400 mt-1"
            >
              <Trash2 :size="16" />
            </button>
          </div>
          <div
            v-if="showMoodForm"
            class="bg-rose-50 rounded-2xl p-4 space-y-3 border border-rose-100"
          >
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-2 block">Mood</label>
              <div class="grid grid-cols-5 gap-2">
                <button
                  v-for="(cfg, key) in moodConfig"
                  :key="key"
                  @click="newMood.mood = key"
                  class="flex flex-col items-center py-2 rounded-xl border-2 transition-all"
                  :class="
                    newMood.mood === key
                      ? 'border-rose-400 bg-white shadow-md'
                      : 'border-slate-200 bg-white hover:border-rose-200'
                  "
                >
                  <span class="text-xl">{{ cfg.emoji }}</span>
                  <span class="text-[9px] text-slate-500 mt-0.5">{{
                    cfg.label.split(' ')[0]
                  }}</span>
                </button>
              </div>
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-2 block"
                >Intensity: {{ newMood.intensity }}/3</label
              ><input
                type="range"
                v-model.number="newMood.intensity"
                min="1"
                max="3"
                step="1"
                class="w-full accent-rose-400"
              />
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block">Notes</label
              ><input
                type="text"
                v-model="newMood.note"
                placeholder="Optional notes..."
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-300 bg-white"
              />
            </div>
            <div class="flex gap-2">
              <button
                @click="addMood"
                :disabled="moodSubmitting"
                class="flex-1 bg-rose-500 hover:bg-rose-600 disabled:bg-rose-300 text-white font-semibold py-2 rounded-xl text-sm flex items-center justify-center gap-2"
              >
                <Loader2 v-if="moodSubmitting" :size="14" class="animate-spin" />{{
                  moodSubmitting ? 'Saving...' : '✓ Save'
                }}
              </button>
              <button
                @click="showMoodForm = false"
                class="px-4 py-2 rounded-xl border border-slate-200 text-slate-500 text-sm hover:bg-slate-100"
              >
                Cancel
              </button>
            </div>
          </div>
          <button
            v-if="!showMoodForm"
            @click="showMoodForm = true"
            class="w-full flex items-center justify-center gap-2 py-3 border-2 border-dashed border-rose-200 rounded-2xl text-rose-500 hover:bg-rose-50 text-sm font-semibold"
          >
            <Plus :size="16" /> Add Entry
          </button>
        </div>
      </div>
    </template>
  </div>
</template>