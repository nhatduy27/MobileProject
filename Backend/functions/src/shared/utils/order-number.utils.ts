/**
 * Order Number Utilities
 *
 * Format: ORD-YYMMDD-XXXX
 * Example: ORD-260107-0001
 */

/**
 * Generate order number
 */
export function generateOrderNumber(sequence: number): string {
  const now = new Date();
  const yy = String(now.getFullYear()).slice(-2);
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const dd = String(now.getDate()).padStart(2, '0');
  const seq = String(sequence).padStart(4, '0');

  return `ORD-${yy}${mm}${dd}-${seq}`;
}

/**
 * Parse order number to get date
 */
export function parseOrderDate(orderNumber: string): Date | null {
  const match = orderNumber.match(/ORD-(\d{2})(\d{2})(\d{2})-/);
  if (!match) return null;

  const [, yy, mm, dd] = match;
  const year = 2000 + parseInt(yy, 10);
  const month = parseInt(mm, 10) - 1;
  const day = parseInt(dd, 10);

  return new Date(year, month, day);
}

/**
 * Generate transaction reference
 * Format: TXN-TIMESTAMP-RANDOM
 */
export function generateTransactionRef(): string {
  const timestamp = Date.now().toString(36).toUpperCase();
  const random = Math.random().toString(36).substring(2, 6).toUpperCase();
  return `TXN-${timestamp}-${random}`;
}

/**
 * Generate payout reference
 * Format: PAY-YYMMDD-XXXX
 */
export function generatePayoutRef(sequence: number): string {
  const now = new Date();
  const yy = String(now.getFullYear()).slice(-2);
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const dd = String(now.getDate()).padStart(2, '0');
  const seq = String(sequence).padStart(4, '0');

  return `PAY-${yy}${mm}${dd}-${seq}`;
}
