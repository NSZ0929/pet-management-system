<script setup lang="ts">
import { ref } from 'vue'
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
} from 'lucide-vue-next'

defineProps<{
  headerTitle: string
}>()

// ─── 类型定义 ────────────────────────────────────────────────
interface FeedingEntry {
  id: number
  time: string
  foodType: string
  amount: number
  unit: string
  water: number
  appetite: 'great' | 'normal' | 'poor'
  note: string
}

interface ExcretionEntry {
  id: number
  time: string
  type: 'urine' | 'defecation'
  color: string
  consistency: string
  abnormal: boolean
  note: string
}

interface SleepEntry {
  id: number
  date: string
  bedtime: string
  wakeTime: string
  quality: 1 | 2 | 3 | 4 | 5
  note: string
}

interface ActivityEntry {
  id: number
  date: string
  type: string
  duration: number
  energyLevel: 1 | 2 | 3 | 4 | 5
  behaviors: string[]
  note: string
}

interface MoodEntry {
  id: number
  date: string
  mood: 'happy' | 'nervous' | 'stressed' | 'low' | 'neutral'
  intensity: 1 | 2 | 3
  triggers: string
  note: string
}

// ─── 状态：哪个模块展开 ─────────────────────────────────────
const expandedSection = ref<string | null>('feeding')
const toggleSection = (name: string) => {
  expandedSection.value = expandedSection.value === name ? null : name
}

// ─── 今日日期 ─────────────────────────────────────────────
const today: string = new Date().toISOString().split('T')[0] ?? ''
const now: string = new Date().toTimeString().slice(0, 5) ?? ''

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// D9. 饮食记录 Feeding Log
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const feedingLogs = ref<FeedingEntry[]>([
  {
    id: 1,
    time: '08:00',
    foodType: '皇家成犬粮 Royal Canin',
    amount: 120,
    unit: 'g',
    water: 200,
    appetite: 'great',
    note: '今天胃口很好',
  },
])
const showFeedingForm = ref(false)
const newFeeding = ref<Omit<FeedingEntry, 'id'>>({
  time: now,
  foodType: '',
  amount: 0,
  unit: 'g',
  water: 0,
  appetite: 'normal',
  note: '',
})

const addFeeding = () => {
  feedingLogs.value.push({ ...newFeeding.value, id: Date.now() })
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
}
const deleteFeeding = (id: number) => {
  feedingLogs.value = feedingLogs.value.filter((e) => e.id !== id)
}

const appetiteLabel: Record<string, { label: string; color: string }> = {
  great: { label: '胃口极好 Great', color: 'text-emerald-600 bg-emerald-50' },
  normal: { label: '正常 Normal', color: 'text-blue-600 bg-blue-50' },
  poor: { label: '食欲不振 Poor', color: 'text-amber-600 bg-amber-50' },
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// D10. 排泄记录 Excretion Log
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const excretionLogs = ref<ExcretionEntry[]>([
  {
    id: 1,
    time: '07:30',
    type: 'urine',
    color: '淡黄色',
    consistency: '',
    abnormal: false,
    note: '',
  },
  {
    id: 2,
    time: '09:00',
    type: 'defecation',
    color: '棕色',
    consistency: '成形',
    abnormal: false,
    note: '',
  },
])
const showExcretionForm = ref(false)
const newExcretion = ref<Omit<ExcretionEntry, 'id'>>({
  time: now,
  type: 'urine',
  color: '',
  consistency: '',
  abnormal: false,
  note: '',
})
const addExcretion = () => {
  excretionLogs.value.push({ ...newExcretion.value, id: Date.now() })
  showExcretionForm.value = false
  newExcretion.value = {
    time: now,
    type: 'urine',
    color: '',
    consistency: '',
    abnormal: false,
    note: '',
  }
}
const deleteExcretion = (id: number) => {
  excretionLogs.value = excretionLogs.value.filter((e) => e.id !== id)
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// D11. 睡眠记录 Sleep Tracking
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const sleepLogs = ref<SleepEntry[]>([
  { id: 1, date: today, bedtime: '22:00', wakeTime: '06:30', quality: 4, note: '睡得很踏实' },
])
const showSleepForm = ref(false)
const newSleep = ref<Omit<SleepEntry, 'id'>>({
  date: today,
  bedtime: '',
  wakeTime: '',
  quality: 3,
  note: '',
})
const addSleep = () => {
  sleepLogs.value.push({ ...newSleep.value, id: Date.now() })
  showSleepForm.value = false
  newSleep.value = { date: today, bedtime: '', wakeTime: '', quality: 3, note: '' }
}
const deleteSleep = (id: number) => {
  sleepLogs.value = sleepLogs.value.filter((e) => e.id !== id)
}
const sleepDuration = (entry: SleepEntry): string => {
  const bedParts = entry.bedtime.split(':').map(Number)
  const wakeParts = entry.wakeTime.split(':').map(Number)
  const bh = bedParts[0] ?? 0
  const bm = bedParts[1] ?? 0
  const wh = wakeParts[0] ?? 0
  const wm = wakeParts[1] ?? 0
  let mins = wh * 60 + wm - (bh * 60 + bm)
  if (mins < 0) mins += 1440
  return `${Math.floor(mins / 60)}h ${mins % 60}m`
}
const qualityStars = (q: number) => '★'.repeat(q) + '☆'.repeat(5 - q)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// D12. 运动/行为监测 Activity & Behavior
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const activityLogs = ref<ActivityEntry[]>([
  {
    id: 1,
    date: today,
    type: '散步 Walk',
    duration: 30,
    energyLevel: 4,
    behaviors: [],
    note: '',
  },
])
const showActivityForm = ref(false)
const newActivity = ref<Omit<ActivityEntry, 'id'>>({
  date: today,
  type: '',
  duration: 0,
  energyLevel: 3,
  behaviors: [],
  note: '',
})
const behaviorOptions = [
  '抓挠 Scratching',
  '过度舔舐 Licking',
  '悲鸣 Whining',
  '颤抖 Trembling',
  '攻击性 Aggressive',
  '食异物 Pica',
]
const toggleBehavior = (b: string) => {
  const idx = newActivity.value.behaviors.indexOf(b)
  if (idx === -1) {
    newActivity.value.behaviors.push(b)
  } else {
    newActivity.value.behaviors.splice(idx, 1)
  }
}
const addActivity = () => {
  activityLogs.value.push({
    ...newActivity.value,
    behaviors: [...newActivity.value.behaviors],
    id: Date.now(),
  })
  showActivityForm.value = false
  newActivity.value = {
    date: today,
    type: '',
    duration: 0,
    energyLevel: 3,
    behaviors: [],
    note: '',
  }
}
const deleteActivity = (id: number) => {
  activityLogs.value = activityLogs.value.filter((e) => e.id !== id)
}
const energyLabel: Record<number, string> = {
  1: '很低 Very Low',
  2: '偏低 Low',
  3: '正常 Normal',
  4: '活跃 Active',
  5: '极度活跃 Hyperactive',
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// D13. 心情情绪 Emotional State
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const moodLogs = ref<MoodEntry[]>([
  { id: 1, date: today, mood: 'happy', intensity: 2, triggers: '', note: '今天很开心！' },
])
const showMoodForm = ref(false)
const newMood = ref<Omit<MoodEntry, 'id'>>({
  date: today,
  mood: 'neutral',
  intensity: 2,
  triggers: '',
  note: '',
})
const addMood = () => {
  moodLogs.value.push({ ...newMood.value, id: Date.now() })
  showMoodForm.value = false
  newMood.value = { date: today, mood: 'neutral', intensity: 2, triggers: '', note: '' }
}
const deleteMood = (id: number) => {
  moodLogs.value = moodLogs.value.filter((e) => e.id !== id)
}
const moodConfig: Record<string, { emoji: string; label: string; color: string; bg: string }> = {
  happy: {
    emoji: '😊',
    label: '开心 Happy',
    color: 'text-yellow-600',
    bg: 'bg-yellow-50 border-yellow-200',
  },
  nervous: {
    emoji: '😰',
    label: '紧张 Nervous',
    color: 'text-purple-600',
    bg: 'bg-purple-50 border-purple-200',
  },
  stressed: {
    emoji: '😖',
    label: '压力大 Stressed',
    color: 'text-red-600',
    bg: 'bg-red-50 border-red-200',
  },
  low: {
    emoji: '😔',
    label: '低落 Low mood',
    color: 'text-slate-600',
    bg: 'bg-slate-50 border-slate-200',
  },
  neutral: {
    emoji: '😐',
    label: '平静 Neutral',
    color: 'text-teal-600',
    bg: 'bg-teal-50 border-teal-200',
  },
}

// ─── 模块配置 ──────────────────────────────────────────────
const sections = [
  {
    key: 'feeding',
    icon: Utensils,
    label: '饮食记录',
    sublabel: 'Feeding Log',
    color: 'teal',
    count: () => feedingLogs.value.length,
  },
  {
    key: 'excretion',
    icon: Droplets,
    label: '排泄记录',
    sublabel: 'Excretion Log',
    color: 'blue',
    count: () => excretionLogs.value.length,
  },
  {
    key: 'sleep',
    icon: Moon,
    label: '睡眠记录',
    sublabel: 'Sleep Tracking',
    color: 'indigo',
    count: () => sleepLogs.value.length,
  },
  {
    key: 'activity',
    icon: Activity,
    label: '运动监测',
    sublabel: 'Activity',
    color: 'amber',
    count: () => activityLogs.value.length,
  },
  {
    key: 'mood',
    icon: Heart,
    label: '心情情绪',
    sublabel: 'Emotional State',
    color: 'rose',
    count: () => moodLogs.value.length,
  },
]
</script>

<template>
  <div class="space-y-4 max-w-4xl mx-auto">
    <!-- 页头 -->
    <div
      class="bg-gradient-to-r from-teal-500 to-cyan-500 rounded-3xl p-6 text-white shadow-lg shadow-teal-200"
    >
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-extrabold tracking-tight">🐾 日常记录</h2>
          <p class="text-teal-100 text-sm mt-1">Daily Life Tracking · {{ today }}</p>
        </div>
        <div class="text-right">
          <p class="text-3xl font-black">{{ sections.length }}</p>
          <p class="text-teal-100 text-xs">监测模块 Modules</p>
        </div>
      </div>
      <!-- 快捷统计 -->
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

    <!-- ━━━ D9 饮食记录 ━━━ -->
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
            <p class="font-bold text-slate-800">饮食记录</p>
            <p class="text-xs text-slate-400">Feeding Log · 今日 {{ feedingLogs.length }} 条</p>
          </div>
        </div>
        <component
          :is="expandedSection === 'feeding' ? ChevronUp : ChevronDown"
          :size="20"
          class="text-slate-400"
        />
      </button>

      <div v-if="expandedSection === 'feeding'" class="px-5 pb-5 space-y-3">
        <!-- 记录列表 -->
        <div
          v-for="entry in feedingLogs"
          :key="entry.id"
          class="bg-slate-50 rounded-2xl p-4 flex items-start justify-between gap-3"
        >
          <div class="flex-1 space-y-1">
            <div class="flex items-center gap-2 flex-wrap">
              <span class="font-semibold text-slate-800">{{ entry.foodType || '未填写食物' }}</span>
              <span
                class="text-xs px-2 py-0.5 rounded-full font-medium"
                :class="appetiteLabel[entry.appetite]?.color"
              >
                {{ appetiteLabel[entry.appetite]?.label }}
              </span>
            </div>
            <div class="flex gap-4 text-sm text-slate-500">
              <span class="flex items-center gap-1"><Clock :size="12" />{{ entry.time }}</span>
              <span>🍖 {{ entry.amount }}{{ entry.unit }}</span>
              <span>💧 {{ entry.water }}ml</span>
            </div>
            <p v-if="entry.note" class="text-xs text-slate-400 italic">{{ entry.note }}</p>
          </div>
          <button
            @click="deleteFeeding(entry.id)"
            class="text-slate-300 hover:text-red-400 transition-colors mt-1"
          >
            <Trash2 :size="16" />
          </button>
        </div>

        <!-- 添加表单 -->
        <div
          v-if="showFeedingForm"
          class="bg-teal-50 rounded-2xl p-4 space-y-3 border border-teal-100"
        >
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block">时间 Time</label>
              <input
                type="time"
                v-model="newFeeding.time"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-300 bg-white"
              />
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block"
                >食物种类 Food Type</label
              >
              <input
                type="text"
                v-model="newFeeding.foodType"
                placeholder="e.g. 皇家成犬粮"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-300 bg-white"
              />
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block">饭量 Amount (g)</label>
              <input
                type="number"
                v-model.number="newFeeding.amount"
                placeholder="120"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-300 bg-white"
              />
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block"
                >饮水量 Water (ml)</label
              >
              <input
                type="number"
                v-model.number="newFeeding.water"
                placeholder="200"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-300 bg-white"
              />
            </div>
          </div>
          <div>
            <label class="text-xs font-semibold text-slate-600 mb-1 block">胃口 Appetite</label>
            <div class="flex gap-2">
              <button
                v-for="opt in ['great', 'normal', 'poor']"
                :key="opt"
                @click="newFeeding.appetite = opt as any"
                class="flex-1 py-2 rounded-xl text-xs font-semibold border-2 transition-all"
                :class="
                  newFeeding.appetite === opt
                    ? 'border-teal-500 bg-teal-500 text-white'
                    : 'border-slate-200 text-slate-600 bg-white hover:border-teal-300'
                "
              >
                {{ opt === 'great' ? '😋 极好' : opt === 'normal' ? '😊 正常' : '😞 不振' }}
              </button>
            </div>
          </div>
          <div>
            <label class="text-xs font-semibold text-slate-600 mb-1 block">备注 Note</label>
            <input
              type="text"
              v-model="newFeeding.note"
              placeholder="可选备注..."
              class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-300 bg-white"
            />
          </div>
          <div class="flex gap-2">
            <button
              @click="addFeeding"
              class="flex-1 bg-teal-500 hover:bg-teal-600 text-white font-semibold py-2 rounded-xl text-sm transition-colors"
            >
              ✓ 保存 Save
            </button>
            <button
              @click="showFeedingForm = false"
              class="px-4 py-2 rounded-xl border border-slate-200 text-slate-500 text-sm hover:bg-slate-100 transition-colors"
            >
              取消
            </button>
          </div>
        </div>

        <button
          v-if="!showFeedingForm"
          @click="showFeedingForm = true"
          class="w-full flex items-center justify-center gap-2 py-3 border-2 border-dashed border-teal-200 rounded-2xl text-teal-500 hover:border-teal-400 hover:bg-teal-50 transition-all text-sm font-semibold"
        >
          <Plus :size="16" /> 添加记录 Add Entry
        </button>
      </div>
    </div>

    <!-- ━━━ D10 排泄记录 ━━━ -->
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
            <p class="font-bold text-slate-800">排泄记录</p>
            <p class="text-xs text-slate-400">Excretion Log · 今日 {{ excretionLogs.length }} 条</p>
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
          v-for="entry in excretionLogs"
          :key="entry.id"
          class="bg-slate-50 rounded-2xl p-4 flex items-start justify-between gap-3"
        >
          <div class="flex-1 space-y-1">
            <div class="flex items-center gap-2">
              <span class="text-lg">{{ entry.type === 'urine' ? '💧' : '💩' }}</span>
              <span class="font-semibold text-slate-800">
                {{ entry.type === 'urine' ? '小便 Urine' : '大便 Defecation' }}
              </span>
              <span
                v-if="entry.abnormal"
                class="flex items-center gap-1 text-xs px-2 py-0.5 rounded-full bg-red-50 text-red-500 font-medium"
              >
                <AlertCircle :size="10" /> 异常 Abnormal
              </span>
              <span
                v-else
                class="flex items-center gap-1 text-xs px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-500 font-medium"
              >
                <CheckCircle2 :size="10" /> 正常 Normal
              </span>
            </div>
            <div class="flex gap-4 text-sm text-slate-500">
              <span class="flex items-center gap-1"><Clock :size="12" />{{ entry.time }}</span>
              <span v-if="entry.color">颜色: {{ entry.color }}</span>
              <span v-if="entry.consistency">形状: {{ entry.consistency }}</span>
            </div>
            <p v-if="entry.note" class="text-xs text-slate-400 italic">{{ entry.note }}</p>
          </div>
          <button
            @click="deleteExcretion(entry.id)"
            class="text-slate-300 hover:text-red-400 transition-colors mt-1"
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
              <label class="text-xs font-semibold text-slate-600 mb-1 block">时间 Time</label>
              <input
                type="time"
                v-model="newExcretion.time"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300 bg-white"
              />
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block">类型 Type</label>
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
                  💧 小便
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
                  💩 大便
                </button>
              </div>
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block">颜色 Color</label>
              <input
                type="text"
                v-model="newExcretion.color"
                placeholder="e.g. 淡黄色"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300 bg-white"
              />
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block">形状/稠度</label>
              <input
                type="text"
                v-model="newExcretion.consistency"
                placeholder="e.g. 成形"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300 bg-white"
              />
            </div>
          </div>
          <div class="flex items-center gap-3">
            <label class="text-xs font-semibold text-slate-600">是否异常 Abnormal?</label>
            <button
              @click="newExcretion.abnormal = !newExcretion.abnormal"
              class="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border-2 transition-all"
              :class="
                newExcretion.abnormal
                  ? 'border-red-400 bg-red-50 text-red-500'
                  : 'border-slate-200 text-slate-500 bg-white'
              "
            >
              <component :is="newExcretion.abnormal ? AlertCircle : CheckCircle2" :size="14" />
              {{ newExcretion.abnormal ? '异常' : '正常' }}
            </button>
          </div>
          <div>
            <label class="text-xs font-semibold text-slate-600 mb-1 block">备注 Note</label>
            <input
              type="text"
              v-model="newExcretion.note"
              placeholder="可选备注..."
              class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-300 bg-white"
            />
          </div>
          <div class="flex gap-2">
            <button
              @click="addExcretion"
              class="flex-1 bg-blue-500 hover:bg-blue-600 text-white font-semibold py-2 rounded-xl text-sm transition-colors"
            >
              ✓ 保存 Save
            </button>
            <button
              @click="showExcretionForm = false"
              class="px-4 py-2 rounded-xl border border-slate-200 text-slate-500 text-sm hover:bg-slate-100 transition-colors"
            >
              取消
            </button>
          </div>
        </div>

        <button
          v-if="!showExcretionForm"
          @click="showExcretionForm = true"
          class="w-full flex items-center justify-center gap-2 py-3 border-2 border-dashed border-blue-200 rounded-2xl text-blue-500 hover:border-blue-400 hover:bg-blue-50 transition-all text-sm font-semibold"
        >
          <Plus :size="16" /> 添加记录 Add Entry
        </button>
      </div>
    </div>

    <!-- ━━━ D11 睡眠记录 ━━━ -->
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
            <p class="font-bold text-slate-800">睡眠记录</p>
            <p class="text-xs text-slate-400">Sleep Tracking · 今日 {{ sleepLogs.length }} 条</p>
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
          v-for="entry in sleepLogs"
          :key="entry.id"
          class="bg-slate-50 rounded-2xl p-4 flex items-start justify-between gap-3"
        >
          <div class="flex-1 space-y-1">
            <div class="flex items-center gap-3">
              <span class="font-semibold text-slate-800"
                >🌙 {{ entry.bedtime }} → ☀️ {{ entry.wakeTime }}</span
              >
              <span
                class="text-xs px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-600 font-medium"
              >
                {{ sleepDuration(entry) }}
              </span>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-amber-400 text-sm">{{ qualityStars(entry.quality) }}</span>
              <span class="text-xs text-slate-400">睡眠质量 Sleep Quality</span>
            </div>
            <p v-if="entry.note" class="text-xs text-slate-400 italic">{{ entry.note }}</p>
          </div>
          <button
            @click="deleteSleep(entry.id)"
            class="text-slate-300 hover:text-red-400 transition-colors mt-1"
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
              <label class="text-xs font-semibold text-slate-600 mb-1 block"
                >入睡时间 Bedtime</label
              >
              <input
                type="time"
                v-model="newSleep.bedtime"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 bg-white"
              />
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block"
                >醒来时间 Wake Time</label
              >
              <input
                type="time"
                v-model="newSleep.wakeTime"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 bg-white"
              />
            </div>
          </div>
          <div>
            <label class="text-xs font-semibold text-slate-600 mb-2 block"
              >睡眠质量 Quality ({{ newSleep.quality }}/5)</label
            >
            <input
              type="range"
              v-model.number="newSleep.quality"
              min="1"
              max="5"
              step="1"
              class="w-full accent-indigo-500"
            />
            <div class="flex justify-between text-xs text-slate-400 mt-1">
              <span>差 Poor</span><span>好 Great</span>
            </div>
          </div>
          <div>
            <label class="text-xs font-semibold text-slate-600 mb-1 block">备注 Note</label>
            <input
              type="text"
              v-model="newSleep.note"
              placeholder="可选备注..."
              class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 bg-white"
            />
          </div>
          <div class="flex gap-2">
            <button
              @click="addSleep"
              class="flex-1 bg-indigo-500 hover:bg-indigo-600 text-white font-semibold py-2 rounded-xl text-sm transition-colors"
            >
              ✓ 保存 Save
            </button>
            <button
              @click="showSleepForm = false"
              class="px-4 py-2 rounded-xl border border-slate-200 text-slate-500 text-sm hover:bg-slate-100 transition-colors"
            >
              取消
            </button>
          </div>
        </div>

        <button
          v-if="!showSleepForm"
          @click="showSleepForm = true"
          class="w-full flex items-center justify-center gap-2 py-3 border-2 border-dashed border-indigo-200 rounded-2xl text-indigo-500 hover:border-indigo-400 hover:bg-indigo-50 transition-all text-sm font-semibold"
        >
          <Plus :size="16" /> 添加记录 Add Entry
        </button>
      </div>
    </div>

    <!-- ━━━ D12 运动/行为监测 ━━━ -->
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
            <p class="font-bold text-slate-800">运动 / 行为监测</p>
            <p class="text-xs text-slate-400">
              Activity & Behavior · 今日 {{ activityLogs.length }} 条
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
          v-for="entry in activityLogs"
          :key="entry.id"
          class="bg-slate-50 rounded-2xl p-4 flex items-start justify-between gap-3"
        >
          <div class="flex-1 space-y-1">
            <div class="flex items-center gap-2 flex-wrap">
              <span class="font-semibold text-slate-800">🏃 {{ entry.type || '未填写活动' }}</span>
              <span class="text-xs px-2 py-0.5 rounded-full bg-amber-50 text-amber-600 font-medium">
                {{ entry.duration }} 分钟
              </span>
              <span
                class="text-xs px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 font-medium"
              >
                精力: {{ energyLabel[entry.energyLevel] }}
              </span>
            </div>
            <div v-if="entry.behaviors.length" class="flex gap-1 flex-wrap">
              <span
                v-for="b in entry.behaviors"
                :key="b"
                class="text-xs px-2 py-0.5 rounded-full bg-red-50 text-red-500 font-medium"
                >⚠ {{ b }}</span
              >
            </div>
            <p v-if="entry.note" class="text-xs text-slate-400 italic">{{ entry.note }}</p>
          </div>
          <button
            @click="deleteActivity(entry.id)"
            class="text-slate-300 hover:text-red-400 transition-colors mt-1"
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
              <label class="text-xs font-semibold text-slate-600 mb-1 block"
                >活动类型 Activity Type</label
              >
              <input
                type="text"
                v-model="newActivity.type"
                placeholder="e.g. 散步 Walk"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-300 bg-white"
              />
            </div>
            <div>
              <label class="text-xs font-semibold text-slate-600 mb-1 block"
                >时长 Duration (分钟)</label
              >
              <input
                type="number"
                v-model.number="newActivity.duration"
                placeholder="30"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-300 bg-white"
              />
            </div>
          </div>
          <div>
            <label class="text-xs font-semibold text-slate-600 mb-2 block">
              精力指数 Energy Level: {{ energyLabel[newActivity.energyLevel] }}
            </label>
            <input
              type="range"
              v-model.number="newActivity.energyLevel"
              min="1"
              max="5"
              step="1"
              class="w-full accent-amber-500"
            />
            <div class="flex justify-between text-xs text-slate-400 mt-1">
              <span>很低 Very Low</span><span>极高 Hyper</span>
            </div>
          </div>
          <div>
            <label class="text-xs font-semibold text-slate-600 mb-2 block"
              >异常行为 Abnormal Behaviors</label
            >
            <div class="flex flex-wrap gap-2">
              <button
                v-for="b in behaviorOptions"
                :key="b"
                @click="toggleBehavior(b)"
                class="text-xs px-3 py-1.5 rounded-xl border-2 font-medium transition-all"
                :class="
                  newActivity.behaviors.includes(b)
                    ? 'border-red-400 bg-red-50 text-red-500'
                    : 'border-slate-200 bg-white text-slate-500 hover:border-slate-300'
                "
              >
                {{ b }}
              </button>
            </div>
          </div>
          <div>
            <label class="text-xs font-semibold text-slate-600 mb-1 block">备注 Note</label>
            <input
              type="text"
              v-model="newActivity.note"
              placeholder="可选备注..."
              class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-300 bg-white"
            />
          </div>
          <div class="flex gap-2">
            <button
              @click="addActivity"
              class="flex-1 bg-amber-500 hover:bg-amber-600 text-white font-semibold py-2 rounded-xl text-sm transition-colors"
            >
              ✓ 保存 Save
            </button>
            <button
              @click="showActivityForm = false"
              class="px-4 py-2 rounded-xl border border-slate-200 text-slate-500 text-sm hover:bg-slate-100 transition-colors"
            >
              取消
            </button>
          </div>
        </div>

        <button
          v-if="!showActivityForm"
          @click="showActivityForm = true"
          class="w-full flex items-center justify-center gap-2 py-3 border-2 border-dashed border-amber-200 rounded-2xl text-amber-500 hover:border-amber-400 hover:bg-amber-50 transition-all text-sm font-semibold"
        >
          <Plus :size="16" /> 添加记录 Add Entry
        </button>
      </div>
    </div>

    <!-- ━━━ D13 心情情绪 ━━━ -->
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
            <p class="font-bold text-slate-800">心情情绪</p>
            <p class="text-xs text-slate-400">Emotional State · 今日 {{ moodLogs.length }} 条</p>
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
          v-for="entry in moodLogs"
          :key="entry.id"
          class="rounded-2xl p-4 flex items-start justify-between gap-3 border"
          :class="moodConfig[entry.mood]?.bg"
        >
          <div class="flex-1 space-y-1">
            <div class="flex items-center gap-2">
              <span class="text-2xl">{{ moodConfig[entry.mood]?.emoji }}</span>
              <span class="font-semibold" :class="moodConfig[entry.mood]?.color">
                {{ moodConfig[entry.mood]?.label }}
              </span>
              <span class="text-xs text-slate-400">强度 {{ entry.intensity }}/3</span>
            </div>
            <div class="text-xs text-slate-400 flex items-center gap-1">
              <Clock :size="12" />{{ entry.date }}
              <span v-if="entry.triggers" class="ml-2">触发: {{ entry.triggers }}</span>
            </div>
            <p v-if="entry.note" class="text-xs text-slate-500 italic">{{ entry.note }}</p>
          </div>
          <button
            @click="deleteMood(entry.id)"
            class="text-slate-300 hover:text-red-400 transition-colors mt-1"
          >
            <Trash2 :size="16" />
          </button>
        </div>

        <div
          v-if="showMoodForm"
          class="bg-rose-50 rounded-2xl p-4 space-y-3 border border-rose-100"
        >
          <div>
            <label class="text-xs font-semibold text-slate-600 mb-2 block">心情 Mood</label>
            <div class="grid grid-cols-5 gap-2">
              <button
                v-for="(cfg, key) in moodConfig"
                :key="key"
                @click="newMood.mood = key as any"
                class="flex flex-col items-center py-2 rounded-xl border-2 transition-all"
                :class="
                  newMood.mood === key
                    ? 'border-rose-400 bg-white shadow-md'
                    : 'border-slate-200 bg-white hover:border-rose-200'
                "
              >
                <span class="text-xl">{{ cfg.emoji }}</span>
                <span class="text-[9px] text-slate-500 mt-0.5 leading-tight text-center">{{
                  cfg.label.split(' ')[0]
                }}</span>
              </button>
            </div>
          </div>
          <div>
            <label class="text-xs font-semibold text-slate-600 mb-2 block"
              >强度 Intensity: {{ newMood.intensity }}/3</label
            >
            <input
              type="range"
              v-model.number="newMood.intensity"
              min="1"
              max="3"
              step="1"
              class="w-full accent-rose-400"
            />
            <div class="flex justify-between text-xs text-slate-400 mt-1">
              <span>轻微 Mild</span><span>强烈 Strong</span>
            </div>
          </div>
          <div>
            <label class="text-xs font-semibold text-slate-600 mb-1 block"
              >触发原因 Trigger (可选)</label
            >
            <input
              type="text"
              v-model="newMood.triggers"
              placeholder="e.g. 有陌生人来访"
              class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-300 bg-white"
            />
          </div>
          <div>
            <label class="text-xs font-semibold text-slate-600 mb-1 block">备注 Note</label>
            <input
              type="text"
              v-model="newMood.note"
              placeholder="可选备注..."
              class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-rose-300 bg-white"
            />
          </div>
          <div class="flex gap-2">
            <button
              @click="addMood"
              class="flex-1 bg-rose-500 hover:bg-rose-600 text-white font-semibold py-2 rounded-xl text-sm transition-colors"
            >
              ✓ 保存 Save
            </button>
            <button
              @click="showMoodForm = false"
              class="px-4 py-2 rounded-xl border border-slate-200 text-slate-500 text-sm hover:bg-slate-100 transition-colors"
            >
              取消
            </button>
          </div>
        </div>

        <button
          v-if="!showMoodForm"
          @click="showMoodForm = true"
          class="w-full flex items-center justify-center gap-2 py-3 border-2 border-dashed border-rose-200 rounded-2xl text-rose-500 hover:border-rose-400 hover:bg-rose-50 transition-all text-sm font-semibold"
        >
          <Plus :size="16" /> 添加记录 Add Entry
        </button>
      </div>
    </div>
  </div>
</template>
