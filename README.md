# 🎯 ShotLog - User & Developer Guide

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?logo=springboot&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs&logoColor=white)
![PrimeVue](https://img.shields.io/badge/PrimeVue-4-06B6D4)
![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-4-38BDF8?logo=tailwindcss&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Flyway-336791?logo=postgresql&logoColor=white)

ShotLog is a shooting-sport session tracker. Shooters log **training** and **competition** sessions (as series of
individual shots), see everything on a shared **calendar**, and track season stats on a **dashboard** — while club
admins manage users, roles, teams, clubs, and seasons through a fine-grained, role-based settings area.

![Dashboard View](docs/images/dashboard.png)

---

## 📱 At a Glance

1. **Sign up or log in** — self-registration creates a `USER` account with no admin rights by default.
2. **Land on the Dashboard** — a personal greeting, season stats, and recent/upcoming sessions.
3. **Log sessions** — record a `Training` (series of shots) or a `Competition` (opponent, home/away, series of shots),
   either from the session pages or by clicking an event on the **Calendar**.
4. **Manage your profile** — avatar, display name, home club — all self-service, no permission required.
5. **(If permitted) manage the club** — users, roles & permissions, clubs, teams, and seasons, all under **Settings**,
   each tab independently gated by permission.

> [!NOTE]
> Everything through step 4 only requires being logged in. **Settings** is the only permission-gated area —
> see [🔐 Permissions & Access Control](#-permissions--access-control) below.

---

## 📱 Features & UI Walkthrough

### 1. Navigation Bar

- **Overview:** Persistent top bar shown on every authenticated page.
- **Key UI Elements:**
    - `ShotLog` logo/wordmark — links back to the Dashboard.
    - `Dashboard` / `Training` / `Competition` / `Calendar` — primary nav links, highlighted when active.
    - Language toggle — switches the UI between English and German (`vue-i18n`).
    - Theme toggle — switches between light and dark mode.
    - Avatar button (top right) — opens a popup menu: `Profile`, `Settings` (only shown if the user has at least one
      `view_*_tab` permission), and `Logout`.
- **Related Code:** [`frontend/src/App.vue`](./frontend/src/App.vue), [
  `frontend/src/router/index.js`](./frontend/src/router/index.js)

### 2. Login & Registration

- **Overview:** A single card with `Sign in` / `Sign up` tabs, reached at `/login`. Unauthenticated users are redirected
  here for any other route.
- **Key UI Elements:**
    - `Username` / `Password` (Sign in) — submits against the session-based auth endpoint; shows an error toast on
      invalid credentials.
    - `Username` / `Display Name` / `Password` / `Confirm Password` (Sign up) — inline field validation (min-length,
      confirm-password match); successful signup switches back to the Sign in tab.
    - Language/theme toggles are also available here, before login.
- **Related Code:** [`frontend/src/views/LoginView.vue`](./frontend/src/views/LoginView.vue), [
  `frontend/src/components/LoginForm.vue`](./frontend/src/components/LoginForm.vue), [
  `frontend/src/components/SignupForm.vue`](./frontend/src/components/SignupForm.vue), [
  `ApiAuthController.java`](./src/main/java/dev/fkreuzer/shotlog/controller/api/ApiAuthController.java)

### 3. Dashboard

- **Overview:** Landing page after login — a time-of-day greeting plus an at-a-glance summary of the active season.
- **Key UI Elements:**
    - **Season stats card** — training count, competition count, average score, best score, and home/away split for the
      active season.
    - **Recent sessions card** — the last 5 sessions that have already happened.
    - **Upcoming sessions card** — the next 3 sessions still to come.
- **Related Code:** [`frontend/src/views/DashboardView.vue`](./frontend/src/views/DashboardView.vue), [
  `frontend/src/components/dashboard/SeasonStatsCard.vue`](./frontend/src/components/dashboard/SeasonStatsCard.vue), [
  `RecentSessionsCard.vue`](./frontend/src/components/dashboard/RecentSessionsCard.vue), [
  `UpcomingSessionsCard.vue`](./frontend/src/components/dashboard/UpcomingSessionsCard.vue)

### 4. Training Sessions

- **Overview:** Card grid of every training session, filterable by season and searchable by title/date/time.
- **Key UI Elements:**
    - `Season` multiselect — filters the grid to one or more seasons (defaults to the active season).
    - `Search` field — live client-side filter over title, date, and time.
    - `+ New training session` — opens the training modal in create mode.
    - **Session card** — click anywhere on the card to edit; a trash icon (visible on hover, always visible on mobile)
      deletes it with a confirmation dialog.
    - **Training modal** — description, date, start time, season (read-only), decimal-scoring toggle, and a dynamic *
      *series/shots editor**: add a series, mark it a test shot, and enter each shot's score (steppered 0.0–10.9).
- **Related Code:** [`frontend/src/views/TrainingPage.vue`](./frontend/src/views/TrainingPage.vue), [
  `frontend/src/components/SessionGrid.vue`](./frontend/src/components/SessionGrid.vue), [
  `frontend/src/components/session/TrainingSessionModal.vue`](./frontend/src/components/session/TrainingSessionModal.vue), [
  `SessionSeriesEditor.vue`](./frontend/src/components/session/SessionSeriesEditor.vue), [
  `useSessionList.js`](./frontend/src/composables/useSessionList.js), [
  `useSeasonFilter.js`](./frontend/src/composables/useSeasonFilter.js), [
  `useSessionSearch.js`](./frontend/src/composables/useSessionSearch.js)

### 5. Competition Sessions

- **Overview:** Same card-grid pattern as Training, plus opponent/venue/team fields and calendar import.
- **Key UI Elements:**
    - Everything from the Training grid (season filter, search, click-to-edit cards).
    - `Import dates (.ics)` — uploads a calendar file to bulk-create competition sessions.
    - **Competition modal** — adds `Opponent` (club), `Team`, and a `Home match` toggle on top of the training fields.
- **Related Code:** [`frontend/src/views/CompetitionPage.vue`](./frontend/src/views/CompetitionPage.vue), [
  `frontend/src/components/session/CompetitionSessionModal.vue`](./frontend/src/components/session/CompetitionSessionModal.vue), [
  `ApiSessionController.java`](./src/main/java/dev/fkreuzer/shotlog/controller/api/ApiSessionController.java)

### 6. Calendar

- **Overview:** Month view of every session (training and competition together), color-coded by type.
- **Key UI Elements:**
    - Month grid (FullCalendar) — `today` / `prev` / `next` navigation.
    - Color legend — green for Training, red for Competition.
    - Clicking an event opens the matching session modal (Training or Competition) directly in edit mode.
- **Related Code:** [`frontend/src/views/CalenderView.vue`](./frontend/src/views/CalenderView.vue)

### 7. Profile

- **Overview:** Self-service account page — no permission required, every logged-in user has one.
- **Key UI Elements:**
    - `Upload avatar` — picks an image (client-side downscaled/cropped to a 256×256 JPEG before upload, 2 MB server-side
      limit).
    - `Remove avatar` — shown only if an avatar is set.
    - `Display name` field + `Save` — only enabled once the value actually changed.
    - `Home club` dropdown — sets the user's default club, saved immediately on change.
    - Read-only `Roles` and `Teams` tags, and the current active season.
- **Related Code:** [`frontend/src/views/ProfileView.vue`](./frontend/src/views/ProfileView.vue), [
  `ApiAuthController.java`](./src/main/java/dev/fkreuzer/shotlog/controller/api/ApiAuthController.java) (`/me`,
  `/me/avatar`)

### 8. Settings — User Management

- **Overview:** Table of every registered user. Gated by `view_user_tab`.
- **Key UI Elements:**
    - `+ New user` (`create_user`) — opens a create dialog: username, display name, password, roles (multiselect).
    - Table columns: `ID`, `Username`, `Display Name`, `Roles` (tags).
    - Row edit icon (`edit_user`) — dialog to change username, display name, roles, and optionally reset the password.
    - Row delete icon (`delete_user`) — confirmation dialog, then deletes.
- **Related Code:** [`frontend/src/views/settings/UserTab.vue`](./frontend/src/views/settings/UserTab.vue), [
  `CreateUserModal.vue`](./frontend/src/components/settings/CreateUserModal.vue), [
  `ApiUserController.java`](./src/main/java/dev/fkreuzer/shotlog/controller/api/ApiUserController.java)

### 9. Settings — Role Management

- **Overview:** Table of every role and the permissions granted to it. Gated by `view_role_tab`.
- **Key UI Elements:**
    - `+ New role` (`create_role`) — dialog with a role name and a **permission matrix** (resource rows ×
      view/create/edit/delete columns, checkbox per cell).
    - Table's `Permissions` column — a compact, grouped tag summary per role (click a resource tag to see its granted
      actions in a popover).
    - Row edit icon (`edit_role`) — same name + permission-matrix dialog, pre-filled. If the edited role is the current
      user's own role, their session's cached permissions are refreshed immediately after saving.
    - Row delete icon (`delete_role`) — confirmation dialog, then deletes.
- **Related Code:** [`frontend/src/views/settings/RoleTab.vue`](./frontend/src/views/settings/RoleTab.vue), [
  `PermissionMatrix.vue`](./frontend/src/components/settings/PermissionMatrix.vue), [
  `PermissionSummary.vue`](./frontend/src/components/settings/PermissionSummary.vue), [
  `CreateRoleModal.vue`](./frontend/src/components/settings/CreateRoleModal.vue), [
  `ApiSettingsController.java`](./src/main/java/dev/fkreuzer/shotlog/controller/api/ApiSettingsController.java)

### 10. Settings — Club Management

- **Overview:** Table of shooting clubs/locations, used as opponents and home clubs elsewhere in the app. Gated by
  `view_club_tab`.
- **Key UI Elements:**
    - `+ New club` (`create_club`) — dialog: club name, location.
    - Table's `Location` column links out to a Google Maps search for that address.
    - A `Home club` tag marks the current user's own home club in the list.
    - Row edit icon (`edit_club`) / delete icon (`delete_club`, blocked with a message if the club is still referenced
      by sessions).
- **Related Code:** [
  `frontend/src/views/settings/ClubManagementTab.vue`](./frontend/src/views/settings/ClubManagementTab.vue), [
  `CreateLocationModal.vue`](./frontend/src/components/settings/CreateLocationModal.vue), [
  `ApiLocationController.java`](./src/main/java/dev/fkreuzer/shotlog/controller/api/ApiLocationController.java)

### 11. Settings — Team Management

- **Overview:** Accordion of teams for a selected season, with roster management per team. Gated by `view_team_tab`.
- **Key UI Elements:**
    - `Season` dropdown — scopes the visible teams to one season.
    - `+ New team` (`create_team`) — name-only creation dialog, scoped to the selected season.
    - Team accordion header — member count, `+` icon (`edit_team`) to add a member, trash icon (`delete_team`) to delete
      the team (prompts to also delete attached competition sessions if any exist).
    - **Add member dialog** — team role (`Member`/`Leader`) select + searchable user list; clicking a user adds them
      immediately.
    - Per-member trash icon (`edit_team`) — removes that member from the team roster.
- **Related Code:** [`frontend/src/views/settings/TeamTab.vue`](./frontend/src/views/settings/TeamTab.vue), [
  `ApiTeamController.java`](./src/main/java/dev/fkreuzer/shotlog/controller/api/ApiTeamController.java)

### 12. Settings — Season Management

- **Overview:** Simple list of seasons, with exactly one marked active at a time. Gated by `view_season_tab`.
- **Key UI Elements:**
    - `+ New season` (`create_season`) — dialog with a single description field.
    - `Set active` button — switches which season is active app-wide (affects default filters on
      Training/Competition/Dashboard/Profile).
    - Row edit icon (`edit_season`) / delete icon (`delete_season`, blocked if the season is active or still in use).
- **Related Code:** [`frontend/src/views/settings/SeasonTab.vue`](./frontend/src/views/settings/SeasonTab.vue), [
  `ApiSeasonController.java`](./src/main/java/dev/fkreuzer/shotlog/controller/api/ApiSeasonController.java)

### 13. Settings — Access Denied

- **Overview:** Fallback shown when a user reaches `/settings` (or a specific tab) without any matching permission — a
  lock icon and an explanatory message, no data or actions.
- **Related Code:** [
  `frontend/src/views/settings/AccessDeniedTab.vue`](./frontend/src/views/settings/AccessDeniedTab.vue)

![Settings — Role Management](docs/images/role-management.png)

---

## 🔐 Permissions & Access Control

Access control has two independent layers:

- **Core app pages** (Dashboard, Training, Competition, Calendar, Profile) require only being **logged in** — no
  permission, and identical for every role.
- **Settings** is gated permission-by-permission, resource by resource: `user`, `role`, `club`, `team`, `season`, each
  with `view` / `create` / `edit` / `delete` actions. A missing `view_<resource>_tab` permission hides that whole tab
  from the Settings nav; missing `create`/`edit`/`delete` hides just the corresponding buttons on an otherwise-visible
  tab.

Two roles ship by default:

| Role      | Dashboard / Training / Competition / Calendar / Profile | Settings — Users | Settings — Roles | Settings — Clubs | Settings — Teams | Settings — Seasons |
|:----------|:-------------------------------------------------------:|:----------------:|:----------------:|:----------------:|:----------------:|:------------------:|
| **ADMIN** |                            ✅                            |      ✅ Full      |      ✅ Full      |      ✅ Full      |      ✅ Full      |       ✅ Full       |
| **USER**  |                            ✅                            |        ❌         |        ❌         |        ❌         |        ❌         |         ❌          |

> [!NOTE]
> Roles beyond `ADMIN`/`USER` are not hardcoded — an admin creates them in **Settings → Role Management**, mixing and
> matching any of the 20 permissions below (e.g. a "Sport Leader" role with full team management but read-only everything
> else). The table above only reflects the two seeded roles; a custom role's access is whatever its permission matrix
> says.

**Full permission catalog** (resource × action — what the Role Management permission matrix actually offers):

| Resource   | view | create | edit | delete |
|:-----------|:----:|:------:|:----:|:------:|
| **user**   |  ✅   |   ✅    |  ✅   |   ✅    |
| **role**   |  ✅   |   ✅    |  ✅   |   ✅    |
| **club**   |  ✅   |   ✅    |  ✅   |   ✅    |
| **team**   |  ✅   |   ✅    |  ✅   |   ✅    |
| **season** |  ✅   |   ✅    |  ✅   |   ✅    |

> [!IMPORTANT]
> Granting a permission via the database directly (rather than through the UI) does **not** take effect for an
> already-logged-in user until they reload the app or log back in — the permission set is only refreshed by `fetchUser()`
> on login/app mount.

There is also a separate, unrelated **per-team role** (`Member` / `Leader`) set on each team roster entry in Settings →
Team Management — this only labels a person's role within one specific team and has no effect on app-wide permissions.

---

## 📁 Repository Quick Map

```
shotlog/
├── src/main/java/dev/fkreuzer/shotlog/
│   ├── controller/api/       # REST controllers (see links throughout this doc)
│   ├── domain/                # JPA entities (UserAccount, Role, Permission, Session, Team, Season, ShootingPlace, ...)
│   ├── repository/            # Spring Data repositories
│   ├── security/              # SecurityUser / Spring Security wiring
│   ├── service/                # Business logic (e.g. ShootingPlaceService)
│   └── config/                 # DefaultUserInitializer, etc.
├── src/main/resources/
│   ├── application*.yml       # Spring config per profile (dev/prod)
│   └── db/migration/           # Flyway migrations (V1 … V18) — schema + permission seeding
├── frontend/src/
│   ├── views/                  # Route-level pages (Dashboard, Training, Competition, Calendar, Profile, Settings/*)
│   ├── components/             # Reusable UI (SessionGrid, modals, dashboard cards, settings widgets)
│   ├── composables/            # useSessionList, useSeasonFilter, useSessionSearch, useSessionForm
│   ├── stores/                 # Pinia stores (auth.js, notifications.js)
│   ├── i18n/                   # en.json / de.json — every user-facing string
│   └── router/index.js         # Routes + permission-based navigation guards
├── .claude/
│   ├── agents/                 # Project-specific coding agents (backend-test-writer, permissions-migration, vue-frontend)
│   ├── runbooks/                # Step-by-step guides (adding-a-permission.md)
│   └── diary/                   # Per-session dev diary (YYYY-MM-DD.md)
└── CLAUDE.md                    # Stack + conventions summary for AI-assisted development
```

---

## 🛠️ Local Development

| Task                       | Command                                                                 |
|:---------------------------|:------------------------------------------------------------------------|
| Run backend tests          | `mvn test`                                                              |
| Run one backend test class | `mvn -Dtest=ClassName test`                                             |
| Start frontend dev server  | `cd frontend && npm run dev`                                            |
| Build frontend             | `cd frontend && npm run build`                                          |
| Full app                   | `mvn spring-boot:run` (frontend is bundled via `frontend-maven-plugin`) |

Requires a local PostgreSQL instance (`shotlog_dev` database) — Flyway applies all migrations automatically on startup.
A default `admin` / `admin` account is created on first run if no user named `admin` exists yet.

> [!WARNING]
> Change the default `admin` password before exposing any environment beyond local development.
