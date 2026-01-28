import { ApiProperty } from '@nestjs/swagger';
import {
  KpiStat,
  TimeSlotData,
  TopProduct,
  RevenuePeriod,
} from '../entities/revenue-analytics.entity';

/**
 * Revenue Analytics Response DTO
 */
export class RevenueAnalyticsDto {
  @ApiProperty({ enum: RevenuePeriod, example: 'today' })
  period: RevenuePeriod;

  @ApiProperty({
    type: 'array',
    example: [
      {
        title: 'Doanh thu hôm nay',
        value: '1.25M',
        subtitle: '↑ 12% so với hôm qua',
        color: '#FF6B35',
      },
      {
        title: 'Số đơn hôm nay',
        value: '124',
        subtitle: '↑ 8% so với hôm qua',
        color: '#2196F3',
      },
      {
        title: 'Đơn trung bình',
        value: '101K',
        subtitle: '↑ 5% so với hôm qua',
        color: '#9C27B0',
      },
      {
        title: 'Doanh thu tháng',
        value: '38.5M',
        subtitle: '11 ngày đầu tháng',
        color: '#FF9800',
      },
    ],
  })
  stats: KpiStat[];

  @ApiProperty({
    type: 'array',
    example: [
      {
        emoji: '🌅',
        title: 'Sáng (6:00 - 10:59)',
        ordersCount: 42,
        percentage: 35,
        amount: '438K',
      },
      {
        emoji: '☀️',
        title: 'Trưa (11:00 - 16:59)',
        ordersCount: 52,
        percentage: 42,
        amount: '525K',
      },
      {
        emoji: '🌙',
        title: 'Tối (17:00 - 21:59)',
        ordersCount: 30,
        percentage: 23,
        amount: '287K',
      },
    ],
  })
  timeSlots: TimeSlotData[];

  @ApiProperty({
    type: 'array',
    example: [
      {
        rank: '🥇',
        name: 'Cơm gà xối mỡ',
        quantity: 32,
        unitPrice: 45000,
        totalRevenue: '1.44M',
      },
      {
        rank: '🥈',
        name: 'Phở bò',
        quantity: 28,
        unitPrice: 50000,
        totalRevenue: '1.40M',
      },
      {
        rank: '🥉',
        name: 'Trà sữa trân châu',
        quantity: 45,
        unitPrice: 25000,
        totalRevenue: '1.13M',
      },
    ],
  })
  topProducts: TopProduct[];

  constructor(partial: Partial<RevenueAnalyticsDto>) {
    Object.assign(this, partial);
  }
}
