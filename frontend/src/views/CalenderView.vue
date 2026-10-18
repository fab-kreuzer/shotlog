<template>
  <div>
    <PageHeader :subtitle="$t('calendar.subtitle')" :title="$t('calendar.title')" color="blue" icon="pi pi-calendar">
      <template #actions>
        <div class="flex items-center gap-4 text-sm text-surface-600">
          <span class="flex items-center gap-1.5">
            <span class="w-2.5 h-2.5 rounded-full" style="background-color: #4caf50"/>
            {{ $t('session.typeTraining') }}
          </span>
          <span class="flex items-center gap-1.5">
            <span class="w-2.5 h-2.5 rounded-full" style="background-color: #f44336"/>
            {{ $t('session.typeCompetition') }}
          </span>
        </div>
      </template>
    </PageHeader>

    <!-- Calendar -->
    <Card>
      <template #content>
        <div ref="calendarEl"></div>
      </template>
    </Card>

  </div>
</template>

<script setup>
import {onBeforeUnmount, onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {useRouter} from 'vue-router'
import {Calendar} from '@fullcalendar/core'
import dayGridPlugin from '@fullcalendar/daygrid'
import Card from 'primevue/card'
import {api} from '@/api/http'
import PageHeader from '@/components/PageHeader.vue'

const {t} = useI18n()
const router = useRouter()

const calendarEl = ref(null)
let calendar = null

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
    eventClick: function (info) {
      const type = info.event.extendedProps.sessionType
      router.push(`${type === 'COMPETITION' ? '/competition' : '/training'}/${info.event.id}`)
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