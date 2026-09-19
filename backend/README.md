# Sentinel AML — Backend

Real-time money-laundering detection service for MeridianTrust Bank. Ingests customer,
account, and transaction data, runs a configurable rule engine, and raises risk-scored
alerts that analysts work as cases — with an immutable audit trail.

## Stack

- Java 17+ / Spring Boot 3.3 (Web, Data JPA, Security, Validation)
- PostgreSQL 16 (prod) · H2 in PostgreSQL-compatibility mode (zero-setup dev)
- Flyway migrations · JWT auth · springdoc OpenAPI · Maven

## Run

```bash
# Dev — H2 in-memory, no database needed (default)
./mvnw spring-boot:run

# PostgreSQL profile
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

- API base: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Seeded users: `analyst / analyst123` (ANALYST), `admin / admin123` (ADMIN)

Sample CSVs to load (in this order) live in `sample-data/`:

```bash
# grab a token, then:
curl -F "file=@sample-data/customers.csv"    .../api/v1/ingest/customers    -H "Authorization: Bearer $T"
curl -F "file=@sample-data/accounts.csv"     .../api/v1/ingest/accounts     -H "Authorization: Bearer $T"
curl -F "file=@sample-data/transactions.csv" .../api/v1/ingest/transactions -H "Authorization: Bearer $T"
```

Ingesting transactions runs detection automatically. Regenerate data with
`python3 sample-data/generate_dataset.py`.

## Architecture

Layered: `web` (controllers) → `service` → `repository` → `domain` (JPA entities).

- `detection/` — the rule engine. `DetectionEngine` runs each `DetectionRule` and
  delegates persistence to `AlertPersister` (own transaction + dedup).
- `security/` — JWT filter, `SecurityConfig` (stateless, role-based), error writer.
- `web/response/` — every endpoint returns the same `ApiResponse<T>` envelope.
- `web/error/` — `GlobalExceptionHandler` maps exceptions to that envelope.

## Response envelope

Success and error share one shape:

```jsonc
{ "data": T | null,                       // omitted on error
  "metaData": { "timestamp": "...", "id": "..." },
  "errors": [ { "code": "...", "message": "..." } ] }  // omitted on success
```

## Detection rules

Tunable via the `rule_config` table (no redeploy) or `PATCH /api/v1/admin/rules/{code}`.

| Code | Trigger |
|------|---------|
| `CTR_THRESHOLD` | single txn ≥ 10,000 INR |
| `STRUCTURING` | 3+ txns of 9,000–9,999 from one account in 24h |
| `RAPID_MOVEMENT` | ≥ 80% of a deposit moved out within 48h |
| `HIGH_RISK_JURISDICTION` | counterparty in a high-risk country |
| `BEHAVIORAL_DEVIATION` | daily value > 3× the account's 90-day average |

Each alert carries a risk score (0–100), severity, evidence transaction IDs, and a
human-readable explanation. Alerts are de-duplicated by a unique `dedup_key`.

## Data model

`customer → account → transaction`. Detection produces `alert`; analysts group alerts
into `aml_case` and record a disposition. All alert/case transitions are written to the
append-only `audit_log`. Reference tables: `exchange_rate` (INR normalization),
`high_risk_jurisdiction`, `rule_config`, `app_user`.

## Security

- JWT bearer tokens; roles `ANALYST` and `ADMIN`, enforced at the API layer.
- `/admin/**` and `/audit/**` require `ADMIN`.
- PII masked in list responses, full only in detail views.
- Secrets come from env (`JWT_SECRET`, `DB_*`); nothing hardcoded for prod.

## Config

`src/main/resources/application.yml` — `dev` (H2, default) and `postgres` profiles.
Migrations in `src/main/resources/db/migration`.
