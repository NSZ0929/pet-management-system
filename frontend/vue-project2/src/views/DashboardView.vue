<script setup lang="ts">
import { onMounted, computed, ref, watch } from 'vue'
import {
  Thermometer,
  Scale,
  CheckCircle,
  FileText,
  Loader2,
  AlertCircle,
  PawPrint,
  ChevronDown,
  Activity,
  Plus,
  X,
  Trash2,
} from 'lucide-vue-next'
import { usePetData } from '../composables/usePetData'
import { addVitalSign, getVitalSigns, deleteVitalSign, type VitalSign } from '../api/dailyLog'
import PetAvatar from '../components/PetAvatar.vue'

defineProps<{ headerTitle?: string }>()
const emit = defineEmits<{
  navigate: [view: 'dashboard' | 'profile' | 'daily' | 'appointment' | 'settings' | 'medical']
}>()

const { currentPet, allPets, medicalRecords, isLoadingPetData, petDataError, loadPets, selectPet } =
  usePetData()

onMounted(() => {
  loadPets()
})

const recentRecords = computed(() => medicalRecords.value)
const currentYear = new Date().getFullYear()

// ── Vital Signs ──────────────────────────────────────────────
const vitalSigns = ref<VitalSign[]>([])
const vitalLoading = ref(false)
const showVitalForm = ref(false)
const vitalSubmitting = ref(false)
const deletingVitalId = ref<number | null>(null)
const newVital = ref({ temperature: '', weight: '' })
const latestVital = computed(() => vitalSigns.value[0] ?? null)

const loadVitalSigns = async () => {
  if (!currentPet.value?.id) return
  vitalLoading.value = true
  try {
    const res = await getVitalSigns(currentPet.value.id)
    vitalSigns.value = res.data.sort(
      (a, b) => new Date(b.recordTime ?? 0).getTime() - new Date(a.recordTime ?? 0).getTime(),
    )
  } catch {
    console.error('Failed to load vital signs')
  } finally {
    vitalLoading.value = false
  }
}

const submitVitalSign = async () => {
  if (!currentPet.value?.id) return
  if (!newVital.value.temperature && !newVital.value.weight) return
  vitalSubmitting.value = true
  try {
    await addVitalSign({
      petId: currentPet.value.id,
      temperature: newVital.value.temperature ? Number(newVital.value.temperature) : undefined,
      weight: newVital.value.weight ? Number(newVital.value.weight) : undefined,
    })
    await loadVitalSigns()
    showVitalForm.value = false
    newVital.value = { temperature: '', weight: '' }
  } catch {
    console.error('Failed to add vital sign')
  } finally {
    vitalSubmitting.value = false
  }
}

const handleDeleteVital = async (id: number) => {
  deletingVitalId.value = id
  try {
    await deleteVitalSign(id)
    await loadVitalSigns()
  } catch {
    console.error('Delete failed')
  } finally {
    deletingVitalId.value = null
  }
}

watch(
  () => currentPet.value?.id,
  () => {
    loadVitalSigns()
  },
  { immediate: true },
)
</script>

<template>
  <div class="space-y-6">
    <div
      v-if="isLoadingPetData"
      class="flex items-center justify-center py-20 gap-3 text-slate-400"
    >
      <Loader2 :size="24" class="animate-spin text-teal-500" />
      <span class="text-sm font-medium">Loading pet data...</span>
    </div>

    <div
      v-else-if="petDataError"
      class="flex items-center gap-3 bg-red-50 border border-red-100 text-red-500 px-5 py-4 rounded-2xl"
    >
      <AlertCircle :size="18" class="shrink-0" />
      <p class="text-sm font-medium">{{ petDataError }}</p>
    </div>

    <div
      v-else-if="allPets.length === 0"
      class="text-center py-20 bg-white rounded-3xl shadow-lg border border-slate-100"
    >
      <PawPrint :size="48" class="text-slate-200 mx-auto mb-4" />
      <p class="text-slate-500 font-bold text-lg">No pet profiles yet</p>
      <p class="text-slate-400 text-sm mt-2">Please add your pet in the Profile page first. 🐾</p>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6 items-stretch">
      <!-- Left column -->
      <div class="lg:col-span-1 flex flex-col gap-6">
        <div
          v-if="allPets.length > 1"
          class="bg-white rounded-2xl p-4 shadow-md border border-slate-100"
        >
          <label class="block text-xs font-bold text-slate-400 mb-2 uppercase tracking-wider"
            >Select Pet</label
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

        <div class="bg-white rounded-3xl p-5 shadow-lg border border-slate-100 flex-1">
          <PetAvatar :avatarUrl="currentPet?.avatar" :editable="false" size="md" class="mb-4" />
          <div class="space-y-2 px-1 pb-1">
            <div class="flex items-center justify-between bg-slate-50 p-3 rounded-xl">
              <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Name</span>
              <span class="font-bold text-slate-700">{{ currentPet?.name ?? '—' }}</span>
            </div>
            <div class="flex items-center justify-between bg-slate-50 p-3 rounded-xl">
              <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Breed</span>
              <span class="font-bold text-slate-700">{{ currentPet?.breed || 'Not set' }}</span>
            </div>
            <div class="flex items-center justify-between bg-slate-50 p-3 rounded-xl">
              <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Age</span>
              <span class="font-bold text-slate-700">{{
                currentPet?.age != null ? `${currentPet.age} yr` : 'Not set'
              }}</span>
            </div>
            <div class="flex items-center justify-between bg-slate-50 p-3 rounded-xl">
              <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Owner</span>
              <span class="font-bold text-slate-700">{{
                currentPet?.owner?.name || 'Not set'
              }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Right column -->
      <div class="lg:col-span-2 space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 items-stretch">
          <!-- Vital Signs -->
          <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100 flex flex-col">
            <div class="flex items-center justify-between mb-4">
              <h3 class="font-bold text-slate-800">Vital Signs</h3>
              <button
                @click="showVitalForm = !showVitalForm"
                class="flex items-center gap-1 text-xs font-bold px-3 py-1.5 rounded-xl transition-all"
                :class="
                  showVitalForm
                    ? 'bg-slate-100 text-slate-500'
                    : 'bg-teal-50 text-teal-600 hover:bg-teal-100'
                "
              >
                <component :is="showVitalForm ? X : Plus" :size="13" />
                {{ showVitalForm ? 'Cancel' : 'Record' }}
              </button>
            </div>

            <!-- Add form -->
            <div v-if="showVitalForm" class="mb-4 p-3 bg-slate-50 rounded-xl space-y-3">
              <div class="grid grid-cols-2 gap-2">
                <div>
                  <label class="text-xs font-bold text-slate-400 block mb-1">Temperature (°C)</label>
                  <input
                    v-model="newVital.temperature"
                    type="number"
                    step="0.1"
                    placeholder="38.5"
                    class="w-full px-3 py-2 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-teal-400 bg-white"
                  />
                </div>
                <div>
                  <label class="text-xs font-bold text-slate-400 block mb-1">Weight (kg)</label>
                  <input
                    v-model="newVital.weight"
                    type="number"
                    step="0.1"
                    placeholder="5.0"
                    class="w-full px-3 py-2 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-teal-400 bg-white"
                  />
                </div>
              </div>
              <button
                @click="submitVitalSign"
                :disabled="vitalSubmitting"
                class="w-full py-2 bg-teal-500 hover:bg-teal-600 disabled:bg-teal-300 text-white rounded-xl text-sm font-bold transition-all flex items-center justify-center gap-2"
              >
                <Loader2 v-if="vitalSubmitting" :size="13" class="animate-spin" />
                {{ vitalSubmitting ? 'Saving...' : '✓ Save' }}
              </button>
            </div>

            <!-- Latest data -->
            <div v-if="vitalLoading" class="flex justify-center py-4">
              <Loader2 :size="20" class="animate-spin text-teal-400" />
            </div>
            <div v-else class="space-y-3">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-red-50 flex items-center justify-center">
                  <Thermometer :size="18" class="text-red-400" />
                </div>
                <div>
                  <p class="text-xs text-slate-400 font-bold uppercase">Temperature</p>
                  <p v-if="latestVital?.temperature" class="text-base font-bold text-slate-700">
                    {{ latestVital.temperature }} °C
                  </p>
                  <p v-else class="text-base font-bold text-slate-400 italic">No data yet</p>
                </div>
              </div>
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-blue-50 flex items-center justify-center">
                  <Scale :size="18" class="text-blue-400" />
                </div>
                <div>
                  <p class="text-xs text-slate-400 font-bold uppercase">Weight</p>
                  <p v-if="latestVital?.weight" class="text-base font-bold text-slate-700">
                    {{ latestVital.weight }} kg
                  </p>
                  <p v-else class="text-base font-bold text-slate-400 italic">No data yet</p>
                </div>
              </div>
            </div>

            <!-- History: fixed height + scroll + delete -->
            <div v-if="vitalSigns.length > 0" class="mt-4">
              <p class="text-xs font-bold text-slate-400 mb-2">All Records</p>
              <div class="h-28 overflow-y-auto space-y-1 pr-1">
                <div
                  v-for="vs in vitalSigns"
                  :key="vs.id"
                  class="flex items-center justify-between text-xs text-slate-500 bg-slate-50 px-2 py-1.5 rounded-lg group hover:bg-red-50 transition-colors"
                >
                  <span class="text-slate-400 shrink-0">{{
                    vs.recordTime ? new Date(vs.recordTime).toLocaleDateString('zh-CN') : '—'
                  }}</span>
                  <span v-if="vs.temperature" class="mx-1">🌡 {{ vs.temperature }}°C</span>
                  <span v-if="vs.weight" class="mx-1">⚖️ {{ vs.weight }}kg</span>
                  <button
                    @click="handleDeleteVital(vs.id!)"
                    :disabled="deletingVitalId === vs.id"
                    class="ml-1 shrink-0 text-slate-300 hover:text-red-400 opacity-0 group-hover:opacity-100 transition-all"
                  >
                    <Loader2 v-if="deletingVitalId === vs.id" :size="12" class="animate-spin" />
                    <Trash2 v-else :size="12" />
                  </button>
                </div>
              </div>
            </div>
            <p class="mt-2 text-xs text-slate-400 text-center">{{ vitalSigns.length }} records total</p>
          </div>

          <!-- Medical Records -->
          <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100 flex flex-col">
            <h3 class="font-bold text-slate-800 mb-4 flex items-center justify-between">
              Medical Records
              <span class="text-xs bg-slate-100 text-slate-500 px-2 py-1 rounded-lg">{{
                currentYear
              }}</span>
            </h3>

            <div
              v-if="recentRecords.length === 0"
              class="flex-1 flex flex-col items-center justify-center py-6"
            >
              <FileText :size="32" class="text-slate-200 mb-2" />
              <p class="text-sm text-slate-400">No medical records yet.</p>
            </div>

            <!-- Medical record scroll list -->
            <div v-else class="h-52 overflow-y-auto space-y-2 pr-1">
              <div
                v-for="record in recentRecords"
                :key="record.id"
                class="flex items-start gap-3 p-3 bg-slate-50 rounded-xl border border-transparent hover:border-teal-100 transition-colors"
              >
                <CheckCircle :size="15" class="text-teal-500 mt-0.5 shrink-0" />
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-bold text-slate-700 truncate">{{ record.title }}</p>
                  <p class="text-xs text-slate-400 mt-0.5">{{ record.visitDate }}</p>
                </div>
              </div>
            </div>

            <button
              @click="emit('navigate', 'medical')"
              class="w-full mt-4 py-2.5 bg-teal-500 hover:bg-teal-600 text-white rounded-xl text-sm font-bold shadow-md shadow-teal-100 transition-all flex items-center justify-center gap-2"
            >
              <FileText :size="15" /> View Full Report
            </button>
          </div>
        </div>

        <!-- Quick Access -->
        <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
          <h3 class="font-bold text-slate-800 mb-5 flex items-center gap-2">
            <Activity :size="18" class="text-teal-500" />
            Quick Access
          </h3>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
            <div
              @click="emit('navigate', 'appointment')"
              class="flex flex-col items-center gap-2 p-4 bg-teal-50 rounded-2xl hover:bg-teal-100 transition-colors cursor-pointer"
            >
              <span class="text-2xl">💉</span>
              <span class="text-xs font-bold text-teal-700 text-center">Vaccines</span>
            </div>
            <div
              @click="emit('navigate', 'appointment')"
              class="flex flex-col items-center gap-2 p-4 bg-blue-50 rounded-2xl hover:bg-blue-100 transition-colors cursor-pointer"
            >
              <span class="text-2xl">💊</span>
              <span class="text-xs font-bold text-blue-700 text-center">Medications</span>
            </div>
            <div
              @click="emit('navigate', 'daily')"
              class="flex flex-col items-center gap-2 p-4 bg-amber-50 rounded-2xl hover:bg-amber-100 transition-colors cursor-pointer"
            >
              <span class="text-2xl">🍖</span>
              <span class="text-xs font-bold text-amber-700 text-center">Feeding Log</span>
            </div>
            <div
              @click="emit('navigate', 'medical')"
              class="flex flex-col items-center gap-2 p-4 bg-purple-50 rounded-2xl hover:bg-purple-100 transition-colors cursor-pointer"
            >
              <span class="text-2xl">📋</span>
              <span class="text-xs font-bold text-purple-700 text-center">Medical Records</span>
            </div>
          </div>
        </div>

        <!-- Owner Summary -->
        <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
          <h3 class="font-bold text-slate-800 mb-4">Owner Summary</h3>
          <div v-if="currentPet?.owner" class="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div class="bg-slate-50 p-3 rounded-xl">
              <p class="text-xs text-slate-400 font-bold uppercase mb-1">Name</p>
              <p class="text-sm font-bold text-slate-700">{{ currentPet.owner.name }}</p>
            </div>
            <div class="bg-slate-50 p-3 rounded-xl">
              <p class="text-xs text-slate-400 font-bold uppercase mb-1">Contact</p>
              <p class="text-sm font-bold text-slate-700">
                {{ currentPet.owner.contact || 'Not set' }}
              </p>
            </div>
            <div class="bg-slate-50 p-3 rounded-xl">
              <p class="text-xs text-slate-400 font-bold uppercase mb-1">Address</p>
              <p class="text-sm font-bold text-slate-700 truncate">
                {{ currentPet.owner.address || 'Not set' }}
              </p>
            </div>
          </div>
          <div v-else class="text-center py-4 text-slate-400 text-sm">No owner info available.</div>
        </div>
      </div>
    </div>
  </div>
</template>