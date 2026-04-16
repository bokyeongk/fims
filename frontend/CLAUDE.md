# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Dev server
npm run dev

# Build (TypeScript check + Vite build)
npm run build

# Lint
npm run lint

# Preview production build
npm run preview
```

## Architecture

React 19 + Vite 8 + TypeScript 6 SPA following a feature-oriented structure.

### Key Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| React | 19 | UI framework |
| TanStack Query | v5 | Server state management |
| shadcn/ui | latest | Component library (Radix UI + Tailwind) |
| axios | 1.x | HTTP client |
| Tailwind CSS | 3.x | Styling |

### Directory Structure

```
src/
├── api/          # axios client + per-domain API hooks (useQuery/useMutation)
├── components/
│   ├── ui/       # shadcn auto-generated components (do not edit manually)
│   └── common/   # shared app components
├── hooks/        # custom React hooks
├── lib/          # utilities (cn() helper via clsx + tailwind-merge)
├── pages/        # route-level page components
└── main.tsx      # app entry — QueryClientProvider wraps entire tree
```

### API Layer (`src/api/`)

- `client.ts` — singleton axios instance with `baseURL: '/api'`
  - Request interceptor: attaches `Bearer` token from `localStorage.accessToken`
  - Response interceptor: on 401 clears token and redirects to `/login`
- TanStack Query default config (in `main.tsx`): `staleTime: 5min`, `retry: 1`
- Pattern: create per-feature files (e.g., `src/api/user.ts`) exporting `useQuery`/`useMutation` hooks that use `apiClient`

### Path Alias

`@` maps to `src/` — use `@/components/...`, `@/lib/utils`, etc.

### shadcn/ui

- Base color: `slate`, CSS variables enabled
- Add components: `npx shadcn@latest add <component>`
- Components land in `src/components/ui/` — do not edit these files directly
