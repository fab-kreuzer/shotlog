import {computed, onMounted, reactive, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {api} from '@/api/http'
import {useAuthStore} from '@/stores/auth'

/**
 * Shared state + logic for a session create/edit modal of a fixed type
 * ('TRAINING' | 'COMPETITION'). The training and competition modals both use
 * this and only differ in the type-specific fields they render.
 */
export function useSessionForm(sessionType, {onSaved} = {}) {
    const {t} = useI18n()
    const auth = useAuthStore()

    const visible = ref(false)
    const locations = ref([])
    const seasons = ref([])
    const assignedTeams = ref([])
    const isEditing = ref(false)
    const editingId = ref(null)

    // Teams are scoped to a season; the session dialog only offers the teams
    // that belong to the currently active season.
    const activeSeasonId = computed(() => seasons.value.find(s => s.active)?.id ?? null)
    const visibleTeams = computed(() =>
        assignedTeams.value.filter(tm => tm.season?.id === activeSeasonId.value)
    )

    const form = reactive({
        sessionDate: '',
        sessionTime: '',
        enemyId: null,
        seasonId: null,
        teamId: null,
        title: '',
        sessionType,
        decimalScoring: false,
        home: false,
        series: []
    })

    // DatePicker works with Date objects; the form/API use 'yyyy-MM-dd' and 'HH:mm' strings.
    function pad(n) {
        return String(n).padStart(2, '0')
    }

    const sessionDateModel = computed({
        get: () => (form.sessionDate ? new Date(`${form.sessionDate}T00:00:00`) : null),
        set: (d) => {
            form.sessionDate = d ? `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` : ''
        }
    })

    const sessionTimeModel = computed({
        get: () => {
            if (!form.sessionTime) return null
            const [h, m] = form.sessionTime.split(':')
            const d = new Date()
            d.setHours(Number(h), Number(m), 0, 0)
            return d
        },
        set: (d) => {
            form.sessionTime = d ? `${pad(d.getHours())}:${pad(d.getMinutes())}` : ''
        }
    })

    function createEmptySeries() {
        return {
            seriesNumber: form.series.length + 1,
            testShot: false,
            shots: Array.from({length: 10}, (_, i) => ({
                shotNumber: i + 1,
                value: 0
            }))
        }
    }

    function addSeries() {
        form.series.push(createEmptySeries())
    }

    function removeSeries(index) {
        form.series.splice(index, 1)
        form.series.forEach((s, i) => {
            s.seriesNumber = i + 1
        })
    }

    function resetForm() {
        const now = new Date()
        form.sessionDate = now.toISOString().split('T')[0]
        form.sessionTime = pad(now.getHours()) + ':' + pad(now.getMinutes())
        form.enemyId = locations.value.length > 0 ? locations.value[0].id : null
        form.seasonId = (seasons.value.find(s => s.active) ?? seasons.value[0])?.id ?? null
        form.teamId = visibleTeams.value.length > 0 ? visibleTeams.value[0].id : null
        form.title = ''
        form.sessionType = sessionType
        form.decimalScoring = false
        form.home = false
        form.series = [createEmptySeries()]
        isEditing.value = false
        editingId.value = null
    }

    function openCreate() {
        resetForm()
        visible.value = true
    }

    function openEdit(session) {
        isEditing.value = true
        editingId.value = session.id
        form.sessionDate = session.sessionDate
        form.sessionTime = session.sessionTime
        form.title = session.title
        form.enemyId = session.enemy ? session.enemy.id : null
        form.seasonId = session.season ? session.season.id : null
        form.teamId = session.team ? session.team.id : null
        form.sessionType = session.sessionType
        form.decimalScoring = session.decimalScoring
        form.home = session.home

        form.series = (session.series || []).map((s, i) => ({
            seriesNumber: i + 1,
            testShot: s.testShot,
            shots: (s.shots || []).map((shot, si) => ({
                shotNumber: si + 1,
                value: shot.value || 0
            }))
        }))

        form.series.forEach(s => {
            while (s.shots.length < 10) {
                s.shots.push({shotNumber: s.shots.length + 1, value: 0})
            }
        })

        visible.value = true
    }

    function close() {
        visible.value = false
    }

    function onVisibleChange(value) {
        visible.value = value
    }

    async function handleSubmit() {
        const data = {
            sessionDate: form.sessionDate,
            sessionTime: form.sessionTime,
            enemyId: form.enemyId,
            seasonId: form.seasonId,
            teamId: form.teamId,
            sessionType: form.sessionType,
            title: form.title,
            decimalScoring: form.decimalScoring,
            home: form.home,
            series: form.series.map((s, i) => ({
                seriesNumber: i + 1,
                testShot: s.testShot,
                shots: s.shots.map((shot, si) => ({
                    shotNumber: si + 1,
                    value: shot.value
                }))
            }))
        }

        try {
            if (isEditing.value) {
                await api.updateSession(editingId.value, data)
            } else {
                await api.createSession(data)
            }
            close()
            onSaved?.()
        } catch (err) {
            console.error('Error saving session:', err)
            alert(t('session.saveError'))
        }
    }

    onMounted(async () => {
        try {
            locations.value = await api.getLocations()
        } catch (err) {
            console.error('Error fetching locations:', err)
        }
        try {
            seasons.value = await api.getSeasons()
        } catch (err) {
            console.error('Error fetching seasons:', err)
        }
        try {
            const userId = auth.user?.id
            if (userId != null) {
                assignedTeams.value = await api.getAssignedTeams(userId)
            }
        } catch (err) {
            console.error('Error fetching assigned teams:', err)
        }
    })

    return {
        visible,
        locations,
        seasons,
        assignedTeams,
        visibleTeams,
        isEditing,
        form,
        sessionDateModel,
        sessionTimeModel,
        addSeries,
        removeSeries,
        openCreate,
        openEdit,
        close,
        onVisibleChange,
        handleSubmit
    }
}