import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../core/admin.service';
import { RuleConfig } from '../../core/models';

// A tunable parameter rendered as a typed input, per rule code.
interface ParamField {
  key: string;
  label: string;
  step?: number; // number input step (e.g. 0.1 for ratios)
}

// UI-specific field definitions so analysts edit named inputs, not raw JSON.
const RULE_FIELDS: Record<string, ParamField[]> = {
  CTR_THRESHOLD: [{ key: 'thresholdInr', label: 'Threshold (INR)' }],
  STRUCTURING: [
    { key: 'minCount', label: 'Min transactions' },
    { key: 'lowerInr', label: 'Lower amount (INR)' },
    { key: 'upperInr', label: 'Upper amount (INR)' },
    { key: 'windowHours', label: 'Window (hours)' },
  ],
  RAPID_MOVEMENT: [
    { key: 'outflowRatio', label: 'Outflow ratio (0–1)', step: 0.05 },
    { key: 'windowHours', label: 'Window (hours)' },
  ],
  HIGH_RISK_JURISDICTION: [],
  BEHAVIORAL_DEVIATION: [
    { key: 'multiplier', label: 'Deviation multiplier', step: 0.5 },
    { key: 'baselineDays', label: 'Baseline (days)' },
  ],
};

interface RuleRow extends RuleConfig {
  fields: ParamField[];
  params: Record<string, number>; // editable typed values
}

@Component({
  selector: 'app-rule-config',
  imports: [FormsModule],
  templateUrl: './rule-config.component.html',
})
export class RuleConfigComponent {
  private admin = inject(AdminService);

  rules = signal<RuleRow[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  savingCode = signal<string | null>(null);
  savedCode = signal<string | null>(null);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.admin.rules().subscribe({
      next: (r) => {
        this.rules.set(r.map((rule) => this.toRow(rule)));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load rules. ADMIN role required.');
        this.loading.set(false);
      },
    });
  }

  save(rule: RuleRow): void {
    this.savingCode.set(rule.ruleCode);
    this.savedCode.set(null);
    this.admin
      .updateRule(rule.ruleCode, {
        baseScore: rule.baseScore,
        enabled: rule.enabled,
        paramsJson: JSON.stringify(rule.params),
      })
      .subscribe({
        next: (updated) => {
          const row = this.toRow(updated);
          this.rules.update((list) =>
            list.map((item) => (item.ruleCode === row.ruleCode ? row : item)),
          );
          this.savingCode.set(null);
          this.savedCode.set(rule.ruleCode);
          setTimeout(() => this.savedCode.set(null), 2500);
        },
        error: () => {
          this.savingCode.set(null);
          this.error.set(`Could not save rule ${rule.ruleCode}.`);
        },
      });
  }

  // Parse the backend paramsJson into typed values keyed by the rule's fields.
  private toRow(rule: RuleConfig): RuleRow {
    const fields = RULE_FIELDS[rule.ruleCode] ?? [];
    let parsed: Record<string, number> = {};
    try {
      parsed = JSON.parse(rule.paramsJson || '{}');
    } catch {
      parsed = {};
    }
    const params: Record<string, number> = {};
    for (const f of fields) {
      params[f.key] = Number(parsed[f.key] ?? 0);
    }
    return { ...rule, fields, params };
  }
}
