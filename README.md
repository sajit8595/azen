# Sentinel AML

Real-time anti-money-laundering monitoring with an Angular 22 frontend, Spring Boot API, configurable detection rules, case management, and audit history.

## Requirements

- Java 21 exactly
- Node.js 22.12+ or 24
- npm 11

The Maven Wrapper is included. Verify Java with `java --version`; other Java versions are rejected by the build.

On macOS with Homebrew, select Java 21 if needed:

```bash
export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

## Run Locally

The default profile uses an in-memory H2 database and requires no database setup.

**Terminal 1 - backend:**

```bash
cd backend
./mvnw spring-boot:run
```

Windows: run `mvnw.cmd spring-boot:run`. Wait for `Started SentinelApplication`.

**Terminal 2 - frontend:**

```bash
cd frontend
npm ci
npm start
```

Open <http://localhost:4200> and sign in:

| Role | Username | Password |
| --- | --- | --- |
| Analyst | `analyst` | `analyst123` |
| Administrator | `admin` | `admin123` |

## Load Demo Data

Sign in as `admin`, open **Data Ingestion**, and upload once in this order:

1. `backend/demo-sample-data/customers.csv`
2. `backend/demo-sample-data/accounts.csv`
3. `backend/demo-sample-data/transactions.csv`

A clean run inserts 5 customers, 5 accounts, and 11 transactions, producing examples of all five detection rules. H2 data resets when the backend stops.

## Application Flow

### Data and detection

```mermaid
flowchart LR
  A[Customers CSV] --> D[Validation]
  B[Accounts CSV] --> D
  C[Transactions CSV] --> D
  D --> E[Currency normalization]
  E --> F[Detection rules]
  F --> G[Deduplication]
  G --> H[Risk-scored alerts]
```

### Investigation

```mermaid
flowchart LR
  A[Dashboard] --> B[Alert queue]
  B --> C[Review evidence]
  C --> D[Create case]
  D --> E[Record disposition]
  E --> F[Audit trail]
```

### Administration

```mermaid
flowchart LR
  A[Administrator] --> B[Enable or tune rules]
  B --> C[Rule configuration database]
  C --> D[Detection engine]
  A --> E[Review audit trail]
```

## Screenshots

### Login
![Login](docs/screenshots/01-login.png)

### Data ingestion
![Data ingestion](docs/screenshots/02-ingestion.png)

### Dashboard
![Dashboard](docs/screenshots/03-dashboard.png)

### Alert queue
![Alert queue](docs/screenshots/04-alert-queue.png)

### Alert evidence
![Alert detail](docs/screenshots/05-alert-detail.png)

### Case workflow
![Cases](docs/screenshots/08-cases.png)
![Case detail](docs/screenshots/09-case-detail.png)

### Rule configuration
![Rule configuration](docs/screenshots/06-rule-configuration.png)

### Audit trail
![Audit trail](docs/screenshots/07-audit-trail.png)

## Persistent PostgreSQL

Use PostgreSQL instead of H2 when data must survive restarts:

```bash
createdb sentinel_aml
cd backend
SPRING_PROFILES_ACTIVE=postgres \
DB_URL=jdbc:postgresql://localhost:5432/sentinel_aml \
DB_USER=postgres \
DB_PASSWORD=postgres \
./mvnw spring-boot:run
```

Replace the credentials with your local PostgreSQL user and password. Flyway creates the schema automatically; upload the demo files only once.

## Useful URLs

- Application: <http://localhost:4200>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI: <http://localhost:8080/v3/api-docs>
- H2 console: <http://localhost:8080/h2-console> (`jdbc:h2:mem:sentinel`, user `sa`, empty password)

## Test and Build

```bash
cd backend && ./mvnw test
cd ../frontend && npm ci && npm test -- --watch=false && npm run build
```

## Troubleshooting

- **Wrong Java:** `java --version` and `backend/mvnw --version` must report Java 21.
- **Port already used:** check with `lsof -nP -iTCP:8080 -iTCP:4200 -sTCP:LISTEN`.
- **Login fails:** verify <http://localhost:8080/v3/api-docs> opens first.
- **Duplicate CSV IDs:** restart H2 or use a new empty PostgreSQL database.
- **No alerts:** upload customers, accounts, then transactions in that order.

See [the demo walkthrough](backend/demo-sample-data/README.md) and [presentation notes](DEMO_PITCH.md).