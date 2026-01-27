import {
  Controller,
  Post,
  Param,
  Body,
  UseGuards,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import { AuthGuard } from '../../../core/guards/auth.guard';
import { RolesGuard } from '../../../core/guards/roles.guard';
import { Roles } from '../../../core/decorators/roles.decorator';
import { CurrentUser } from '../../../core/decorators/current-user.decorator';
import { UserRole } from '../../users/entities/user.entity';
import { PaymentsService } from '../payments.service';
import { CreatePaymentDto } from '../dto';

@Controller('orders')
@UseGuards(AuthGuard, RolesGuard)
export class PaymentsController {
  constructor(private readonly paymentsService: PaymentsService) {}

  /**
   * Create payment for order
   * POST /api/orders/:orderId/payment
   */
  @Post(':orderId/payment')
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
   * Verify SEPAY payment (polling endpoint)
   * POST /api/orders/:orderId/payment/verify
   */
  @Post(':orderId/payment/verify')
  @Roles(UserRole.CUSTOMER)
  @HttpCode(HttpStatus.OK)
  async verifyPayment(
    @CurrentUser('uid') customerId: string,
    @Param('orderId') orderId: string,
  ) {
    const result = await this.paymentsService.verifyPayment(customerId, orderId);
    
    return {
      matched: result.matched,
      message: result.matched 
        ? 'Payment verified successfully' 
        : 'Payment not yet confirmed',
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
