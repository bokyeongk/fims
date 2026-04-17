# FIMS Backend — 환경변수 설정 가이드

Spring Boot 애플리케이션 실행 전 아래 환경변수를 설정해야 한다.

---

## 환경변수 목록

### 데이터베이스

| 환경변수 | 기본값 | 설명 |
|----------|--------|------|
| `DB_USERNAME` | `postgres` | PostgreSQL 접속 사용자명 |
| `DB_PASSWORD` | `postgres` | PostgreSQL 접속 패스워드 |

- 접속 URL: `jdbc:postgresql://localhost:5432/hubilon?currentSchema=fims`
- DB 및 스키마(`fims`)는 사전에 생성되어 있어야 한다.

---

### JWT 인증

| 환경변수 | 기본값 | 설명 |
|----------|--------|------|
| `JWT_SECRET` | (내장 기본값, 운영 시 반드시 교체) | Base64 인코딩된 HS256 서명 키 (최소 256-bit) |

- 운영 환경에서는 반드시 안전한 랜덤 키로 교체한다.
- 새 키 생성 예시:
  ```bash
  openssl rand -base64 32
  ```
- 생성된 값을 Base64로 한 번 더 인코딩하여 사용한다.

---

### Jasypt 암호화

| 환경변수 | 기본값 | 설명 |
|----------|--------|------|
| `JASYPT_ENCRYPTOR_PASSWORD` | `local-dev-secret` | `application.yaml` 내 `ENC(...)` 값 복호화 키 |

- `application.yaml`에 민감 값을 `ENC(암호화값)` 형태로 저장할 때 사용한다.
- 암호화/복호화 테스트: `./gradlew test --tests JasyptTest`

---

### OAuth2 소셜 로그인

#### Google

| 환경변수 | 설명 |
|----------|------|
| `GOOGLE_CLIENT_ID` | Google Cloud Console에서 발급한 OAuth2 Client ID |
| `GOOGLE_CLIENT_SECRET` | Google Cloud Console에서 발급한 OAuth2 Client Secret |

- Google Cloud Console → API 및 서비스 → 사용자 인증 정보
- 승인된 리디렉션 URI에 추가:
  ```
  http://localhost:8080/login/oauth2/code/google
  ```

#### Naver

| 환경변수 | 설명 |
|----------|------|
| `NAVER_CLIENT_ID` | 네이버 개발자 센터에서 발급한 Client ID |
| `NAVER_CLIENT_SECRET` | 네이버 개발자 센터에서 발급한 Client Secret |

- 네이버 개발자 센터 → 애플리케이션 등록
- 서비스 URL: `http://localhost:8080`
- Callback URL에 추가:
  ```
  http://localhost:8080/login/oauth2/code/naver
  ```

#### Kakao

| 환경변수 | 설명 |
|----------|------|
| `KAKAO_CLIENT_ID` | Kakao Developers에서 발급한 REST API 키 |
| `KAKAO_CLIENT_SECRET` | Kakao Developers → 보안 탭에서 발급한 Client Secret |

- Kakao Developers → 내 애플리케이션 → 앱 설정 → 카카오 로그인
- Redirect URI에 추가:
  ```
  http://localhost:8080/login/oauth2/code/kakao
  ```
- 동의항목: `account_email`, `profile_nickname` 활성화 필요

---

### 프론트엔드 연동

| 환경변수 | 기본값 | 설명 |
|----------|--------|------|
| `ALLOWED_REDIRECT_ORIGINS` | `http://localhost:5173` | OAuth2 콜백 후 리다이렉트를 허용할 프론트엔드 도메인 (쉼표 구분 다중 설정 가능) |

- 운영 환경 예시: `https://fims.hubilon.com`

---

## 로컬 개발 환경 설정 방법

### IntelliJ IDEA

Run/Debug Configurations → Environment variables에 아래 형식으로 입력:

```
DB_USERNAME=postgres;DB_PASSWORD=postgres;JASYPT_ENCRYPTOR_PASSWORD=local-dev-secret;JWT_SECRET=<your-key>;GOOGLE_CLIENT_ID=<value>;GOOGLE_CLIENT_SECRET=<value>;NAVER_CLIENT_ID=<value>;NAVER_CLIENT_SECRET=<value>;KAKAO_CLIENT_ID=<value>;KAKAO_CLIENT_SECRET=<value>;ALLOWED_REDIRECT_ORIGINS=http://localhost:5173
```

### Gradle 실행 시

```bash
GOOGLE_CLIENT_ID=xxx \
GOOGLE_CLIENT_SECRET=xxx \
NAVER_CLIENT_ID=xxx \
NAVER_CLIENT_SECRET=xxx \
KAKAO_CLIENT_ID=xxx \
KAKAO_CLIENT_SECRET=xxx \
./gradlew bootRun
```

### `.env` 파일 (IDE 플러그인 사용 시)

`.env` 파일을 프로젝트 루트에 생성하고 `.gitignore`에 반드시 추가한다.

```dotenv
DB_USERNAME=postgres
DB_PASSWORD=postgres
JASYPT_ENCRYPTOR_PASSWORD=local-dev-secret
JWT_SECRET=<your-base64-encoded-key>

GOOGLE_CLIENT_ID=<value>
GOOGLE_CLIENT_SECRET=<value>

NAVER_CLIENT_ID=<value>
NAVER_CLIENT_SECRET=<value>

KAKAO_CLIENT_ID=<value>
KAKAO_CLIENT_SECRET=<value>

ALLOWED_REDIRECT_ORIGINS=http://localhost:5173
```

> `.env` 파일에 실제 키 값을 저장하는 경우 절대 git에 커밋하지 않는다.
