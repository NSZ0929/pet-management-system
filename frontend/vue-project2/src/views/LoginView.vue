<script setup lang="ts">
import { ref, reactive } from 'vue'
import { PawPrint, Eye, EyeOff, Loader2, AlertCircle, CheckCircle2 } from 'lucide-vue-next'
import { login, register } from '../api/auth'

// ── emit: notify parent on login success ──────────────────────────────
const emit = defineEmits<{
  (e: 'login-success', user: { id: number; username: string; role: string }): void
}>()

// ── State ───────────────────────────────────────────────────
const mode = ref<'login' | 'register'>('login')
const showPassword = ref(false)
const isLoading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

// ── Clear form on mode switch ──────────────────────────────────────
const switchMode = (target: 'login' | 'register') => {
  mode.value = target
  errorMsg.value = ''
  successMsg.value = ''
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
}

// ── Login ───────────────────────────────────────────────────
const handleLogin = async () => {
  errorMsg.value = ''
  if (!form.username.trim() || !form.password.trim()) {
    errorMsg.value = 'Please enter your username and password.'
    return
  }

  isLoading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    const data = res.data

    // Save token and user info to localStorage
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))

    emit('login-success', data.user)
  } catch (err: unknown) {
    const response = (err as { response?: { data?: unknown } })?.response
    const msg = response?.data ?? 'Login failed. Please check your username and password.'
    errorMsg.value = typeof msg === 'string' ? msg : 'Login failed. Please try again.'
  } finally {
    isLoading.value = false
  }
}

// ── Register ───────────────────────────────────────────────────
const handleRegister = async () => {
  errorMsg.value = ''
  successMsg.value = ''

  if (!form.username.trim() || !form.password.trim()) {
    errorMsg.value = 'Please enter your username and password.'
    return
  }
  if (form.password !== form.confirmPassword) {
    errorMsg.value = 'Passwords do not match.'
    return
  }
  if (form.password.length < 6) {
    errorMsg.value = 'Password must be at least 6 characters.'
    return
  }

  isLoading.value = true
  try {
    await register({ username: form.username, password: form.password })
    successMsg.value = 'Registration successful! Please log in.'
    setTimeout(() => switchMode('login'), 1500)
  } catch (err: unknown) {
    const response = (err as { response?: { data?: unknown } })?.response
    const msg = response?.data ?? 'Registration failed.'
    errorMsg.value = typeof msg === 'string' ? msg : 'Registration failed. Username may already be taken.'
  } finally {
    isLoading.value = false
  }
}

const handleSubmit = () => {
  if (mode.value === 'login') {
    handleLogin()
  } else {
    handleRegister()
  }
}
</script>

<template>
  <div
    class="min-h-screen bg-gradient-to-br from-teal-50 via-cyan-50 to-slate-100 flex items-center justify-center p-4"
  >
    <!-- Background decoration -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-32 -left-32 w-96 h-96 bg-teal-200/30 rounded-full blur-3xl"></div>
      <div
        class="absolute -bottom-32 -right-32 w-96 h-96 bg-cyan-200/30 rounded-full blur-3xl"
      ></div>
    </div>

    <!-- Card -->
    <div class="relative w-full max-w-md">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div
          class="inline-flex items-center justify-center w-16 h-16 bg-teal-500 rounded-3xl shadow-lg shadow-teal-200 mb-4"
        >
          <PawPrint :size="32" class="text-white" />
        </div>
        <h1 class="text-3xl font-extrabold text-slate-800 tracking-tight">PetCare</h1>
        <p class="text-slate-400 text-sm mt-1">Pet Health Management Platform</p>
      </div>

      <!-- Main card -->
      <div class="bg-white rounded-3xl shadow-xl shadow-slate-200/60 overflow-hidden">
        <!-- Tab toggle -->
        <div class="flex border-b border-slate-100">
          <button
            @click="switchMode('login')"
            class="flex-1 py-4 text-sm font-bold transition-all"
            :class="
              mode === 'login'
                ? 'text-teal-600 border-b-2 border-teal-500 bg-teal-50/50'
                : 'text-slate-400 hover:text-slate-600'
            "
          >
            Login
          </button>
          <button
            @click="switchMode('register')"
            class="flex-1 py-4 text-sm font-bold transition-all"
            :class="
              mode === 'register'
                ? 'text-teal-600 border-b-2 border-teal-500 bg-teal-50/50'
                : 'text-slate-400 hover:text-slate-600'
            "
          >
            Register
          </button>
        </div>

        <!-- Form -->
        <div class="p-8 space-y-5">
          <!-- Username -->
          <div>
            <label class="block text-xs font-bold text-slate-600 mb-2">Username</label>
            <input
              v-model="form.username"
              type="text"
              placeholder="Enter username"
              @keyup.enter="handleSubmit"
              class="w-full px-4 py-3 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-800 placeholder-slate-300 text-sm font-medium focus:outline-none focus:border-teal-400 focus:bg-white transition-all"
            />
          </div>

          <!-- Password -->
          <div>
            <label class="block text-xs font-bold text-slate-600 mb-2">Password</label>
            <div class="relative">
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Enter password"
                @keyup.enter="handleSubmit"
                class="w-full px-4 py-3 pr-12 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-800 placeholder-slate-300 text-sm font-medium focus:outline-none focus:border-teal-400 focus:bg-white transition-all"
              />
              <button
                @click="showPassword = !showPassword"
                type="button"
                class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-300 hover:text-slate-500 transition-colors"
              >
                <component :is="showPassword ? EyeOff : Eye" :size="18" />
              </button>
            </div>
          </div>

          <!-- Confirm password (register only) -->
          <Transition name="slide">
            <div v-if="mode === 'register'">
              <label class="block text-xs font-bold text-slate-600 mb-2">
                Confirm Password
              </label>
              <input
                v-model="form.confirmPassword"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Re-enter password"
                @keyup.enter="handleSubmit"
                class="w-full px-4 py-3 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-800 placeholder-slate-300 text-sm font-medium focus:outline-none focus:border-teal-400 focus:bg-white transition-all"
              />
            </div>
          </Transition>

          <!-- Error message -->
          <Transition name="fade">
            <div
              v-if="errorMsg"
              class="flex items-center gap-2 bg-red-50 border border-red-100 text-red-500 text-sm px-4 py-3 rounded-2xl"
            >
              <AlertCircle :size="16" class="shrink-0" />
              {{ errorMsg }}
            </div>
          </Transition>

          <!-- Success message -->
          <Transition name="fade">
            <div
              v-if="successMsg"
              class="flex items-center gap-2 bg-emerald-50 border border-emerald-100 text-emerald-600 text-sm px-4 py-3 rounded-2xl"
            >
              <CheckCircle2 :size="16" class="shrink-0" />
              {{ successMsg }}
            </div>
          </Transition>

          <!-- Submit button -->
          <button
            @click="handleSubmit"
            :disabled="isLoading"
            class="w-full py-3.5 rounded-2xl bg-teal-500 hover:bg-teal-600 disabled:bg-teal-300 text-white font-bold text-sm transition-all shadow-lg shadow-teal-200 flex items-center justify-center gap-2"
          >
            <Loader2 v-if="isLoading" :size="18" class="animate-spin" />
            <span v-if="!isLoading">
              {{ mode === 'login' ? 'Login' : 'Register' }}
            </span>
            <span v-else>
              {{ mode === 'login' ? 'Logging in...' : 'Registering...' }}
            </span>
          </button>
        </div>
      </div>

      <!-- Footer hint -->
      <p class="text-center text-xs text-slate-400 mt-6">
        Backend running at <span class="font-mono text-teal-500">localhost:8080</span>
      </p>
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
  max-height: 100px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>