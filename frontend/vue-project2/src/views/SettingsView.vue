<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import {
  User,
  Lock,
  Bell,
  Info,
  Check,
  Eye,
  EyeOff,
  ChevronRight,
  Shield,
  Smartphone,
  Mail,
  Heart,
  AlertCircle,
  CheckCircle2,
} from 'lucide-vue-next'
import { changePassword, updateUsername } from '../api/auth'

defineProps<{ headerTitle?: string }>()

const activeSection = ref<'profile' | 'security' | 'notifications' | 'about'>(
  'profile',
)

// ── User Info ──────────────────────────────────────────────
const storedUser = JSON.parse(localStorage.getItem('user') || '{}')
const username = ref(storedUser.username || 'Unknown User')
const userRole = ref(storedUser.role || 'USER')

// ── Profile ──────────────────────────────────────────────
const profileForm = reactive({ username: username.value })
const profileSaving = ref(false)
const profileSuccess = ref(false)
const profileError = ref('')

const saveProfile = async () => {
  if (!profileForm.username.trim()) {
    profileError.value = 'Username cannot be empty.'
    return
  }
  profileSaving.value = true
  profileError.value = ''
  profileSuccess.value = false
  try {
    const res = await updateUsername(profileForm.username.trim())
    // Update local storage
    username.value = res.data.username
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    user.username = res.data.username
    localStorage.setItem('user', JSON.stringify(user))
    profileSuccess.value = true
    // Username change requires re-login to refresh token
    setTimeout(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.reload()
    }, 2000)
  } catch (err) {
    profileError.value =
      (err as { response?: { data?: string } })?.response?.data || 'Save failed. Please try again.'
  } finally {
    profileSaving.value = false
  }
}

// ── Change Password ──────────────────────────────────────────────
const passwordForm = reactive({ current: '', newPwd: '', confirm: '' })
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
const strengthLabel = computed(() => ['', 'Weak', 'Fair', 'Strong', 'Very Strong'][passwordStrength.value])
const strengthColor = computed(
  () => ['', 'bg-red-400', 'bg-amber-400', 'bg-teal-400', 'bg-green-500'][passwordStrength.value],
)

const savePassword = async () => {
  passwordError.value = ''
  if (!passwordForm.current || !passwordForm.newPwd || !passwordForm.confirm) {
    passwordError.value = 'Please fill in all password fields.'
    return
  }
  if (passwordForm.newPwd !== passwordForm.confirm) {
    passwordError.value = 'New passwords do not match.'
    return
  }
  if (passwordForm.newPwd.length < 6) {
    passwordError.value = 'New password must be at least 6 characters.'
    return
  }
  passwordSaving.value = true
  try {
    await changePassword(passwordForm.current, passwordForm.newPwd)
    passwordSuccess.value = true
    passwordForm.current = ''
    passwordForm.newPwd = ''
    passwordForm.confirm = ''
    setTimeout(() => (passwordSuccess.value = false), 3000)
  } catch (err) {
    passwordError.value =
      (err as { response?: { data?: string } })?.response?.data || 'Save failed. Please try again.'
  } finally {
    passwordSaving.value = false
  }
}

// ── Notification Settings ──────────────────────────────────────────────
const notifications = reactive({
  appointmentReminder: true,
  vaccinationDue: true,
  medicationReminder: false,
  feedingReminder: true,
  emailNotify: false,
  pushNotify: true,
})

// ── About ──────────────────────────────────────────────
const appInfo = {
  name: 'PetCare',
  version: '1.0.0',
  description: 'Professional Pet Health Management System',
  backend: 'Spring Boot 3.5.9',
  frontend: 'Vue 3 + TypeScript',
  database: 'MySQL 8.4',
}

const sections = [
  { key: 'profile', label: 'Profile', icon: User, color: 'text-teal-500', bg: 'bg-teal-50' },
  { key: 'security', label: 'Security', icon: Lock, color: 'text-amber-500', bg: 'bg-amber-50' },
  { key: 'notifications', label: 'Notifications', icon: Bell, color: 'text-blue-500', bg: 'bg-blue-50' },
  { key: 'about', label: 'About', icon: Info, color: 'text-slate-500', bg: 'bg-slate-100' },
] as const
</script>

<template>
  <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
    <!-- Left sidebar -->
    <div class="lg:col-span-1 space-y-3">
      <div class="bg-white rounded-3xl p-5 shadow-lg border border-slate-100 text-center">
        <div
          class="w-16 h-16 rounded-2xl bg-gradient-to-br from-teal-400 to-cyan-500 flex items-center justify-center mx-auto mb-3 shadow-lg shadow-teal-100"
        >
          <span class="text-2xl font-bold text-white">{{ username.charAt(0).toUpperCase() }}</span>
        </div>
        <p class="font-bold text-slate-800">{{ username }}</p>
        <span
          class="inline-block mt-1 text-xs font-bold px-2 py-0.5 bg-teal-50 text-teal-600 rounded-full"
          >{{ userRole }}</span
        >
      </div>

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

    <!-- Right content -->
    <div class="lg:col-span-3">
      <!-- Profile -->
      <div
        v-if="activeSection === 'profile'"
        class="bg-white rounded-3xl p-7 shadow-lg border border-slate-100 space-y-6"
      >
        <div class="flex items-center gap-3 pb-4 border-b border-slate-100">
          <div class="w-9 h-9 rounded-xl bg-teal-50 flex items-center justify-center">
            <User :size="18" class="text-teal-500" />
          </div>
          <div>
            <h2 class="font-bold text-lg text-slate-800">Profile</h2>
            <p class="text-xs text-slate-400">Update your username — synced to backend in real time.</p>
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-bold text-slate-600">Username</label>
          <input
            v-model="profileForm.username"
            type="text"
            class="w-full px-4 py-3 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-800 font-medium focus:outline-none focus:border-teal-400 transition-all"
            placeholder="Enter new username"
          />
          <p class="text-xs text-slate-400">You will need to log in again after changing your username.</p>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-bold text-slate-600">Account Role</label>
          <div
            class="w-full px-4 py-3 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-400 font-medium flex items-center gap-2"
          >
            <Shield :size="15" class="text-slate-300" />{{ userRole }}
            <span class="ml-auto text-xs bg-slate-200 text-slate-500 px-2 py-0.5 rounded-full"
              >Read-only</span
            >
          </div>
        </div>

        <div
          v-if="profileSuccess"
          class="flex items-center gap-2 text-green-600 bg-green-50 px-4 py-3 rounded-xl text-sm font-medium"
        >
          <CheckCircle2 :size="16" /> Username updated successfully!
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
          {{ profileSaving ? 'Saving...' : 'Save Changes' }}
        </button>
      </div>

      <!-- Security -->
      <div
        v-else-if="activeSection === 'security'"
        class="bg-white rounded-3xl p-7 shadow-lg border border-slate-100 space-y-6"
      >
        <div class="flex items-center gap-3 pb-4 border-b border-slate-100">
          <div class="w-9 h-9 rounded-xl bg-amber-50 flex items-center justify-center">
            <Lock :size="18" class="text-amber-500" />
          </div>
          <div>
            <h2 class="font-bold text-lg text-slate-800">Account Security</h2>
            <p class="text-xs text-slate-400">Change your password — verified by backend.</p>
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-bold text-slate-600">Current Password</label>
          <div class="relative">
            <input
              v-model="passwordForm.current"
              :type="showPasswords.current ? 'text' : 'password'"
              class="w-full px-4 py-3 pr-11 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-800 font-medium focus:outline-none focus:border-amber-400 transition-all"
              placeholder="Enter current password"
            />
            <button
              @click="showPasswords.current = !showPasswords.current"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <Eye v-if="!showPasswords.current" :size="18" /><EyeOff v-else :size="18" />
            </button>
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-bold text-slate-600">New Password</label>
          <div class="relative">
            <input
              v-model="passwordForm.newPwd"
              :type="showPasswords.new ? 'text' : 'password'"
              class="w-full px-4 py-3 pr-11 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-800 font-medium focus:outline-none focus:border-amber-400 transition-all"
              placeholder="Enter new password (min. 6 characters)"
            />
            <button
              @click="showPasswords.new = !showPasswords.new"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <Eye v-if="!showPasswords.new" :size="18" /><EyeOff v-else :size="18" />
            </button>
          </div>
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
              Password strength: {{ strengthLabel }}
            </p>
          </div>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-bold text-slate-600">Confirm New Password</label>
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
              placeholder="Re-enter new password"
            />
            <button
              @click="showPasswords.confirm = !showPasswords.confirm"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <Eye v-if="!showPasswords.confirm" :size="18" /><EyeOff v-else :size="18" />
            </button>
          </div>
          <p
            v-if="passwordForm.confirm && passwordForm.confirm !== passwordForm.newPwd"
            class="text-xs text-red-400"
          >
            Passwords do not match.
          </p>
        </div>

        <div
          v-if="passwordSuccess"
          class="flex items-center gap-2 text-green-600 bg-green-50 px-4 py-3 rounded-xl text-sm font-medium"
        >
          <CheckCircle2 :size="16" /> Password changed successfully!
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
          {{ passwordSaving ? 'Changing...' : 'Change Password' }}
        </button>
      </div>

      <!-- Notifications -->
      <div
        v-else-if="activeSection === 'notifications'"
        class="bg-white rounded-3xl p-7 shadow-lg border border-slate-100 space-y-6"
      >
        <div class="flex items-center gap-3 pb-4 border-b border-slate-100">
          <div class="w-9 h-9 rounded-xl bg-blue-50 flex items-center justify-center">
            <Bell :size="18" class="text-blue-500" />
          </div>
          <div>
            <h2 class="font-bold text-lg text-slate-800">Notification Settings</h2>
            <p class="text-xs text-slate-400">Manage the types of reminders you want to receive.</p>
          </div>
        </div>
        <div class="space-y-2">
          <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">Pet Reminders</p>
          <div
            v-for="item in [
              {
                key: 'appointmentReminder',
                icon: '📅',
                label: 'Appointment Reminder',
                desc: 'Reminder 1 day before appointment',
              },
              { key: 'vaccinationDue', icon: '💉', label: 'Vaccine Due', desc: 'Reminder 7 days before vaccine expiry' },
              {
                key: 'medicationReminder',
                icon: '💊',
                label: 'Medication Reminder',
                desc: 'Daily medication time reminder',
              },
              { key: 'feedingReminder', icon: '🍖', label: 'Feeding Reminder', desc: 'Daily feeding time reminder' },
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
          <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">Notification Method</p>
          <div
            v-for="item in [
              { key: 'pushNotify', icon: Smartphone, label: 'App Push', desc: 'Receive in-app notifications' },
              { key: 'emailNotify', icon: Mail, label: 'Email Notification', desc: 'Send to registered email address' },
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

      <!-- About -->
      <div v-else-if="activeSection === 'about'" class="space-y-4">
        <div class="bg-white rounded-3xl p-7 shadow-lg border border-slate-100">
          <div class="flex items-center gap-4 mb-6">
            <div
              class="w-14 h-14 rounded-2xl bg-gradient-to-br from-teal-400 to-cyan-500 flex items-center justify-center shadow-lg shadow-teal-100"
            >
              <span class="text-2xl">🐾</span>
            </div>
            <div>
              <h2 class="font-bold text-xl text-slate-800">{{ appInfo.name }}</h2>
              <p class="text-slate-400 text-sm">{{ appInfo.description }}</p>
              <span
                class="inline-block mt-1 text-xs font-bold px-2 py-0.5 bg-teal-50 text-teal-600 rounded-full"
                >v{{ appInfo.version }}</span
              >
            </div>
          </div>
          <div class="space-y-3">
            <div
              v-for="item in [
                { label: 'Frontend', value: appInfo.frontend },
                { label: 'Backend', value: appInfo.backend },
                { label: 'Database', value: appInfo.database },
                { label: 'Version', value: `v${appInfo.version}` },
              ]"
              :key="item.label"
              class="flex items-center justify-between py-2.5 border-b border-slate-50"
            >
              <span class="text-sm text-slate-500">{{ item.label }}</span>
              <span class="text-sm font-bold text-slate-700">{{ item.value }}</span>
            </div>
          </div>
        </div>
        <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
          <h3 class="font-bold text-slate-800 mb-4 flex items-center gap-2">
            <Heart :size="16" class="text-red-400" /> Development Team
          </h3>
          <p class="text-sm text-slate-500 leading-relaxed">
            PetCare
            is a health management app designed for pet owners — track growth, health, medical records, and daily life.
          </p>
          <div class="mt-4 p-3 bg-teal-50 rounded-xl">
            <p class="text-xs text-teal-600 text-center font-medium">
              🐾 Caring for every pet with love
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>