import { RevenuePeriod } from '../entities/revenue-analytics.entity';

/**
 * Period boundaries (start and end dates)
 */
export interface PeriodBoundaries {
  periodStart: Date;
  periodEnd: Date;
  prevStart: Date;
  prevEnd: Date;
  periodLabel: string; // For trend subtitle (e.g., "hôm qua", "tuần trước")
}

/**
 * Calculate period boundaries for analytics queries
 * All dates in UTC timezone
 *
 * @param period - Revenue period enum
 * @returns Period start/end and previous period start/end
 */
export function calculatePeriodBoundaries(period: RevenuePeriod): PeriodBoundaries {
  const now = new Date();

  switch (period) {
    case RevenuePeriod.TODAY:
      return calculateTodayBoundaries(now);
    case RevenuePeriod.WEEK:
      return calculateWeekBoundaries(now);
    case RevenuePeriod.MONTH:
      return calculateMonthBoundaries(now);
    case RevenuePeriod.YEAR:
      return calculateYearBoundaries(now);
    default:
      throw new Error(`Invalid period: ${period}`);
  }
}

/**
 * Calculate today boundaries (UTC)
 * Today: 00:00:00 - 23:59:59
 * Previous: Yesterday 00:00:00 - 23:59:59
 */
function calculateTodayBoundaries(now: Date): PeriodBoundaries {
  const periodStart = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
  const periodEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);

  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  const prevStart = new Date(
    yesterday.getFullYear(),
    yesterday.getMonth(),
    yesterday.getDate(),
    0,
    0,
    0,
    0,
  );
  const prevEnd = new Date(
    yesterday.getFullYear(),
    yesterday.getMonth(),
    yesterday.getDate(),
    23,
    59,
    59,
    999,
  );

  return {
    periodStart,
    periodEnd,
    prevStart,
    prevEnd,
    periodLabel: 'hôm qua',
  };
}

/**
 * Calculate week boundaries (Mon-Sun)
 * Current week: Monday 00:00 - Sunday 23:59
 * Previous: Previous Monday-Sunday
 */
function calculateWeekBoundaries(now: Date): PeriodBoundaries {
  const dayOfWeek = now.getDay(); // 0=Sun, 1=Mon, ..., 6=Sat
  const daysFromMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1; // Convert to Mon=0

  // Current week start (Monday 00:00)
  const periodStart = new Date(now);
  periodStart.setDate(periodStart.getDate() - daysFromMonday);
  periodStart.setHours(0, 0, 0, 0);

  // Current week end (Sunday 23:59)
  const periodEnd = new Date(periodStart);
  periodEnd.setDate(periodEnd.getDate() + 6);
  periodEnd.setHours(23, 59, 59, 999);

  // Previous week start (7 days before current week start)
  const prevStart = new Date(periodStart);
  prevStart.setDate(prevStart.getDate() - 7);

  // Previous week end
  const prevEnd = new Date(periodEnd);
  prevEnd.setDate(prevEnd.getDate() - 7);

  return {
    periodStart,
    periodEnd,
    prevStart,
    prevEnd,
    periodLabel: 'tuần trước',
  };
}

/**
 * Calculate month boundaries
 * Current month: 1st 00:00 - last day 23:59
 * Previous: 1st-last of previous month
 */
function calculateMonthBoundaries(now: Date): PeriodBoundaries {
  // Current month start (1st day 00:00)
  const periodStart = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0, 0);

  // Current month end (last day 23:59)
  const periodEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999);

  // Previous month start
  const prevStart = new Date(now.getFullYear(), now.getMonth() - 1, 1, 0, 0, 0, 0);

  // Previous month end
  const prevEnd = new Date(now.getFullYear(), now.getMonth(), 0, 23, 59, 59, 999);

  return {
    periodStart,
    periodEnd,
    prevStart,
    prevEnd,
    periodLabel: 'tháng trước',
  };
}

/**
 * Calculate year boundaries
 * Current year: Jan 1 00:00 - Dec 31 23:59
 * Previous: Jan 1 - Dec 31 of previous year
 */
function calculateYearBoundaries(now: Date): PeriodBoundaries {
  // Current year start (Jan 1 00:00)
  const periodStart = new Date(now.getFullYear(), 0, 1, 0, 0, 0, 0);

  // Current year end (Dec 31 23:59)
  const periodEnd = new Date(now.getFullYear(), 11, 31, 23, 59, 59, 999);

  // Previous year start
  const prevStart = new Date(now.getFullYear() - 1, 0, 1, 0, 0, 0, 0);

  // Previous year end
  const prevEnd = new Date(now.getFullYear() - 1, 11, 31, 23, 59, 59, 999);

  return {
    periodStart,
    periodEnd,
    prevStart,
    prevEnd,
    periodLabel: 'năm ngoái',
  };
}
