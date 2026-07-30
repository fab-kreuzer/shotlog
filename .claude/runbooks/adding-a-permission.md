# Adding a new permission, end-to-end

Runbook for wiring up a brand-new permission (a new settings tab, or a new create/edit/delete action) all the way from
the database to the UI. The SQL/migration mechanics are also covered by the `permissions-migration` agent — this doc
adds the frontend half and the pitfalls that don't show up until runtime.

## 1. Decide the permission name(s)

- A new settings tab: `view_<resource>_tab` (e.g. `view_season_tab`).
- A new CRUD action on an existing resource: `create_<resource>`, `edit_<resource>`, `delete_<resource>` (e.g.
  `edit_team`).
- `permission_name` must be globally unique across the whole `permissions` table (not just per resource) — it's the
  exact string checked by `hasAuthority(...)` on the backend and by `permissions.includes(...)` on the frontend.

## 2. Flyway migration

Add `permissions` rows and grant them to roles in one migration. See `.claude/agents/permissions-migration.md` for the
exact idempotent SQL shape (`ON CONFLICT DO NOTHING`, `resource`/`action` columns, the "grant ADMIN everything"
`CROSS JOIN`). Don't skip the grant step — permission rows with no `role_permissions` row are invisible to everyone,
including ADMIN.

## 3. Backend enforcement

Add `@PreAuthorize("hasAuthority('permission_name')")` to every controller method the permission should gate (see
`ApiTeamController.java` for the pattern — it's on the GET, POST, and DELETE mappings for `/teams`, not just the
create/delete ones).

## 4. Frontend: exposing it to the UI

Permissions reach the frontend through a single channel: `GET /api/auth/me` returns a `permissions` array (flattened,
deduped, from every role the user has — see `ApiAuthController.getCurrentUserInfo`). Nothing else carries permission
data.

- `frontend/src/stores/auth.js` — `hasPermission(permissionName)` checks
  `user.value.permissions?.includes(permissionName)`. This is the one function to call; don't re-derive permission
  checks elsewhere.
- For a new **settings tab**: add an entry to the `navItems` array in `frontend/src/views/settings/SettingsView.vue`
  with `permission: 'view_<resource>_tab'` — the list is already filtered by `auth.hasPermission(item.permission)`, so
  adding the entry is the whole job.
- For a **button/action** gated by permission (not a whole tab), guard it with `v-if="auth.hasPermission('edit_x')"` the
  same way.
- Add the tab's i18n label (`settings.tab<Name>` or similar) to **both** `frontend/src/i18n/en.json` and `de.json` — see
  the `vue-frontend` agent for the bilingual-update rule.

## 5. Testing checklist

- `mvn test` — confirm nothing that asserts on the permission set (e.g. a `DefaultUserInitializer` test) broke.
- Manually verify by logging in as a user with the role, or, if you changed permissions for an **already logged-in**
  session, see the pitfall below before assuming it's broken.

## Pitfalls (things that have actually gone wrong here)

- **"Nothing is automatic."** A permission inserted into `permissions` but never inserted into `role_permissions` for
  any role is dead weight — every role, including ADMIN, needs an explicit grant row.
- **Stale frontend session.** `useAuthStore.user` is only populated by `fetchUser()`, which runs on login and app
  mount — it is *not* re-fetched automatically when the database's `role_permissions` changes. If you grant a permission
  via SQL to a user who is already logged in in the browser, they won't see the new tab/button until they reload the
  app (which re-triggers `fetchUser()` on mount) or log out and back in. Don't mistake this for a broken grant — check
  whether the browser session was refreshed before digging into the SQL.
- **`resource`/`action` are `NOT NULL`.** Every `permissions` insert needs both columns set (added in
  `V12__permission_resource_action.sql`); a bare `INSERT INTO permissions (permission_name, description) VALUES (...)`
  will fail.
- **A missing `@PreAuthorize` is a silent hole.** The frontend tab check and the backend endpoint check are two
  independent gates — hiding a tab in the UI does nothing to stop a direct API call if the corresponding controller
  method has no `@PreAuthorize`. Always add both.
