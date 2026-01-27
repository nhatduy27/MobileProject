import {
  Controller,
  Post,
  Body,
  HttpCode,
  HttpStatus,
  Logger,
} from '@nestjs/common';
import { PaymentsService } from '../payments.service';

/**
 * Webhooks Controller
 * Handle payment notifications from SEPAY
 */
@Controller('webhooks')
export class WebhooksController {
  private readonly logger = new Logger(WebhooksController.name);

  constructor(private readonly paymentsService: PaymentsService) {}

  /**
   * SEPAY Webhook
   * POST /api/webhooks/sepay
   * 
   * Receive bank transfer notifications from SEPAY
   * 
   * Request body example:
   * {
   *   "id": "txn_xxx",
   *   "gateway": "MB",
   *   "transactionDate": "2026-01-27 10:00:00",
   *   "accountNumber": "00012112005000",
   *   "transferType": "in",
   *   "transferAmount": 70000,
   *   "content": "PAY order_abc123",
   *   "code": null
   * }
   */
  @Post('sepay')
  @HttpCode(HttpStatus.OK)
  async handleSepayWebhook(@Body() body: any) {
    this.logger.log(`SEPAY webhook received: ${JSON.stringify(body)}`);

    try {
      // Extract data from webhook
      const {
        id: transactionId,
        gateway: bankCode,
        transactionDate,
        transferAmount,
        content,
      } = body;

      // Validate required fields
      if (!transactionId || !transferAmount || !content) {
        this.logger.warn('SEPAY webhook missing required fields');
        return { success: false, message: 'Missing required fields' };
      }

      // Extract order ID from content (format: "PAY order_abc123" or "PAY_order_abc123")
      const contentUpper = content.toUpperCase();
      let orderId: string | null = null;

      if (contentUpper.includes('PAY')) {
        const match = content.match(/PAY[_\s]+(\w+)/i);
        if (match && match[1]) {
          orderId = match[1];
        }
      }

      if (!orderId) {
        this.logger.warn(`SEPAY webhook: Could not extract order ID from content: ${content}`);
        return { success: false, message: 'Invalid payment content format' };
      }

      this.logger.log(
        `Processing SEPAY payment: Order ${orderId}, Amount ${transferAmount}đ, TxnId ${transactionId}`,
      );

      // Process payment confirmation
      await this.paymentsService.confirmSepayPayment(
        orderId,
        transferAmount,
        transactionId,
        bankCode,
        transactionDate,
      );

      this.logger.log(`SEPAY payment confirmed for order ${orderId}`);

      return { success: true, message: 'Payment confirmed' };
    } catch (error) {
      this.logger.error('SEPAY webhook processing failed:', error);
      // Return success to prevent SEPAY retry, but log error
      return { success: true, message: 'Received' };
    }
  }
}
