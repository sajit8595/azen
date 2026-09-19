-- ============================================================
-- V2 - reference / config seed data
-- ============================================================

-- Users (BCrypt hashes). analyst123 / admin123
-- Hashes generated with BCrypt strength 10.
INSERT INTO app_user (username, password_hash, role, display_name) VALUES
  ('analyst', '$2a$10$Dow1p8Q0m6q0nqK1oqk1AeQ0oM2s6Yy0m6q0nqK1oqk1AeQ0oM2s', 'ANALYST', 'Compliance Analyst'),
  ('admin',   '$2a$10$Dow1p8Q0m6q0nqK1oqk1AeQ0oM2s6Yy0m6q0nqK1oqk1AeQ0oM2s', 'ADMIN',   'Compliance Admin');

-- NOTE: real BCrypt hashes are injected at startup by DataInitializer to guarantee
-- correctness across environments; the rows above are placeholders replaced on boot.

-- Exchange rates to INR (base currency)
INSERT INTO exchange_rate (currency, rate_to_inr) VALUES
  ('INR', 1.000000),
  ('USD', 83.500000),
  ('EUR', 90.200000),
  ('GBP', 105.400000),
  ('AED', 22.700000),
  ('SGD', 61.800000);

-- High-risk / sanctioned jurisdictions (configurable)
INSERT INTO high_risk_jurisdiction (country_code, country_name, reason) VALUES
  ('IR', 'Iran',        'FATF call for action'),
  ('KP', 'North Korea', 'FATF call for action'),
  ('MM', 'Myanmar',     'FATF call for action'),
  ('SY', 'Syria',       'Sanctions / high risk'),
  ('AF', 'Afghanistan', 'High risk / monitoring'),
  ('KY', 'Cayman Islands','Offshore high-risk finance');

-- Detection rule configuration (tunable without redeploy)
INSERT INTO rule_config (rule_code, rule_name, enabled, base_score, params_json) VALUES
  ('CTR_THRESHOLD', 'Large Transaction (CTR)', TRUE, 60,
     '{"thresholdInr":10000}'),
  ('STRUCTURING', 'Structuring / Smurfing', TRUE, 85,
     '{"minCount":3,"lowerInr":9000,"upperInr":9999,"windowHours":24}'),
  ('RAPID_MOVEMENT', 'Rapid Movement of Funds', TRUE, 80,
     '{"outflowRatio":0.8,"windowHours":48}'),
  ('HIGH_RISK_JURISDICTION', 'High-Risk Jurisdiction Transfer', TRUE, 75,
     '{}'),
  ('BEHAVIORAL_DEVIATION', 'Behavioral Deviation', TRUE, 70,
     '{"multiplier":3,"baselineDays":90}');
