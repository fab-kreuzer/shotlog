# Thymeleaf → Vue.js Migration Notes

## Overview

This document describes the migration of the ShotLog frontend from Thymeleaf server-side rendering to a Vue.js Single
Page Application (SPA).

## Architecture Changes

### Before (Thymeleaf)

- Server-side rendered HTML templates with Thymeleaf expressions
- Form-based POST/redirect pattern for data mutations
- Spring Security form login with page redirects
- HTMX for partial page updates
- Inline `<script>` blocks for client-side logic

### After (Vue.js)

- Client-side SPA with Vue 3 (Composition API)
- REST API (JSON) for all data operations
- Session-based auth with JSON login/logout endpoints
- Vue Router for client-side navigation
- Pinia for state management
- Component-based architecture with clean separation

## File Mapping

### Templates → Vue Views

| Thymeleaf Template                  | Vue Component                                                      | Notes                                         |
|-------------------------------------|--------------------------------------------------------------------|-----------------------------------------------|
| `layout.html`                       | `App.vue`                                                          | Navbar with active state, logout button       |
| `login.html`                        | `views/LoginView.vue`                                              | Form login with notifications                 |
| `dashboard.html`                    | `views/DashboardView.vue`                                          | Simple page with test notification            |
| `overview.html`                     | `views/OverviewView.vue`                                           | Session cards, edit/delete, type filtering    |
| `calender.html`                     | `views/CalenderView.vue`                                           | FullCalendar integration                      |
| `settings.html`                     | `views/SettingsView.vue`                                           | Profile tab + Admin tab (users/roles CRUD)    |
| `fragments/createSessionModal.html` | `components/SessionModal.vue`                                      | Create/edit session with dynamic series/shots |
| `notifications.js`                  | `components/NotificationContainer.vue` + `stores/notifications.js` | Toast notification system                     |

### Controllers → API Controllers

| Old Controller                           | New API Controller                        | Notes                                   |
|------------------------------------------|-------------------------------------------|-----------------------------------------|
| `AuthController` (GET /login)            | `SpaController` (forwards to index.html)  | Login page now served by Vue            |
| `DashboardController`                    | `SpaController`                           | Dashboard is a Vue route                |
| `OverviewController` (GET, POST, DELETE) | `ApiSessionController` (/api/sessions/*)  | JSON-based CRUD replaces form POST      |
| `CalenderController` (/api/sessions)     | `ApiSessionController` (/api/sessions/*)  | Consolidated into single API controller |
| `SettingsController` (mixed form/REST)   | `ApiSettingsController` (/api/settings/*) | Fully REST-based user/role CRUD         |
| `ApiLocationController`                  | Unchanged                                 | Already REST-based                      |
| —                                        | `ApiAuthController` (/api/auth/me)        | New: returns current user info as JSON  |

### Key Behavioral Mappings

| Thymeleaf Behavior                      | Vue Equivalent                                          |
|-----------------------------------------|---------------------------------------------------------|
| `th:if`, `th:each`                      | `v-if`, `v-for`                                         |
| `th:text`, `th:value`                   | `{{ }}` interpolation, `v-model`                        |
| `th:classappend` for active nav         | `:class="{ active: ... }"` with route checking          |
| `th:action` + form POST                 | `@submit.prevent` + `api.createSession()` fetch call    |
| `th:replace` fragments                  | Vue components with `<SessionModal>`                    |
| `sec:authorize="hasAuthority()"`        | `v-if="auth.isAdmin"` via Pinia store                   |
| Page reload after mutation              | Reactive data re-fetch (`loadSessions()`, `loadData()`) |
| `#temporals.format()`                   | Custom `formatDate()` / `formatTime()` functions        |
| Inline `<script>` with DOM manipulation | Vue reactive state + Composition API                    |

## API Endpoints

### Authentication

- `POST /api/auth/login` — Form login (username/password), returns JSON
- `POST /api/auth/logout` — Logout, returns JSON
- `GET /api/auth/me` — Current user info (id, username, roles, authorities)

### Sessions

- `GET /api/sessions` — All sessions for current user
- `GET /api/sessions/by-type?type=training|competition` — Sessions filtered by type
- `GET /api/sessions/{id}` — Single session
- `POST /api/sessions` — Create session (JSON body)
- `PUT /api/sessions/{id}` — Update session (JSON body)
- `DELETE /api/sessions/{id}` — Delete session

### Locations

- `GET /api/locations` — All shooting places (unchanged)

### Settings (Admin only)

- `GET /api/settings/users` — List all users
- `POST /api/settings/users` — Create user
- `PUT /api/settings/users/{id}` — Update user
- `DELETE /api/settings/users/{id}` — Delete user
- `GET /api/settings/roles` — List all roles
- `POST /api/settings/roles` — Create role
- `PUT /api/settings/roles/{id}` — Update role
- `DELETE /api/settings/roles/{id}` — Delete role

## Security Changes

- CSRF disabled (SPA uses session cookies, no form tokens)
- Form login processes at `/api/auth/login` with JSON success/failure handlers
- API endpoints return 401 JSON for unauthenticated requests (not redirects)
- `/api/auth/**` is public (for login flow)
- `/api/settings/**` requires ADMIN role
- `/api/**` requires authentication
- All other paths are public (served by SPA)

## Frontend Project Structure

```
frontend/
├── index.html              # Entry point
├── package.json            # Dependencies (Vue 3, Vue Router, Pinia, FullCalendar)
├── vite.config.js          # Build config (outputs to src/main/resources/static)
└── src/
    ├── main.js             # App bootstrap
    ├── App.vue             # Root component with navbar
    ├── api/
    │   └── http.js         # API communication layer
    ├── assets/css/         # Migrated CSS files
    ├── components/
    │   ├── NotificationContainer.vue
    │   └── SessionModal.vue
    ├── router/
    │   └── index.js        # Vue Router config with auth guards
    ├── stores/
    │   ├── auth.js          # Authentication state (Pinia)
    │   └── notifications.js # Notification state (Pinia)
    └── views/
        ├── CalenderView.vue
        ├── DashboardView.vue
        ├── LoginView.vue
        ├── OverviewView.vue
        └── SettingsView.vue
```

## Build & Development

### Development

1. Start Spring Boot backend: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
2. Start Vue dev server: `cd frontend && npm run dev`
3. Vue dev server proxies `/api` requests to `localhost:8080`

### Production Build

- `./mvnw package` — frontend-maven-plugin automatically:
    1. Installs Node.js
    2. Runs `npm install`
    3. Runs `npm run build` (outputs to `src/main/resources/static`)
    4. Spring Boot packages static files into the JAR

## Dependencies Removed

- `spring-boot-starter-thymeleaf`
- `thymeleaf-extras-springsecurity6`
- `htmx-spring-boot-thymeleaf`

## Dependencies Added (frontend/package.json)

- `vue` ^3.5.13
- `vue-router` ^4.5.0
- `pinia` ^2.3.0
- `@fullcalendar/core` ^6.1.20
- `@fullcalendar/daygrid` ^6.1.20

## Maven Plugin Added

- `frontend-maven-plugin` 1.15.1 — Integrates npm build into Maven lifecycle
