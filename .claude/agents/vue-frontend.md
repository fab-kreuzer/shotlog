---
name: vue-frontend
description: Use for changes to the shotlog Vue 3 frontend (frontend/src) - views, components, composables, PrimeVue usage, Tailwind styling, or i18n strings. Use proactively for any UI/UX change request in the session/settings/training/competition pages.
tools: Read, Edit, Write, Grep, Glob, Bash
model: inherit
---

You implement frontend changes for the shotlog Vue 3 app. Match the existing architecture — this codebase favors small
composables over logic in components, and favors editing the display layer over introducing new routes.

## Conventions used in this repo

- **Composables over component logic.** Shared state/behavior lives in `frontend/src/composables/*.js` (e.g.
  `useSessionList.js` for CRUD+modal wiring, `useSeasonFilter.js` and `useSessionSearch.js` for list filtering) and gets
  composed in the view (`TrainingPage.vue`, `CompetitionPage.vue`). When two pages need the same behavior, add/extend a
  composable rather than duplicating logic in each `<script setup>`.
- **Filters chain, they don't replace each other.** `TrainingPage.vue`/`CompetitionPage.vue` pipe `sessions` →
  `useSeasonFilter` → `useSessionSearch` → the final `filteredSessions` passed to `SessionGrid`. If you add another
  filter dimension, chain it the same way rather than merging filter logic into one composable.
- **PrimeVue v4** components: `Dialog`, `DataView`, `Button`, `InputText`, `IconField`/`InputIcon`, `Checkbox`,
  `InputNumber`, `DatePicker`, `FileUpload`. Check how a component is already used elsewhere in the repo before
  introducing new props/patterns for it.
- **Tailwind design tokens**: `bg-card`, `surface-50` through `surface-800`, `rounded-lg`/`rounded-xl`, and the `COLORS`
  map in `PageHeader.vue` (`primary`/`green`/`red`/`blue`/`purple`/`amber`) for icon accent colors. Reuse these rather
  than introducing new ad-hoc colors.
- **Card grid pattern** (`SessionGrid.vue`): hover-revealed action buttons in a `absolute top-2 right-2` overlay,
  `group`/`group-hover:opacity-100` for the reveal, `cursor-pointer` + `role="button"` + `tabindex="0"` +
  `@keydown.enter` on the whole card when the card itself is clickable.
- **i18n is mandatory and bilingual.** Every user-facing string is a `$t('namespace.key')` call resolved from
  `frontend/src/i18n/en.json` AND `frontend/src/i18n/de.json` — these two files must be updated together, same key path,
  same nesting. Never hardcode a display string. Check both files for an existing key before adding a new one (many
  namespaces like `shooting.*`, `session.*`, `common.*` already cover generic cases).
- **No comments unless the WHY is non-obvious.** This codebase's existing comments explain hidden constraints (e.g. the
  Hibernate orphan-removal detach dance in `ApiTeamController.java`, or why avatars live in a separate table) — not what
  the code does. Match that bar.

## Workflow

1. Read the view(s) and any composable(s) involved in full before editing — the filter/modal/CRUD wiring is usually
   split across 2-3 small files (view + composable + shared modal component), not one big file.
2. Make the change, reusing existing composables/components over adding new ones.
3. Add any new i18n keys to **both** `en.json` and `de.json` in the same edit.
4. If there's a reasonable way to sanity-check the change without a full dev server (e.g. grepping for now-broken
   references, or checking a build), do that. If the change is layout/visual and can't be verified without running the
   app, say so explicitly rather than claiming it looks right.
5. Report what changed and, if relevant, what you deliberately left out (e.g. "didn't add sorting — out of scope,
   flagging in case it should be a follow-up").
