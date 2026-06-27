# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

@AGENTS.md

## Commands

```bash
npm run dev      # start dev server (Turbopack) at http://localhost:3000
npm run build    # production build — always run before declaring work done
npm run lint     # ESLint
```

There are no tests. `npm run build` is the correctness check.

## Stack

- **Next.js 16** (App Router, React 19) — see AGENTS.md warning: this version has breaking changes
- **Tailwind v4** — CSS-first config, no `tailwind.config.ts`. Utilities live in `@layer utilities`; unlayered CSS beats them regardless of specificity. Never add bare `* { margin: 0 }` or similar resets — they will silently kill all spacing utilities.
- **ShadCN** — components in `components/ui/`, configured via `components.json` (base color: neutral, CSS variables on)
- **Leaflet** — map library, SSR-incompatible. Always import via `next/dynamic` with `ssr: false`. See `components/map/LeafletMap.tsx` for the pattern.

## Architecture

### Routes

```
app/
  page.tsx                  landing page (static)
  (auth)/login/page.tsx     sign in
  (auth)/signup/page.tsx    sign up — role toggle: driver | provider
  onboarding/page.tsx       3-step vehicle registration (driver only)
  explore/page.tsx          map + provider list (60/40 desktop, bottom strip mobile)
  provider/[id]/page.tsx    provider detail — photos, amenities, reviews, book CTA
  order/[id]/page.tsx       order confirmation — time picker, price breakdown
  route/[id]/page.tsx       post-payment route map
```

All navigation flows are mock-only (no backend). Auth pages fake success after 1 s and redirect.

### Design system

Theme is controlled via `data-theme` attribute on `<html>` (`"dark"` default, `"light"` toggle). Preference is persisted to `localStorage` under the key `"volzen-theme"`. A flash-prevention inline script in `app/layout.tsx` reads this before React hydrates.

All colours are CSS custom properties — never use raw hex in new components:

| Token | Dark | Light |
|---|---|---|
| `--bg` | `#0a0f0d` | `#f4faf7` |
| `--accent` | `#4ade80` | `#16a34a` |
| `--accent-fg` | `#0a0f0d` | `#ffffff` |
| `--text` | `#f0fdf4` | `#0d1f18` |
| `--text-muted` | `#86efac` | `#3d6b52` |
| `--glass-bg` | `rgba(255,255,255,0.05)` | `rgba(255,255,255,0.6)` |
| `--glass-border` | `rgba(74,222,128,0.18)` | `rgba(22,163,74,0.18)` |

Glass card pattern (use the `.glass` utility class, not inline styles):
```tsx
<div className="glass glass-hover p-6">...</div>
```

### Mock data

All provider, vehicle, and review data lives in `lib/mock-data.ts`. This is the single source of truth for the prototype — no API calls anywhere. `VN_DONG_FORMAT` is the shared VND currency formatter.

### ThemeToggle

`components/ThemeToggle.tsx` is a client component. Import it only in server components that are rendered as part of a page — it must not be imported inside other `"use client"` files that are already in the client bundle unnecessarily.
