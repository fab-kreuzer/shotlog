import {THEME} from '@/lib/shotsapp-target'

/** Colour scale of the DISAG ShotsApp: >= 10.0 red, >= 9.0 gold, otherwise green. `value` is in rings. */
export function shotColor(value) {
    return value >= 10 ? THEME.danger : value >= 9 ? THEME.warning : THEME.success
}
