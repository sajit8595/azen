// Client-side PII masking helpers for list views.
// The backend already masks PII in list endpoints; these guard against any
// unmasked value slipping through and keep list rendering consistent.

export function maskName(name?: string | null): string {
  if (!name) return '—';
  const parts = name.trim().split(/\s+/);
  return parts
    .map((p) => (p.length <= 1 ? p : `${p[0]}${'*'.repeat(Math.max(1, p.length - 1))}`))
    .join(' ');
}

export function maskAccount(acct?: string | null): string {
  if (!acct) return '—';
  const s = String(acct);
  if (s.length <= 4) return '****';
  return `****${s.slice(-4)}`;
}
