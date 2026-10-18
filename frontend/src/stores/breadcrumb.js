import {defineStore} from 'pinia'
import {ref} from 'vue'

/**
 * Label for the last breadcrumb item when it depends on loaded data (e.g. the session title).
 * Cleared on every route change by AppBreadcrumb.
 */
export const useBreadcrumbStore = defineStore('breadcrumb', () => {
    const dynamicLabel = ref('')

    function setLabel(label) {
        dynamicLabel.value = label ?? ''
    }

    return {dynamicLabel, setLabel}
})
