<script setup lang="ts">
import { ref, reactive } from 'vue'
import { PawPrint, Eye, EyeOff, Loader2, AlertCircle, CheckCircle2 } from 'lucide-vue-next'
import { login, register } from '../api/auth'

// ── emit：登录成功后通知父组件 ──────────────────────────────
const emit = defineEmits<{
  (e: 'login-success', user: { id: number; username: string; role: string }): void
}>()

// ── 状态 ───────────────────────────────────────────────────
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

// ── 切换模式时清空表单 ──────────────────────────────────────
const switchMode = (target: 'login' | 'register') => {
  mode.value = target
  errorMsg.value = ''
  successMsg.value = ''
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
}

// ── 登录 ───────────────────────────────────────────────────
const handleLogin = async () => {
  errorMsg.value = ''
  if (!form.username.trim() || !form.password.trim()) {
    errorMsg.value = '请填写用户名和密码'
    return
  }

  isLoading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    const data = res.data

    // 保存 token 和用户信息到 localStorage
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))

    emit('login-success', data.user)
  } catch (err: unknown) {
    const response = (err as { response?: { data?: unknown } })?.response
    const msg = response?.data ?? '登录失败，请检查用户名和密码'
    errorMsg.value = typeof msg === 'string' ? msg : '登录失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

// ── 注册 ───────────────────────────────────────────────────
const handleRegister = async () => {
  errorMsg.value = ''
  successMsg.value = ''

  if (!form.username.trim() || !form.password.trim()) {
    errorMsg.value = '请填写用户名和密码'
    return
  }
  if (form.password !== form.confirmPassword) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }
  if (form.password.length < 6) {
    errorMsg.value = '密码长度至少 6 位'
    return
  }

  isLoading.value = true
  try {
    await register({ username: form.username, password: form.password })
    successMsg.value = '注册成功！请登录'
    setTimeout(() => switchMode('login'), 1500)
  } catch (err: unknown) {
    const response = (err as { response?: { data?: unknown } })?.response
    const msg = response?.data ?? '注册失败'
    errorMsg.value = typeof msg === 'string' ? msg : '注册失败，用户名可能已存在'
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
    <!-- 背景装饰 -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-32 -left-32 w-96 h-96 bg-teal-200/30 rounded-full blur-3xl"></div>
      <div
        class="absolute -bottom-32 -right-32 w-96 h-96 bg-cyan-200/30 rounded-full blur-3xl"
      ></div>
    </div>

    <!-- 卡片 -->
    <div class="relative w-full max-w-md">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div
          class="inline-flex items-center justify-center w-16 h-16 bg-teal-500 rounded-3xl shadow-lg shadow-teal-200 mb-4"
        >
          <PawPrint :size="32" class="text-white" />
        </div>
        <h1 class="text-3xl font-extrabold text-slate-800 tracking-tight">PetCare</h1>
        <p class="text-slate-400 text-sm mt-1">宠物健康管理平台</p>
      </div>

      <!-- 主卡片 -->
      <div class="bg-white rounded-3xl shadow-xl shadow-slate-200/60 overflow-hidden">
        <!-- Tab 切换 -->
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
            登录 Login
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
            注册 Register
          </button>
        </div>

        <!-- 表单 -->
        <div class="p-8 space-y-5">
          <!-- 用户名 -->
          <div>
            <label class="block text-xs font-bold text-slate-600 mb-2"> 用户名 Username </label>
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              @keyup.enter="handleSubmit"
              class="w-full px-4 py-3 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-800 placeholder-slate-300 text-sm font-medium focus:outline-none focus:border-teal-400 focus:bg-white transition-all"
            />
          </div>

          <!-- 密码 -->
          <div>
            <label class="block text-xs font-bold text-slate-600 mb-2"> 密码 Password </label>
            <div class="relative">
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
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

          <!-- 确认密码（注册时显示） -->
          <Transition name="slide">
            <div v-if="mode === 'register'">
              <label class="block text-xs font-bold text-slate-600 mb-2">
                确认密码 Confirm Password
              </label>
              <input
                v-model="form.confirmPassword"
                :type="showPassword ? 'text' : 'password'"
                placeholder="再次输入密码"
                @keyup.enter="handleSubmit"
                class="w-full px-4 py-3 rounded-2xl border-2 border-slate-100 bg-slate-50 text-slate-800 placeholder-slate-300 text-sm font-medium focus:outline-none focus:border-teal-400 focus:bg-white transition-all"
              />
            </div>
          </Transition>

          <!-- 错误提示 -->
          <Transition name="fade">
            <div
              v-if="errorMsg"
              class="flex items-center gap-2 bg-red-50 border border-red-100 text-red-500 text-sm px-4 py-3 rounded-2xl"
            >
              <AlertCircle :size="16" class="shrink-0" />
              {{ errorMsg }}
            </div>
          </Transition>

          <!-- 成功提示 -->
          <Transition name="fade">
            <div
              v-if="successMsg"
              class="flex items-center gap-2 bg-emerald-50 border border-emerald-100 text-emerald-600 text-sm px-4 py-3 rounded-2xl"
            >
              <CheckCircle2 :size="16" class="shrink-0" />
              {{ successMsg }}
            </div>
          </Transition>

          <!-- 提交按钮 -->
          <button
            @click="handleSubmit"
            :disabled="isLoading"
            class="w-full py-3.5 rounded-2xl bg-teal-500 hover:bg-teal-600 disabled:bg-teal-300 text-white font-bold text-sm transition-all shadow-lg shadow-teal-200 flex items-center justify-center gap-2"
          >
            <Loader2 v-if="isLoading" :size="18" class="animate-spin" />
            <span v-if="!isLoading">
              {{ mode === 'login' ? '登录 Login' : '注册 Register' }}
            </span>
            <span v-else>
              {{ mode === 'login' ? '登录中...' : '注册中...' }}
            </span>
          </button>
        </div>
      </div>

      <!-- 底部提示 -->
      <p class="text-center text-xs text-slate-400 mt-6">
        后端运行于 <span class="font-mono text-teal-500">localhost:8080</span>
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
