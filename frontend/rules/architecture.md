# Frontend Architecture

React 19 + Vite + TypeScript SPA.

## Build & Run Commands

```bash
# (frontend/ 디렉토리에서 실행)

npm run dev       # Dev server
npm run build     # TypeScript check + Vite build
npm run lint      # ESLint
npm run preview   # Preview production build
```

## Key Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| React | 19 | UI framework |
| TanStack Query | v5 | Server state management |
| shadcn/ui | latest | Component library (Radix UI + Tailwind) |
| axios | 1.x | HTTP client |
| Tailwind CSS | 3.x | Styling |

## Directory Structure

```
src/
├── api/              # axios client + per-domain query/mutation hooks
├── components/
│   ├── ui/           # shadcn auto-generated (do not edit manually)
│   └── common/       # shared app components
├── hooks/            # custom React hooks
├── lib/              # utilities (cn() helper via clsx + tailwind-merge)
├── pages/            # route-level page components
└── main.tsx          # app entry — QueryClientProvider wraps entire tree
```

## API Layer (`src/api/`)

- `client.ts` — singleton axios instance with `baseURL: '/api'`
  - Request interceptor: attaches `Bearer` token from `localStorage.accessToken`
  - Response interceptor: on 401 clears token and redirects to `/login`
- TanStack Query default config (in `main.tsx`): `staleTime: 5min`, `retry: 1`
- Pattern: create per-feature files (e.g., `src/api/user.ts`) exporting `useQuery`/`useMutation` hooks that use `apiClient`

## Conventions

- Path alias: `@` → `src/` — use `@/components/...`, `@/lib/utils`, etc.
- shadcn/ui base color: `slate`, CSS variables enabled
- Add shadcn components: `npx shadcn@latest add <component>` — components land in `src/components/ui/`, do not edit these files directly
