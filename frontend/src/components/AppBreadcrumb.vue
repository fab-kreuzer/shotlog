<template>
  <Breadcrumb v-if="items.length > 1" :home="home" :model="items.slice(1)" class="app-breadcrumb mb-6 !p-0 !bg-transparent">
    <template #item="{item, props}">
      <router-link v-if="item.route" v-slot="{href, navigate}" :to="item.route" custom>
        <a :href="href" v-bind="props.action" class="flex items-center gap-1.5 hover:text-primary-500" @click="navigate">
          <i v-if="item.icon" :class="item.icon"/>
          <span v-if="item.label">{{ item.label }}</span>
        </a>
      </router-link>
      <span v-else class="max-w-[16rem] truncate font-medium text-surface-800" aria-current="page">{{ item.label }}</span>
    </template>
  </Breadcrumb>
</template>

<script setup>
import {computed, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useI18n} from 'vue-i18n'
import Breadcrumb from 'primevue/breadcrumb'
import {useBreadcrumbStore} from '@/stores/breadcrumb'

const route = useRoute()
const router = useRouter()
const {t} = useI18n()
const store = useBreadcrumbStore()

// Pages with `meta.dynamic` (the session page) set their crumb label once their data is loaded.
watch(() => route.fullPath, () => store.setLabel(''))

// Chain from the dashboard down to the current route via `meta.parent`.
const items = computed(() => {
  const chain = []
  let current = route
  let guard = 0
  while (current && guard++ < 6) {
    const isLast = chain.length === 0
    const label = current.meta?.dynamic
        ? store.dynamicLabel
        : current.meta?.titleKey ? t(current.meta.titleKey) : ''
    if (label) chain.unshift({label, route: isLast ? undefined : {name: current.name, params: current.params}})
    const parentName = current.meta?.parent
    current = parentName
        ? router.resolve({name: parentName, params: current.meta?.parentParams ? current.params : {}})
        : null
  }
  return chain
})

const home = computed(() => ({icon: 'pi pi-home', route: {name: 'dashboard'}, label: ''}))
</script>
