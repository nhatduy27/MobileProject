import {
  Controller,
  Get,
  Query,
  UseGuards,
  BadRequestException,
  InternalServerErrorException,
} from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { CurrentUser } from '../../../core/decorators/current-user.decorator';
import { UserRole } from '../../../core/interfaces/user.interface';
import { RevenueService } from '../services/revenue.service';
import { RevenueQueryDto, RevenueAnalyticsDto } from '../dto';
import { RevenuePeriod } from '../entities/revenue-analytics.entity';

/**
 * Revenue Analytics Controller
 *
 * Handles revenue analytics endpoints for owners
 * All endpoints require OWNER role
 *
 * Base URL: /owner/revenue
 */
@ApiTags('Owner - Revenue Analytics')
@ApiBearerAuth('firebase-auth')
@UseGuards(AuthGuard, RolesGuard)
@Roles(UserRole.OWNER)
@Controller('owner/revenue')
export class RevenueController {
  constructor(private readonly revenueService: RevenueService) {}

  /**
   * GET /owner/revenue?period=today|week|month|year
   * Get revenue analytics for selected period
   *
   * REV-003
   */
  @Get()
  @ApiOperation({
    summary: 'Get revenue analytics',
    description:
      'Get detailed revenue analytics for selected period with KPIs, time-slot breakdown, and top products',
  })
  @ApiQuery({
    name: 'period',
    enum: RevenuePeriod,
    example: RevenuePeriod.TODAY,
    description: 'Period for revenue analytics',
    required: true,
  })
  @ApiResponse({
    status: 200,
    description: 'Revenue analytics retrieved successfully',
    type: RevenueAnalyticsDto,
    schema: {
      example: {
        success: true,
        data: {
          period: 'today',
          stats: [
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
          timeSlots: [
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
          topProducts: [
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
        },
        timestamp: '2026-01-28T10:30:00.000Z',
      },
    },
  })
  @ApiResponse({
    status: 400,
    description: 'Invalid period parameter',
    schema: {
      example: {
        success: false,
        error: {
          code: 'REV_001',
          message: 'Invalid period parameter. Must be one of: today, week, month, year',
        },
        timestamp: '2026-01-28T10:30:00.000Z',
      },
    },
  })
  @ApiResponse({
    status: 403,
    description: 'Access denied - Not an owner',
    schema: {
      example: {
        success: false,
        error: {
          code: 'REV_002',
          message: 'Access denied. Only shop owners can access revenue analytics',
        },
        timestamp: '2026-01-28T10:30:00.000Z',
      },
    },
  })
  @ApiResponse({
    status: 500,
    description: 'Internal server error',
    schema: {
      example: {
        success: false,
        error: {
          code: 'REV_500',
          message: 'Failed to retrieve revenue analytics',
        },
        timestamp: '2026-01-28T10:30:00.000Z',
      },
    },
  })
  async getRevenueAnalytics(
    @CurrentUser('uid') ownerId: string,
    @Query() queryDto: RevenueQueryDto,
  ): Promise<RevenueAnalyticsDto> {
    try {
      // Validate period parameter
      if (!Object.values(RevenuePeriod).includes(queryDto.period)) {
        throw new BadRequestException({
          code: 'REV_001',
          message: 'Invalid period parameter. Must be one of: today, week, month, year',
        });
      }

      // Call service
      const analytics = await this.revenueService.getRevenueAnalytics(ownerId, queryDto.period);

      // Map to DTO
      return new RevenueAnalyticsDto({
        period: analytics.period,
        stats: analytics.stats,
        timeSlots: analytics.timeSlots,
        topProducts: analytics.topProducts,
      });
    } catch (error) {
      console.error('DEBUG_ERROR:', error); // Handle known errors
      const err = error as any;

      if (err.response) {
        throw error;
      }

      throw new InternalServerErrorException({
        success: false,
        error: {
          code: 'REV_500',
          message: err.message || 'Failed to retrieve revenue analytics',
          details: err.details || null,
        },
        timestamp: new Date().toISOString(),
      });
    }
  }
}
