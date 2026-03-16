<script setup lang="ts">
import {
  AlertCircle,
  Bell,
  Check,
  CheckCircle2,
  ChevronRight,
  Eye,
  EyeOff,
  Globe,
  Heart,
  Info,
  Lock,
  Mail,
  Moon,
  PawPrint,
  Shield,
  Smartphone,
  Sun,
  User,
} from 'lucide-vue-next';
import { computed, reactive, ref } from 'vue';

defineProps<{ headerTitle?: string }>()

// ── 当前激活的设置分区 ────────────────────────────────────
const activeSection = ref<'profile' | 'security' | 'notifications' | 'preferences' | 'about'>(
  'profile',
)

// ── 用户信息（从 localStorage 读取） ─────────────────────
const storedUser = JSON.parse(localStorage.getItem('user') || '{}')
const username = ref(storedUser.username || '未知用户')
const userRole = ref(storedUser.role || 'USER')

// ── 个人信息表单 ──────────────────────────────────────────
const profileForm = reactive({
  username: username.value,
})
const profileSaving = ref(false)
const profileSuccess = ref(false)
const profileError = ref('')

const saveProfile = async () => {
  profileSaving.value = true
  profileError.value = ''
  profileSuccess.value = false
  try {
    // 暂时只更新本地显示（后端暂无修改用户名接口）
    await new Promise((r) => setTimeout(r, 600))
    username.value = profileForm.username
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    user.username = profileForm.username
    localStorage.setItem('user', JSON.stringify(user))
    profileSuccess.value = true
    setTimeout(() => (profileSuccess.value = false), 3000)
  } catch {
    profileError.value = '保存失败，请稍后重试'
  } finally {
    profileSaving.value = false
  }
}

// ── 密码修改表单 ──────────────────────────────────────────
const passwordForm = reactive({
  current: '',
  newPwd: '',
  confirm: '',
})
const showPasswords = reactive({ current: false, new: false, confirm: false })
const passwordSaving = ref(false)
const passwordSuccess = ref(false)
const passwordError = ref('')

const passwordStrength = computed(() => {
  const p = passwordForm.newPwd
  if (!p) return 0
  let score = 0
  if (p.length >= 8) score++
  if (/[A-Z]/.test(p)) score++
  if (/[0-9]/.test(p)) score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  return score
})

const strengthLabel = computed(() => {
  const labels = ['', '弱', '一般', '较强', '强']
  return labels[passwordStrength.value]
})

const strengthColor = computed(() => {
  const colors = ['', 'bg-red-400', 'bg-amber-400', 'bg-teal-400', 'bg-green-500']
  return colors[passwordStrength.value]
})

const savePassword = async () => {
  passwordError.value = ''
  if (!passwordForm.current || !passwordForm.newPwd || !passwordForm.confirm) {
    passwordError.value = '请填写所有密码字段'
    return
  }
  if (passwordForm.newPwd !== passwordForm.confirm) {
    passwordError.value = '两次输入的新密码不一致'
    return
  }
  if (passwordForm.newPwd.length < 6) {
    passwordError.value = '新密码至少需要6位'
    return
  }
  passwordSaving.value = true
  try {
    // 后端暂无修改密码接口，模拟成功
    await new Promise((r) => setTimeout(r, 800))
    passwordSuccess.value = true
    passwordForm.current = ''
    passwordForm.newPwd = ''
    passwordForm.confirm = ''
    setTimeout(() => (passwordSuccess.value = false), 3000)
  } catch {
    passwordError.value = '密码修改失败，请稍后重试'
  } finally {
    passwordSaving.value = false
  }
}

// ── 通知设置 ──────────────────────────────────────────────
const notifications = reactive({
  appointmentReminder: true,
  vaccinationDue: true,
  medicationReminder: false,
  feedingReminder: true,
  emailNotify: false,
  pushNotify: true,
})

// ── 系统偏好 ──────────────────────────────────────────────
const preferences = reactive({
  theme: 'light' as 'light' | 'dark',
  language: 'zh-CN',
  compactMode: false,
})

// ── 关于应用 ──────────────────────────────────────────────
const appInfo = {
  name: 'PetCare',
  version: '1.0.0',
  description: '专业宠物健康管理系统',
  backend: 'Spring Boot 3.5.9',
  frontend: 'Vue 3 + TypeScript',
  database: 'MySQL 8.4',
}

// ── 导航菜单 ──────────────────────────────────────────────
const sections = [
  { key: 'profile', label: '个人信息', icon: User, color: 'text-teal-500', bg: 'bg-teal-50' },
  { key: 'security', label: '账号安全', icon: Lock, color: 'text-amber-500', bg: 'bg-amber-50' },
  { key: 'notifications', label: '通知设置', icon: Bell, color: 'text-blue-500', bg: 'bg-blue-50' },
  {
    key: 'preferences',
    label: '系统偏好',
    icon: Globe,
    color: 'text-purple-500',
    bg: 'bg-purple-50',
  },
  { key: 'about', label: '关于应用', icon: Info, color: 'text-slate-500', bg: 'bg-slate-100' },
] as const
</script>

<template>
  <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
    <!-- 左侧导航 -->
    <div class="lg:col-span-1 space-y-3">
      <!-- 用户头像卡片 -->
      <div class="bg-white rounded-3xl p-5 shadow-lg border border-slate-100 text-center">
        <div
          class="w-16 h-16 rounded-2xl bg-gradient-to-br from-teal-400 to-cyan-500 flex items-center justify-center mx-auto mb-3 shadow-lg shadow-teal-100"
        >
          <span class="text-2xl font-bold text-white">{{ username.charAt(0).toUpperCase() }}</span>
        </div>
        <p class="font-bold text-slate-800">{{ username }}</p>
        <span
          class="inline-block mt-1 text-xs font-bold px-2 py-0.5 bg-teal-50 text-teal-600 rounded-full"
        >
          {{ userRole }}
        </span>
      </div>

      <!-- 导航菜单 -->
      <div class="bg-white rounded-3xl p-3 shadow-lg border border-slate-100">
        <button
          v-for="section in sections"
          :key="section.key"
          @click="activeSection = section.key"
          :class="[
            'w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-bold transition-all',
            activeSection === section.key
              ? 'bg-teal-500 text-white shadow-md shadow-teal-100'
              : 'text-slate-600 hover:bg-slate-50',
          ]"
        >
          <div
            :class="[
              'w-7 h-7 rounded-lg flex items-center justify-center transition-all',
              activeSection === section.key ? 'bg-white/20' : section.bg,
            ]"
          >
            <component
              :is="section.icon"
              :size="15"
              :class="activeSection === section.key ? 'text-white' : section.color"
            />
          </div>
          {{ section.label }}
          <ChevronRight :size="14" class="ml-auto opacity-50" />
        </button>
      </div>
    </div>

    <!-- 右侧内容区 -->
    <div class="lg:col-span-3">
      <!-- 个人信息 -->
      <div
        v-if="activeSection === 'profile'"
        class="bg-white rounded-3xl p-7 shadow-lg border border-slate-100 space-y-6"
      >
        <div class="flex items-center gap-3 pb-4 border-b border-slate-100">
          <div class="w-9 h-9 rounded-xl bg-teal-50 flex items-center justify-center">
            <User :size="18" class="text-teal-500" />
          </div>
          <div>
            <h2 class="font-bold text-lg text-slate-800">个人信息</h2>
            <p class="text-xs text-slate-400">管理你的账户基本信息</p>
          </div>
        </div>

        <!-- 用户名 -->
        <div class="space-y-2">
          <label class="text-sm font-bold text-slate-600">用户名 Username</label>
          <input
            v-model="profileForm.username"
            type="text"
            class="w-full px-4 py-3 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-800 font-medium focus:outline-none focus:border-teal-400 transition-all"
            placeholder="输入用户名"
          />
          <p class="text-xs text-slate-400">用户名将显示在界面左下角</p>
        </div>

        <!-- 角色（只读） -->
        <div class="space-y-2">
          <label class="text-sm font-bold text-slate-600">账户角色 Role</label>
          <div
            class="w-full px-4 py-3 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-400 font-medium flex items-center gap-2"
          >
            <Shield :size="15" class="text-slate-300" />
            {{ userRole }}
            <span class="ml-auto text-xs bg-slate-200 text-slate-500 px-2 py-0.5 rounded-full"
              >只读</span
            >
          </div>
        </div>

        <!-- 成功/错误提示 -->
        <div
          v-if="profileSuccess"
          class="flex items-center gap-2 text-green-600 bg-green-50 px-4 py-3 rounded-xl text-sm font-medium"
        >
          <CheckCircle2 :size="16" /> 保存成功！
        </div>
        <div
          v-if="profileError"
          class="flex items-center gap-2 text-red-500 bg-red-50 px-4 py-3 rounded-xl text-sm font-medium"
        >
          <AlertCircle :size="16" /> {{ profileError }}
        </div>

        <button
          @click="saveProfile"
          :disabled="profileSaving"
          class="px-6 py-2.5 bg-teal-500 hover:bg-teal-600 disabled:bg-teal-300 text-white rounded-xl text-sm font-bold transition-all flex items-center gap-2"
        >
          <Check v-if="!profileSaving" :size="15" />
          <div
            v-else
            class="w-4 h-4 border-2 border-white/50 border-t-white rounded-full animate-spin"
          />
          {{ profileSaving ? '保存中...' : '保存修改' }}
        </button>
      </div>

      <!-- 账号安全 -->
      <div
        v-else-if="activeSection === 'security'"
        class="bg-white rounded-3xl p-7 shadow-lg border border-slate-100 space-y-6"
      >
        <div class="flex items-center gap-3 pb-4 border-b border-slate-100">
          <div class="w-9 h-9 rounded-xl bg-amber-50 flex items-center justify-center">
            <Lock :size="18" class="text-amber-500" />
          </div>
          <div>
            <h2 class="font-bold text-lg text-slate-800">账号安全</h2>
            <p class="text-xs text-slate-400">修改密码，保护账号安全</p>
          </div>
        </div>

        <!-- 当前密码 -->
        <div class="space-y-2">
          <label class="text-sm font-bold text-slate-600">当前密码 Current Password</label>
          <div class="relative">
            <input
              v-model="passwordForm.current"
              :type="showPasswords.current ? 'text' : 'password'"
              class="w-full px-4 py-3 pr-11 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-800 font-medium focus:outline-none focus:border-amber-400 transition-all"
              placeholder="输入当前密码"
            />
            <button
              @click="showPasswords.current = !showPasswords.current"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <Eye v-if="!showPasswords.current" :size="18" />
              <EyeOff v-else :size="18" />
            </button>
          </div>
        </div>

        <!-- 新密码 -->
        <div class="space-y-2">
          <label class="text-sm font-bold text-slate-600">新密码 New Password</label>
          <div class="relative">
            <input
              v-model="passwordForm.newPwd"
              :type="showPasswords.new ? 'text' : 'password'"
              class="w-full px-4 py-3 pr-11 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-800 font-medium focus:outline-none focus:border-amber-400 transition-all"
              placeholder="输入新密码（至少6位）"
            />
            <button
              @click="showPasswords.new = !showPasswords.new"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <Eye v-if="!showPasswords.new" :size="18" />
              <EyeOff v-else :size="18" />
            </button>
          </div>
          <!-- 密码强度 -->
          <div v-if="passwordForm.newPwd" class="space-y-1">
            <div class="flex gap-1">
              <div
                v-for="i in 4"
                :key="i"
                :class="[
                  'h-1.5 flex-1 rounded-full transition-all',
                  i <= passwordStrength ? strengthColor : 'bg-slate-200',
                ]"
              />
            </div>
            <p class="text-xs" :class="passwordStrength >= 3 ? 'text-green-500' : 'text-slate-400'">
              密码强度：{{ strengthLabel }}
            </p>
          </div>
        </div>

        <!-- 确认新密码 -->
        <div class="space-y-2">
          <label class="text-sm font-bold text-slate-600">确认新密码 Confirm Password</label>
          <div class="relative">
            <input
              v-model="passwordForm.confirm"
              :type="showPasswords.confirm ? 'text' : 'password'"
              class="w-full px-4 py-3 pr-11 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-800 font-medium focus:outline-none focus:border-amber-400 transition-all"
              :class="
                passwordForm.confirm && passwordForm.confirm !== passwordForm.newPwd
                  ? 'border-red-300'
                  : ''
              "
              placeholder="再次输入新密码"
            />
            <button
              @click="showPasswords.confirm = !showPasswords.confirm"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <Eye v-if="!showPasswords.confirm" :size="18" />
              <EyeOff v-else :size="18" />
            </button>
          </div>
          <p
            v-if="passwordForm.confirm && passwordForm.confirm !== passwordForm.newPwd"
            class="text-xs text-red-400"
          >
            两次密码不一致
          </p>
        </div>

        <div
          v-if="passwordSuccess"
          class="flex items-center gap-2 text-green-600 bg-green-50 px-4 py-3 rounded-xl text-sm font-medium"
        >
          <CheckCircle2 :size="16" /> 密码修改成功！
        </div>
        <div
          v-if="passwordError"
          class="flex items-center gap-2 text-red-500 bg-red-50 px-4 py-3 rounded-xl text-sm font-medium"
        >
          <AlertCircle :size="16" /> {{ passwordError }}
        </div>

        <button
          @click="savePassword"
          :disabled="passwordSaving"
          class="px-6 py-2.5 bg-amber-500 hover:bg-amber-600 disabled:bg-amber-300 text-white rounded-xl text-sm font-bold transition-all flex items-center gap-2"
        >
          <Lock v-if="!passwordSaving" :size="15" />
          <div
            v-else
            class="w-4 h-4 border-2 border-white/50 border-t-white rounded-full animate-spin"
          />
          {{ passwordSaving ? '修改中...' : '修改密码' }}
        </button>
      </div>

      <!-- 通知设置 -->
      <div
        v-else-if="activeSection === 'notifications'"
        class="bg-white rounded-3xl p-7 shadow-lg border border-slate-100 space-y-6"
      >
        <div class="flex items-center gap-3 pb-4 border-b border-slate-100">
          <div class="w-9 h-9 rounded-xl bg-blue-50 flex items-center justify-center">
            <Bell :size="18" class="text-blue-500" />
          </div>
          <div>
            <h2 class="font-bold text-lg text-slate-800">通知设置</h2>
            <p class="text-xs text-slate-400">管理你希望接收的提醒类型</p>
          </div>
        </div>

        <div class="space-y-2">
          <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">宠物提醒</p>

          <div
            v-for="item in [
              {
                key: 'appointmentReminder',
                icon: '📅',
                label: '预约提醒',
                desc: '就诊预约前1天提醒',
              },
              { key: 'vaccinationDue', icon: '💉', label: '疫苗到期', desc: '疫苗到期前7天提醒' },
              {
                key: 'medicationReminder',
                icon: '💊',
                label: '用药提醒',
                desc: '每日用药时间提醒',
              },
              { key: 'feedingReminder', icon: '🍖', label: '喂食提醒', desc: '每日喂食时间提醒' },
            ]"
            :key="item.key"
            class="flex items-center justify-between p-4 bg-slate-50 rounded-2xl hover:bg-slate-100 transition-colors"
          >
            <div class="flex items-center gap-3">
              <span class="text-xl">{{ item.icon }}</span>
              <div>
                <p class="text-sm font-bold text-slate-700">{{ item.label }}</p>
                <p class="text-xs text-slate-400">{{ item.desc }}</p>
              </div>
            </div>
            <button
              @click="(notifications as any)[item.key] = !(notifications as any)[item.key]"
              :class="[
                'relative w-11 h-6 rounded-full transition-all duration-300',
                (notifications as any)[item.key] ? 'bg-teal-500' : 'bg-slate-200',
              ]"
            >
              <div
                :class="[
                  'absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-all duration-300',
                  (notifications as any)[item.key] ? 'left-5' : 'left-0.5',
                ]"
              />
            </button>
          </div>
        </div>

        <div class="space-y-2">
          <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">通知方式</p>
          <div
            v-for="item in [
              { key: 'pushNotify', icon: Smartphone, label: '应用推送', desc: '在应用内接收通知' },
              { key: 'emailNotify', icon: Mail, label: '邮件通知', desc: '发送到注册邮箱' },
            ]"
            :key="item.key"
            class="flex items-center justify-between p-4 bg-slate-50 rounded-2xl hover:bg-slate-100 transition-colors"
          >
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-xl bg-blue-100 flex items-center justify-center">
                <component :is="item.icon" :size="16" class="text-blue-500" />
              </div>
              <div>
                <p class="text-sm font-bold text-slate-700">{{ item.label }}</p>
                <p class="text-xs text-slate-400">{{ item.desc }}</p>
              </div>
            </div>
            <button
              @click="(notifications as any)[item.key] = !(notifications as any)[item.key]"
              :class="[
                'relative w-11 h-6 rounded-full transition-all duration-300',
                (notifications as any)[item.key] ? 'bg-blue-500' : 'bg-slate-200',
              ]"
            >
              <div
                :class="[
                  'absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-all duration-300',
                  (notifications as any)[item.key] ? 'left-5' : 'left-0.5',
                ]"
              />
            </button>
          </div>
        </div>
      </div>

      <!-- 系统偏好 -->
      <div
        v-else-if="activeSection === 'preferences'"
        class="bg-white rounded-3xl p-7 shadow-lg border border-slate-100 space-y-6"
      >
        <div class="flex items-center gap-3 pb-4 border-b border-slate-100">
          <div class="w-9 h-9 rounded-xl bg-purple-50 flex items-center justify-center">
            <Globe :size="18" class="text-purple-500" />
          </div>
          <div>
            <h2 class="font-bold text-lg text-slate-800">系统偏好</h2>
            <p class="text-xs text-slate-400">自定义你的使用体验</p>
          </div>
        </div>

        <!-- 主题 -->
        <div class="space-y-3">
          <label class="text-sm font-bold text-slate-600">主题 Theme</label>
          <div class="grid grid-cols-2 gap-3">
            <button
              @click="preferences.theme = 'light'"
              :class="[
                'flex items-center gap-3 p-4 rounded-2xl border-2 transition-all',
                preferences.theme === 'light'
                  ? 'border-teal-400 bg-teal-50'
                  : 'border-slate-100 bg-slate-50 hover:border-slate-200',
              ]"
            >
              <div
                class="w-8 h-8 rounded-lg bg-white border border-slate-200 flex items-center justify-center shadow-sm"
              >
                <Sun :size="16" class="text-amber-500" />
              </div>
              <div class="text-left">
                <p class="text-sm font-bold text-slate-700">浅色</p>
                <p class="text-xs text-slate-400">Light Mode</p>
              </div>
              <Check
                v-if="preferences.theme === 'light'"
                :size="16"
                class="ml-auto text-teal-500"
              />
            </button>
            <button
              @click="preferences.theme = 'dark'"
              :class="[
                'flex items-center gap-3 p-4 rounded-2xl border-2 transition-all',
                preferences.theme === 'dark'
                  ? 'border-teal-400 bg-teal-50'
                  : 'border-slate-100 bg-slate-50 hover:border-slate-200',
              ]"
            >
              <div
                class="w-8 h-8 rounded-lg bg-slate-800 flex items-center justify-center shadow-sm"
              >
                <Moon :size="16" class="text-slate-300" />
              </div>
              <div class="text-left">
                <p class="text-sm font-bold text-slate-700">深色</p>
                <p class="text-xs text-slate-400">Dark Mode</p>
              </div>
              <Check v-if="preferences.theme === 'dark'" :size="16" class="ml-auto text-teal-500" />
            </button>
          </div>
          <p class="text-xs text-slate-400 bg-slate-50 px-3 py-2 rounded-xl">
            🚧 深色模式将在后续版本中实装
          </p>
        </div>

        <!-- 语言 -->
        <div class="space-y-2">
          <label class="text-sm font-bold text-slate-600">语言 Language</label>
          <div class="grid grid-cols-2 gap-3">
            <button
              @click="preferences.language = 'zh-CN'"
              :class="[
                'flex items-center gap-2 p-3 rounded-xl border-2 transition-all text-sm font-bold',
                preferences.language === 'zh-CN'
                  ? 'border-teal-400 bg-teal-50 text-teal-700'
                  : 'border-slate-100 bg-slate-50 text-slate-600 hover:border-slate-200',
              ]"
            >
              <span class="text-lg">🇨🇳</span> 中文
              <Check v-if="preferences.language === 'zh-CN'" :size="14" class="ml-auto" />
            </button>
            <button
              @click="preferences.language = 'en'"
              :class="[
                'flex items-center gap-2 p-3 rounded-xl border-2 transition-all text-sm font-bold',
                preferences.language === 'en'
                  ? 'border-teal-400 bg-teal-50 text-teal-700'
                  : 'border-slate-100 bg-slate-50 text-slate-600 hover:border-slate-200',
              ]"
            >
              <span class="text-lg">🇬🇧</span> English
              <Check v-if="preferences.language === 'en'" :size="14" class="ml-auto" />
            </button>
          </div>
        </div>

        <!-- 紧凑模式 -->
        <div class="flex items-center justify-between p-4 bg-slate-50 rounded-2xl">
          <div>
            <p class="text-sm font-bold text-slate-700">紧凑模式 Compact Mode</p>
            <p class="text-xs text-slate-400">减少间距，显示更多内容</p>
          </div>
          <button
            @click="preferences.compactMode = !preferences.compactMode"
            :class="[
              'relative w-11 h-6 rounded-full transition-all duration-300',
              preferences.compactMode ? 'bg-purple-500' : 'bg-slate-200',
            ]"
          >
            <div
              :class="[
                'absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-all duration-300',
                preferences.compactMode ? 'left-5' : 'left-0.5',
              ]"
            />
          </button>
        </div>
      </div>

      <!-- 关于应用 -->
      <div v-else-if="activeSection === 'about'" class="space-y-4">
        <!-- 应用信息 -->
        <div class="bg-white rounded-3xl p-7 shadow-lg border border-slate-100">
          <div class="flex items-center gap-4 mb-6">
            <div
              class="w-14 h-14 rounded-2xl bg-gradient-to-br from-teal-400 to-cyan-500 flex items-center justify-center shadow-lg shadow-teal-100"
            >
              <PawPrint :size="28" class="text-white" />
            </div>
            <div>
              <h2 class="font-bold text-xl text-slate-800">{{ appInfo.name }}</h2>
              <p class="text-slate-400 text-sm">{{ appInfo.description }}</p>
              <span
                class="inline-block mt-1 text-xs font-bold px-2 py-0.5 bg-teal-50 text-teal-600 rounded-full"
              >
                v{{ appInfo.version }}
              </span>
            </div>
          </div>

          <div class="space-y-3">
            <div
              v-for="item in [
                { label: '前端框架', value: appInfo.frontend },
                { label: '后端框架', value: appInfo.backend },
                { label: '数据库', value: appInfo.database },
                { label: '版本号', value: `v${appInfo.version}` },
              ]"
              :key="item.label"
              class="flex items-center justify-between py-2.5 border-b border-slate-50"
            >
              <span class="text-sm text-slate-500">{{ item.label }}</span>
              <span class="text-sm font-bold text-slate-700">{{ item.value }}</span>
            </div>
          </div>
        </div>

        <!-- 团队信息 -->
        <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
          <h3 class="font-bold text-slate-800 mb-4 flex items-center gap-2">
            <Heart :size="16" class="text-red-400" /> 开发团队
          </h3>
          <p class="text-sm text-slate-500 leading-relaxed">
            PetCare
            是一款专为宠物主人设计的健康管理应用，帮助你记录宠物的成长、健康状况、医疗记录和日常生活。
          </p>
          <div class="mt-4 p-3 bg-teal-50 rounded-xl">
            <p class="text-xs text-teal-600 text-center font-medium">
              🐾 用爱心守护每一只宠物的健康
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
