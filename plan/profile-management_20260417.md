# 프로필 관리 구현 플랜

## 요구사항 요약

- **화면 진입 시 정보 노출**: 이름, 전화번호, 이메일, 소셜 연동 계정(네이버·카카오·구글), 서명
- **수정 기능**:
  - 이름, 전화번호, 이메일 직접 편집
  - 전화번호: 숫자만 입력 가능, 자동 `010-XXXX-XXXX` 포맷 적용 (마스킹 라이브러리 미사용, 010 포맷만 지원)
  - 이메일: 변경 시 중복확인 + 이메일 인증 필수 (인증 완료 전 저장 버튼 비활성화)
  - 소셜 계정 연동 현황 노출 — **추천 사항** (해제 기능 없음, 추가만 제공)
  - **최소 1개 이상의 소셜 계정 유지 규칙** (조회만, 해제 불가)
  - 서명: 캔버스 직접 서명 입력 / 저장 / 삭제 (Base64, 인코딩 후 문자열 기준 최대 273KB)

---

## 도메인 모델 변경 사항

### `User` 엔티티 필드 추가 (db-optimizer)

| 필드 | 타입 | 설명 |
|------|------|------|
| `phone` | `VARCHAR(30)` | 전화번호 (nullable) |
| `signatureData` | `TEXT` | 서명 캔버스 Base64 데이터 (nullable, 인코딩 후 문자열 기준 최대 273KB) |

---

## API 설계 초안

| Method | Endpoint | 요청 Body / Param | 응답 | 설명 |
|--------|----------|-------------------|------|------|
| GET | `/api/v1/users/profile` | — | `ProfileResponse` | 프로필 전체 조회 |
| PUT | `/api/v1/users/profile` | `{ name, phone, email }` | `ProfileResponse` | 기본 정보 수정 (phone: 010 포맷 서버 검증 포함) |
| GET | `/api/v1/users/check-email` | `?email=xxx` | `{ available: boolean }` | 이메일 중복 확인 (인증 필요, Rate Limit 적용) |
| POST | `/api/v1/users/verify-email` | `{ email }` | `ApiResponse<Void>` | 이메일 인증 코드 발송 |
| POST | `/api/v1/users/verify-email/confirm` | `{ email, code }` | `ApiResponse<Void>` | 이메일 인증 코드 확인 (5회 초과 시 코드 만료) |
| PUT | `/api/v1/users/profile/signature` | `{ signatureData: "data:image/png;base64,..." }` | `{ signatureData }` | 서명 저장 (인코딩 문자열 기준 273KB 초과 시 오류) |
| DELETE | `/api/v1/users/profile/signature` | — | `ApiResponse<Void>` | 서명 삭제 |
| GET | `/oauth2/authorization/{provider}` | — | redirect | 소셜 계정 추가 (redirectUri는 sessionStorage로 전달) |

### `ProfileResponse` DTO

```json
{
  "id": 1,
  "name": "홍길동",
  "phone": "010-1234-5678",
  "email": "user@example.com",
  "signatureData": "data:image/png;base64,...",
  "socialAccounts": [
    { "provider": "GOOGLE", "email": "user@gmail.com" }
  ]
}
```

- `socialAccounts`: 실제 연동된 계정만 반환 (미연동 계정 미포함)
- 프론트에서 구글/네이버/카카오 3개 행 고정 노출 후 연동 여부를 클라이언트에서 판단

### 소셜 계정 추가 플로우 (추천 사항)

- 프론트에서 OAuth 시작 전 `sessionStorage.setItem('oauth2RedirectUri', '/settings/profile')` 저장 (쿠키 미사용 — XSS 취약점 방지)
- OAuth 완료 후 `OAuth2CallbackPage`에서 `sessionStorage`의 `oauth2RedirectUri` 값을 읽어 해당 경로로 `navigate()`
- `OAuth2SuccessHandler`는 리다이렉트 경로를 직접 다루지 않고, 허용된 origin(`application.yaml`의 `app.oauth2.allowed-redirect-origins`)으로만 리다이렉트
- `OAuth2SuccessHandler`에서 이미 연동된 소셜 계정 재연결 시도 시: 신규 연결 없이 기존 연동 계정 유지 + 성공 응답 처리 ("이미 연동된 계정" 안내)

---

## 프론트엔드 화면 설계

### 페이지 / 컴포넌트 목록

```
pages/
  SettingsProfilePage.tsx          # /settings/profile 라우트

components/profile/
  ProfileForm.tsx                  # 기본 정보 폼 (이름, 전화번호, 이메일)
  EmailVerification.tsx            # 이메일 중복확인 + 인증 코드 발송/확인 UI
  SocialAccountList.tsx            # 소셜 연동 목록 (구글/네이버/카카오 각 행, 추가만)
  SocialAccountItem.tsx            # 개별 소셜 계정 행 (연동 상태 + 추가 버튼)
  SignatureCanvas.tsx              # 캔버스 서명 입력 / 미리보기 / 저장 / 삭제

utils/
  phone.ts                         # formatPhone(value: string): string — 전화번호 포맷 유틸 전용 파일

api/
  profile.ts                       # useProfile, useUpdateProfile,
                                   # useCheckEmail, useSendVerifyEmail, useConfirmVerifyEmail,
                                   # useSaveSignature, useDeleteSignature 훅
```

### 라우팅

`App.tsx`에 `/settings/profile` 라우트 추가 (navItems.ts에는 이미 존재)

### 주요 UI 요소

1. **기본 정보 섹션**
   - 이름, 전화번호, 이메일 입력 필드 (기본: 읽기 전용 → 수정 버튼 클릭 시 편집 모드)
   - 전화번호:
     - `onKeyDown`으로 숫자 외 키 차단
     - `onChange`에서 `formatPhone()` 호출하여 자동 `010-XXXX-XXXX` 포맷 변환
     - 010 포맷만 허용, `onBlur` 시 010 시작 아닌 경우 경고 메시지 표시
     - 서버 측에서도 `010-\d{4}-\d{4}` 정규식 검증 수행
   - 이메일 변경 시:
     1. 이메일 입력 값 변경 시 `isVerified = false` 즉시 리셋 → 저장 버튼 비활성화
     2. '중복확인' 버튼으로 이메일 중복 여부 확인
     3. 중복 없으면 '인증코드 발송' 버튼 활성화
     4. 인증 코드 입력 후 '확인' 버튼으로 인증 완료 → `isVerified = true`
     5. 인증 완료(`isVerified = true`) 시에만 저장 버튼 활성화

2. **소셜 계정 섹션** *(추천 사항)*
   - 구글 / 네이버 / 카카오 3개 행 고정 노출
   - 연동된 계정: 연동 이메일 표시 (버튼 없음)
   - 미연동 계정: '추가' 버튼 (OAuth 리다이렉트 전 `sessionStorage` 저장)
   - 이미 연동된 계정 재연결 시: 서버 응답에 따라 "이미 연동된 계정입니다" 안내 toast 표시

3. **서명 섹션**
   - 서명 없으면: 빈 캔버스 표시 — 직접 마우스/터치로 서명 입력
   - 서명 있으면: 저장된 서명 미리보기 + '다시 서명' / '삭제' 버튼
   - '저장' 버튼: `canvas.toDataURL('image/png')` 추출 → 273KB(인코딩 문자열 기준) 초과 시 프론트에서 경고, 통과 시 API 전송
   - '지우기' 버튼: 캔버스 초기화 (API 호출 없음)
   - '삭제' 성공 후: `signatureData = null` 상태 갱신 → 빈 캔버스 모드로 자동 전환

4. **하단 저장 버튼**
   - 이메일 변경 시: `isVerified = false` 상태에서 비활성화
   - 이메일 미변경 또는 인증 완료 상태에서만 활성화

### API 연동 포인트

- 페이지 진입 시: `GET /api/v1/users/profile`
- 저장: `PUT /api/v1/users/profile`
- 이메일 중복확인: `GET /api/v1/users/check-email?email=xxx`
- 이메일 인증 코드 발송: `POST /api/v1/users/verify-email`
- 이메일 인증 코드 확인: `POST /api/v1/users/verify-email/confirm`
- 서명 저장: `PUT /api/v1/users/profile/signature`
- 서명 삭제: `DELETE /api/v1/users/profile/signature`
- 소셜 추가: `sessionStorage` 저장 후 리다이렉트 → `/oauth2/authorization/{provider}`

---

## 작업 순서

### 1. db-optimizer: 도메인 엔티티 및 영속성 어댑터

- `User` 엔티티에 `phone`, `signatureData` 필드 추가
- `User`에 업데이트 메서드 추가 (`updateProfile`, `updateSignature`, `clearSignature`)

### 2. backend-architect: Use Case 및 API 컨트롤러

- **UseCase 3개로 분리**:
  - `ProfileUseCase`: GetProfile, UpdateProfile 메서드
  - `EmailVerificationUseCase`: CheckEmail, SendVerifyEmail, ConfirmVerifyEmail 메서드
  - `SignatureUseCase`: SaveSignature, DeleteSignature 메서드
- 각 UseCase 구현체를 단일 `ProfileService`에서 통합 구현 (인터페이스만 분리, 구현 클래스는 1개)
- `ProfileService` 구현 세부:
  - `updateProfile`: phone 필드 `010-\d{4}-\d{4}` 정규식 서버 검증
  - `checkEmail`: Rate Limiting 적용 (Spring의 `@RateLimiter` 또는 인터셉터 수준 — 동일 사용자 기준 분당 10회 제한)
  - `confirmVerifyEmail`: 인증 코드 시도 횟수 관리 (5회 초과 시 코드 즉시 만료, `ErrorCode.EMAIL_VERIFY_EXCEEDED` 반환)
  - `saveSignature`: Base64 인코딩 문자열 바이트 길이 기준 273KB(279,552 bytes) 초과 시 `ServiceException(ErrorCode.SIGNATURE_TOO_LARGE)` 발생
  - 이메일 인증 코드: 인메모리(`ConcurrentHashMap`) TTL 5분 관리 (Redis 미도입 시 대체)
- `OAuth2SuccessHandler` 수정:
  - 이미 인증된 사용자(`SecurityContext`)가 OAuth 완료 시 `UserSocialAccount` 추가
  - 이미 연동된 소셜 계정 재연결 시: 예외 없이 정상 처리 (기존 연동 유지)
  - 리다이렉트는 `app.oauth2.allowed-redirect-origins` 허용 origin 범위 내로만 처리
- `ProfileController` 엔드포인트 구현
- `ErrorCode` 추가: `SIGNATURE_TOO_LARGE`, `EMAIL_VERIFY_INVALID`, `EMAIL_VERIFY_EXPIRED`, `EMAIL_VERIFY_EXCEEDED`, `PHONE_FORMAT_INVALID`
- 다국어 메시지 키 추가 (`ko`, `en`): `profile.update.success`, `signature.too.large`, `email.verify.sent`, `email.verify.success`, `email.already.exists`, `email.verify.exceeded`, `phone.format.invalid`
- `SecurityConfig` 업데이트: `/api/v1/users/**` 인증 필요로 설정

### 3. frontend-architect: UI 개발 및 API 연동

- `App.tsx`에 `/settings/profile` 라우트 추가
- `utils/phone.ts`에 `formatPhone` 함수 구현 (전화번호 포맷 전용 파일)
- `api/profile.ts` TanStack Query 훅 구현
- 컴포넌트 구현:
  - `ProfileForm`: 이름/전화번호/이메일 편집, onBlur 전화번호 경고, 이메일 변경 시 `isVerified` 리셋
  - `EmailVerification`: 중복확인 → 인증코드 발송 → 코드 입력 → 확인 플로우
  - `SocialAccountList`, `SocialAccountItem`: 연동 현황 + 추가 버튼, 재연결 안내 toast
  - `SignatureCanvas`: HTML Canvas API 직접 구현, 프론트 273KB 사전 검증, 삭제 후 빈 캔버스 전환
- `SettingsProfilePage.tsx` 페이지 조립

---

## 미결 사항 / 확인 필요

1. **이메일 인증 코드 저장소**: Redis 미설치 시 인메모리(`ConcurrentHashMap`) TTL 관리로 대체
   → 백엔드 구현 시 Redis 의존성 여부 팀장에게 확인 후 결정

## Review 결과
- 검토일: 2026-04-17
- 검토 항목: 보안 / 리팩토링 / 기능
- 결과: 2차 검토 이슈 10건 전체 반영 완료
