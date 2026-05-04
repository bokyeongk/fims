# 견적서 구글 시트 연동 구현 플랜

## 요구사항 요약
- 견적서 입력 폼: 견적 일자, 계약자, 견적번호(자동생성), 품목 목록(유동 row), 총합, 비고
- 작성 데이터 → **DB 저장** (구글 시트 생성은 이 단계에서 하지 않음)
- 견적서 목록 화면 → **DB 데이터 기준** 노출
- 목록 클릭 → 작성 내용 **상세 화면** 표시
- 상세 화면 하단 **"구글시트 이동" 버튼** → 클릭 시 DB 데이터 기반으로 구글 시트 생성(최초 1회) 후 해당 URL로 이동

### 데이터 흐름
```
[작성 폼] → POST /quotes/sheets → [DB 저장] → 목록 노출
[목록 클릭] → 상세 화면
[구글시트 이동 버튼] → POST /quotes/sheets/{id}/google-sheet
                     → sheetUrl DB에 없으면: DB 데이터 읽기 → 구글 시트 생성 → URL 저장
                     → sheetUrl DB에 있으면: 기존 URL 즉시 반환
                     → window.open(sheetUrl)
```

---

## API 설계 초안

> **경로 변경**: 기존 `Quote` 모듈(`/api/v1/quotes`)과 책임 충돌 방지를 위해 `/api/v1/quote-sheets`로 분리

| Method | Endpoint | 설명 | 응답 |
|--------|----------|------|------|
| POST | /api/v1/quote-sheets | 견적서 DB 저장 | `QuoteSheetResponse` |
| GET  | /api/v1/quote-sheets | 견적서 목록 조회 (DB 기준) | `PageResponse<QuoteSheetSummary>` |
| GET  | /api/v1/quote-sheets/{id} | 견적서 상세 조회 | `QuoteSheetDetailResponse` |
| POST | /api/v1/quote-sheets/{id}/google-sheet | 구글 시트 생성/URL 반환 | `{ sheetUrl }` |

### POST /api/v1/quotes/sheets — 견적서 저장 (구글 시트 생성 없음)
요청 Body:
```json
{
  "quoteDate": "2026-05-04",
  "contractorName": "홍길동",
  "items": [
    {
      "itemName": "현관문",
      "spec": "900*2100",
      "category": "문/문틀",
      "quantity": 1,
      "unit": "EA",
      "unitPrice": 150000
    }
  ],
  "note": "비고 내용"
}
```
응답:
```json
{
  "quoteId": 1,
  "quoteNumber": "Q-20260504-001",
  "contractorName": "홍길동",
  "quoteDate": "2026-05-04",
  "totalAmount": 150000,
  "createdAt": "2026-05-04T10:00:00"
}
```

### GET /api/v1/quote-sheets — 목록 (`PageResponse<QuoteSheetSummary>`)
- "구글시트 이동" 버튼은 **상세 모달 전용**이므로 목록에서는 `sheetUrl` 미포함
- `hasGoogleSheet`(boolean)으로 시트 생성 여부만 표시 (아이콘 등 UX 힌트 용도)

```json
{
  "content": [
    {
      "quoteId": 1,
      "quoteNumber": "Q-20260504-001",
      "contractorName": "홍길동",
      "quoteDate": "2026-05-04",
      "totalAmount": 150000,
      "hasGoogleSheet": true,
      "createdAt": "2026-05-04T10:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### GET /api/v1/quote-sheets/{id} — 상세
```json
{
  "quoteId": 1,
  "quoteNumber": "Q-20260504-001",
  "contractorName": "홍길동",
  "quoteDate": "2026-05-04",
  "totalAmount": 150000,
  "note": "비고 내용",
  "sheetUrl": null,
  "createdAt": "2026-05-04T10:00:00",
  "items": [
    {
      "itemName": "현관문",
      "spec": "900*2100",
      "category": "문/문틀",
      "quantity": 1,
      "unit": "EA",
      "unitPrice": 150000,
      "amount": 150000
    }
  ]
}
```

### POST /api/v1/quote-sheets/{id}/google-sheet — 구글 시트 생성/조회
- **소유권 검증**: JWT `userId`와 `QuoteSheet.userId`가 일치하지 않으면 403 반환
- DB에 `sheetUrl`이 이미 존재하면 → Drive API로 파일 유효성 확인
  - 유효하면: 즉시 반환
  - 무효(사용자가 드라이브에서 삭제한 경우): `sheetUrl` 초기화 후 재생성 흐름 진행
- 없으면 DB 데이터 기반으로 구글 시트 생성 → `sheetUrl` DB 업데이트 → 반환
- **중복 요청 방지**: DB 레벨 `SELECT ... FOR UPDATE` 락으로 동시 시트 생성 차단

응답:
```json
{
  "sheetUrl": "https://docs.google.com/spreadsheets/d/..."
}
```

---

## 구글 시트 템플릿 설계

### 시트 구조 (A~H열)
| 행 | 내용 |
|----|------|
| 1 | 제목: "인테리어 필름 견적서" |
| 2 | 견적번호: [자동생성값] |
| 3 | 견적일자: [quoteDate] |
| 4 | 계약자: [contractorName] |
| 5 | 공백 |
| 6 | 헤더: 번호 / 품명 / 규격 / 구분 / 수량 / 단위 / 단가 / 금액 |
| 7~N | 품목 데이터 rows (유동) |
| N+1 | 합계: `=SUM(H7:HN)` 함수 삽입 |
| N+2 | 비고: [note] |

### 금액 계산
- 각 행의 금액 = `=E{row}*G{row}` (수량 * 단가) → 시트 함수로 처리
- 총합 = `=SUM(H7:H{lastRow})` → 시트 함수로 처리

---

## 구글 시트 연동 방식

### OAuth2 Access Token 활용
- 사용자가 Google 소셜 로그인 시 발급된 Access Token / Refresh Token을 DB에 암호화 후 저장
  - **암호화 방식**: 기존 `JasyptConfig`의 `PBEWithMD5AndDES`는 취약하므로 토큰 전용 `EncryptedStringConverter`를 별도 구현 (AES-256/GCM 기반 `javax.crypto.Cipher` 직접 사용)
  - `UserSocialAccount` 엔티티의 토큰 컬럼에 `@Convert(converter = TokenEncryptedStringConverter.class)` 적용
- Sheets API 호출 시 해당 토큰 사용 (Google Drive / Sheets 스코프 추가 필요)
- 토큰 만료 시 Refresh Token으로 갱신; Refresh Token 갱신 실패(앱 권한 취소 등) 시 `GOOGLE_AUTH_EXPIRED` 에러코드 반환 → 프론트엔드에서 재로그인 유도
- `GoogleSheetsAdapter` 내 예외 처리 시 토큰 값을 로그/응답에 절대 포함하지 않음

### 필요 추가 OAuth2 스코프
- `https://www.googleapis.com/auth/spreadsheets`
- `https://www.googleapis.com/auth/drive.file`
- 스코프 부재 감지 시: `GOOGLE_SCOPE_INSUFFICIENT` 에러코드 반환 → 프론트엔드에서 추가 스코프 동의 OAuth2 재인증 redirect 유도

### 구글 시트 생성 흐름 (`POST /{id}/google-sheet` 호출 시)
1. `SELECT ... FOR UPDATE`로 `QuoteSheet` 락 획득 (중복 요청 차단)
2. JWT `userId`와 `QuoteSheet.userId` 소유권 검증 → 불일치 시 403
3. `sheetUrl`이 이미 존재하면 → Drive API로 파일 유효성 확인
   - 유효: 즉시 반환
   - 무효(삭제됨): `sheetUrl = null`로 초기화 후 4번 진행
4. `sheetUrl` 없으면:
   1. DB에서 `QuoteSheet` + `QuoteItem` 목록 조회
   2. Sheets API로 새 스프레드시트 생성
   3. 헤더/메타정보 셀 데이터 입력
   4. 품목 rows 데이터 입력 (DB 저장된 값 기준)
   5. 금액 계산 함수(`=E*G`, 표시 목적) 및 합계 함수(`=SUM`) 삽입
   6. **Drive API로 시트 공유 권한을 "작성자 본인만 접근"으로 설정**
   7. 생성된 `sheetUrl`을 DB의 `QuoteSheet.sheetUrl` 컬럼에 업데이트
   8. `sheetUrl` 반환
- **트랜잭션 경계**: 구글 시트 생성(외부 API)은 DB 트랜잭션 밖에서 실행. 시트 생성 후 DB 업데이트 실패 시 → 보상 로직(생성된 시트 Drive API로 삭제 시도)
- **보상 로직 실패 시**: 고아 시트 식별을 위해 `ERROR` 레벨 로그 기록 (spreadsheetId 포함). 향후 정리 배치 처리 대상으로 관리

---

## 백엔드 구조 설계

### 신규 도메인 모델
> `QuoteSheet`는 기존 `quote` 모듈과 경로/책임 충돌 방지를 위해 별도 `quote-sheet` 모듈로 분리

```
modules/quote-sheet/
├── domain/model/
│   ├── QuoteSheet.java         (신규: userId, quoteNumber, contractorName, quoteDate, totalAmount, sheetUrl, note)
│   └── QuoteItem.java          (신규: itemName, spec, category, quantity, unit, unitPrice, amount)
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateQuoteSheetUseCase.java              (신규: DB 저장)
│   │   │   ├── GetQuoteSheetListUseCase.java             (신규: 목록 조회)
│   │   │   ├── GetQuoteSheetDetailUseCase.java           (신규: 상세 조회)
│   │   │   └── GenerateGoogleSheetUseCase.java           (신규: 구글 시트 생성/URL 반환)
│   │   └── out/
│   │       ├── QuoteSheetRepository.java                 (신규)
│   │       └── GoogleSheetsPort.java                     (신규: createSheet, isSheetValid, deleteSheet 메서드)
│   └── service/
│       ├── QuoteSheetService.java                        (신규: Create/Get UseCase 담당)
│       └── GoogleSheetOrchestrationService.java          (신규: GenerateGoogleSheetUseCase 담당 — 외부 I/O 분리)
├── adapter/
│   ├── in/web/
│   │   ├── QuoteSheetController.java                     (신규: 4개 엔드포인트)
│   │   ├── CreateQuoteSheetRequest.java                  (신규)
│   │   ├── QuoteItemRequest.java                         (신규)
│   │   ├── QuoteSheetResponse.java                       (신규: 저장 응답)
│   │   ├── QuoteSheetSummaryResponse.java                (신규: 목록 응답)
│   │   ├── QuoteSheetDetailResponse.java                 (신규: 상세 응답)
│   │   └── GoogleSheetUrlResponse.java                   (신규: { sheetUrl })
│   └── out/
│       ├── persistence/
│       │   ├── QuoteSheetJpaRepository.java              (신규)
│       │   ├── QuoteItemJpaRepository.java               (신규)
│       │   └── QuoteSheetRepositoryImpl.java             (신규)
│       └── google/
│           └── GoogleSheetsAdapter.java                  (신규: GoogleSheetsPort 구현)
```

### 견적번호 자동생성 규칙
- 형식: `Q-{YYYYMMDD}-{3자리순번}` (예: `Q-20260504-001`)
- 순번 기준: 서버의 `LocalDate.now()` (사용자 입력 `quoteDate`와 무관)
- **범위**: 견적번호는 **전체 시스템 공통 시퀀스** (단일 사용자 앱 기준). 다중 사업자 지원 필요 시 `userId` 기반 연번으로 전환 필요
- **동시성 처리**: PostgreSQL `SEQUENCE` (`quote_sheet_number_seq`) 사용하여 중복 방지. `quoteNumber` 컬럼에 UNIQUE 제약조건 추가

### totalAmount 정책
- **서버에서 계산**: `items`의 `quantity * unitPrice` 합산값을 DB에 저장
- 구글 시트의 `=E*G`, `=SUM()` 수식은 표시 목적으로만 사용 (DB 저장값과 독립)
- 요청 Body에 `totalAmount` 미포함 (클라이언트 계산값 신뢰 안 함)

### 인가(Authorization) 정책
- POST/GET 모두 JWT에서 추출한 `userId` 기반으로 본인 데이터만 처리
- `QuoteSheetService`에서 `userId`를 필수 파라미터로 받아 필터링
- **소유권 검증**: `POST /{id}/google-sheet`는 서비스 레이어에서 `QuoteSheet.userId == JWT userId` 비교 → 불일치 시 `FORBIDDEN` 에러코드 반환
- `@PreAuthorize("isAuthenticated()")` 컨트롤러 레벨 적용

### 입력 유효성 검사 (`CreateQuoteSheetRequest`)
- `quoteDate`: `@NotNull`
- `contractorName`: `@NotBlank`
- `items`: `@NotEmpty` (최소 1개 이상)
- `items[].quantity`: `@Min(1)`
- `items[].unitPrice`: `@Min(0)`

### 신규 의존성 (build.gradle)
```groovy
implementation 'com.google.apis:google-api-services-sheets:v4-rev612-1.25.0'
implementation 'com.google.apis:google-api-services-drive:v3-rev197-1.25.0'
implementation 'com.google.api-client:google-api-client:2.2.0'
implementation 'com.google.auth:google-auth-library-oauth2-http:1.20.0'
```

### UserSocialAccount 수정
- Google 소셜 계정에 `accessToken`, `refreshToken` 필드 추가
- 두 필드 모두 `@Convert(converter = EncryptedStringConverter.class)` 적용 (Jasypt 암호화)
- OAuth2 로그인 시 토큰 저장 로직 추가

### QuoteSheet ↔ QuoteItem 연관관계 전략
- `QuoteSheet`에 `@OneToMany(mappedBy = "quoteSheet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)` 적용
- 목록 조회 시 N+1 방지: `QuoteSheetRepositoryImpl`에서 Fetch Join 또는 `@EntityGraph` 사용

---

## 프론트엔드 화면 설계

### 페이지/컴포넌트 목록
```
pages/
└── QuoteSheetPage.tsx              (신규: 견적서 목록 + 생성 버튼)

components/quote-sheet/
├── QuoteSheetList.tsx              (신규: DB 기준 목록)
├── QuoteSheetListItem.tsx          (신규: 목록 아이템 - 클릭 시 상세 모달 열기)
├── QuoteSheetCreateModal.tsx       (신규: 견적서 입력 폼 모달)
├── QuoteSheetDetailModal.tsx       (신규: 견적서 상세 내용 + "구글시트 이동" 버튼)
├── QuoteItemTable.tsx              (신규: 품목 동적 row 테이블, 작성/조회 공용)
└── QuoteItemRow.tsx                (신규: 개별 품목 입력 row)
```

### 라우팅
- `/quote-sheets` → `QuoteSheetPage.tsx` (하단 네비게이션 기존 '견적서' 탭 활용 또는 신규 탭)

### 주요 UI 요소

1. **견적서 목록 화면** (`QuoteSheetPage.tsx`)
   - 상단: "견적서 작성" 버튼
   - 목록 (DB 기준): 견적번호 / 계약자 / 견적일자 / 총금액 / 생성일
   - 각 항목 클릭 → **상세 모달** 오픈 (구글 시트 이동 아님)

2. **견적서 작성 모달** (`QuoteSheetCreateModal.tsx`)
   - 견적 일자 (date picker)
   - 계약자명 (text input)
   - 견적번호 (자동생성, 읽기전용 표시)
   - 품목 테이블: 품명 / 규격 / 구분 / 수량 / 단위 / 단가 / 금액(자동계산)
   - "+ 행 추가" / "행 삭제" 버튼
   - 총합 표시 (프론트 실시간 계산)
   - 비고 (textarea)
   - **"저장" 버튼** → POST /api/v1/quotes/sheets (DB 저장)

3. **견적서 상세 모달** (`QuoteSheetDetailModal.tsx`)
   - 견적번호 / 계약자 / 견적일자 읽기전용 표시
   - 품목 테이블 읽기전용 표시 (DB 저장된 내용)
   - 총합 / 비고 읽기전용 표시
   - 하단 고정: **"구글시트 이동" 버튼**
     - 클릭 → POST /api/v1/quote-sheets/{id}/google-sheet 호출
     - 로딩 중 버튼 비활성화 + 스피너 표시
     - 응답의 `sheetUrl` → **`https://docs.google.com/` 으로 시작하는지 검증 후** `window.open(sheetUrl, '_blank')`
     - `GOOGLE_AUTH_EXPIRED` 에러 수신 시 → 토스트 메시지 + 재로그인 유도 버튼 표시
     - `GOOGLE_SCOPE_INSUFFICIENT` 에러 수신 시 → 추가 권한 동의 페이지로 redirect

### API 연동 포인트 (`api/quote-sheet.ts` 신규 파일)
- `createQuoteSheet(data)` → POST /api/v1/quote-sheets
- `getQuoteSheetList(page, size)` → GET /api/v1/quote-sheets
- `getQuoteSheetDetail(id)` → GET /api/v1/quote-sheets/{id}
- `generateGoogleSheet(id)` → POST /api/v1/quote-sheets/{id}/google-sheet

---

## 작업 순서

1. **db-optimizer**: `modules/quote-sheet/` 신규 모듈 생성, `QuoteSheet`·`QuoteItem` 엔티티 및 영속성 어댑터 구현, `UserSocialAccount` 토큰 필드 추가
2. **backend-architect**: `QuoteSheetService`(Create/Get) + `GoogleSheetOrchestrationService`(GenerateGoogleSheet) 구현, `GoogleSheetsAdapter` 구현, `TokenEncryptedStringConverter` 구현, 컨트롤러 연결, OAuth2 스코프 확장
3. **frontend-architect**: `QuoteSheetPage`, `QuoteSheetCreateModal`, `QuoteSheetDetailModal`(구글시트 이동 에러 UX 포함), `QuoteItemTable` 컴포넌트 및 API 연동 구현

---

## 미결 사항 / 확인 필요

1. **Google OAuth 재인증 시점**: 기존 로그인 사용자의 Access Token에 Sheets/Drive 스코프가 없는 경우
   - 옵션 A: "구글시트 이동" 버튼 클릭 시 `GOOGLE_SCOPE_INSUFFICIENT` 에러 → 프론트에서 추가 스코프 동의 redirect (**구현 전 반드시 결정 필요**)
   - 옵션 B: 최초 로그인 시 전체 스코프 요청 (기존 사용자 재로그인 필요)
2. **기존 Quote 모듈과의 관계**: `modules/quote`(기존 현장 견적 관리)와 `modules/quote-sheet`(신규 구글 시트 연동 견적서) 분리 운영 (기존 기능 영향 없음)

## Review 결과
- 검토일: 2026-05-04
- 검토 항목: 보안 / 리팩토링 / 기능
- 결과: 이슈 반영 완료 (2차)
  - **[보안]** 토큰 암호화: Jasypt 기존 알고리즘 취약 → 별도 `TokenEncryptedStringConverter`(AES-256/GCM) 구현으로 변경
  - **[보안]** `POST /{id}/google-sheet` 소유권 검증 명시 (userId 불일치 시 403)
  - **[보안]** 보상 로직 실패 시 고아 시트 ERROR 로그 기록 정책 추가
  - **[보안]** `window.open()` 전 `https://docs.google.com/` URL 검증 명시
  - **[기능]** Drive API 파일 유효성 확인 후 반환 (삭제된 시트 재생성 흐름)
  - **[기능]** `SELECT ... FOR UPDATE`로 중복 시트 생성 차단
  - **[기능]** 견적번호 범위(전역 시퀀스) 명시, 다중 사업자 전환 조건 기록
  - **[기능]** `hasGoogleSheet`(목록) vs `sheetUrl`(상세) 설계 의도 명시
  - **[기능]** 구글시트 이동 에러(GOOGLE_AUTH_EXPIRED, GOOGLE_SCOPE_INSUFFICIENT) 프론트 UX 명시
  - **[리팩토링]** API 경로 `/api/v1/quote-sheets`로 분리 (기존 Quote 모듈 충돌 방지)
  - **[리팩토링]** `GoogleSheetOrchestrationService` 분리 (외부 I/O와 비즈니스 로직 분리)
  - **[리팩토링]** `modules/quote-sheet` 별도 모듈로 구조 확정
