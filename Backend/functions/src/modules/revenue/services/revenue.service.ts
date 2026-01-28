import { Injectable } from '@nestjs/common';
import { RevenueRepository } from '../repositories/revenue.repository';
import { ShopsService } from '../../shops/services/shops.service';
import {
  RevenueAnalyticsEntity,
  RevenuePeriod,
  KpiStat,
} from '../entities/revenue-analytics.entity';
import { calculatePeriodBoundaries } from '../utils/period.utils';
import { formatCurrency, formatTrend } from '../utils/format.utils';

@Injectable()
export class RevenueService {
  constructor(
    private readonly revenueRepository: RevenueRepository,
    private readonly shopsService: ShopsService,
  ) {}

  /**
   * Get revenue analytics for owner's shop
   */
  async getRevenueAnalytics(
    ownerId: string,
    period: RevenuePeriod,
  ): Promise<RevenueAnalyticsEntity> {
    // Verify owner's shop
    const shop = await this.shopsService.getMyShop(ownerId);

    // Calculate period boundaries
    const { periodStart, periodEnd, prevStart, prevEnd, periodLabel } =
      calculatePeriodBoundaries(period);

    // Query COMPLETED orders for current period
    const currentOrders = await this.revenueRepository.getCompletedOrdersByPeriod(
      shop.id,
      periodStart,
      periodEnd,
    );

    // Query COMPLETED orders for previous period (for trend)
    const previousOrders = await this.revenueRepository.getCompletedOrdersByPeriod(
      shop.id,
      prevStart,
      prevEnd,
    );

    // Calculate current period KPIs
    const currentRevenue = currentOrders.reduce((sum, order) => sum + (order.total || 0), 0);
    const currentOrderCount = currentOrders.length;
    const currentAvgOrderValue =
      currentOrderCount > 0 ? Math.round(currentRevenue / currentOrderCount) : 0;

    // Calculate previous period KPIs
    const previousRevenue = previousOrders.reduce((sum, order) => sum + (order.total || 0), 0);
    const previousOrderCount = previousOrders.length;
    const previousAvgOrderValue =
      previousOrderCount > 0 ? Math.round(previousRevenue / previousOrderCount) : 0;

    // Calculate month-to-date revenue (for 4th KPI card)
    const monthToDateRevenue = await this.getMonthToDateRevenue(shop.id);

    // Build stats cards
    const stats = this.buildStatsCards(
      period,
      currentRevenue,
      currentOrderCount,
      currentAvgOrderValue,
      previousRevenue,
      previousOrderCount,
      previousAvgOrderValue,
      monthToDateRevenue,
      periodLabel,
    );

    // Aggregate time slots
    const timeSlots = this.revenueRepository.aggregateOrdersByTimeSlot(
      currentOrders,
      currentRevenue,
    );

    // Get top 3 products
    const topProducts = this.revenueRepository.getRankingByProduct(currentOrders, 3);

    return new RevenueAnalyticsEntity({
      period,
      stats,
      timeSlots,
      topProducts,
    });
  }

  /**
   * Get month-to-date revenue for current month
   */
  private async getMonthToDateRevenue(shopId: string): Promise<number> {
    const now = new Date();
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0, 0);
    const monthEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0, 23, 59, 59, 999);

    const orders = await this.revenueRepository.getCompletedOrdersByPeriod(
      shopId,
      monthStart,
      monthEnd,
    );

    return orders.reduce((sum, order) => sum + (order.total || 0), 0);
  }

  /**
   * Build 4 KPI stat cards with formatted values and trends
   */
  private buildStatsCards(
    period: RevenuePeriod,
    currentRevenue: number,
    currentOrderCount: number,
    currentAvgOrderValue: number,
    previousRevenue: number,
    previousOrderCount: number,
    previousAvgOrderValue: number,
    monthToDateRevenue: number,
    periodLabel: string,
  ): KpiStat[] {
    const periodLabels = {
      [RevenuePeriod.TODAY]: 'hôm nay',
      [RevenuePeriod.WEEK]: 'tuần này',
      [RevenuePeriod.MONTH]: 'tháng này',
      [RevenuePeriod.YEAR]: 'năm nay',
    };

    const currentPeriodLabel = periodLabels[period];

    return [
      {
        title: `Doanh thu ${currentPeriodLabel}`,
        value: formatCurrency(currentRevenue),
        subtitle: formatTrend(currentRevenue, previousRevenue, periodLabel),
        color: '#FF6B35',
      },
      {
        title: `Số đơn ${currentPeriodLabel}`,
        value: currentOrderCount.toString(),
        subtitle: formatTrend(currentOrderCount, previousOrderCount, periodLabel),
        color: '#2196F3',
      },
      {
        title: 'Đơn trung bình',
        value: formatCurrency(currentAvgOrderValue),
        subtitle: formatTrend(currentAvgOrderValue, previousAvgOrderValue, periodLabel),
        color: '#9C27B0',
      },
      {
        title: 'Doanh thu tháng',
        value: formatCurrency(monthToDateRevenue),
        subtitle: this.getMonthSubtitle(),
        color: '#FF9800',
      },
    ];
  }

  /**
   * Get month subtitle (e.g., "11 ngày đầu tháng")
   */
  private getMonthSubtitle(): string {
    const now = new Date();
    const dayOfMonth = now.getDate();
    return `${dayOfMonth} ngày đầu tháng`;
  }
}
