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

## Responsive Navigation

모바일과 웹 환경에서 서로 다른 네비게이션을 활성화한다. JavaScript window width 감지 없이 TailwindCSS breakpoint만으로 처리한다.

| 환경 | Breakpoint | 컴포넌트 |
|------|-----------|---------|
| 모바일 | `< md` (768px 미만) | `BottomNavigation.tsx` — 화면 하단 고정 탭 |
| 웹/데스크탑 | `≥ md` (768px 이상) | `Sidebar.tsx` — 좌측 고정 LNB |

- 네비게이션 메뉴 아이템은 `src/components/common/navItems.ts`에서 중앙 관리하며, BottomNavigation과 Sidebar가 공유한다.
- 가시성 전환은 CSS class(`md:hidden` / `hidden md:flex`)로만 처리한다.

## Responsive Strategy

프로젝트는 **Single Codebase(반응형)**를 지향하며, 기기별 특성에 따라 다음과 같이 최적화한다.

### 1. 반응형 레이아웃

하나의 컴포넌트 내에서 TailwindCSS Media Query와 Grid 시스템으로 레이아웃을 대응한다.

| 환경 | Breakpoint | Tailwind prefix |
|------|-----------|----------------|
| Mobile | `< 768px` | (기본값) |
| Tablet | `768px ~ 1024px` | `md:` |
| Desktop | `> 1024px` | `lg:` |

### 2. 조건부 렌더링 (Feature Gating)

- **정보 밀도**: Desktop에서는 데이터 테이블 컬럼을 확장하여 더 많은 정보를 노출하고, Mobile에서는 핵심 요약 정보 위주로 노출한다.
- **기능 제한**: 엑셀 업로드/다운로드, 대용량 로그 조회 등 복잡한 관리자 기능은 **Desktop 전용(`isDesktop`)**으로 구현하여 모바일 성능 저하를 방지한다.
- **구현 방식**: `useMediaQuery` 훅이나 전역 `deviceStatus` 상태를 참조하여 컴포넌트 단위로 분기 처리한다.

```tsx
// 예시
const { isDesktop } = useMediaQuery()
{isDesktop && <ExcelDownloadButton />}
```

### 3. 모바일 최적화 (Mobile-First)

- **터치 인터페이스**: 모든 버튼과 클릭 요소는 최소 `44×44px` 이상의 터치 영역을 확보한다.
- **성능 관리**: Desktop 전용 무거운 라이브러리는 Dynamic Import로 필요 시에만 로드하여 Webview 로딩 속도를 최적화한다.

```tsx
// 예시
const HeavyChart = lazy(() => import('@/components/HeavyChart'))
```

### 4. 개발 가이드

- 컴포넌트 작성 시 **"이 기능이 모바일에서도 유효한가?"** 를 먼저 자문한다.
- 모바일에서 불필요한 고해상도 이미지나 복잡한 애니메이션은 지양한다.
- CSS만으로 처리 가능한 반응형은 `useMediaQuery` 없이 Tailwind class로만 구현한다.

---

## Conventions

- Path alias: `@` → `src/` — use `@/components/...`, `@/lib/utils`, etc.
- shadcn/ui base color: `slate`, CSS variables enabled
- Add shadcn components: `npx shadcn@latest add <component>` — components land in `src/components/ui/`, do not edit these files directly

## UI 컴포넌트 사용 규칙

**모든 UI 요소는 기본 HTML 태그 대신 shadcn/ui 컴포넌트를 사용한다.**

| HTML 태그 | shadcn/ui 컴포넌트 | 비고 |
|-----------|-------------------|------|
| `<button>` | `<Button>` | `@/components/ui/button` |
| `<input>` | `<Input>` | `@/components/ui/input` |
| `<label>` | `<Label>` | `@/components/ui/label` |
| `<input type="checkbox">` | `<Checkbox>` | `@/components/ui/checkbox` |
| `<select>` | `<Select>` | `@/components/ui/select` |

- 필요한 컴포넌트가 없으면 `npx shadcn@latest add <component>` 로 추가 후 사용한다.
- shadcn 컴포넌트에 없는 요소(Canvas, SVG 등)만 예외적으로 기본 HTML 태그를 허용한다.
- shadcn 컴포넌트에 Tailwind 클래스를 추가할 때는 `className` prop을 사용한다.
