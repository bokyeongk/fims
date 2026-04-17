# 멀티 소셜 계정 연동 구조 개편 플랜

## 요구사항 요약
- 한 User 계정에 카카오 / 네이버 / 구글 세 소셜 계정을 모두 연동 가능
- `email`은 필수값 아님 (소셜 provider가 이메일을 제공하지 않는 경우 null 허용)
- 현재 `User` 테이블에 박혀있는 `provider`, `provider_id` 필드를 별도 `user_social_accounts` 테이블로 분리

---

## 현재 구조 문제점

```
users
├── provider       (GOOGLE | NAVER | KAKAO | LOCAL)  ← 1개만 등록 가능
└── provider_id    ← provider 당 1개
```

---

## 목표 구조 (ERD)

```
users
├── id          PK
├── name        VARCHAR(100) NOT NULL
├── email       VARCHAR(255) NULL        ← 필수 아님
├── password    VARCHAR(255) NULL        ← LOCAL 로그인 시에만 사용
├── role        VARCHAR(20)  NOT NULL    (USER | ADMIN)
├── status      VARCHAR(20)  NOT NULL    (ACTIVE | INACTIVE)
├── create_date
└── modify_date

user_social_accounts
├── id          PK
├── user_id     FK → users.id
├── provider    VARCHAR(20)  NOT NULL    (GOOGLE | NAVER | KAKAO)
├── provider_id VARCHAR(255) NOT NULL    ← 소셜 provider의 고유 사용자 ID
├── email       VARCHAR(255) NULL        ← 소셜 provider에서 제공한 이메일 (표시용)
├── create_date
└── modify_date

UNIQUE (provider, provider_id)          ← 동일 소셜 계정 중복 연동 방지
```

---

## API 설계 변경 없음

기존 OAuth2 플로우 그대로 유지. DB 레이어만 변경된다.

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/oauth2/authorization/{provider}` | 소셜 로그인 시작 (변경 없음) |
| GET | `/api/v1/auth/me` | 현재 로그인 사용자 정보 반환 (변경 없음) |

---

## 소셜 로그인 처리 로직 변경

```
[소셜 로그인 콜백 수신]
    1. provider + providerId 로 user_social_accounts 조회
       ├── 있음 → 연결된 users 조회 → JWT 발급
       └── 없음 → users 신규 생성
                 → user_social_accounts 신규 생성
                 → JWT 발급
```

> 현재는 미지원이지만 추후 "기존 로그인 상태에서 다른 소셜 계정 연동" 기능 추가 시  
> `user_social_accounts`에 row만 추가하면 되는 구조.

---

## 작업 범위

### db-optimizer 작업

**1. UserSocialAccount 엔티티 신규 생성**
- 파일: `modules/user/domain/model/UserSocialAccount.java`
- `BaseJpaEntity` 상속
- `@ManyToOne(fetch = FetchType.LAZY) User user`
- `provider`: `@Enumerated(EnumType.STRING)`, NOT NULL
- `providerId`: NOT NULL
- `email`: NULL 허용
- `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"}))`

**2. User 엔티티 수정**
- `email`: `nullable=false` → `nullable=true`
- `provider` 필드 제거
- `provider_id` 필드 제거
- `@Table` uniqueConstraint (`provider`, `provider_id`) 제거
- `ofSocial()` 팩토리 메서드 시그니처 변경:
  ```java
  // 변경 전
  User.ofSocial(email, name, provider, providerId)
  // 변경 후
  User.ofSocial(name, email)   // email nullable
  ```

**3. UserSocialAccountRepository 포트 신규 생성**
- 파일: `application/port/out/UserSocialAccountRepository.java`
- 메서드:
  ```java
  Optional<UserSocialAccount> findByProviderAndProviderId(OAuthProvider provider, String providerId);
  UserSocialAccount save(UserSocialAccount account);
  List<UserSocialAccount> findByUserId(Long userId);
  ```

**4. UserRepository 포트 수정**
- `findByProviderAndProviderId()` 메서드 제거

**5. 영속성 어댑터 신규/수정**
- `UserSocialAccountJpaRepository.java` 신규
- `UserSocialAccountRepositoryImpl.java` 신규
- `UserRepositoryImpl.java`: `findByProviderAndProviderId` 구현 제거

---

### backend-architect 작업

**1. CustomOAuth2UserService 수정**
- `userRepository.findByProviderAndProviderId()` → `userSocialAccountRepository.findByProviderAndProviderId()` 로 변경
- 신규 유저 생성 로직:
  ```
  UserSocialAccount 없음
    → User.ofSocial(name, email) 생성 후 저장
    → UserSocialAccount 생성 후 저장 (user, provider, providerId, email)
  UserSocialAccount 있음
    → socialAccount.getUser() 로 User 조회
  ```

**2. MeResponse 수정**
- `provider` 필드 제거 또는 `List<String> providers` 로 변경
  - 연동된 소셜 계정 목록 반환: `["GOOGLE", "NAVER"]`

**3. GetUserUseCase / UserService 수정**
- `getUserByEmail()`: email nullable 대응 (email null 가능성 처리)

---

## 작업 순서

1. **db-optimizer**: `User` 엔티티 수정, `UserSocialAccount` 엔티티 및 영속성 어댑터 신규 생성
2. **backend-architect**: `CustomOAuth2UserService` 로직 수정, `MeResponse` 수정

---

## 미결 사항 / 확인 필요

- 기존 `users` 테이블의 `provider`, `provider_id` 컬럼 데이터 마이그레이션 여부
  - `ddl-auto: update` 환경이므로 컬럼 제거는 자동으로 일어나지 않음
  - 운영 반영 전 수동 DDL 실행 필요 (개발 환경은 테이블 drop 후 재생성 권장)
- 추후 소셜 계정 연동 해제 기능 추가 시 `user_social_accounts` row 삭제로 처리
