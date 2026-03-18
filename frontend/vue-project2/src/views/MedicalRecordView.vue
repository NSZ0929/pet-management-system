<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import {
  FileText,
  Plus,
  Trash2,
  Loader2,
  AlertCircle,
  RefreshCw,
  Search,
  ChevronDown,
  ChevronUp,
  X,
  Check,
} from 'lucide-vue-next'
import {
  getMedicalRecordsByPet,
  addMedicalRecord,
  deleteMedicalRecord,
  getAllDiseases,
  type MedicalRecord,
  type Disease,
  type AddMedicalRecordPayload,
} from '../api/medicalRecord.ts'
import { currentPet, allPets, loadPets, selectPet } from '../composables/usePetData'

defineProps<{ headerTitle?: string }>()

// ── 数据 ──────────────────────────────────────────────────
const records = ref<MedicalRecord[]>([])
const diseases = ref<Disease[]>([])
const loading = ref(false)
const error = ref('')

// ── 加载 ──────────────────────────────────────────────────
const loadRecords = async () => {
  if (!currentPet.value?.id) return
  loading.value = true
  error.value = ''
  try {
    const res = await getMedicalRecordsByPet(currentPet.value.id)
    records.value = res.data
  } catch {
    error.value = '加载医疗记录失败，请检查后端连接'
  } finally {
    loading.value = false
  }
}

const loadDiseases = async () => {
  try {
    const res = await getAllDiseases()
    diseases.value = res.data
  } catch {
    console.error('加载疾病列表失败')
  }
}

onMounted(async () => {
  await loadPets()
  await loadDiseases()
  await loadRecords()
})

watch(
  () => currentPet.value?.id,
  () => loadRecords(),
)

// ── 搜索过滤 ──────────────────────────────────────────────
const searchKeyword = ref('')
const filteredRecords = computed(() => {
  if (!searchKeyword.value.trim()) return records.value
  return records.value.filter(
    (r: MedicalRecord) =>
      r.title.includes(searchKeyword.value) || r.description?.includes(searchKeyword.value),
  )
})

// ── 添加表单 ──────────────────────────────────────────────
const showAddForm = ref(false)
const submitting = ref(false)
const submitError = ref('')
const newRecord = ref<AddMedicalRecordPayload>({
  title: '',
  description: '',
  visitDate: new Date().toISOString().split('T')[0],
})
const selectedDiseaseId = ref<number | undefined>(undefined)
const diseaseSearch = ref('')
const showDiseaseDropdown = ref(false)

const filteredDiseases = computed(() => {
  if (!diseaseSearch.value.trim()) return diseases.value
  return diseases.value.filter(
    (d: Disease) =>
      d.name.includes(diseaseSearch.value) || d.category?.includes(diseaseSearch.value),
  )
})

const selectedDisease = computed(() =>
  diseases.value.find((d: Disease) => d.id === selectedDiseaseId.value),
)

const selectDisease = (disease: Disease) => {
  selectedDiseaseId.value = disease.id
  diseaseSearch.value = disease.name
  showDiseaseDropdown.value = false
}

const clearDisease = () => {
  selectedDiseaseId.value = undefined
  diseaseSearch.value = ''
}

const handleAddRecord = async () => {
  if (!currentPet.value?.id) return
  if (!newRecord.value.title.trim()) {
    submitError.value = '请填写病历标题'
    return
  }
  submitting.value = true
  submitError.value = ''
  try {
    await addMedicalRecord(newRecord.value, currentPet.value.id, selectedDiseaseId.value)
    await loadRecords()
    showAddForm.value = false
    newRecord.value = {
      title: '',
      description: '',
      visitDate: new Date().toISOString().split('T')[0],
    }
    selectedDiseaseId.value = undefined
    diseaseSearch.value = ''
  } catch {
    submitError.value = '添加失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

// ── 删除 ──────────────────────────────────────────────────
const deletingId = ref<number | null>(null)

const handleDelete = async (id: number) => {
  if (!confirm('确定要删除这条医疗记录吗？')) return
  deletingId.value = id
  try {
    await deleteMedicalRecord(id)
    await loadRecords()
  } catch {
    error.value = '删除失败，请稍后重试'
  } finally {
    deletingId.value = null
  }
}

// ── 展开详情 ──────────────────────────────────────────────
const expandedId = ref<number | null>(null)
const toggleExpand = (id: number) => {
  expandedId.value = expandedId.value === id ? null : id
}
</script>

<template>
  <div class="space-y-6 max-w-4xl mx-auto">
    <!-- 无宠物提示 -->
    <div
      v-if="!currentPet"
      class="text-center py-20 bg-white rounded-3xl shadow-lg border border-slate-100"
    >
      <span class="text-5xl">🐾</span>
      <p class="font-bold text-slate-600 mt-4 text-lg">请先在「档案」页面添加宠物</p>
    </div>

    <template v-else>
      <!-- 顶部操作栏 -->
      <div class="flex items-center justify-between gap-4 flex-wrap">
        <!-- 宠物切换 -->
        <div class="flex gap-2 flex-wrap" v-if="allPets.length > 1">
          <button
            v-for="pet in allPets"
            :key="pet.id"
            @click="selectPet(pet)"
            :class="[
              'px-3 py-1.5 rounded-xl text-sm font-bold transition-all',
              currentPet?.id === pet.id
                ? 'bg-teal-500 text-white shadow-md shadow-teal-100'
                : 'bg-white text-slate-500 border border-slate-200 hover:border-teal-300',
            ]"
          >
            🐾 {{ pet.name }}
          </button>
        </div>

        <!-- 添加按钮 -->
        <button
          @click="showAddForm = !showAddForm"
          class="flex items-center gap-2 px-4 py-2 bg-teal-500 hover:bg-teal-600 text-white rounded-xl text-sm font-bold shadow-md shadow-teal-100 transition-all ml-auto"
        >
          <component :is="showAddForm ? X : Plus" :size="16" />
          {{ showAddForm ? '取消' : '添加记录' }}
        </button>
      </div>

      <!-- 错误提示 -->
      <div
        v-if="error"
        class="flex items-center gap-3 bg-red-50 border border-red-100 text-red-500 px-5 py-3 rounded-2xl text-sm"
      >
        <AlertCircle :size="16" />{{ error }}
        <button @click="loadRecords" class="ml-auto flex items-center gap-1 font-bold">
          <RefreshCw :size="13" />重试
        </button>
      </div>

      <!-- 添加表单 -->
      <transition name="slide">
        <div
          v-if="showAddForm"
          class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100 space-y-4"
        >
          <h3 class="font-bold text-slate-800 flex items-center gap-2">
            <FileText :size="18" class="text-teal-500" /> 添加就诊记录
          </h3>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <!-- 左列 -->
            <div class="space-y-3">
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">病历标题 *</label>
                <input
                  v-model="newRecord.title"
                  type="text"
                  placeholder="e.g. 年度疫苗接种"
                  class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
                />
              </div>
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">就诊日期</label>
                <input
                  v-model="newRecord.visitDate"
                  type="date"
                  class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
                />
              </div>
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">描述 / 症状</label>
                <textarea
                  v-model="newRecord.description"
                  rows="3"
                  placeholder="描述症状、处理方式等..."
                  class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all resize-none"
                />
              </div>
            </div>

            <!-- 右列：关联疾病 -->
            <div class="space-y-3">
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">关联疾病（可选）</label>
                <div class="relative">
                  <div class="flex gap-2">
                    <input
                      v-model="diseaseSearch"
                      type="text"
                      placeholder="搜索疾病名称..."
                      @focus="showDiseaseDropdown = true"
                      @input="showDiseaseDropdown = true"
                      class="flex-1 px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
                    />
                    <button
                      v-if="selectedDiseaseId"
                      @click="clearDisease"
                      class="px-3 py-2 bg-slate-100 hover:bg-slate-200 rounded-xl text-slate-500 transition-all"
                    >
                      <X :size="14" />
                    </button>
                  </div>

                  <!-- 已选疾病 -->
                  <div
                    v-if="selectedDisease && !showDiseaseDropdown"
                    class="mt-2 flex items-center gap-2 bg-teal-50 border border-teal-100 px-3 py-2 rounded-xl"
                  >
                    <Check :size="14" class="text-teal-500" />
                    <span class="text-sm font-bold text-teal-700">{{ selectedDisease.name }}</span>
                    <span v-if="selectedDisease.category" class="text-xs text-teal-400">{{
                      selectedDisease.category
                    }}</span>
                  </div>

                  <!-- 下拉列表 -->
                  <div
                    v-if="showDiseaseDropdown && filteredDiseases.length > 0"
                    class="absolute z-10 w-full mt-1 bg-white border border-slate-100 rounded-2xl shadow-xl max-h-48 overflow-y-auto"
                  >
                    <div
                      v-for="disease in filteredDiseases"
                      :key="disease.id"
                      @click="selectDisease(disease)"
                      class="flex items-center justify-between px-4 py-2.5 hover:bg-teal-50 cursor-pointer transition-colors"
                    >
                      <span class="text-sm font-medium text-slate-700">{{ disease.name }}</span>
                      <span
                        v-if="disease.category"
                        class="text-xs text-slate-400 bg-slate-100 px-2 py-0.5 rounded-full"
                        >{{ disease.category }}</span
                      >
                    </div>
                  </div>

                  <!-- 无匹配 -->
                  <div
                    v-if="showDiseaseDropdown && filteredDiseases.length === 0"
                    class="absolute z-10 w-full mt-1 bg-white border border-slate-100 rounded-2xl shadow-xl px-4 py-3 text-sm text-slate-400 text-center"
                  >
                    暂无匹配的疾病记录
                  </div>
                </div>
              </div>

              <!-- 疾病列表预览 -->
              <div v-if="diseases.length > 0" class="bg-slate-50 rounded-xl p-3">
                <p class="text-xs font-bold text-slate-400 mb-2">常见疾病</p>
                <div class="flex flex-wrap gap-1.5">
                  <button
                    v-for="disease in diseases.slice(0, 8)"
                    :key="disease.id"
                    @click="selectDisease(disease)"
                    :class="[
                      'text-xs px-2.5 py-1 rounded-full font-medium transition-all',
                      selectedDiseaseId === disease.id
                        ? 'bg-teal-500 text-white'
                        : 'bg-white border border-slate-200 text-slate-600 hover:border-teal-300',
                    ]"
                  >
                    {{ disease.name }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 错误提示 -->
          <div
            v-if="submitError"
            class="flex items-center gap-2 text-red-500 bg-red-50 px-4 py-2.5 rounded-xl text-sm"
          >
            <AlertCircle :size="14" /> {{ submitError }}
          </div>

          <div class="flex gap-2">
            <button
              @click="handleAddRecord"
              :disabled="submitting"
              class="flex-1 py-2.5 bg-teal-500 hover:bg-teal-600 disabled:bg-teal-300 text-white rounded-xl text-sm font-bold transition-all flex items-center justify-center gap-2"
            >
              <Loader2 v-if="submitting" :size="15" class="animate-spin" />
              {{ submitting ? '提交中...' : '确认添加' }}
            </button>
            <button
              @click="showAddForm = false"
              class="px-5 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all"
            >
              取消
            </button>
          </div>
        </div>
      </transition>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-3 gap-4">
        <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 text-center">
          <p class="text-2xl font-black text-teal-500">{{ records.length }}</p>
          <p class="text-xs text-slate-400 mt-1 font-medium">总记录数</p>
        </div>
        <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 text-center">
          <p class="text-2xl font-black text-blue-500">{{ currentPet?.name }}</p>
          <p class="text-xs text-slate-400 mt-1 font-medium">当前宠物</p>
        </div>
        <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 text-center">
          <p class="text-2xl font-black text-amber-500">{{ records[0]?.visitDate ?? '—' }}</p>
          <p class="text-xs text-slate-400 mt-1 font-medium">最近就诊</p>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="relative">
        <Search :size="16" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索病历标题或描述..."
          class="w-full pl-10 pr-4 py-3 rounded-2xl border-2 border-slate-100 bg-white text-sm font-medium focus:outline-none focus:border-teal-400 transition-all shadow-sm"
        />
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="flex justify-center py-12">
        <Loader2 :size="32" class="animate-spin text-teal-400" />
      </div>

      <!-- 空状态 -->
      <div
        v-else-if="filteredRecords.length === 0"
        class="text-center py-16 bg-white rounded-3xl shadow-lg border border-slate-100"
      >
        <FileText :size="48" class="text-slate-200 mx-auto mb-4" />
        <p class="text-slate-500 font-bold text-lg">
          {{ searchKeyword ? '没有匹配的记录' : '暂无就诊记录' }}
        </p>
        <p class="text-slate-400 text-sm mt-2">
          {{ searchKeyword ? '换个关键词试试' : '点击右上角「添加记录」开始记录就诊信息' }}
        </p>
      </div>

      <!-- 记录列表 -->
      <div v-else class="space-y-3">
        <div
          v-for="record in filteredRecords"
          :key="record.id"
          class="bg-white rounded-2xl shadow-md border border-slate-100 overflow-hidden transition-all hover:shadow-lg"
        >
          <!-- 记录头部 -->
          <div class="flex items-center gap-4 p-4 cursor-pointer" @click="toggleExpand(record.id)">
            <div class="w-10 h-10 rounded-xl bg-teal-50 flex items-center justify-center shrink-0">
              <FileText :size="18" class="text-teal-500" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="font-bold text-slate-800 truncate">{{ record.title }}</p>
              <p class="text-xs text-slate-400 mt-0.5">📅 {{ record.visitDate ?? '日期未记录' }}</p>
            </div>
            <div class="flex items-center gap-2 shrink-0">
              <button
                @click.stop="handleDelete(record.id)"
                :disabled="deletingId === record.id"
                class="p-2 rounded-xl text-slate-300 hover:text-red-400 hover:bg-red-50 transition-all"
              >
                <Loader2 v-if="deletingId === record.id" :size="15" class="animate-spin" />
                <Trash2 v-else :size="15" />
              </button>
              <component
                :is="expandedId === record.id ? ChevronUp : ChevronDown"
                :size="16"
                class="text-slate-400"
              />
            </div>
          </div>

          <!-- 展开详情 -->
          <div v-if="expandedId === record.id" class="px-4 pb-4 border-t border-slate-50">
            <div class="mt-3 space-y-2">
              <div v-if="record.description" class="bg-slate-50 rounded-xl p-3">
                <p class="text-xs font-bold text-slate-400 mb-1">描述 / 症状</p>
                <p class="text-sm text-slate-600 leading-relaxed">{{ record.description }}</p>
              </div>
              <div v-else class="text-xs text-slate-400 italic text-center py-2">暂无描述</div>
            </div>
          </div>
        </div>
      </div>
    </template>
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
