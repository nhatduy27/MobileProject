/**
 * Format currency to Vietnamese readable format
 *
 * @param amount - Amount in VND
 * @returns Formatted string (e.g., "1.25M", "438K", "0đ")
 */
export function formatCurrency(amount: number): string {
  if (amount >= 1_000_000) {
    const millions = amount / 1_000_000;
    return millions % 1 === 0 ? `${millions}M` : `${millions.toFixed(1)}M`;
  }
  if (amount >= 1_000) {
    return `${Math.round(amount / 1_000)}K`;
  }
  if (amount === 0) {
    return '0đ';
  }
  return amount.toLocaleString('vi-VN') + 'đ';
}

/**
 * Format trend percentage with arrow indicator
 *
 * @param currentValue - Current period value
 * @param previousValue - Previous period value
 * @param periodLabel - Label for period (e.g., "hôm qua", "tuần trước")
 * @returns Formatted trend string (e.g., "↑ 12% so với hôm qua")
 */
export function formatTrend(
  currentValue: number,
  previousValue: number,
  periodLabel: string,
): string {
  if (previousValue === 0 && currentValue === 0) {
    return 'Không có doanh thu';
  }

  if (previousValue === 0 && currentValue > 0) {
    return 'Lần đầu tiên';
  }

  const trendPercent = ((currentValue - previousValue) / previousValue) * 100;
  const absPercent = Math.abs(trendPercent);
  const arrow = trendPercent >= 0 ? '↑' : '↓';

  return `${arrow} ${Math.round(absPercent)}% so với ${periodLabel}`;
}

/**
 * Calculate percentage of total
 *
 * @param part - Part value
 * @param total - Total value
 * @returns Percentage (0-100)
 */
export function calculatePercentage(part: number, total: number): number {
  if (total === 0) return 0;
  return Math.round((part / total) * 100);
}
