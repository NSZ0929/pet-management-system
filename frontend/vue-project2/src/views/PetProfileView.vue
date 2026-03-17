<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  User,
  Home,
  Phone,
  MapPin,
  PawPrint,
  Users,
  Sun,
  Thermometer,
  Dog,
  Plus,
  Loader2,
  AlertCircle,
  Pencil,
  Check,
  X,
  RefreshCw,
} from 'lucide-vue-next'
import { usePetData } from '../composables/usePetData'
import { addPet, addOwner, updatePet, updateOwner } from '../api/pet'

defineProps<{ headerTitle?: string }>()

const { currentPet, allPets, isLoadingPetData, petDataError, loadPets, selectPet } = usePetData()

onMounted(() => {
  loadPets()
})

// ── 添加宠物表单 ──────────────────────────────────────────
const showAddPetForm = ref(false)
const isSubmitting = ref(false)
const submitError = ref('')

const newOwner = ref({ name: '', contact: '', address: '' })
const newPet = ref({ name: '', breed: '', age: '' })

const handleAddPet = async () => {
  if (!newPet.value.name || !newOwner.value.name || !newOwner.value.contact) {
    submitError.value = '请填写宠物名称、主人姓名和联系方式'
    return
  }
  isSubmitting.value = true
  submitError.value = ''
  try {
    // 1. 先创建主人
    const ownerRes = await addOwner({
      name: newOwner.value.name,
      contact: newOwner.value.contact,
      address: newOwner.value.address,
    })
    // 2. 再创建宠物，关联主人
    await addPet(
      {
        name: newPet.value.name,
        breed: newPet.value.breed,
        age: newPet.value.age ? Number(newPet.value.age) : undefined,
      },
      ownerRes.data.id,
    )
    // 3. 重新加载
    await loadPets()
    // 4. 重置表单
    newOwner.value = { name: '', contact: '', address: '' }
    newPet.value = { name: '', breed: '', age: '' }
    showAddPetForm.value = false
  } catch (err) {
    submitError.value = '添加失败，请稍后重试'
    console.error(err)
  } finally {
    isSubmitting.value = false
  }
}

// ── 编辑宠物信息 ──────────────────────────────────────────
const isEditingPet = ref(false)
const editPetForm = ref({ name: '', breed: '', age: '' })

const startEditPet = () => {
  if (!currentPet.value) return
  editPetForm.value = {
    name: currentPet.value.name,
    breed: currentPet.value.breed || '',
    age: currentPet.value.age?.toString() || '',
  }
  isEditingPet.value = true
}

const savePet = async () => {
  if (!currentPet.value) return
  try {
    await updatePet(currentPet.value.id, {
      name: editPetForm.value.name,
      breed: editPetForm.value.breed,
      age: editPetForm.value.age ? Number(editPetForm.value.age) : undefined,
    })
    await loadPets()
    isEditingPet.value = false
  } catch (err) {
    console.error('更新宠物失败', err)
  }
}

// ── 编辑主人信息 ──────────────────────────────────────────
const isEditingOwner = ref(false)
const editOwnerForm = ref({ name: '', contact: '', address: '' })

const startEditOwner = () => {
  if (!currentPet.value?.owner) return
  editOwnerForm.value = {
    name: currentPet.value.owner.name,
    contact: currentPet.value.owner.contact || '',
    address: currentPet.value.owner.address || '',
  }
  isEditingOwner.value = true
}

const saveOwner = async () => {
  if (!currentPet.value?.owner) return
  try {
    await updateOwner(currentPet.value.owner.id, {
      name: editOwnerForm.value.name,
      contact: editOwnerForm.value.contact,
      address: editOwnerForm.value.address,
    })
    await loadPets()
    isEditingOwner.value = false
  } catch (err) {
    console.error('更新主人失败', err)
  }
}
</script>

<template>
  <div class="space-y-6">
    <!-- 加载中 -->
    <div
      v-if="isLoadingPetData"
      class="flex items-center justify-center py-20 gap-3 text-slate-400"
    >
      <Loader2 :size="24" class="animate-spin text-teal-500" />
      <span class="text-sm font-medium">正在加载数据...</span>
    </div>

    <!-- 错误提示 -->
    <div
      v-else-if="petDataError"
      class="flex items-center gap-3 bg-red-50 border border-red-100 text-red-500 px-5 py-4 rounded-2xl"
    >
      <AlertCircle :size="18" class="shrink-0" />
      <p class="text-sm font-medium">{{ petDataError }}</p>
      <button
        @click="loadPets"
        class="ml-auto flex items-center gap-1 text-xs font-bold hover:text-red-600"
      >
        <RefreshCw :size="13" /> 重试
      </button>
    </div>

    <template v-else>
      <!-- 顶部操作栏 -->
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <!-- 多宠物切换 -->
          <div v-if="allPets.length > 1" class="flex gap-2 flex-wrap">
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
        </div>
        <!-- 添加宠物按钮 -->
        <button
          @click="showAddPetForm = !showAddPetForm"
          class="flex items-center gap-2 px-4 py-2 bg-teal-500 hover:bg-teal-600 text-white rounded-xl text-sm font-bold shadow-md shadow-teal-100 transition-all"
        >
          <Plus :size="16" /> 添加宠物
        </button>
      </div>

      <!-- 添加宠物表单 -->
      <transition name="slide">
        <div
          v-if="showAddPetForm"
          class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100 space-y-4"
        >
          <h3 class="font-bold text-slate-800 flex items-center gap-2">
            <PawPrint :size="18" class="text-teal-500" /> 添加新宠物
          </h3>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <!-- 宠物信息 -->
            <div class="space-y-3">
              <p class="text-xs font-bold text-slate-400 uppercase">宠物信息</p>
              <input
                v-model="newPet.name"
                type="text"
                placeholder="宠物名称 *"
                class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
              />
              <input
                v-model="newPet.breed"
                type="text"
                placeholder="品种（如：柯基 Corgi）"
                class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
              />
              <input
                v-model="newPet.age"
                type="number"
                placeholder="年龄（岁）"
                min="0"
                class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
              />
            </div>
            <!-- 主人信息 -->
            <div class="space-y-3">
              <p class="text-xs font-bold text-slate-400 uppercase">主人信息</p>
              <input
                v-model="newOwner.name"
                type="text"
                placeholder="主人姓名 *"
                class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
              />
              <input
                v-model="newOwner.contact"
                type="text"
                placeholder="联系方式 *"
                class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
              />
              <input
                v-model="newOwner.address"
                type="text"
                placeholder="地址（可选）"
                class="w-full px-3 py-2.5 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
              />
            </div>
          </div>

          <div
            v-if="submitError"
            class="flex items-center gap-2 text-red-500 text-sm bg-red-50 px-4 py-2.5 rounded-xl"
          >
            <AlertCircle :size="14" /> {{ submitError }}
          </div>

          <div class="flex gap-2">
            <button
              @click="handleAddPet"
              :disabled="isSubmitting"
              class="flex-1 py-2.5 bg-teal-500 hover:bg-teal-600 disabled:bg-teal-300 text-white rounded-xl text-sm font-bold transition-all flex items-center justify-center gap-2"
            >
              <Loader2 v-if="isSubmitting" :size="15" class="animate-spin" />
              {{ isSubmitting ? '提交中...' : '确认添加' }}
            </button>
            <button
              @click="showAddPetForm = false"
              class="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-all"
            >
              取消
            </button>
          </div>
        </div>
      </transition>

      <!-- 无宠物状态 -->
      <div
        v-if="!currentPet"
        class="text-center py-20 bg-white rounded-3xl shadow-lg border border-slate-100"
      >
        <PawPrint :size="48" class="text-slate-200 mx-auto mb-4" />
        <p class="text-slate-500 font-bold text-lg">还没有宠物档案</p>
        <p class="text-slate-400 text-sm mt-2">点击右上角「添加宠物」开始吧 🐾</p>
      </div>

      <!-- 宠物详情 -->
      <div v-else class="grid grid-cols-1 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        <!-- 左侧：宠物基本资料 -->
        <div class="lg:col-span-1 xl:col-span-1 space-y-6">
          <!-- 宠物卡片 -->
          <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
            <div class="flex items-center justify-between mb-4">
              <h2 class="font-bold text-lg text-slate-800 flex items-center gap-2">
                <Dog :size="18" class="text-teal-500" /> 宠物资料
              </h2>
              <button
                v-if="!isEditingPet"
                @click="startEditPet"
                class="flex items-center gap-1 text-xs font-bold text-slate-400 hover:text-teal-600 transition-colors"
              >
                <Pencil :size="13" /> 编辑
              </button>
              <div v-else class="flex gap-1">
                <button
                  @click="savePet"
                  class="flex items-center gap-1 text-xs font-bold text-teal-600 hover:text-teal-700 bg-teal-50 px-2 py-1 rounded-lg"
                >
                  <Check :size="13" /> 保存
                </button>
                <button
                  @click="isEditingPet = false"
                  class="flex items-center gap-1 text-xs font-bold text-slate-400 hover:text-slate-600 bg-slate-100 px-2 py-1 rounded-lg"
                >
                  <X :size="13" /> 取消
                </button>
              </div>
            </div>

            <!-- 头像占位 -->
            <div
              class="aspect-[4/3] rounded-2xl overflow-hidden mb-5 bg-gradient-to-br from-teal-50 to-cyan-100 flex items-center justify-center"
            >
              <PawPrint :size="56" class="text-teal-300" />
            </div>

            <!-- 查看模式 -->
            <div v-if="!isEditingPet" class="space-y-2">
              <div class="flex justify-between items-center py-2 border-b border-slate-100">
                <span class="text-sm text-slate-500">名字 Name</span>
                <span class="font-bold text-slate-700">{{ currentPet.name }}</span>
              </div>
              <div class="flex justify-between items-center py-2 border-b border-slate-100">
                <span class="text-sm text-slate-500">品种 Breed</span>
                <span class="font-bold text-slate-700">{{ currentPet.breed || '—' }}</span>
              </div>
              <div class="flex justify-between items-center py-2 border-b border-slate-100">
                <span class="text-sm text-slate-500">年龄 Age</span>
                <span class="font-bold text-slate-700">
                  {{ currentPet.age != null ? `${currentPet.age} 岁` : '—' }}
                </span>
              </div>
            </div>

            <!-- 编辑模式 -->
            <div v-else class="space-y-2">
              <div>
                <label class="text-xs font-bold text-slate-400 block mb-1">名字</label>
                <input
                  v-model="editPetForm.name"
                  type="text"
                  class="w-full px-3 py-2 rounded-xl border-2 border-teal-200 bg-white text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
                />
              </div>
              <div>
                <label class="text-xs font-bold text-slate-400 block mb-1">品种</label>
                <input
                  v-model="editPetForm.breed"
                  type="text"
                  class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
                />
              </div>
              <div>
                <label class="text-xs font-bold text-slate-400 block mb-1">年龄（岁）</label>
                <input
                  v-model="editPetForm.age"
                  type="number"
                  min="0"
                  class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-teal-400 transition-all"
                />
              </div>
            </div>
          </div>

          <!-- 健康总结 -->
          <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
            <h2 class="font-bold text-lg text-slate-800 mb-3 flex items-center gap-2">
              <span class="text-lg">🩺</span> 健康总结
            </h2>
            <div class="bg-teal-50 p-4 rounded-xl">
              <p class="text-teal-700 font-bold text-sm">状态待更新</p>
              <p class="text-xs text-teal-500 mt-1">
                请在「日记」模块添加日常记录后自动生成健康报告
              </p>
            </div>
          </div>
        </div>

        <!-- 右侧：主人与家庭信息 -->
        <div class="lg:col-span-2 xl:col-span-3 grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- 主人信息 -->
          <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
            <div class="flex items-center justify-between mb-5">
              <h2 class="font-bold text-lg text-slate-800 flex items-center gap-2">
                <User :size="18" class="text-amber-500" /> 主人信息
              </h2>
              <button
                v-if="!isEditingOwner && currentPet.owner"
                @click="startEditOwner"
                class="flex items-center gap-1 text-xs font-bold text-slate-400 hover:text-amber-600 transition-colors"
              >
                <Pencil :size="13" /> 编辑
              </button>
              <div v-else-if="isEditingOwner" class="flex gap-1">
                <button
                  @click="saveOwner"
                  class="flex items-center gap-1 text-xs font-bold text-amber-600 hover:text-amber-700 bg-amber-50 px-2 py-1 rounded-lg"
                >
                  <Check :size="13" /> 保存
                </button>
                <button
                  @click="isEditingOwner = false"
                  class="flex items-center gap-1 text-xs font-bold text-slate-400 hover:text-slate-600 bg-slate-100 px-2 py-1 rounded-lg"
                >
                  <X :size="13" /> 取消
                </button>
              </div>
            </div>

            <!-- 无主人信息 -->
            <div v-if="!currentPet.owner" class="text-center py-6 text-slate-400">
              <User :size="32" class="mx-auto mb-2 text-slate-200" />
              <p class="text-sm">暂无主人信息</p>
            </div>

            <!-- 查看模式 -->
            <div v-else-if="!isEditingOwner" class="space-y-3">
              <div class="flex items-center gap-3 bg-slate-50 p-3.5 rounded-xl">
                <div
                  class="w-8 h-8 rounded-full bg-amber-100 flex items-center justify-center text-amber-600 shrink-0"
                >
                  <User :size="15" />
                </div>
                <div>
                  <p class="text-xs text-slate-400 font-bold uppercase">姓名</p>
                  <p class="font-bold text-slate-700">{{ currentPet.owner.name }}</p>
                </div>
              </div>
              <div class="flex items-center gap-3 bg-slate-50 p-3.5 rounded-xl">
                <div
                  class="w-8 h-8 rounded-full bg-amber-100 flex items-center justify-center text-amber-600 shrink-0"
                >
                  <Phone :size="15" />
                </div>
                <div>
                  <p class="text-xs text-slate-400 font-bold uppercase">联系方式</p>
                  <p class="font-bold text-slate-700">
                    {{ currentPet.owner.contact || '暂未填写' }}
                  </p>
                </div>
              </div>
              <div class="flex items-center gap-3 bg-slate-50 p-3.5 rounded-xl">
                <div
                  class="w-8 h-8 rounded-full bg-amber-100 flex items-center justify-center text-amber-600 shrink-0"
                >
                  <MapPin :size="15" />
                </div>
                <div>
                  <p class="text-xs text-slate-400 font-bold uppercase">地址</p>
                  <p class="font-bold text-slate-700 text-sm">
                    {{ currentPet.owner.address || '暂未填写' }}
                  </p>
                </div>
              </div>
            </div>

            <!-- 编辑模式 -->
            <div v-else class="space-y-3">
              <div>
                <label class="text-xs font-bold text-slate-400 block mb-1">姓名</label>
                <input
                  v-model="editOwnerForm.name"
                  type="text"
                  class="w-full px-3 py-2 rounded-xl border-2 border-amber-200 bg-white text-sm font-medium focus:outline-none focus:border-amber-400 transition-all"
                />
              </div>
              <div>
                <label class="text-xs font-bold text-slate-400 block mb-1">联系方式</label>
                <input
                  v-model="editOwnerForm.contact"
                  type="text"
                  class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-amber-400 transition-all"
                />
              </div>
              <div>
                <label class="text-xs font-bold text-slate-400 block mb-1">地址</label>
                <input
                  v-model="editOwnerForm.address"
                  type="text"
                  class="w-full px-3 py-2 rounded-xl border-2 border-slate-100 bg-slate-50 text-sm font-medium focus:outline-none focus:border-amber-400 transition-all"
                />
              </div>
            </div>
          </div>

          <!-- 家庭环境（本地填写，后端暂不支持） -->
          <div class="bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
            <h2 class="font-bold text-lg text-slate-800 mb-5 flex items-center gap-2">
              <Home :size="18" class="text-sky-500" /> 家庭环境
            </h2>
            <div class="space-y-3">
              <div class="flex items-center gap-3 bg-slate-50 p-3.5 rounded-xl">
                <div
                  class="w-8 h-8 rounded-full bg-sky-100 flex items-center justify-center text-sky-600 shrink-0"
                >
                  <Sun :size="15" />
                </div>
                <div>
                  <p class="text-xs text-slate-400 font-bold uppercase">居住环境</p>
                  <p class="font-bold text-slate-400 italic text-sm">待完善</p>
                </div>
              </div>
              <div class="flex items-center gap-3 bg-slate-50 p-3.5 rounded-xl">
                <div
                  class="w-8 h-8 rounded-full bg-sky-100 flex items-center justify-center text-sky-600 shrink-0"
                >
                  <Thermometer :size="15" />
                </div>
                <div>
                  <p class="text-xs text-slate-400 font-bold uppercase">环境气温</p>
                  <p class="font-bold text-slate-400 italic text-sm">待完善</p>
                </div>
              </div>
              <div class="flex items-center gap-3 bg-slate-50 p-3.5 rounded-xl">
                <div
                  class="w-8 h-8 rounded-full bg-sky-100 flex items-center justify-center text-sky-600 shrink-0"
                >
                  <PawPrint :size="15" />
                </div>
                <div>
                  <p class="text-xs text-slate-400 font-bold uppercase">其他宠物</p>
                  <p class="font-bold text-slate-400 italic text-sm">待完善</p>
                </div>
              </div>
              <div class="flex items-center gap-3 bg-slate-50 p-3.5 rounded-xl">
                <div
                  class="w-8 h-8 rounded-full bg-sky-100 flex items-center justify-center text-sky-600 shrink-0"
                >
                  <Users :size="15" />
                </div>
                <div>
                  <p class="text-xs text-slate-400 font-bold uppercase">家庭成员</p>
                  <p class="font-bold text-slate-400 italic text-sm">待完善</p>
                </div>
              </div>
            </div>
            <div class="mt-4 p-3 bg-sky-50 rounded-xl">
              <p class="text-xs text-sky-500 text-center">
                🏠 家庭环境字段将在后续版本中加入后端支持
              </p>
            </div>
          </div>

          <!-- 家庭成员 -->
          <div class="md:col-span-2 bg-white rounded-3xl p-6 shadow-lg border border-slate-100">
            <h2 class="font-bold text-lg text-slate-800 mb-4 flex items-center gap-2">
              <Users :size="18" class="text-slate-500" /> 旗下宠物列表
            </h2>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="pet in allPets"
                :key="pet.id"
                @click="selectPet(pet)"
                :class="[
                  'px-4 py-2 rounded-full text-sm font-medium cursor-pointer transition-all',
                  currentPet.id === pet.id
                    ? 'bg-teal-500 text-white shadow-md shadow-teal-100'
                    : 'bg-slate-100 text-slate-700 hover:bg-teal-50 hover:text-teal-700',
                ]"
              >
                🐾 {{ pet.name }}（{{ pet.breed || '未知品种' }}）
              </span>
              <span v-if="allPets.length === 0" class="text-sm text-slate-400">暂无宠物</span>
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
  max-height: 600px;
}
</style>
