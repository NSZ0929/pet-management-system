<script setup lang="ts">
import { ref, computed } from 'vue'
// Import all view components
import DashboardView from './views/DashboardView.vue'
import PetProfileView from './views/PetProfileView.vue'
import DailyLogView from './views/DailyLogView.vue'
import AppointmentView from './views/AppointmentView.vue'
import LoginView from './views/LoginView.vue'
import SettingsView from './views/SettingsView.vue'
import MedicalRecordView from './views/MedicalRecordView.vue'
import NotificationPanel from './components/NotificationPanel.vue'
import { currentPet } from './composables/usePetData'
import {
  Home,
  User,
  Settings,
  LogOut,
  Menu,
  X,
  PawPrint,
  ClipboardList,
  CalendarDays,
  FileText,
} from 'lucide-vue-next'

// ── Auth state ───────────────────────────────────────────────
interface AuthUser {
  id: number
  username: string
  role: string
}

const isLoggedIn = ref(!!localStorage.getItem('token'))
const currentUser = ref<AuthUser | null>(JSON.parse(localStorage.getItem('user') ?? 'null'))

const handleLoginSuccess = (user: AuthUser) => {
  currentUser.value = user
  isLoggedIn.value = true
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  currentUser.value = null
  isLoggedIn.value = false
}

// ── View state management ───────────────────────────────────────────
type View = 'dashboard' | 'profile' | 'daily' | 'appointment' | 'settings' | 'medical'
const currentView = ref<View>('dashboard')
const isSidebarOpen = ref(false)

const setView = (view: View) => {
  currentView.value = view
  isSidebarOpen.value = false
}

const headerTitle = ref('Pet Health Dashboard')

const updateHeaderTitle = (view: View) => {
  switch (view) {
    case 'dashboard':
      headerTitle.value = 'Pet Health Dashboard'
      break
    case 'profile':
      headerTitle.value = 'Pet Profile & Owner Info'
      break
    case 'settings':
      headerTitle.value = 'Settings'
      break
    case 'daily':
      headerTitle.value = 'Daily Log'
      break
    case 'appointment':
      headerTitle.value = 'Appointments & Reminders'
      break
    case 'medical':
      headerTitle.value = 'Medical Records'
      break
    default:
      headerTitle.value = 'PetCare'
  }
}

const handleNavigationClick = (view: View) => {
  setView(view)
  updateHeaderTitle(view)
}

updateHeaderTitle(currentView.value)

// Navigation config
const navigation = [
  { name: 'Dashboard',    icon: Home,          view: 'dashboard'   as View },
  { name: 'Profile',      icon: User,          view: 'profile'     as View },
  { name: 'Daily Log',    icon: ClipboardList, view: 'daily'       as View },
  { name: 'Appointments', icon: CalendarDays,  view: 'appointment' as View },
  { name: 'Medical',      icon: FileText,      view: 'medical'     as View },
  { name: 'Settings',     icon: Settings,      view: 'settings'    as View },
]

// Current pet ID (for notification panel)
const currentPetId = computed(() => currentPet.value?.id ?? null)
</script>

<template>
  <!-- Not logged in: show login page -->
  <LoginView v-if="!isLoggedIn" @login-success="handleLoginSuccess" />

  <!-- Logged in: show main app -->
  <div v-else class="min-h-screen bg-slate-50 flex">
    <!-- Sidebar -->
    <aside
      :class="{ 'translate-x-0': isSidebarOpen, '-translate-x-full': !isSidebarOpen }"
      class="fixed inset-y-0 left-0 z-40 w-64 bg-white shadow-xl lg:relative lg:translate-x-0 lg:flex lg:flex-col p-5 space-y-8 transition-transform duration-300"
    >
      <div
        class="text-3xl font-extrabold text-teal-600 tracking-tight flex items-center justify-center gap-2"
      >
        <PawPrint :size="28" /> PetCare
      </div>

      <!-- Navigation links -->
      <nav class="flex-1 space-y-2">
        <button
          v-for="item in navigation"
          :key="item.name"
          @click="handleNavigationClick(item.view)"
          :class="[
            currentView === item.view
              ? 'bg-teal-100/70 text-teal-700 font-bold shadow-md shadow-teal-100 border-l-4 border-teal-500'
              : 'text-slate-500 hover:bg-slate-100 hover:text-slate-700 font-medium',
            'flex items-center gap-3 px-4 py-3 rounded-xl transition-all w-full text-left',
          ]"
        >
          <component :is="item.icon" :size="20" />
          {{ item.name }}
        </button>
      </nav>

      <!-- Bottom: user info + logout -->
      <div class="pt-6 border-t border-slate-100 space-y-2">
        <!-- Current user -->
        <div class="px-4 py-2 rounded-xl bg-slate-50">
          <p class="text-xs text-slate-400">Logged in as</p>
          <p class="text-sm font-bold text-slate-700 truncate">{{ currentUser?.username }}</p>
          <p class="text-xs text-teal-500 font-medium">{{ currentUser?.role }}</p>
        </div>
        <!-- Logout button -->
        <button
          @click="handleLogout"
          class="flex items-center gap-3 px-4 py-3 rounded-xl text-red-500 hover:bg-red-50 w-full text-left font-medium transition-all"
        >
          <LogOut :size="20" /> Logout
        </button>
      </div>
    </aside>

    <!-- Main content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- Top header -->
      <header
        class="bg-white shadow-sm p-4 lg:p-6 sticky top-0 z-30 flex items-center justify-between"
      >
        <div class="flex items-center gap-4">
          <button
            @click="isSidebarOpen = !isSidebarOpen"
            class="text-slate-500 lg:hidden p-2 rounded-full hover:bg-slate-100 transition-colors"
          >
            <component :is="isSidebarOpen ? X : Menu" :size="24" />
          </button>
          <h1 class="text-2xl font-bold text-slate-800">{{ headerTitle }}</h1>
        </div>

        <!-- Right: notifications + avatar -->
        <div class="flex items-center gap-3">
          <!-- Notification panel -->
          <NotificationPanel :pet-id="currentPetId" />

          <div class="text-right hidden sm:block">
            <p class="text-sm font-bold text-slate-700">{{ currentUser?.username }}</p>
            <p class="text-xs text-slate-400">{{ currentUser?.role }}</p>
          </div>
          <div
            class="w-10 h-10 rounded-full border-2 border-teal-400 bg-teal-100 flex items-center justify-center text-teal-600 font-bold text-sm"
          >
            {{ currentUser?.username?.charAt(0).toUpperCase() }}
          </div>
        </div>
      </header>

      <!-- View content -->
      <main class="flex-1 overflow-x-hidden overflow-y-auto p-4 lg:p-8">
        <div
          v-if="isSidebarOpen"
          @click="isSidebarOpen = false"
          class="fixed inset-0 bg-black/30 z-30 lg:hidden"
        ></div>

        <transition name="fade" mode="out-in">
          <div :key="currentView">
            <DashboardView
              v-if="currentView === 'dashboard'"
              :header-title="headerTitle"
              @navigate="handleNavigationClick"
            />
            <PetProfileView v-else-if="currentView === 'profile'" :header-title="headerTitle" />
            <DailyLogView v-else-if="currentView === 'daily'" :header-title="headerTitle" />
            <AppointmentView
              v-else-if="currentView === 'appointment'"
              :header-title="headerTitle"
            />
            <MedicalRecordView v-else-if="currentView === 'medical'" />
            <SettingsView v-else-if="currentView === 'settings'" />
          </div>
        </transition>
      </main>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>