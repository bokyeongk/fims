# 소셜 로그인 구현 플랜

## 요구사항 요약
- 로그인 전용 페이지(`/login`) 구현
- 네이버, 카카오, 구글 OAuth2 소셜 로그인 연동
- 로그인 성공 시 JWT 발급 → 앱 내부 인증 유지
- 미인증 사용자 접근 시 `/login`으로 리다이렉트

---

## OAuth2 플로우 개요

```
[프론트] 소셜 로그인 버튼 클릭
    → 백엔드 /api/v1/auth/oauth2/{provider} 리다이렉트
    → 각 소셜 Provider 인증 페이지
    → 백엔드 콜백 수신 (인가 코드 → 사용자 정보 조회)
    → 신규 유저: 자동 가입 / 기존 유저: 조회
    → JWT 발급 후 HttpOnly 쿠키(`accessToken`)에 저장
    → 프론트 /oauth2/callback 으로 리다이렉트 (토큰은 쿠키로 전달, URL에 미포함)
[프론트] 쿠키 자동 첨부 → 홈(/)으로 이동
```

---

## API 설계 초안


| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/auth/oauth2/google` | 구글 OAuth2 인증 시작 (리다이렉트) |
| GET | `/api/v1/auth/oauth2/naver` | 네이버 OAuth2 인증 시작 (리다이렉트) |
| GET | `/api/v1/auth/oauth2/kakao` | 카카오 OAuth2 인증 시작 (리다이렉트) |
| GET | `/login/oauth2/code/{registrationId}` | 콜백 처리 (Spring Security 자동 처리) |
| GET | `/api/v1/auth/me` | 현재 로그인 사용자 정보 조회 (JWT 필요) |

> 콜백 성공 시 JWT는 **HttpOnly 쿠키**(`accessToken`, Secure, SameSite=Lax)에 저장하고, 프론트엔드 `/oauth2/callback`으로 리다이렉트한다. 토큰은 URL에 포함하지 않는다.
>
> **리다이렉트 URL 화이트리스트**: 서버 측에서 허용 도메인 목록(예: `http://localhost:5173`, `https://fims.hubilon.com`)을 환경변수(`ALLOWED_REDIRECT_ORIGINS`)로 관리하며, 목록 외 도메인으로의 리다이렉트는 차단한다.

---

## 도메인 모델 변경 (User 엔티티)

기존 `password` 필드는 소셜 로그인 사용자에게 불필요하므로 nullable로 변경.  
소셜 로그인 식별을 위한 필드 추가.

```
User (변경사항)
├── provider       : VARCHAR(20)  -- GOOGLE | NAVER | KAKAO | LOCAL (nullable)
├── providerId     : VARCHAR(255) -- 소셜 Provider의 사용자 고유 ID (nullable)
└── password       : nullable 로 변경 (소셜 로그인 시 null)
```

---

## 프론트엔드 화면 설계

### 페이지 / 컴포넌트 목록

```
pages/
└── LoginPage.tsx           # 로그인 전용 페이지 (레이아웃 미포함)

components/auth/
├── SocialLoginButton.tsx   # 소셜 로그인 버튼 (provider props)
└── LoginCard.tsx           # 로고 + 버튼 3개를 감싸는 카드

pages/
└── OAuth2CallbackPage.tsx  # /oauth2/callback — 토큰 파싱 후 홈 이동
```

### 라우팅 (App.tsx 변경)

```
/login            → LoginPage (AppLayout 제외)
/oauth2/callback  → OAuth2CallbackPage (AppLayout 제외)
/* (보호 라우트)  → 미인증 시 /login 리다이렉트
```

### 주요 UI 요소 (LoginPage)

- FIMS 로고 / 서비스 설명 텍스트
- 소셜 로그인 버튼 3개 (각 브랜드 컬러 + 공식 아이콘)
  - 구글: 흰 배경 + Google 아이콘
  - 카카오: 노란 배경 + 카카오 아이콘
  - 네이버: 초록 배경 + 네이버 아이콘

### API 연동 포인트

- 소셜 버튼 클릭: `window.location.href = '/api/v1/auth/oauth2/{provider}'`
- OAuth2CallbackPage: 별도 토큰 파싱 없이 `/`으로 이동 (JWT는 HttpOnly 쿠키로 자동 첨부됨)
- `api/client.ts`: axios `withCredentials: true` 설정으로 쿠키 자동 전송
- `api/client.ts` 401 인터셉터: `/login`으로 리다이렉트 (이미 구현됨)

---

## 백엔드 구현 상세

### 의존성 추가 (build.gradle)
```
implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
```

### application.yaml 추가 설정
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: email, profile
          naver:
            client-id: ${NAVER_CLIENT_ID}
            client-secret: ${NAVER_CLIENT_SECRET}
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/naver"
            scope: email, name
          kakao:
            client-id: ${KAKAO_CLIENT_ID}
            client-secret: ${KAKAO_CLIENT_SECRET}
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/kakao"
            client-authentication-method: client_secret_post
            scope: account_email, profile_nickname
        provider:
          naver:
            authorization-uri: https://nid.naver.com/oauth2.0/authorize
            token-uri: https://nid.naver.com/oauth2.0/token
            user-info-uri: https://openapi.naver.com/v1/nid/me
            user-name-attribute: response
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id
```

### 신규 파일 (백엔드)

| 경로 | 설명 |
|------|------|
| `config/security/OAuth2SuccessHandler.java` | 인증 성공 후 JWT 발급 → HttpOnly 쿠키 설정 → 화이트리스트 검증 후 프론트 리다이렉트 |
| `config/security/CustomOAuth2UserService.java` | 소셜 사용자 정보 조회 및 DB 저장/조회 |
| `modules/user/domain/model/OAuthProvider.java` | Enum: GOOGLE, NAVER, KAKAO, LOCAL |

### 변경 파일 (백엔드)

| 경로 | 변경 내용 |
|------|----------|
| `User.java` | provider, providerId 필드 추가 / password nullable 처리 / email은 표시용으로만 저장하며 식별 키로 사용하지 않음 (동일 이메일 + 다른 provider = 별개 계정) |
| `SecurityConfig.java` | oauth2Login 설정 추가, 콜백 경로 permitAll 추가, CSRF state 파라미터 검증은 Spring Security 기본 처리에 위임 (별도 구현 불필요) |

---

## 작업 순서

1. **db-optimizer**: `User` 엔티티에 `provider`, `providerId` 추가 / `password` nullable 변경
2. **backend-architect**: OAuth2 설정 (`CustomOAuth2UserService`, `OAuth2SuccessHandler`, `SecurityConfig` 수정)
3. **frontend-architect**: `LoginPage`, `OAuth2CallbackPage`, `SocialLoginButton` 컴포넌트 구현, 보호 라우트 추가

---
## 참고 사항
### 소셜 플랫폼 환경변수 키 목록
실제 값은 환경변수(`.env` 또는 서버 환경변수)로 설정하여 사용한다. 코드 및 플랜 파일에 실제 값을 기재하지 않는다.

| 환경변수 키             | 설명                        |
|-------------------------|-----------------------------|
| `KAKAO_CLIENT_ID`       | 카카오 REST API Key         |
| `KAKAO_CLIENT_SECRET`   | 카카오 Client Secret        |
| `NAVER_CLIENT_ID`       | 네이버 Client ID            |
| `NAVER_CLIENT_SECRET`   | 네이버 Client Secret        |
| `GOOGLE_CLIENT_ID`      | 구글 OAuth2 Client ID       |
| `GOOGLE_CLIENT_SECRET`  | 구글 OAuth2 Client Secret   |

### 추가 적용 사항
- 모든 소셜 로그인 연동 가능
- 소셜 계정 탈퇴 / 연동 해제 플로우는 추후 고려
---

## Review 결과
- 검토일: 2026-04-17
- 검토 항목: 보안 / 리팩토링 / 기능
- 결과: 이슈 발견 및 플랜 수정 완료
  - [보안] 민감정보 평문 → 환경변수 키 목록으로 대체
  - [보안] JWT URL 전달 → HttpOnly 쿠키 전달로 변경
  - [보안] JWT localStorage → HttpOnly 쿠키(withCredentials)로 변경
  - [보안] 오픈 리다이렉트 → ALLOWED_REDIRECT_ORIGINS 화이트리스트 명시
  - [보안] state 파라미터 → Spring Security 기본 처리 위임 명시
  - [기능] 동일 이메일 다중 provider → provider+providerId 기준 분리 계정 정책 명시
  - [기능] 소셜 계정 탈퇴/연동 해제 → 추후 고려로 명시
