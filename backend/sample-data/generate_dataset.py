#!/usr/bin/env python3
"""
Sentinel AML - synthetic dataset generator.

Produces three CSVs under sample-data/ that the ingestion endpoints consume:
  - customers.csv
  - accounts.csv
  - transactions.csv

The transactions deliberately embed the laundering typologies required by the
brief so the detection engine has real patterns to catch:
  1. CTR_THRESHOLD          - single txn >= 10,000 INR
  2. STRUCTURING            - 3+ txns of 9,000-9,999 from one account within 24h
  3. RAPID_MOVEMENT         - >=80% of a deposit moved out within 48h
  4. HIGH_RISK_JURISDICTION - counterparty in a high-risk country
  5. BEHAVIORAL_DEVIATION   - daily value > 3x the 90-day rolling average

Deterministic (fixed seed) so the demo is reproducible.
"""
import csv
import random
from datetime import datetime, timedelta

random.seed(42)
OUT = "."
BASE = datetime(2026, 9, 1, 9, 0, 0)

# ---------- customers ----------
CUST_HEADER = [
    "customer_id","first_name","last_name","gender","date_of_birth","email",
    "phone_number","city","state","country","occupation","annual_income",
    "customer_segment","kyc_status","risk_rating","is_politically_exposed","customer_since"
]
customers = [
    ["CUST_00001","Krishna","Sharma","M","1985-05-26","krishna.sharma31@gmail.com","+91-6939042955","Gurugram","Haryana","IN","Engineer","355047.0","PREMIUM","VERIFIED","MEDIUM","0","2016-08-26"],
    ["CUST_00002","Anika","Fernandes","F","2008-08-31","anika.fernandes907@outlook.com","+91-8479708607","Gurugram","Haryana","IN","Student","101869.0","RETAIL","VERIFIED","LOW","0","2020-06-15"],
    ["CUST_00003","Rahul","Mehta","M","1979-02-14","rahul.mehta@gmail.com","+91-9812345678","Mumbai","Maharashtra","IN","Business Owner","1200000.0","PREMIUM","VERIFIED","HIGH","1","2014-01-10"],
    ["CUST_00004","Priya","Nair","F","1990-11-02","priya.nair@outlook.com","+91-9900112233","Bengaluru","Karnataka","IN","Consultant","890000.0","PREMIUM","VERIFIED","MEDIUM","0","2018-05-20"],
    ["CUST_00005","Imran","Khan","M","1982-07-19","imran.khan@gmail.com","+91-9765432100","Delhi","Delhi","IN","Trader","640000.0","RETAIL","VERIFIED","HIGH","0","2015-09-30"],
]

# ---------- accounts ----------
ACC_HEADER = [
    "account_id","customer_id","account_type","account_status","currency",
    "open_date","close_date","branch_code","branch_city","current_balance","account_tier"
]
accounts = [
    ["ACC_000001","CUST_00001","NRE","ACTIVE","INR","2016-08-26","","BR122","Gurugram","33507.22","SILVER"],
    ["ACC_000002","CUST_00002","SAVINGS","ACTIVE","INR","2020-06-15","","BR119","Gurugram","25171.02","SILVER"],
    ["ACC_000003","CUST_00003","CURRENT","ACTIVE","INR","2014-01-10","","BR101","Mumbai","540000.00","GOLD"],
    ["ACC_000004","CUST_00004","SAVINGS","ACTIVE","INR","2018-05-20","","BR205","Bengaluru","210000.00","SILVER"],
    ["ACC_000005","CUST_00005","CURRENT","ACTIVE","INR","2015-09-30","","BR310","Delhi","95000.00","GOLD"],
]

# ---------- transactions ----------
TXN_HEADER = [
    "transaction_id","account_id","direction","amount","currency",
    "counterparty_name","counterparty_account","counterparty_country","channel","txn_timestamp"
]
txns = []
_seq = [0]
def add(account, direction, amount, ts, cp_name="Acme Traders", cp_acct="EXT_1000",
        cp_country="IN", currency="INR", channel="NEFT"):
    _seq[0] += 1
    txns.append([
        f"TXN_{_seq[0]:06d}", account, direction, f"{amount:.2f}", currency,
        cp_name, cp_acct, cp_country, channel, ts.strftime("%Y-%m-%d %H:%M:%S")
    ])

# --- baseline "normal" activity for every account (small amounts) ---
for acc in [a[0] for a in accounts]:
    day = BASE - timedelta(days=80)
    for _ in range(25):
        day += timedelta(days=random.randint(1, 3))
        add(acc, random.choice(["CREDIT","DEBIT"]), random.uniform(500, 4000), day,
            channel=random.choice(["UPI","NEFT","IMPS"]))

# --- 1. CTR_THRESHOLD: single large txn on ACC_000003 ---
add("ACC_000003","CREDIT",1500000.00, BASE + timedelta(days=1),
    cp_name="Global Exports Ltd", cp_acct="EXT_5501", channel="WIRE")

# --- 2. STRUCTURING: 4 txns 9,000-9,999 from ACC_000001 within 24h ---
s = BASE + timedelta(days=2, hours=1)
for amt, h in [(9200, 0), (9600, 4), (9100, 9), (9800, 15)]:
    add("ACC_000001","DEBIT", amt, s + timedelta(hours=h),
        cp_name="Cash Deposit", cp_acct="CASH", channel="CASH")

# --- 3. RAPID_MOVEMENT: big deposit then >=80% out within 48h on ACC_000004 ---
dep = BASE + timedelta(days=3, hours=2)
add("ACC_000004","CREDIT", 500000.00, dep, cp_name="Salary Advance", cp_acct="EXT_7010", channel="NEFT")
add("ACC_000004","DEBIT", 250000.00, dep + timedelta(hours=6), cp_name="Vend One", cp_acct="EXT_7011", channel="WIRE")
add("ACC_000004","DEBIT", 180000.00, dep + timedelta(hours=30), cp_name="Vend Two", cp_acct="EXT_7012", channel="WIRE")

# --- 4. HIGH_RISK_JURISDICTION: transfer to Iran from ACC_000005 ---
add("ACC_000005","DEBIT", 45000.00, BASE + timedelta(days=4),
    cp_name="Tehran Trading Co", cp_acct="EXT_9001", cp_country="IR", channel="WIRE")

# --- 5. BEHAVIORAL_DEVIATION: one day far above ACC_000002 baseline ---
b = BASE + timedelta(days=5, hours=3)
for h in range(6):
    add("ACC_000002","DEBIT", 40000.00, b + timedelta(hours=h),
        cp_name="Unknown Merchant", cp_acct=f"EXT_88{h:02d}", channel="UPI")

def write(name, header, rows):
    with open(f"{OUT}/{name}", "w", newline="") as f:
        w = csv.writer(f)
        w.writerow(header)
        w.writerows(rows)
    print(f"{name}: {len(rows)} rows")

write("customers.csv", CUST_HEADER, customers)
write("accounts.csv", ACC_HEADER, accounts)
txns.sort(key=lambda r: r[9])
write("transactions.csv", TXN_HEADER, txns)
