import { Controller, Post, Get, Param, Body, UseGuards, HttpCode, HttpStatus } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation, ApiParam } from '@nestjs/swagger';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { CurrentUser } from '../../../core/decorators/current-user.decorator';
import { UserRole } from '../../users/entities/user.entity';
import { PaymentsService } from '../payments.service';
import { CreatePaymentDto } from '../dto';

@ApiTags('Payments')
@ApiBearerAuth('firebase-auth')
@Controller('orders')
@UseGuards(AuthGuard, RolesGuard)
export class PaymentsController {
  constructor(private readonly paymentsService: PaymentsService) {}

  /**
   * Create payment for order
   * POST /api/orders/:orderId/payment
   */
  @Post(':orderId/payment')
  @ApiOperation({ summary: 'Create payment for order' })
  @ApiParam({ name: 'orderId', description: 'Order ID' })
  @Roles(UserRole.CUSTOMER)
  @HttpCode(HttpStatus.CREATED)
  async createPayment(
    @CurrentUser('uid') customerId: string,
    @Param('orderId') orderId: string,
    @Body() dto: CreatePaymentDto,
  ) {
    const payment = await this.paymentsService.createPayment(customerId, orderId, dto);

    return {
      message: 'Payment created successfully',
      payment: {
        id: payment.id,
        orderId: payment.orderId,
        amount: payment.amount,
        method: payment.method,
        status: payment.status,
        ...(payment.providerData && { providerData: payment.providerData }),
        createdAt: payment.createdAt,
      },
    };
  }

  /**
   * Get payment info for order (includes QR code if SEPAY)
   * GET /api/orders/:orderId/payment
   */
  @Get(':orderId/payment')
  @ApiOperation({ summary: 'Get payment info (includes QR code for SEPAY)' })
  @ApiParam({ name: 'orderId', description: 'Order ID' })
  @Roles(UserRole.CUSTOMER)
  @HttpCode(HttpStatus.OK)
  async getPayment(
    @CurrentUser('uid') customerId: string,
    @Param('orderId') orderId: string,
  ) {
    const payment = await this.paymentsService.getPaymentForCustomer(customerId, orderId);

    return {
      message: 'Payment retrieved successfully',
      payment: {
        id: payment.id,
        orderId: payment.orderId,
        amount: payment.amount,
        method: payment.method,
        status: payment.status,
        ...(payment.providerData && { providerData: payment.providerData }),
        ...(payment.paidAt && { paidAt: payment.paidAt }),
        createdAt: payment.createdAt,
      },
    };
  }

  /**
   * Verify SEPAY payment (polling endpoint)
   * POST /api/orders/:orderId/payment/verify
   */
  @Post(':orderId/payment/verify')
  @ApiOperation({ summary: 'Verify SEPAY payment (polling)' })
  @ApiParam({ name: 'orderId', description: 'Order ID' })
  @Roles(UserRole.CUSTOMER)
  @HttpCode(HttpStatus.OK)
  async verifyPayment(@CurrentUser('uid') customerId: string, @Param('orderId') orderId: string) {
    const result = await this.paymentsService.verifyPayment(customerId, orderId);

    return {
      matched: result.matched,
      message: result.matched ? 'Payment verified successfully' : 'Payment not yet confirmed',
      payment: {
        id: result.payment.id,
        status: result.payment.status,
        method: result.payment.method,
        amount: result.payment.amount,
        ...(result.payment.paidAt && { paidAt: result.payment.paidAt }),
      },
    };
  }
}
