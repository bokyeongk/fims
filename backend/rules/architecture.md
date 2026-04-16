# Backend Architecture

Spring Boot 3.x / Java 17 REST API following **Hexagonal (Ports & Adapters)** architecture.

## Build & Run Commands

```bash
# (backend/ 디렉토리에서 실행)

./gradlew build
./gradlew bootRun
./gradlew test
./gradlew test --tests JasyptTest
./gradlew clean
```

## Module Structure

Each domain module under `src/main/java/com/hubilon/google/modules/` is organized as:

```
modules/<domain>/
├── adapter/in/web/       # Controllers, request/response DTOs
├── adapter/out/          # JPA repository implementations
├── application/
│   ├── port/in/          # Use case interfaces (commands + results as inner records)
│   ├── port/out/         # Output port interfaces (repository contracts)
│   └── service/          # Application service implementing all use case interfaces
└── domain/model/         # JPA entities (extend BaseJpaEntity)
```

Dependencies flow **inward only**: adapters → application → domain.

## Key Conventions

- **Entities**: Extend `BaseJpaEntity` (provides `createDate`, `modifyDate` audit fields)
- **Errors**: Always use `ErrorCode` enum + throw `ServiceException(ErrorCode.XXX)` — never raw exceptions
- **Responses**: Wrap all API responses in `ApiResponse<T>` (`ApiResponse.success(data)` / `ApiResponse.fail(msg)`)
- **i18n**: All user-facing messages via `MessageProvider.getMessage("key")`. Add keys to both `messages/message_ko.properties` and `messages/message_en.properties`
- **Controllers**: Inject use case interfaces (not the service directly). Use `@PreAuthorize` for authorization

## Security & Encryption

- **Jasypt**: Sensitive `application.yaml` values use `ENC(...)`. Secret key from env var `JASYPT_ENCRYPTOR_PASSWORD`. Run `JasyptTest` to encrypt/decrypt.
- **JWT**: Bearer tokens. `JwtProvider` generates access tokens with email + role claims. `JwtAuthenticationFilter` validates on every request.
- DB credentials from `DB_USERNAME` / `DB_PASSWORD` env vars (default: `postgres`/`postgres`)

## Database

PostgreSQL on `localhost:5432`, database `hubilon`. `ddl-auto: update`. Swagger UI at `http://localhost:8080/swagger-ui.html`.
