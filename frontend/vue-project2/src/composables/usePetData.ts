import { ref } from 'vue'
import { getAllPets, getMedicalRecordsByPet, type Pet, type MedicalRecord } from '../api/pet'

// ── 加载状态 ──────────────────────────────────────────────
export const isLoadingPetData = ref(false)
export const petDataError = ref('')

// ── 当前宠物（全局共享） ──────────────────────────────────
export const currentPet = ref<Pet | null>(null)
export const allPets = ref<Pet[]>([])
export const medicalRecords = ref<MedicalRecord[]>([])

// ── 加载所有宠物 ──────────────────────────────────────────
export async function loadPets() {
  isLoadingPetData.value = true
  petDataError.value = ''
  try {
    const res = await getAllPets()
    allPets.value = res.data
    // 默认选第一只宠物
    if (res.data.length > 0 && !currentPet.value) {
      currentPet.value = res.data[0] ?? null
      if (currentPet.value) {
        await loadMedicalRecords(currentPet.value.id)
      }
    }
  } catch (err) {
    petDataError.value = '无法加载宠物数据，请检查后端连接'
    console.error(err)
  } finally {
    isLoadingPetData.value = false
  }
}

// ── 加载医疗记录 ──────────────────────────────────────────
export async function loadMedicalRecords(petId: number) {
  try {
    const res = await getMedicalRecordsByPet(petId)
    medicalRecords.value = res.data
  } catch (err) {
    console.error('加载医疗记录失败', err)
  }
}

// ── 切换当前宠物 ──────────────────────────────────────────
export async function selectPet(pet: Pet) {
  currentPet.value = pet
  await loadMedicalRecords(pet.id)
}

// ── Composable 函数（保持向后兼容） ──────────────────────
export function usePetData() {
  return {
    currentPet,
    allPets,
    medicalRecords,
    isLoadingPetData,
    petDataError,
    loadPets,
    loadMedicalRecords,
    selectPet,
  }
}
