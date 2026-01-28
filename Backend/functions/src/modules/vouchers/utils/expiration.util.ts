/**
 * Voucher Expiration Utility
 *
 * Handles all expiration-related logic for vouchers.
 * Provides consistent expiration semantics across the codebase.
 *
 * **Expiration Rule:** A voucher is considered expired if the current time (now)
 * is AFTER its validTo timestamp.
 *
 * Formula: now > validTo => EXPIRED
 */

import { VoucherEntity } from '../entities';

/**
 * Check if a voucher has expired
 * @param voucher The voucher to check
 * @param now Current time as ISO 8601 string (defaults to current time)
 * @returns true if voucher is expired (now > validTo), false otherwise
 */
export function isVoucherExpired(
  voucher: VoucherEntity,
  now: string = new Date().toISOString(),
): boolean {
  return now > voucher.validTo;
}

/**
 * Check if a voucher is within its valid time range
 * @param voucher The voucher to check
 * @param now Current time as ISO 8601 string (defaults to current time)
 * @returns true if voucher is within validFrom and validTo (inclusive), false otherwise
 */
export function isVoucherInTimeRange(
  voucher: VoucherEntity,
  now: string = new Date().toISOString(),
): boolean {
  return now >= voucher.validFrom && now <= voucher.validTo;
}

/**
 * Check if a voucher has not yet started
 * @param voucher The voucher to check
 * @param now Current time as ISO 8601 string (defaults to current time)
 * @returns true if voucher has not yet started (now < validFrom), false otherwise
 */
export function isVoucherNotStarted(
  voucher: VoucherEntity,
  now: string = new Date().toISOString(),
): boolean {
  return now < voucher.validFrom;
}

/**
 * Get the days remaining until voucher expires
 * @param voucher The voucher to check
 * @param now Current time as ISO 8601 string (defaults to current time)
 * @returns Number of days remaining (can be negative if expired), or 0 if expired
 */
export function getVoucherDaysRemaining(
  voucher: VoucherEntity,
  now: string = new Date().toISOString(),
): number {
  const nowDate = new Date(now);
  const validToDate = new Date(voucher.validTo);
  const diffMs = validToDate.getTime() - nowDate.getTime();
  const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));
  return Math.max(0, diffDays);
}

/**
 * Check if a voucher is "active" from a business perspective
 *
 * A voucher is considered "active" if:
 * 1. isActive flag is true (not deleted/disabled)
 * 2. Within valid time range (validFrom <= now <= validTo)
 * 3. Has usage capacity remaining (currentUsage < usageLimit)
 *
 * @param voucher The voucher to check
 * @param now Current time as ISO 8601 string (defaults to current time)
 * @returns true if all conditions are met, false otherwise
 */
export function isVoucherActiveAndValid(
  voucher: VoucherEntity,
  now: string = new Date().toISOString(),
): boolean {
  return (
    voucher.isActive &&
    !voucher.isDeleted &&
    isVoucherInTimeRange(voucher, now) &&
    voucher.currentUsage < voucher.usageLimit
  );
}

/**
 * Get expiration status of a voucher
 * Useful for display/reporting purposes
 *
 * @param voucher The voucher to check
 * @param now Current time as ISO 8601 string (defaults to current time)
 * @returns Object with status and descriptive fields
 */
export function getVoucherExpirationStatus(
  voucher: VoucherEntity,
  now: string = new Date().toISOString(),
): {
  status: 'ACTIVE' | 'EXPIRED' | 'NOT_STARTED' | 'INACTIVE';
  expired: boolean;
  notStarted: boolean;
  daysRemaining: number;
  validFrom: string;
  validTo: string;
} {
  if (voucher.isDeleted || !voucher.isActive) {
    return {
      status: 'INACTIVE',
      expired: false,
      notStarted: false,
      daysRemaining: 0,
      validFrom: voucher.validFrom,
      validTo: voucher.validTo,
    };
  }

  const expired = isVoucherExpired(voucher, now);
  const notStarted = isVoucherNotStarted(voucher, now);
  const daysRemaining = getVoucherDaysRemaining(voucher, now);

  let status: 'ACTIVE' | 'EXPIRED' | 'NOT_STARTED' | 'INACTIVE' = 'ACTIVE';
  if (expired) status = 'EXPIRED';
  if (notStarted) status = 'NOT_STARTED';
  if (voucher.currentUsage >= voucher.usageLimit) status = 'INACTIVE'; // Limit reached

  return {
    status,
    expired,
    notStarted,
    daysRemaining,
    validFrom: voucher.validFrom,
    validTo: voucher.validTo,
  };
}
