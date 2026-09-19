// Domain models mirroring the frozen API contract (SHARED_PLAN.md section 6).
// Backend is the source of truth: entity IDs are strings (CUST_/ACC_/TXN_),
// alert/case IDs are numbers.

export type Role = 'ANALYST' | 'ADMIN';

// Uniform response envelope. On error `data` is null and `errors` is populated.
export interface ApiResponse<T> {
  data: T | null;
  metaData: {
    timestamp: string;
    id?: string;
  };
  errors?: ApiErrorDetail[];
}

export interface ApiErrorDetail {
  code: string;
  message: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  role: Role;
  username: string;
}

// Spring Data pageable response shape
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // current page index (0-based)
  size: number;
  first: boolean;
  last: boolean;
}

export type KycStatus = 'PENDING' | 'VERIFIED' | 'REJECTED';
export type RiskRating = 'LOW' | 'MEDIUM' | 'HIGH';

// List view — PII masked (GET /customers)
export interface CustomerSummary {
  id: string;
  maskedName: string;
  city?: string;
  state?: string;
  riskRating?: RiskRating;
  customerSegment?: string;
  politicallyExposed?: boolean;
}

// Detail view — full PII (GET /customers/{id})
export interface CustomerDetail {
  id: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phoneNumber?: string;
  city?: string;
  state?: string;
  country?: string;
  occupation?: string;
  annualIncome?: number;
  customerSegment?: string;
  kycStatus?: KycStatus;
  riskRating?: RiskRating;
  politicallyExposed?: boolean;
}

export type TxnDirection = 'CREDIT' | 'DEBIT';

export interface Transaction {
  id: string;
  accountId: string;
  direction: TxnDirection;
  amount: number;
  currency: string;
  amountInr: number;
  counterpartyName?: string;
  counterpartyCountry?: string;
  channel?: string;
  txnTimestamp: string;
}

export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type AlertStatus = 'OPEN' | 'IN_REVIEW' | 'CLOSED';

export interface Alert {
  id: number;
  customerId: string;
  accountId: string;
  ruleCode: string;
  ruleName: string;
  riskScore: number;
  severity: Severity;
  status: AlertStatus;
  explanation: string;
  evidenceTxnIds: string[];
  createdAt: string;
}

export interface AlertStats {
  open: number;
  inReview: number;
  closed: number;
  bySeverity: Record<string, number>;
  byRule: Record<string, number>;
}

export type CaseStatus = 'NEW' | 'INVESTIGATING' | 'DISPOSED';
export type Disposition = 'CONFIRMED_SAR' | 'FALSE_POSITIVE' | 'CLEARED';

export interface AmlCase {
  id: number;
  alertId: number;
  status: CaseStatus;
  assignedTo?: string;
  disposition?: Disposition;
  dispositionReason?: string;
  analystId?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateCaseRequest {
  alertId: number;
}

export interface DispositionRequest {
  disposition: Disposition;
  reason: string;
}

export interface RuleConfig {
  id: number;
  ruleCode: string;
  ruleName: string;
  enabled: boolean;
  baseScore: number;
  paramsJson: string;
}

export interface RuleConfigUpdate {
  enabled?: boolean;
  baseScore?: number;
  paramsJson?: string;
}

export interface AuditLog {
  id: number;
  entityType: string;
  entityId: string;
  action: string;
  actorId: string;
  createdAt: string;
  detailsJson?: string;
}

export interface IngestResult {
  inserted: number;
  failed: number;
  errors: string[];
}
