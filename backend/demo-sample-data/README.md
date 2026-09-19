# UI Ingestion Demo

This compact synthetic dataset is designed for a live Sentinel AML demonstration. It contains five independent customer stories and intentionally triggers all five configured detection rules.

## Before the Demo

Use an empty database so the dashboard begins with zero alerts. Start the backend with the PostgreSQL profile, start the Angular frontend, and sign in at <http://localhost:4200>.

Demo login:

- Username: `admin`
- Password: `admin123`

## Upload Through the UI

1. Open **Data Ingestion** in the sidebar.
2. Under **1. Customers**, select `backend/demo-sample-data/customers.csv`, then select **Upload**.
3. Under **2. Accounts**, select `backend/demo-sample-data/accounts.csv`, then select **Upload**.
4. Under **3. Transactions**, select `backend/demo-sample-data/transactions.csv`, then select **Upload**.
5. Confirm the UI reports 5 customers, 5 accounts, and 11 transactions inserted with zero failures.
6. Open **Dashboard** and then **Alerts** to review the results.

Always upload in this order because accounts reference customers and transactions reference accounts.

## Demo Stories

| Customer | Account | Expected rule | Demonstration |
| --- | --- | --- | --- |
| Maya Kapoor | `DEMO_ACC_BEHAVIOR` | `BEHAVIORAL_DEVIATION` | Three INR 1,000 baseline days followed by an INR 5,000 day, exceeding 3x average |
| Rohan Mehta | `DEMO_ACC_STRUCT` | `STRUCTURING` | Three cash debits from INR 9,000-9,999 within seven hours |
| Sara Iyer | `DEMO_ACC_RAPID` | `RAPID_MOVEMENT` | INR 8,000 credited and INR 6,500 moved out within 23 hours |
| Arjun Patel | `DEMO_ACC_RISK` | `HIGH_RISK_JURISDICTION` | A transaction involving Iran (`IR`) regardless of amount |
| Neha Das | `DEMO_ACC_CTR` | `CTR_THRESHOLD` | A single INR 15,000 transaction above the configured INR 10,000 threshold |

## Expected Outcome

The transaction upload should create one primary alert for each configured rule:

- Behavioral Deviation
- Structuring / Smurfing
- Rapid Movement of Funds
- High-Risk Jurisdiction Transfer
- Large Transaction (CTR)

Open an alert to demonstrate its risk score, human-readable explanation, and evidence transaction IDs. Create a case from an alert, apply a disposition, and open **Audit Trail** to demonstrate the recorded actions.

## Repeatability

The IDs use a `DEMO_` prefix. Re-uploading the same files into the same database is not a clean rerun: records reuse the same primary keys, and alert deduplication suppresses duplicate alerts. For each presentation, use a new empty database or clear the prior demo records first.
