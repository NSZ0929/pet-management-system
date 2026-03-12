<script setup lang="ts">
import { onMounted, computed } from 'vue'
import {
  Thermometer,
  Scale,
  Clock,
  CheckCircle,
  FileText,
  Loader2,
  AlertCircle,
  PawPrint,
  ChevronDown,
  Activity,
} from 'lucide-vue-next'
import { usePetData } from '../composables/usePetData'

defineProps<{ headerTitle?: string }>()

const { currentPet, allPets, medicalRecords, isLoadingPetData, petDataError, loadPets, selectPet } =
  usePetData()

onMounted(() => {
  loadPets()
})

// 最近3条医疗记录作为"就诊记录"展示
const recentRecords = computed(() => medicalRecords.value.slice(0, 3))

// 当前年份
const currentYear = new Date().getFullYear()
</script>

<template>
  <div class="space-y-6">
    <!-- 加载中 -->
    <div
      v-if="isLoadingPetData"
      class="flex items-center justify-center py-20 gap-3 text-slate-400"
    >
      <Loader2 :size="24" class="animate-spin text-teal-500" />
      <span class="text-sm font-medium">正在加载宠物数据...</span>
    </div>

    <!-- 错误提示 -->
    <div
      v-else-if="petDataError"
      class="flex items-center gap-3 bg-red-50 border border-red-100 text-red-500 px-5 py-4 rounded-2xl"
    >
      <AlertCircle :size="18" class="shrink-0" />
      <p class="text-sm font-medium">{{ petDataError }}</p>
    </div>

    <!-- 无宠物数据 -->
    <div
      v-else-if="allPets.length === 0"
      class="text-center py-20 bg-white rounded-3xl shadow-lg border border-slate-100"
    >
      <PawPrint :size="48" class="text-slate-200 mx-auto mb-4" />
      <p class="text-slate-500 font-bold text-lg">还没有宠物档案</p>
      <p class="text-slate-400 text-sm mt-2">请先在「档案 Profile」页面添加你的宠物 🐾</p>
    </div>

    <!-- 主内容 -->
    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧列 -->
      <div class="lg:col-span-1 space-y-6">
        <!-- 宠物选择器（多宠物时显示） -->
        <div
          v-if="allPets.length > 1"
          class="bg-white rounded-2xl p-4 shadow-md border border-slate-100"
        >
          <label class="block text-xs font-bold text-slate-400 mb-2 uppercase tracking-wider"
            >选择宠物</label
          >
          <div class="relative">
            <select
              :value="currentPet?.id"
              @change="
                (e) => {
                  const pet = allPets.find(
                    (p) => p.id === Number((e.target as HTMLSelectElement).value),
                  )
                  if (pet) selectPet(pet)
                }
              "
              class="w-full px-3 py-2.5 pr-8 rounded-xl border-2 border-slate-100 bg-slate-50 text-slate-800 text-sm font-bold focus:outline-none focus:border-teal-400 appearance-none transition-all"
            >
              <option v-for="pet in allPets" :key="pet.id" :value="pet.id">
                {{ pet.name }}（{{ pet.breed }}）
              </option>
            </select>
            <ChevronDown
              :size="16"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none"
            />
          </div>
        </div>

        <!-- 宠物卡片 -->
        <div class="bg-white rounded-3xl p-5 shadow-lg border border-slate-100">
          <!-- 头像占位 -->
          <div
            class="aspect-[4/3] rounded-2xl overflow-hidden mb-4 bg-gradient-to-br from-teal-50 to-cyan-100 flex items-center justify-center"
          >
            <PawPrint :size="64" class="text-teal-300" />
          </div>

          <div class="space-y-2 px-1 pb-1">
            <div class="flex items-center justify-between bg-slate-50 p-3 rounded-xl">
              <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Name</span>
              <span class="font-bold text-slate-700">{{ currentPet?.name ?? '—' }}</span>
            </div>
            <div class="flex items-center justify-between bg-slate-50 p-3 rounded-xl">
              <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Breed</span>
              <span class="font-bold text-slate-700">{{ currentPet?.breed || '暂未填写' }}</span>
            </div>
            <div class="flex items-center justify-between bg-slate-50 p-3 rounded-xl">
              <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Age</span>
              <span class="font-bold text-slate-700">
                {{ currentPet?.age != null ? `${currentPet.age} 岁` : '暂未填写' }}
              </span>
            </div>
            <div class="flex items-center justify-between bg-slate-50 p-3 rounded-xl">
              <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Owner</span>
              <span class="font-bold text-slate-700">{{
                currentPet?.owner?.name || '暂未填写'
              }}</span>
            </div>
          </div>
        </div>

        <!-- 就诊记录 -->
        <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
          <h3 class="font-bold text-slate-800 mb-4 flex items-center justify-between">
            就诊记录 Medical Records
            <span class="text-xs bg-slate-100 text-slate-500 px-2 py-1 rounded-lg">{{
              currentYear
            }}</span>
          </h3>

          <!-- 空状态 -->
          <div v-if="recentRecords.length === 0" class="text-center py-6">
            <FileText :size="32" class="text-slate-200 mx-auto mb-2" />
            <p class="text-sm text-slate-400">暂无就诊记录</p>
          </div>

          <!-- 记录列表 -->
          <div v-else class="space-y-3">
            <div
              v-for="record in recentRecords"
              :key="record.id"
              class="flex items-start gap-3 p-3 bg-slate-50 rounded-xl hover:border-teal-100 border border-transparent transition-colors"
            >
              <CheckCircle :size="16" class="text-teal-500 mt-0.5 shrink-0" />
              <div class="flex-1 min-w-0">
                <p class="text-sm font-bold text-slate-700 truncate">{{ record.title }}</p>
                <p class="text-xs text-slate-400 mt-0.5">{{ record.visitDate }}</p>
                <p v-if="record.description" class="text-xs text-slate-500 mt-0.5 truncate">
                  {{ record.description }}
                </p>
              </div>
            </div>
          </div>

          <button
            class="w-full mt-4 py-2.5 bg-teal-500 hover:bg-teal-600 text-white rounded-xl text-sm font-bold shadow-md shadow-teal-100 transition-all flex items-center justify-center gap-2"
          >
            <FileText :size="15" /> 查看完整报告
          </button>
        </div>
      </div>

      <!-- 右侧区域 -->
      <div class="lg:col-span-2 space-y-6">
        <!-- 生命体征卡片 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Vital Signs -->
          <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
            <h3 class="font-bold text-slate-800 mb-5">生命体征 Vital Signs</h3>
            <div class="space-y-4">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-red-50 flex items-center justify-center">
                  <Thermometer :size="18" class="text-red-400" />
                </div>
                <div>
                  <p class="text-xs text-slate-400 font-bold uppercase">体温 Temperature</p>
                  <p class="text-base font-bold text-slate-400 italic">暂未记录</p>
                </div>
              </div>
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-blue-50 flex items-center justify-center">
                  <Scale :size="18" class="text-blue-400" />
                </div>
                <div>
                  <p class="text-xs text-slate-400 font-bold uppercase">体重 Weight</p>
                  <p class="text-base font-bold text-slate-400 italic">暂未记录</p>
                </div>
              </div>
            </div>
            <div class="mt-4 p-3 bg-slate-50 rounded-xl">
              <p class="text-xs text-slate-400 text-center">生命体征数据将在后续版本中对接 📡</p>
            </div>
          </div>

          <!-- 活动图表 -->
          <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
            <div class="flex justify-between items-start mb-4">
              <h3 class="font-bold text-slate-800">活动趋势 Activity</h3>
              <span class="text-xs text-slate-400 flex items-center gap-1">
                <Clock :size="12" /> 今日
              </span>
            </div>
            <div class="h-28 w-full relative">
              <svg
                viewBox="0 0 300 100"
                class="w-full h-full overflow-visible"
                preserveAspectRatio="none"
              >
                <defs>
                  <linearGradient id="chartGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" style="stop-color: #14b8a6; stop-opacity: 0.15" />
                    <stop offset="100%" style="stop-color: #14b8a6; stop-opacity: 0" />
                  </linearGradient>
                </defs>
                <path
                  d="M0,80 C50,80 50,20 100,20 C150,20 150,60 200,60 C250,60 250,10 300,10 L300,100 L0,100 Z"
                  fill="url(#chartGradient)"
                />
                <path
                  d="M0,80 C50,80 50,20 100,20 C150,20 150,60 200,60 C250,60 250,10 300,10"
                  fill="none"
                  stroke="#14B8A6"
                  stroke-width="2.5"
                  stroke-linecap="round"
                />
                <circle cx="100" cy="20" r="3.5" fill="white" stroke="#14B8A6" stroke-width="2" />
                <circle cx="200" cy="60" r="3.5" fill="white" stroke="#14B8A6" stroke-width="2" />
                <circle cx="300" cy="10" r="3.5" fill="white" stroke="#14B8A6" stroke-width="2" />
              </svg>
              <div class="flex justify-between text-[10px] text-slate-300 mt-1">
                <span>早</span><span>午</span><span>晚</span><span>现在</span>
              </div>
            </div>
            <p class="text-xs text-slate-400 text-center mt-2">活动数据来自「日记」模块记录</p>
          </div>
        </div>

        <!-- 快速导航卡片 -->
        <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
          <h3 class="font-bold text-slate-800 mb-5 flex items-center gap-2">
            <Activity :size="18" class="text-teal-500" />
            快速入口 Quick Access
          </h3>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
            <div
              class="flex flex-col items-center gap-2 p-4 bg-teal-50 rounded-2xl hover:bg-teal-100 transition-colors cursor-pointer group"
            >
              <span class="text-2xl">💉</span>
              <span class="text-xs font-bold text-teal-700 text-center">疫苗记录</span>
            </div>
            <div
              class="flex flex-col items-center gap-2 p-4 bg-blue-50 rounded-2xl hover:bg-blue-100 transition-colors cursor-pointer group"
            >
              <span class="text-2xl">💊</span>
              <span class="text-xs font-bold text-blue-700 text-center">用药管理</span>
            </div>
            <div
              class="flex flex-col items-center gap-2 p-4 bg-amber-50 rounded-2xl hover:bg-amber-100 transition-colors cursor-pointer group"
            >
              <span class="text-2xl">🍖</span>
              <span class="text-xs font-bold text-amber-700 text-center">饮食记录</span>
            </div>
            <div
              class="flex flex-col items-center gap-2 p-4 bg-purple-50 rounded-2xl hover:bg-purple-100 transition-colors cursor-pointer group"
            >
              <span class="text-2xl">📅</span>
              <span class="text-xs font-bold text-purple-700 text-center">预约提醒</span>
            </div>
          </div>
        </div>

        <!-- 宠物主人信息摘要 -->
        <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
          <h3 class="font-bold text-slate-800 mb-4">主人信息摘要 Owner Summary</h3>
          <div v-if="currentPet?.owner" class="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div class="bg-slate-50 p-3 rounded-xl">
              <p class="text-xs text-slate-400 font-bold uppercase mb-1">姓名</p>
              <p class="text-sm font-bold text-slate-700">{{ currentPet.owner.name }}</p>
            </div>
            <div class="bg-slate-50 p-3 rounded-xl">
              <p class="text-xs text-slate-400 font-bold uppercase mb-1">联系方式</p>
              <p class="text-sm font-bold text-slate-700">
                {{ currentPet.owner.contact || '暂未填写' }}
              </p>
            </div>
            <div class="bg-slate-50 p-3 rounded-xl">
              <p class="text-xs text-slate-400 font-bold uppercase mb-1">地址</p>
              <p class="text-sm font-bold text-slate-700 truncate">
                {{ currentPet.owner.address || '暂未填写' }}
              </p>
            </div>
          </div>
          <div v-else class="text-center py-4 text-slate-400 text-sm">暂无主人信息</div>
        </div>
      </div>
    </div>
  </div>
</template>
