<script setup lang="ts">
import { ref } from 'vue'
import { PawPrint, Camera, Loader2 } from 'lucide-vue-next'
import { uploadAvatar } from '../api/pet'

const props = defineProps<{
  petId?: number
  avatarUrl?: string
  editable?: boolean // 是否显示上传按钮
  size?: 'sm' | 'md' | 'lg' // 尺寸
}>()

const emit = defineEmits<{
  uploaded: [avatarUrl: string]
}>()

const uploading = ref(false)
const uploadError = ref('')
const localUrl = ref(props.avatarUrl ?? '')

// 图片完整URL（后端返回的是相对路径）
const fullUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

// 点击头像触发文件选择
const fileInput = ref<HTMLInputElement | null>(null)
const triggerUpload = () => {
  if (!props.editable || !props.petId) return
  fileInput.value?.click()
}

const handleFileChange = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file || !props.petId) return

  // 校验文件类型
  if (!file.type.startsWith('image/')) {
    uploadError.value = '请选择图片文件'
    return
  }
  // 校验文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    uploadError.value = '图片不能超过 5MB'
    return
  }

  uploading.value = true
  uploadError.value = ''

  // 本地预览
  const reader = new FileReader()
  reader.onload = (e) => {
    localUrl.value = e.target?.result as string
  }
  reader.readAsDataURL(file)

  try {
    const res = await uploadAvatar(props.petId, file)
    localUrl.value = fullUrl(res.data.avatarUrl)
    emit('uploaded', res.data.avatarUrl)
  } catch {
    uploadError.value = '上传失败，请重试'
    localUrl.value = props.avatarUrl ? fullUrl(props.avatarUrl) : ''
  } finally {
    uploading.value = false
    // 清空 input，允许重复上传同一文件
    if (fileInput.value) fileInput.value.value = ''
  }
}

const sizeClass = {
  sm: 'aspect-square w-full max-w-[120px]',
  md: 'aspect-[4/3] w-full',
  lg: 'aspect-[4/3] w-full',
}
</script>

<template>
  <div :class="['relative group rounded-2xl overflow-hidden', sizeClass[size ?? 'md']]">
    <!-- 隐藏的文件输入 -->
    <input ref="fileInput" type="file" accept="image/*" class="hidden" @change="handleFileChange" />

    <!-- 头像图片或占位符 -->
    <div
      class="w-full h-full bg-gradient-to-br from-teal-50 to-cyan-100 flex items-center justify-center"
    >
      <img
        v-if="localUrl || avatarUrl"
        :src="localUrl || fullUrl(avatarUrl ?? '')"
        alt="宠物头像"
        class="w-full h-full object-cover"
        @error="localUrl = ''"
      />
      <PawPrint v-else :size="size === 'sm' ? 32 : 56" class="text-teal-300" />
    </div>

    <!-- 上传遮罩（可编辑时悬停显示） -->
    <div
      v-if="editable && petId"
      @click="triggerUpload"
      class="absolute inset-0 bg-black/40 flex flex-col items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer"
    >
      <Loader2 v-if="uploading" :size="24" class="text-white animate-spin" />
      <template v-else>
        <Camera :size="24" class="text-white mb-1" />
        <span class="text-white text-xs font-bold">点击上传</span>
      </template>
    </div>

    <!-- 上传错误提示 -->
    <div
      v-if="uploadError"
      class="absolute bottom-0 left-0 right-0 bg-red-500/90 text-white text-xs text-center py-1"
    >
      {{ uploadError }}
    </div>
  </div>
</template>
