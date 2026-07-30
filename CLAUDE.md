# shotlog

A shooting-sport session tracker: log training/competition sessions (series of shots), manage seasons, teams, clubs, and
users, with a fine-grained role/permission system.

## Stack

- **Backend**: Java 17, Spring Boot 3.2.0, Maven. `spring-boot-starter-web`, `-data-jpa`, `-security`. Package root:
  `dev.fkreuzer.shotlog` (`controller`, `domain`, `dto`, `repository`, `security`, `service`, `web`).
- **Database**: PostgreSQL, schema managed by **Flyway** (`src/main/resources/db/migration/V*.sql`). H2 is test-only.
- **Frontend**: Vue 3 + Vite, PrimeVue 4 (component library + theme), Tailwind CSS 4, vue-i18n (English + German),
  Pinia (state), vue-router. Source under `frontend/src`. Built into the Spring Boot jar via `frontend-maven-plugin`.

## Running & testing

- Backend tests: `mvn test` (or `mvn -Dtest=ClassName test` for one class). Always run the full suite before calling a
  backend change done, not just the touched test class.
- Frontend dev server: `cd frontend && npm run dev`. Build: `npm run build`.
- Dev DB: Postgres on `localhost:5432/shotlog_dev` (see `application-dev.yml`); Flyway runs migrations automatically on
  startup (`baseline-on-migrate: true`).

## Key conventions

- **Permissions are fine-grained and nothing is automatic.** `roles` / `permissions` / `role_permissions` (join table) —
  adding a permission row does NOT grant it to any role; every new permission needs an explicit `role_permissions`
  insert. See `.claude/runbooks/adding-a-permission.md` for the full path (migration →
  `@PreAuthorize('hasAuthority(...)')` → frontend `SettingsView.vue` nav entry → i18n), and
  `.claude/agents/permissions-migration.md` for the SQL conventions.
- **Frontend permissions** flow through one channel: `GET /api/auth/me` returns a `permissions` array, checked via
  `useAuthStore.hasPermission(name)` (`frontend/src/stores/auth.js`). That store is only refreshed on login/app mount —
  a DB-side grant to an already-logged-in session won't show up until reload/re-login.
- **i18n is mandatory and bilingual.** Every user-facing string goes through `$t('namespace.key')`;
  `frontend/src/i18n/en.json` and `de.json` must be updated together with the same key path. Never hardcode display
  text.
- **Composables over component logic** on the frontend — shared list/filter/CRUD behavior lives in
  `frontend/src/composables/*.js` (e.g. `useSessionList`, `useSeasonFilter`, `useSessionSearch`) and gets composed in
  the view, not duplicated per page. See `.claude/agents/vue-frontend.md`.
- **Backend tests** use JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`/`@InjectMocks`),
  Arrange/Act/Assert comments, and set up the current user via a real `SecurityContextHolder` authentication rather than
  mocking `getCurrentUser()`. See `.claude/agents/backend-test-writer.md`.
- **Flyway migrations**: check `src/main/resources/db/migration/V*.sql` for the current highest version before naming a
  new one (`V{next}__snake_case_description.sql`) — don't trust memory of the last number. Seed-data inserts should be
  idempotent (`ON CONFLICT ... DO NOTHING`).

## Project tooling set up for this repo

- `.claude/agents/` — `backend-test-writer`, `permissions-migration`, `vue-frontend` subagents encoding the conventions
  above.
- `.claude/runbooks/` — step-by-step guides for recurring tasks (currently: `adding-a-permission.md`).
- `.claude/diary/` — one markdown file per dev session (`YYYY-MM-DD.md`) capturing what went well / what not to do /
  what's useful. Add a new entry when asked to "write in the diary."
