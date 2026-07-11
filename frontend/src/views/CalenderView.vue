<template>
  <div>
    <!-- Page header -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-surface-800">{{ $t('calendar.title') }}</h1>
      <p class="mt-1 text-surface-500">{{ $t('calendar.subtitle') }}</p>
    </div>

    <!-- Calendar -->
    <Card>
      <template #content>
        <div ref="calendarEl"></div>
      </template>
    </Card>

    <!-- Session Modals (one per type; the matching one opens on edit) -->
    <TrainingSessionModal ref="trainingModal" @saved="refreshCalendar"/>
    <CompetitionSessionModal ref="competitionModal" @saved="refreshCalendar"/>
  </div>
</template>

<script setup>
import {onBeforeUnmount, onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {Calendar} from '@fullcalendar/core'
import dayGridPlugin from '@fullcalendar/daygrid'
import Card from 'primevue/card'
import {api} from '@/api/http'
import TrainingSessionModal from '@/components/session/TrainingSessionModal.vue'
import CompetitionSessionModal from '@/components/session/CompetitionSessionModal.vue'

const {t} = useI18n()

const calendarEl = ref(null)
const trainingModal = ref(null)
const competitionModal = ref(null)
let calendar = null

function modalForType(type) {
  return type === 'COMPETITION' ? competitionModal.value : trainingModal.value
}

function colorForSessionType(type) {
  switch (type) {
    case 'TRAINING':
      return '#4caf50'
    case 'COMPETITION':
      return '#f44336'
    default:
      return '#9e9e9e'
  }
}

function refreshCalendar() {
  if (calendar) {
    calendar.refetchEvents()
  }
}

onMounted(() => {
  if (!calendarEl.value) return

  calendar = new Calendar(calendarEl.value, {
    plugins: [dayGridPlugin],
    locale: 'de',
    headerToolbar: {
      left: 'today',
      center: 'title',
      right: 'prev,next'
    },
    firstDay: 1,
    initialView: 'dayGridMonth',
    buttonText: {
      today: t('calendar.today')
    },
    events: async function (fetchInfo, successCallback, failureCallback) {
      try {
        const sessions = await api.getSessions()
        const events = sessions.map(session => ({
          id: session.id,
          title: `${session.title}`,
          start: `${session.sessionDate}T${session.sessionTime}`,
          color: colorForSessionType(session.sessionType),
          extendedProps: {
            decimalScoring: session.decimalScoring,
            formattedShotSum: session.formattedShotSum,
            sessionType: session.sessionType
          }
        }))
        successCallback(events)
      } catch (err) {
        console.error('Error fetching sessions:', err)
        failureCallback(err)
      }
    },
    eventClick: async function (info) {
      const sessionId = info.event.id
      try {
        const session = await api.getSession(sessionId)
        modalForType(session.sessionType)?.openEdit(session)
      } catch (err) {
        console.error('Error fetching session:', err)
      }
    }
  })

  calendar.render()
})

onBeforeUnmount(() => {
  if (calendar) {
    calendar.destroy()
    calendar = null
  }
})
</script>