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

// ── Data ──────────────────────────────────────────────────
const records = ref<MedicalRecord[]>([])
const diseases = ref<Disease[]>([])
const loading = ref(false)
const error = ref('')

// ── Load ──────────────────────────────────────────────────
const loadRecords = async () => {
  if (!currentPet.value?.id) return
  loading.value = true
  error.value = ''
  try {
    const res = await getMedicalRecordsByPet(currentPet.value.id)
    records.value = res.data
  } catch {
    error.value = 'Failed to load medical records. Check backend connection.'
  } finally {
    loading.value = false
  }
}

const loadDiseases = async () => {
  try {
    const res = await getAllDiseases()
    diseases.value = res.data
  } catch {
    console.error('Failed to load disease list')
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

// ── Search/Filter ──────────────────────────────────────────────
const searchKeyword = ref('')
const filteredRecords = computed(() => {
  if (!searchKeyword.value.trim()) return records.value
  return records.value.filter(
    (r: MedicalRecord) =>
      r.title.includes(searchKeyword.value) || r.description?.includes(searchKeyword.value),
  )
})

// ── Add Form ──────────────────────────────────────────────
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
    submitError.value = 'Please enter a record title.'
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
    submitError.value = 'Failed to add. Please try again.'
  } finally {
    submitting.value = false
  }
}

// ── Delete ──────────────────────────────────────────────────
const deletingId = ref<number | null>(null)

const handleDelete = async (id: number) => {
  if (!confirm('Are you sure you want to delete this medical record?')) return
  deletingId.value = id
  try {
    await deleteMedicalRecord(id)
    await loadRecords()
  } catch {
    error.value = 'Delete failed. Please try again.'
  } finally {
    deletingId.value = null
  }
}

// ── Expand Details ──────────────────────────────────────────────
const expandedId = ref<number | null>(null)
const toggleExpand = (id: number) => {
  expandedId.value = expandedId.value === id ? null : id
}
</script>

<template>
  <div class="space-y-6 max-w-4xl mx-auto">
    <!-- No pet warning -->
    <div
      v-if="!currentPet"
      class="text-center py-20 bg-white rounded-3xl shadow-lg border border-slate-100"
    >
      <span class="text-5xl">🐾</span>
      <p class="font-bold text-slate-600 mt-4 text-lg">Please add a pet in the Profile page first.</p>
    </div>

    <template v-else>
      <!-- Toolbar -->
      <div class="flex items-center justify-between gap-4 flex-wrap">
        <!-- Pet selector -->
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

        <!-- Add button -->
        <button
          @click="showAddForm = !showAddForm"
          class="flex items-center gap-2 px-4 py-2 bg-teal-500 hover:bg-teal-600 text-white rounded-xl text-sm font-bold shadow-md shadow-teal-100 transition-all ml-auto"
        >
          <component :is="showAddForm ? X : Plus" :size="16" />
          {{ showAddForm ? 'Cancel' : 'Add Record' }}
        </button>
      </div>

      <!-- Error -->
      <div
        v-if="error"
        class="flex items-center gap-3 bg-red-50 border border-red-100 text-red-500 px-5 py-3 rounded-2xl text-sm"
      >
        <AlertCircle :size="16" />{{ error }}
        <button @click="loadRecords" class="ml-auto flex items-center gap-1 font-bold">
          <RefreshCw :size="13" /> Retry
        </button>
      </div>

      <!-- Add form -->
      <transition name="slide">
        <div
          v-if="showAddForm"
          class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100 space-y-4"
        >
          <h3 class="font-bold text-slate-800 flex items-center gap-2">
            <FileText :size="18" class="text-teal-500" /> Add Medical Record
          </h3>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <!-- Left column -->
            <div class="space-y-3">
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">Record Title *</label>
                <input
                  v-model="newRecord.title"
                  type="text"
                  placeholder="e.g. Annual Vaccination"
                  class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
                />
              </div>
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">Visit Date</label>
                <input
                  v-model="newRecord.visitDate"
                  type="date"
                  class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
                />
              </div>
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">Description / Symptoms</label>
                <textarea
                  v-model="newRecord.description"
                  rows="3"
                  placeholder="Describe symptoms, treatment, etc..."
                  class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all resize-none"
                />
              </div>
            </div>

            <!-- Right column: linked diseases -->
            <div class="space-y-3">
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">Linked Diseases (optional)</label>
                <div class="relative">
                  <div class="flex gap-2">
                    <input
                      v-model="diseaseSearch"
                      type="text"
                      placeholder="Search disease name..."
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

                  <!-- Selected disease -->
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

                  <!-- Dropdown -->
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

                  <!-- No match -->
                  <div
                    v-if="showDiseaseDropdown && filteredDiseases.length === 0"
                    class="absolute z-10 w-full mt-1 bg-white border border-slate-100 rounded-2xl shadow-xl px-4 py-3 text-sm text-slate-400 text-center"
                  >
                    No matching diseases found
                  </div>
                </div>
              </div>

              <!-- Disease list preview -->
              <div v-if="diseases.length > 0" class="bg-slate-50 rounded-xl p-3">
                <p class="text-xs font-bold text-slate-400 mb-2">Common Diseases</p>
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

          <!-- Submit error -->
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
              {{ submitting ? 'Submitting...' : 'Confirm' }}
            </button>
            <button
              @click="showAddForm = false"
              class="px-5 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all"
            >
              Cancel
            </button>
          </div>
        </div>
      </transition>

      <!-- Stats cards -->
      <div class="grid grid-cols-3 gap-4">
        <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 text-center">
          <p class="text-2xl font-black text-teal-500">{{ records.length }}</p>
          <p class="text-xs text-slate-400 mt-1 font-medium">Total Records</p>
        </div>
        <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 text-center">
          <p class="text-2xl font-black text-blue-500">{{ currentPet?.name }}</p>
          <p class="text-xs text-slate-400 mt-1 font-medium">Current Pet</p>
        </div>
        <div class="bg-white rounded-2xl p-4 shadow-md border border-slate-100 text-center">
          <p class="text-2xl font-black text-amber-500">{{ records[0]?.visitDate ?? '—' }}</p>
          <p class="text-xs text-slate-400 mt-1 font-medium">Latest Visit</p>
        </div>
      </div>

      <!-- Search bar -->
      <div class="relative">
        <Search :size="16" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="Search by title or description..."
          class="w-full pl-10 pr-4 py-3 rounded-2xl border-2 border-slate-100 bg-white text-sm font-medium focus:outline-none focus:border-teal-400 transition-all shadow-sm"
        />
      </div>

      <!-- Loading -->
      <div v-if="loading" class="flex justify-center py-12">
        <Loader2 :size="32" class="animate-spin text-teal-400" />
      </div>

      <!-- Empty state -->
      <div
        v-else-if="filteredRecords.length === 0"
        class="text-center py-16 bg-white rounded-3xl shadow-lg border border-slate-100"
      >
        <FileText :size="48" class="text-slate-200 mx-auto mb-4" />
        <p class="text-slate-500 font-bold text-lg">
          {{ searchKeyword ? 'No matching records' : 'No medical records yet' }}
        </p>
        <p class="text-slate-400 text-sm mt-2">
          {{ searchKeyword ? 'Try a different keyword' : 'Click "Add Record" to start logging visits' }}
        </p>
      </div>

      <!-- Records list -->
      <div v-else class="space-y-3">
        <div
          v-for="record in filteredRecords"
          :key="record.id"
          class="bg-white rounded-2xl shadow-md border border-slate-100 overflow-hidden transition-all hover:shadow-lg"
        >
          <!-- Record header -->
          <div class="flex items-center gap-4 p-4 cursor-pointer" @click="toggleExpand(record.id)">
            <div class="w-10 h-10 rounded-xl bg-teal-50 flex items-center justify-center shrink-0">
              <FileText :size="18" class="text-teal-500" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="font-bold text-slate-800 truncate">{{ record.title }}</p>
              <p class="text-xs text-slate-400 mt-0.5">📅 {{ record.visitDate ?? 'Date not recorded' }}</p>
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

          <!-- Expanded details -->
          <div v-if="expandedId === record.id" class="px-4 pb-4 border-t border-slate-50">
            <div class="mt-3 space-y-2">
              <div v-if="record.description" class="bg-slate-50 rounded-xl p-3">
                <p class="text-xs font-bold text-slate-400 mb-1">Description / Symptoms</p>
                <p class="text-sm text-slate-600 leading-relaxed">{{ record.description }}</p>
              </div>
              <div v-else class="text-xs text-slate-400 italic text-center py-2">No description available.</div>
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