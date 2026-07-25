<template>
  <div class="p-6 flex flex-col items-center text-center gap-3">
    <i class="pi pi-lock text-4xl text-surface-400"/>
    <h2 class="text-lg font-semibold text-surface-800">
      {{ $t('settings.accessDeniedTitle') }}
    </h2>
    <p class="text-sm text-surface-500 max-w-md">
      {{ $t('settings.accessDeniedMessage') }}
    </p>
  </div>
</template>

<script setup>
</script>
