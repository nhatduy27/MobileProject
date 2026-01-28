/**
 * Period enum for revenue analytics
 */
export enum RevenuePeriod {
  TODAY = 'today',
  WEEK = 'week',
  MONTH = 'month',
  YEAR = 'year',
}

/**
 * KPI Stat Card
 */
export interface KpiStat {
  title: string;
  value: string; // Formatted (e.g., "1.25M")
  subtitle: string; // Trend info (e.g., "↑ 12% so với hôm qua")
  color: string; // Hex color
}

/**
 * Time Slot Aggregation
 */
export interface TimeSlotData {
  emoji: string;
  title: string;
  ordersCount: number;
  percentage: number;
  amount: string; // Formatted (e.g., "438K")
}

/**
 * Top Product Ranking
 */
export interface TopProduct {
  rank: string; // Emoji: 🥇🥈🥉
  name: string;
  quantity: number;
  unitPrice: number;
  totalRevenue: string; // Formatted (e.g., "1.44M")
}

/**
 * Revenue Analytics Entity
 * Response model for GET /owner/revenue
 */
export class RevenueAnalyticsEntity {
  period: RevenuePeriod;
  stats: KpiStat[];
  timeSlots: TimeSlotData[];
  topProducts: TopProduct[];

  constructor(partial: Partial<RevenueAnalyticsEntity>) {
    Object.assign(this, partial);
  }
}
