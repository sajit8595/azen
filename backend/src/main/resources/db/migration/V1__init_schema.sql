-- ============================================================
-- Sentinel AML - initial schema (V1)
-- Relationships: customer -> account -> transaction
-- Detection produces alerts; analysts group alerts into cases.
-- All state transitions recorded in an immutable audit_log.
-- ============================================================

-- ---------- Security / RBAC ----------
CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(32)  NOT NULL,          -- ANALYST | ADMIN
    display_name  VARCHAR(128),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------- Reference / config tables ----------
CREATE TABLE exchange_rate (
    id            BIGSERIAL PRIMARY KEY,
    currency      VARCHAR(3)  NOT NULL UNIQUE,     -- ISO code
    rate_to_inr   NUMERIC(18,6) NOT NULL,          -- 1 unit of currency = rate_to_inr INR
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE high_risk_jurisdiction (
    id            BIGSERIAL PRIMARY KEY,
    country_code  VARCHAR(3)  NOT NULL UNIQUE,      -- ISO country code
    country_name  VARCHAR(128),
    reason        VARCHAR(255),
    active        BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE rule_config (
    id             BIGSERIAL PRIMARY KEY,
    rule_code      VARCHAR(64)  NOT NULL UNIQUE,    -- CTR_THRESHOLD, STRUCTURING, ...
    rule_name      VARCHAR(128) NOT NULL,
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    base_score     INT          NOT NULL,           -- 0-100
    params_json    TEXT         NOT NULL,           -- tunable thresholds/windows
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------- Core domain ----------
CREATE TABLE customer (
    id                     VARCHAR(32) PRIMARY KEY,  -- CUST_00001
    first_name             VARCHAR(64),
    last_name              VARCHAR(64),
    gender                 VARCHAR(8),
    date_of_birth          DATE,
    email                  VARCHAR(128),
    phone_number           VARCHAR(32),
    city                   VARCHAR(64),
    state                  VARCHAR(64),
    country                VARCHAR(3),
    occupation             VARCHAR(64),
    annual_income          NUMERIC(18,2),
    customer_segment       VARCHAR(32),
    kyc_status             VARCHAR(32),
    risk_rating            VARCHAR(16),              -- LOW | MEDIUM | HIGH
    is_politically_exposed BOOLEAN     NOT NULL DEFAULT FALSE,
    customer_since         DATE,
    created_at             TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE account (
    id                 VARCHAR(32) PRIMARY KEY,      -- ACC_000001
    customer_id        VARCHAR(32) NOT NULL REFERENCES customer(id),
    account_type       VARCHAR(32),
    account_status     VARCHAR(32),
    currency           VARCHAR(3)  NOT NULL DEFAULT 'INR',
    open_date          DATE,
    close_date         DATE,
    branch_code        VARCHAR(16),
    branch_city        VARCHAR(64),
    current_balance    NUMERIC(18,2),
    account_tier       VARCHAR(32),
    created_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_account_customer ON account(customer_id);

CREATE TABLE transaction (
    id                    VARCHAR(40) PRIMARY KEY,   -- TXN_...
    account_id            VARCHAR(32) NOT NULL REFERENCES account(id),
    direction             VARCHAR(8)  NOT NULL,       -- CREDIT | DEBIT
    amount                NUMERIC(18,2) NOT NULL,
    currency              VARCHAR(3)  NOT NULL DEFAULT 'INR',
    amount_inr            NUMERIC(18,2) NOT NULL,     -- normalized
    counterparty_name     VARCHAR(128),
    counterparty_account  VARCHAR(64),
    counterparty_country  VARCHAR(3),
    channel               VARCHAR(32),                -- WIRE, UPI, CASH, NEFT ...
    txn_timestamp         TIMESTAMP   NOT NULL,
    created_at            TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_txn_account ON transaction(account_id);
CREATE INDEX idx_txn_time ON transaction(txn_timestamp);
CREATE INDEX idx_txn_account_time ON transaction(account_id, txn_timestamp);

-- ---------- Alerts ----------
CREATE TABLE alert (
    id             BIGSERIAL PRIMARY KEY,
    customer_id    VARCHAR(32) NOT NULL REFERENCES customer(id),
    account_id     VARCHAR(32) REFERENCES account(id),
    rule_code      VARCHAR(64) NOT NULL,
    rule_name      VARCHAR(128) NOT NULL,
    risk_score     INT         NOT NULL,             -- 0-100
    severity       VARCHAR(16) NOT NULL,             -- LOW | MEDIUM | HIGH | CRITICAL
    status         VARCHAR(16) NOT NULL DEFAULT 'OPEN', -- OPEN | IN_REVIEW | CLOSED
    explanation    TEXT        NOT NULL,
    evidence_txn_ids TEXT,                            -- comma-separated txn ids
    dedup_key      VARCHAR(128) NOT NULL,            -- (customer|rule|window) dedup
    created_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_alert_customer ON alert(customer_id);
CREATE INDEX idx_alert_score ON alert(risk_score DESC);
CREATE INDEX idx_alert_status ON alert(status);
CREATE UNIQUE INDEX uq_alert_dedup ON alert(dedup_key);

-- ---------- Cases ----------
CREATE TABLE aml_case (
    id                  BIGSERIAL PRIMARY KEY,
    alert_id            BIGINT      NOT NULL REFERENCES alert(id),
    status              VARCHAR(16) NOT NULL DEFAULT 'NEW', -- NEW | INVESTIGATING | DISPOSED
    assigned_to         VARCHAR(64),
    disposition         VARCHAR(32),                 -- CONFIRMED_SAR | FALSE_POSITIVE | CLEARED
    disposition_reason  TEXT,
    analyst_id          VARCHAR(64),
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_case_status ON aml_case(status);
CREATE INDEX idx_case_alert ON aml_case(alert_id);

-- ---------- Immutable audit log ----------
CREATE TABLE audit_log (
    id           BIGSERIAL PRIMARY KEY,
    entity_type  VARCHAR(32) NOT NULL,               -- ALERT | CASE | ...
    entity_id    VARCHAR(64) NOT NULL,
    action       VARCHAR(64) NOT NULL,               -- CREATED | STATUS_CHANGE | DISPOSED ...
    actor_id     VARCHAR(64) NOT NULL,
    details_json TEXT,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
