# Sentinel AML

Sentinel AML is a real-time anti-money-laundering monitoring application with:

- An Angular frontend for dashboards, alerts, cases, rules, and audit history
- A Spring Boot REST API with JWT authentication
- H2 for zero-setup local development, with optional PostgreSQL support
- CSV ingestion and transaction risk detection

## Prerequisites

- Java 21 exactly
- Node.js 22 or newer
- npm 11 or newer

The repository includes the Maven Wrapper, so a separate Maven installation is not required.

Verify Java before starting:

```bash
java --version
```

The output must begin with `openjdk 21`. If an existing VS Code terminal still shows Java 25, restart that shell:

```bash
exec zsh -l
java --version
```

## Start the Application

Run the following sections in separate terminals. Load the sample data only when the database is empty.

```bash
# One-time database setup
createdb sentinel_aml 2>/dev/null || true

# Terminal 1: backend on http://localhost:8080
cd /Users/aj1singh/Desktop/PRJ/azen/backend
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
SPRING_PROFILES_ACTIVE=postgres \
DB_URL=jdbc:postgresql://localhost:5432/sentinel_aml \
DB_USER=aj1singh \
DB_PASSWORD= \
./mvnw spring-boot:run

# Terminal 2: frontend on http://localhost:4200
cd /Users/aj1singh/Desktop/PRJ/azen/frontend
npm install
npm start

# Terminal 3: load sample data once, in dependency order
cd /Users/aj1singh/Desktop/PRJ/azen
TOKEN=$(curl -s http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' \
  | python3 -c 'import json,sys; print(json.load(sys.stdin)["data"]["token"])')
curl -X POST http://localhost:8080/api/v1/ingest/customers \
  -H "Authorization: Bearer $TOKEN" \
  -F file=@backend/sample-data/customers.csv
curl -X POST http://localhost:8080/api/v1/ingest/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -F file=@backend/sample-data/accounts.csv
curl -X POST http://localhost:8080/api/v1/ingest/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -F file=@backend/sample-data/transactions.csv
```

Open <http://localhost:4200>. The API runs at <http://localhost:8080>, and Swagger UI is available at <http://localhost:8080/swagger-ui.html>.

## Demo Accounts

| Role | Username | Password |
| --- | --- | --- |
| Analyst | `analyst` | `analyst123` |
| Administrator | `admin` | `admin123` |

These credentials are intended only for local development. The administrator can also access rule configuration and audit views.

## API Documentation

With the backend running:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- H2 console: <http://localhost:8080/h2-console>

H2 development connection:

| Setting | Value |
| --- | --- |
| JDBC URL | `jdbc:h2:mem:sentinel` |
| User | `sa` |
| Password | Empty |

## Architecture

The backend uses a conventional layered Spring architecture:

1. Controllers expose versioned REST endpoints under `/api/v1`.
2. Services implement authentication, ingestion, detection, currency, alerts, cases, and auditing.
3. Spring Data JPA repositories persist the relational domain model.
4. Flyway migrations create the schema and seed users, exchange rates, high-risk jurisdictions, and rule configuration.
5. Angular services call the API and unwrap its `{ data, metaData, errors }` response envelope.
6. Angular feature components provide the operational analyst and administrator screens.

The main relationship is `Customer -> Account -> Transaction -> Alert -> Case`. Alert and case actions also append records to `audit_log`.

## Implemented Features

### Ingestion and validation

- Bulk CSV endpoints load customers, accounts, and transactions.
- Files are loaded in dependency order because accounts reference customers and transactions reference accounts.
- Rows are validated independently and ingestion returns inserted/failed counts with row-level errors.
- Referential integrity is checked before accounts or transactions are persisted.
- `POST /api/v1/transactions` supports incremental single-transaction ingestion.
- The synthetic sample contains 5 customers, 5 accounts, and 140 transactions.

### Currency normalization

Transaction values are converted to INR during ingestion using the seeded `exchange_rate` table. Detection therefore compares normalized values across currencies.

### Detection engine

Each transaction is evaluated against enabled, database-backed rules:

| Rule | Behavior |
| --- | --- |
| `CTR_THRESHOLD` | Flags a normalized transaction at or above the configured reporting threshold |
| `STRUCTURING` | Detects at least three transactions just below the threshold within 24 hours |
| `RAPID_MOVEMENT` | Detects outgoing movement of at least 80% of recently credited funds |
| `HIGH_RISK_JURISDICTION` | Flags counterparties in the configurable high-risk jurisdiction table |
| `BEHAVIORAL_DEVIATION` | Compares recent activity with the customer's historical baseline |

Administrators can enable, disable, and tune rules from the Rule Configuration page. Values are stored in `rule_config`, so changes do not require a redeployment.

### Alerts

- Every match contains a risk score, severity, rule, human-readable explanation, and supporting transaction IDs.
- The alert queue defaults to highest risk first and supports status, score, and sort filters.
- A unique deduplication key prevents repeated alerts for the same customer, rule, and evaluation window.
- Customer names are masked in list views and exposed only in authorized detail views.
- Dashboard metrics group alerts by status, severity, and rule.

### Cases and audit

- Analysts can open a case from an alert and inspect case details.
- Cases support dispositions such as confirmed SAR, false positive, and cleared, with a required reason.
- Alert and case actions append immutable audit entries containing timestamp, actor, entity, action, and details.
- Administrators can view all audit entries or filter by entity type and entity ID.
- A newly loaded dataset has no cases until an analyst creates one from an alert.

### Security

- Login returns a signed JWT that the Angular interceptor attaches to API requests.
- Spring Security enforces authorization at the API layer; Angular guards only supplement it.
- Rule configuration and audit endpoints require `ADMIN`.
- Both `ANALYST` and `ADMIN` can use ingestion endpoints.
- Passwords are BCrypt-hashed, CORS origins are configurable, and API errors use a consistent JSON structure.

### Frontend

- Dashboard: alert totals and severity/rule distributions.
- Alert queue and detail: prioritized alerts, customer context, explanations, and evidence.
- Cases: investigation and disposition workflow.
- Rule Configuration: live database-backed rule settings.
- Audit Trail: immutable action history with optional entity filters.

## Test and Build

Backend:

```bash
cd backend
./mvnw clean test
./mvnw package
```

The Maven build compiles for Java 21 and rejects any other Java major version.

Frontend:

```bash
cd frontend
npm test -- --watch=false
npm run build
```

The production frontend output is written to `frontend/dist/frontend`.

## Database Profiles

Use the `postgres` profile for persistent data. Flyway creates and seeds the required schema when the backend starts. The default `dev` profile uses `jdbc:h2:mem:sentinel`; customer, transaction, alert, case, and audit data disappear when that backend process stops.

## Configuration

Useful environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` | Selects `dev` or `postgres` |
| `DB_URL` | `jdbc:postgresql://localhost:5432/sentinel_aml` | PostgreSQL JDBC URL |
| `DB_USER` | `aj1singh` | PostgreSQL user |
| `DB_PASSWORD` | Empty | PostgreSQL password |
| `JWT_SECRET` | Development-only value | JWT signing secret |
| `CORS_ORIGINS` | `http://localhost:4200` | Allowed frontend origin |

## API Summary

| Area | Endpoints |
| --- | --- |
| Authentication | `POST /api/v1/auth/login` |
| Bulk ingestion | `POST /api/v1/ingest/customers`, `/accounts`, `/transactions` |
| Streaming ingestion | `POST /api/v1/transactions` |
| Alerts | `GET /api/v1/alerts`, `/alerts/stats`, `/alerts/{id}` |
| Customers | `GET /api/v1/customers`, `/customers/{id}`, transaction history |
| Cases | `POST/GET /api/v1/cases`, `GET /cases/{id}`, `PATCH /cases/{id}/disposition` |
| Administration | `GET/PATCH /api/v1/admin/rules` |
| Audit | `GET /api/v1/audit` |

## Project Layout

- `backend/src/main/java` - Spring Boot controllers, services, detection rules, repositories, and domain models
- `backend/src/main/resources/db/migration` - Flyway schema and reference-data migrations
- `backend/sample-data` - synthetic demonstration CSV files
- `frontend/src/app/core` - API clients, authentication, guards, models, and interceptor
- `frontend/src/app/features` - dashboard, alerts, cases, login, and administration
- `AzenioJavahackathon` - original challenge specification and supplied data
