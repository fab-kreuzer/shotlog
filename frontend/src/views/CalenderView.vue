<template>
  <div>
    <h1>Kalender</h1>
    <div id="calender-div">
      <div ref="calendarEl"></div>
    </div>

    <!-- Session Modal -->
    <SessionModal ref="sessionModal" @saved="refreshCalendar"/>
  </div>
</template>

<script setup>
import {onBeforeUnmount, onMounted, ref} from 'vue'
import {Calendar} from '@fullcalendar/core'
import dayGridPlugin from '@fullcalendar/daygrid'
import {api} from '@/api/http'
import SessionModal from '@/components/SessionModal.vue'

const calendarEl = ref(null)
const sessionModal = ref(null)
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
      today: 'Heute'
    },
    events: async function (fetchInfo, successCallback, failureCallback) {
      try {
        const sessions = await api.getSessions()
        const events = sessions.map(session => ({
          id: session.id,
          title: `${session.formattedType} - ${session.series.length} Series`,
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
        sessionModal.value?.openEdit(session)
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
