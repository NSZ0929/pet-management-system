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
  Pencil,
  FlaskConical,
} from 'lucide-vue-next'
import {
  getMedicalRecordsByPet,
  addMedicalRecord,
  updateMedicalRecord,
  deleteMedicalRecord,
  searchMedicalRecords,
  getAllDiseases,
  searchDiseases,
  addDisease,
  updateDisease,
  deleteDisease,
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
const diseaseLoading = ref(false)
const error = ref('')
const searchKeyword = ref('')
const diseaseSearch = ref('')
const editDiseaseSearch = ref('')

// ── Load ──────────────────────────────────────────────────
const loadRecords = async (keyword = '') => {
  if (!currentPet.value?.id) return
  loading.value = true
  error.value = ''
  try {
    const res = keyword.trim()
      ? await searchMedicalRecords(currentPet.value.id, keyword.trim())
      : await getMedicalRecordsByPet(currentPet.value.id)
    records.value = res.data
  } catch {
    error.value = 'Failed to load medical records. Check backend connection.'
  } finally {
    loading.value = false
  }
}

const retryLoadRecords = async () => {
  await loadRecords()
}

const loadDiseases = async () => {
  diseaseLoading.value = true
  try {
    const res = await getAllDiseases()
    diseases.value = res.data
  } catch {
    console.error('Failed to load disease list')
  } finally {
    diseaseLoading.value = false
  }
}

const diseaseSearchResults = ref<Disease[]>([])
const editDiseaseSearchResults = ref<Disease[]>([])

const searchDiseasesRemotely = async (keyword: string, mode: 'add' | 'edit') => {
  if (!keyword.trim()) {
    if (mode === 'add') diseaseSearchResults.value = []
    else editDiseaseSearchResults.value = []
    return
  }

  try {
    const res = await searchDiseases(keyword.trim())
    if (mode === 'add') diseaseSearchResults.value = res.data
    else editDiseaseSearchResults.value = res.data
  } catch {
    console.error('Failed to search diseases')
    if (mode === 'add') diseaseSearchResults.value = []
    else editDiseaseSearchResults.value = []
  }
}

onMounted(async () => {
  await loadPets()
  await loadDiseases()
  await loadRecords()
})

let recordSearchTimer: ReturnType<typeof setTimeout> | null = null
watch(
  [() => currentPet.value?.id, searchKeyword],
  ([petId, keyword]) => {
    if (!petId) return
    if (recordSearchTimer) clearTimeout(recordSearchTimer)
    recordSearchTimer = setTimeout(() => {
      void loadRecords(keyword ?? '')
    }, 250)
  },
)

let diseaseSearchTimer: ReturnType<typeof setTimeout> | null = null
watch(diseaseSearch, (keyword) => {
  if (diseaseSearchTimer) clearTimeout(diseaseSearchTimer)
  diseaseSearchTimer = setTimeout(() => {
    void searchDiseasesRemotely(keyword, 'add')
  }, 250)
})

let editDiseaseSearchTimer: ReturnType<typeof setTimeout> | null = null
watch(editDiseaseSearch, (keyword) => {
  if (editDiseaseSearchTimer) clearTimeout(editDiseaseSearchTimer)
  editDiseaseSearchTimer = setTimeout(() => {
    void searchDiseasesRemotely(keyword, 'edit')
  }, 250)
})

// ── Search/Filter ──────────────────────────────────────────────
const filteredRecords = computed(() => records.value)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Add Record Form
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const showAddForm = ref(false)
const submitting = ref(false)
const submitError = ref('')
const newRecord = ref<AddMedicalRecordPayload>({
  title: '',
  description: '',
  visitDate: new Date().toISOString().split('T')[0],
})
const selectedDiseaseId = ref<number | undefined>(undefined)
const showDiseaseDropdown = ref(false)

const filteredDiseases = computed(() => {
  if (!diseaseSearch.value.trim()) return diseases.value
  return diseaseSearchResults.value
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
  diseaseSearchResults.value = []
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
    diseaseSearchResults.value = []
  } catch {
    submitError.value = 'Failed to add. Please try again.'
  } finally {
    submitting.value = false
  }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Edit Record Form
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const editingRecord = ref<MedicalRecord | null>(null)
const editRecord = ref<AddMedicalRecordPayload>({ title: '', description: '', visitDate: '' })
const editDiseaseId = ref<number | undefined>(undefined)
const showEditDiseaseDropdown = ref(false)
const editSubmitting = ref(false)
const editError = ref('')

const filteredEditDiseases = computed(() => {
  if (!editDiseaseSearch.value.trim()) return diseases.value
  return editDiseaseSearchResults.value
})

const selectedEditDisease = computed(() =>
  diseases.value.find((d: Disease) => d.id === editDiseaseId.value),
)

const openEditRecord = (record: MedicalRecord) => {
  editingRecord.value = record
  editRecord.value = {
    title: record.title,
    description: record.description ?? '',
    visitDate: record.visitDate ?? new Date().toISOString().split('T')[0],
  }
  editDiseaseId.value = record.diseaseId ?? undefined
  editDiseaseSearch.value = record.diseaseName ?? ''
  editDiseaseSearchResults.value = []
  editError.value = ''
  showAddForm.value = false
}

const closeEditRecord = () => {
  editingRecord.value = null
  editError.value = ''
}

const selectEditDisease = (disease: Disease) => {
  editDiseaseId.value = disease.id
  editDiseaseSearch.value = disease.name
  showEditDiseaseDropdown.value = false
}

const clearEditDisease = () => {
  editDiseaseId.value = undefined
  editDiseaseSearch.value = ''
  editDiseaseSearchResults.value = []
}

const handleEditRecord = async () => {
  if (!editingRecord.value) return
  if (!editRecord.value.title.trim()) {
    editError.value = 'Please enter a record title.'
    return
  }
  editSubmitting.value = true
  editError.value = ''
  try {
    await updateMedicalRecord(editingRecord.value.id, editRecord.value, editDiseaseId.value)
    await loadRecords()
    closeEditRecord()
  } catch {
    editError.value = 'Failed to update. Please try again.'
  } finally {
    editSubmitting.value = false
  }
}

// ── Delete Record ──────────────────────────────────────────────────
const deletingId = ref<number | null>(null)

const handleDelete = async (id: number) => {
  if (!confirm('Are you sure you want to delete this medical record?')) return
  deletingId.value = id
  try {
    await deleteMedicalRecord(id)
    if (editingRecord.value?.id === id) closeEditRecord()
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

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Disease Management
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
const showDiseasePanel = ref(false)
const showDiseaseAddForm = ref(false)
const newDisease = ref({ name: '', category: '', description: '' })
const diseaseAddSubmitting = ref(false)
const diseaseAddError = ref('')
const diseaseDeleteError = ref('')

// Edit disease
const editingDisease = ref<Disease | null>(null)
const editDiseaseForm = ref({ name: '', category: '', description: '' })
const diseaseEditSubmitting = ref(false)
const diseaseEditError = ref('')

const diseaseFilterKeyword = ref('')
const filteredDiseaseList = computed(() => {
  if (!diseaseFilterKeyword.value.trim()) return diseases.value
  return diseases.value.filter(
    d =>
      d.name.toLowerCase().includes(diseaseFilterKeyword.value.toLowerCase()) ||
      (d.category ?? '').toLowerCase().includes(diseaseFilterKeyword.value.toLowerCase()),
  )
})

const handleAddDisease = async () => {
  if (!newDisease.value.name.trim()) {
    diseaseAddError.value = 'Disease name cannot be empty.'
    return
  }
  diseaseAddSubmitting.value = true
  diseaseAddError.value = ''
  try {
    await addDisease({
      name: newDisease.value.name.trim(),
      category: newDisease.value.category.trim() || undefined,
      description: newDisease.value.description.trim() || undefined,
    })
    await loadDiseases()
    newDisease.value = { name: '', category: '', description: '' }
    showDiseaseAddForm.value = false
  } catch {
    diseaseAddError.value = 'Failed to add disease.'
  } finally {
    diseaseAddSubmitting.value = false
  }
}

const openEditDisease = (disease: Disease) => {
  editingDisease.value = disease
  editDiseaseForm.value = {
    name: disease.name,
    category: disease.category ?? '',
    description: disease.description ?? '',
  }
  diseaseEditError.value = ''
}

const closeEditDisease = () => {
  editingDisease.value = null
  diseaseEditError.value = ''
}

const handleEditDisease = async () => {
  if (!editingDisease.value) return
  if (!editDiseaseForm.value.name.trim()) {
    diseaseEditError.value = 'Disease name cannot be empty.'
    return
  }
  diseaseEditSubmitting.value = true
  diseaseEditError.value = ''
  try {
    await updateDisease(editingDisease.value.id, {
      name: editDiseaseForm.value.name.trim(),
      category: editDiseaseForm.value.category.trim() || undefined,
      description: editDiseaseForm.value.description.trim() || undefined,
    })
    await loadDiseases()
    closeEditDisease()
  } catch {
    diseaseEditError.value = 'Failed to update disease.'
  } finally {
    diseaseEditSubmitting.value = false
  }
}

const handleDeleteDisease = async (id: number) => {
  diseaseDeleteError.value = ''
  try {
    await deleteDisease(id)
    await loadDiseases()
  } catch (err: unknown) {
    let msg = ''

    if (typeof err === 'object' && err !== null) {
      const e = err as {
        response?: { data?: { message?: string } }
        message?: string
      }

      msg = e.response?.data?.message || e.message || ''
    }

    diseaseDeleteError.value =
      msg || 'Delete failed. This disease may be linked to existing records.'
  }
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

        <!-- Action buttons -->
        <div class="flex gap-2 ml-auto">
          <button
            @click="showDiseasePanel = !showDiseasePanel; showAddForm = false; closeEditRecord()"
            :class="[
              'flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-bold transition-all',
              showDiseasePanel
                ? 'bg-violet-500 text-white shadow-md shadow-violet-100'
                : 'bg-violet-50 text-violet-600 hover:bg-violet-100',
            ]"
          >
            <FlaskConical :size="16" />
            Diseases
          </button>
          <button
            @click="showAddForm = !showAddForm; showDiseasePanel = false; closeEditRecord()"
            class="flex items-center gap-2 px-4 py-2 bg-teal-500 hover:bg-teal-600 text-white rounded-xl text-sm font-bold shadow-md shadow-teal-100 transition-all"
          >
            <component :is="showAddForm ? X : Plus" :size="16" />
            {{ showAddForm ? 'Cancel' : 'Add Record' }}
          </button>
        </div>
      </div>

      <!-- Error -->
      <div
        v-if="error"
        class="flex items-center gap-3 bg-red-50 border border-red-100 text-red-500 px-5 py-3 rounded-2xl text-sm"
      >
        <AlertCircle :size="16" />{{ error }}
        <button @click="retryLoadRecords" class="ml-auto flex items-center gap-1 font-bold">
          <RefreshCw :size="13" /> Retry
        </button>
      </div>

      <!-- Disease Management Panel -->
      <transition name="slide">
        <div v-if="showDiseasePanel" class="bg-white rounded-3xl p-6 shadow-lg border border-violet-100 space-y-4">
          <div class="flex items-center justify-between">
            <h3 class="font-bold text-slate-800 flex items-center gap-2">
              <FlaskConical :size="18" class="text-violet-500" /> Disease Management
            </h3>
            <button
              @click="showDiseaseAddForm = !showDiseaseAddForm; closeEditDisease()"
              class="flex items-center gap-1.5 text-sm font-bold text-violet-600 bg-violet-50 hover:bg-violet-100 px-3 py-1.5 rounded-xl transition-all"
            >
              <Plus :size="14" /> Add Disease
            </button>
          </div>

          <!-- Add disease form -->
          <transition name="slide">
            <div v-if="showDiseaseAddForm" class="bg-slate-50 rounded-2xl p-4 space-y-3 border border-slate-200">
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="text-xs font-bold text-slate-500 block mb-1">Name *</label>
                  <input v-model="newDisease.name" type="text" placeholder="e.g. Parvovirus" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-sm font-medium focus:outline-none focus:border-violet-400 transition-all" />
                </div>
                <div>
                  <label class="text-xs font-bold text-slate-500 block mb-1">Category</label>
                  <input v-model="newDisease.category" type="text" placeholder="e.g. Infectious, Internal" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-sm font-medium focus:outline-none focus:border-violet-400 transition-all" />
                </div>
              </div>
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">Description</label>
                <input v-model="newDisease.description" type="text" placeholder="Brief description (optional)" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-sm font-medium focus:outline-none focus:border-violet-400 transition-all" />
              </div>
              <p v-if="diseaseAddError" class="text-xs text-red-500">{{ diseaseAddError }}</p>
              <div class="flex gap-2">
                <button @click="handleAddDisease" :disabled="diseaseAddSubmitting" class="flex-1 py-2 bg-violet-500 hover:bg-violet-600 disabled:opacity-50 text-white rounded-xl text-sm font-bold transition-all flex items-center justify-center gap-2">
                  <Loader2 v-if="diseaseAddSubmitting" :size="13" class="animate-spin" /> Confirm
                </button>
                <button @click="showDiseaseAddForm = false" class="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all">Cancel</button>
              </div>
            </div>
          </transition>

          <!-- Edit disease form -->
          <transition name="slide">
            <div v-if="editingDisease" class="bg-violet-50 rounded-2xl p-4 space-y-3 border border-violet-200">
              <p class="text-sm font-extrabold text-violet-700">Edit Disease <span class="font-normal text-violet-400">#{{ editingDisease.id }}</span></p>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="text-xs font-bold text-slate-500 block mb-1">Name *</label>
                  <input v-model="editDiseaseForm.name" type="text" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-sm font-medium focus:outline-none focus:border-violet-400 transition-all" />
                </div>
                <div>
                  <label class="text-xs font-bold text-slate-500 block mb-1">Category</label>
                  <input v-model="editDiseaseForm.category" type="text" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-sm font-medium focus:outline-none focus:border-violet-400 transition-all" />
                </div>
              </div>
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">Description</label>
                <input v-model="editDiseaseForm.description" type="text" class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-white text-sm font-medium focus:outline-none focus:border-violet-400 transition-all" />
              </div>
              <p v-if="diseaseEditError" class="text-xs text-red-500">{{ diseaseEditError }}</p>
              <div class="flex gap-2">
                <button @click="handleEditDisease" :disabled="diseaseEditSubmitting" class="flex-1 py-2 bg-violet-500 hover:bg-violet-600 disabled:opacity-50 text-white rounded-xl text-sm font-bold transition-all flex items-center justify-center gap-2">
                  <Loader2 v-if="diseaseEditSubmitting" :size="13" class="animate-spin" /> Save Changes
                </button>
                <button @click="closeEditDisease" class="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all flex items-center justify-center">
                  <X :size="14" />
                </button>
              </div>
            </div>
          </transition>

          <!-- Delete error -->
          <div v-if="diseaseDeleteError" class="flex items-center gap-2 bg-red-50 text-red-600 text-sm px-4 py-3 rounded-xl">
            <AlertCircle :size="15" />
            {{ diseaseDeleteError }}
            <button @click="diseaseDeleteError = ''" class="ml-auto text-xs underline">Dismiss</button>
          </div>

          <!-- Disease filter + list -->
          <div class="relative">
            <Search :size="14" class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input v-model="diseaseFilterKeyword" type="text" placeholder="Filter diseases..." class="w-full pl-8 pr-3 py-2 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-violet-400 transition-all" />
          </div>

          <div v-if="diseaseLoading" class="flex justify-center py-4">
            <Loader2 :size="20" class="animate-spin text-violet-400" />
          </div>
          <div v-else-if="diseases.length === 0" class="text-center py-4 text-slate-400 text-sm">No diseases found. Add one above.</div>
          <div v-else class="space-y-2 max-h-64 overflow-y-auto pr-1">
            <div
              v-for="disease in filteredDiseaseList"
              :key="disease.id"
              :class="[
                'flex items-center justify-between px-4 py-2.5 rounded-xl border transition-all',
                editingDisease?.id === disease.id ? 'border-violet-300 bg-violet-50/60' : 'border-slate-100 bg-slate-50',
              ]"
            >
              <div class="min-w-0">
                <p class="text-sm font-bold text-slate-800 truncate">{{ disease.name }}</p>
                <p v-if="disease.category" class="text-xs text-slate-400">{{ disease.category }}</p>
              </div>
              <div class="flex items-center gap-2 flex-shrink-0">
                <span class="text-xs text-slate-400">#{{ disease.id }}</span>
                <button @click="openEditDisease(disease); showDiseaseAddForm = false" class="p-1 text-slate-300 hover:text-violet-400 transition-colors" title="Edit">
                  <Pencil :size="14" />
                </button>
                <button @click="handleDeleteDisease(disease.id)" class="p-1 text-slate-300 hover:text-red-400 transition-colors" title="Delete">
                  <Trash2 :size="14" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </transition>

      <!-- Add Record Form -->
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
                <label class="text-xs font-bold text-slate-500 block mb-1">Linked Disease (optional)</label>
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

                  <div
                    v-if="selectedDisease && !showDiseaseDropdown"
                    class="mt-2 flex items-center gap-2 bg-teal-50 border border-teal-100 px-3 py-2 rounded-xl"
                  >
                    <Check :size="14" class="text-teal-500" />
                    <span class="text-sm font-bold text-teal-700">{{ selectedDisease.name }}</span>
                    <span v-if="selectedDisease.category" class="text-xs text-teal-400">{{ selectedDisease.category }}</span>
                  </div>

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
                      <span v-if="disease.category" class="text-xs text-slate-400 bg-slate-100 px-2 py-0.5 rounded-full">{{ disease.category }}</span>
                    </div>
                  </div>

                  <div
  v-if="showDiseaseDropdown && diseaseSearch.trim() && filteredDiseases.length === 0"
  class="absolute z-10 w-full mt-1 bg-white border border-slate-100 rounded-2xl shadow-xl px-4 py-3 text-sm text-slate-400 text-center"
>
  No matching diseases found
</div>
                </div>
              </div>

              <!-- Disease quick-pick chips -->
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

      <!-- Edit Record Form -->
      <transition name="slide">
        <div
          v-if="editingRecord"
          class="bg-white rounded-3xl p-6 shadow-lg border border-blue-200 space-y-4"
        >
          <h3 class="font-bold text-blue-700 flex items-center gap-2">
            <Pencil :size="18" class="text-blue-400" />
            Edit Medical Record
            <span class="font-normal text-blue-400 text-sm">#{{ editingRecord.id }}</span>
          </h3>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="space-y-3">
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">Record Title *</label>
                <input
                  v-model="editRecord.title"
                  type="text"
                  class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-blue-400 transition-all"
                />
              </div>
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">Visit Date</label>
                <input
                  v-model="editRecord.visitDate"
                  type="date"
                  class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-blue-400 transition-all"
                />
              </div>
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">Description / Symptoms</label>
                <textarea
                  v-model="editRecord.description"
                  rows="3"
                  class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-blue-400 transition-all resize-none"
                />
              </div>
            </div>

            <div class="space-y-3">
              <div>
                <label class="text-xs font-bold text-slate-500 block mb-1">Update Linked Disease (optional)</label>
                <div class="relative">
                  <div class="flex gap-2">
                    <input
                      v-model="editDiseaseSearch"
                      type="text"
                      placeholder="Search disease to link..."
                      @focus="showEditDiseaseDropdown = true"
                      @input="showEditDiseaseDropdown = true"
                      class="flex-1 px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-blue-400 transition-all"
                    />
                    <button
                      v-if="editDiseaseId"
                      @click="clearEditDisease"
                      class="px-3 py-2 bg-slate-100 hover:bg-slate-200 rounded-xl text-slate-500 transition-all"
                    >
                      <X :size="14" />
                    </button>
                  </div>

                  <div
                    v-if="selectedEditDisease && !showEditDiseaseDropdown"
                    class="mt-2 flex items-center gap-2 bg-blue-50 border border-blue-100 px-3 py-2 rounded-xl"
                  >
                    <Check :size="14" class="text-blue-500" />
                    <span class="text-sm font-bold text-blue-700">{{ selectedEditDisease.name }}</span>
                    <span v-if="selectedEditDisease.category" class="text-xs text-blue-400">{{ selectedEditDisease.category }}</span>
                  </div>

                  <div
                    v-if="showEditDiseaseDropdown && filteredEditDiseases.length > 0"
                    class="absolute z-10 w-full mt-1 bg-white border border-slate-100 rounded-2xl shadow-xl max-h-48 overflow-y-auto"
                  >
                    <div
                      v-for="disease in filteredEditDiseases"
                      :key="disease.id"
                      @click="selectEditDisease(disease)"
                      class="flex items-center justify-between px-4 py-2.5 hover:bg-blue-50 cursor-pointer transition-colors"
                    >
                      <span class="text-sm font-medium text-slate-700">{{ disease.name }}</span>
                      <span v-if="disease.category" class="text-xs text-slate-400 bg-slate-100 px-2 py-0.5 rounded-full">{{ disease.category }}</span>
                    </div>
                  </div>

                  <div
  v-if="showEditDiseaseDropdown && editDiseaseSearch.trim() && filteredEditDiseases.length === 0"
  class="absolute z-10 w-full mt-1 bg-white border border-slate-100 rounded-2xl shadow-xl px-4 py-3 text-sm text-slate-400 text-center"
>
  No matching diseases found
</div>
                </div>
                <p class="text-xs text-slate-400 mt-1.5">Leave blank to keep the current linked disease unchanged.</p>
              </div>
            </div>
          </div>

          <div v-if="editError" class="flex items-center gap-2 text-red-500 bg-red-50 px-4 py-2.5 rounded-xl text-sm">
            <AlertCircle :size="14" /> {{ editError }}
          </div>

          <div class="flex gap-2">
            <button
              @click="handleEditRecord"
              :disabled="editSubmitting"
              class="flex-1 py-2.5 bg-blue-500 hover:bg-blue-600 disabled:bg-blue-300 text-white rounded-xl text-sm font-bold transition-all flex items-center justify-center gap-2"
            >
              <Loader2 v-if="editSubmitting" :size="15" class="animate-spin" />
              {{ editSubmitting ? 'Saving...' : 'Save Changes' }}
            </button>
            <button
              @click="closeEditRecord"
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
          :class="[
            'bg-white rounded-2xl shadow-md border overflow-hidden transition-all hover:shadow-lg',
            editingRecord?.id === record.id ? 'border-blue-300' : 'border-slate-100',
          ]"
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
            <div class="flex items-center gap-1 shrink-0" @click.stop>
              <button
                @click="openEditRecord(record)"
                class="p-2 rounded-xl text-slate-300 hover:text-blue-400 hover:bg-blue-50 transition-all"
                title="Edit"
              >
                <Pencil :size="15" />
              </button>
              <button
                @click="handleDelete(record.id)"
                :disabled="deletingId === record.id"
                class="p-2 rounded-xl text-slate-300 hover:text-red-400 hover:bg-red-50 transition-all"
              >
                <Loader2 v-if="deletingId === record.id" :size="15" class="animate-spin" />
                <Trash2 v-else :size="15" />
              </button>
            </div>
            <component
              :is="expandedId === record.id ? ChevronUp : ChevronDown"
              :size="16"
              class="text-slate-400 shrink-0"
            />
          </div>

          <!-- Expanded details -->
          <div v-if="expandedId === record.id" class="px-4 pb-4 border-t border-slate-50">
            <div class="mt-3 space-y-2">
              <div v-if="record.description" class="bg-slate-50 rounded-xl p-3">
                <p class="text-xs font-bold text-slate-400 mb-1">Description / Symptoms</p>
                <p class="text-sm text-slate-600 leading-relaxed">{{ record.description }}</p>
              </div>
              <div v-if="record.diseaseName" class="bg-blue-50 rounded-xl p-3">
                <p class="text-xs font-bold text-blue-400 mb-1">Linked Disease</p>
                <p class="text-sm font-semibold text-blue-700">{{ record.diseaseName }}</p>
                <p v-if="record.diseaseCategory" class="text-xs text-blue-500 mt-1">
                  {{ record.diseaseCategory }}
                </p>
              </div>
              <div v-if="!record.description && !record.diseaseName" class="text-xs text-slate-400 italic text-center py-2">No additional details available.</div>
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
  max-height: 1200px;
}
</style>