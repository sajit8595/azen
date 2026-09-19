// Map domain enums to Bootstrap badge background classes.

export function severityBadge(severity?: string): string {
  switch (severity) {
    case 'CRITICAL':
      return 'text-bg-danger';
    case 'HIGH':
      return 'text-bg-warning';
    case 'MEDIUM':
      return 'text-bg-info';
    case 'LOW':
      return 'text-bg-success';
    default:
      return 'text-bg-secondary';
  }
}

export function statusBadge(status?: string): string {
  switch (status) {
    case 'OPEN':
    case 'NEW':
      return 'text-bg-warning';
    case 'IN_REVIEW':
    case 'INVESTIGATING':
      return 'text-bg-primary';
    case 'CLOSED':
    case 'DISPOSED':
      return 'text-bg-secondary';
    default:
      return 'text-bg-secondary';
  }
}

export function dispositionBadge(disposition?: string): string {
  switch (disposition) {
    case 'CONFIRMED_SAR':
      return 'text-bg-danger';
    case 'FALSE_POSITIVE':
      return 'text-bg-secondary';
    case 'CLEARED':
      return 'text-bg-success';
    default:
      return 'text-bg-secondary';
  }
}
