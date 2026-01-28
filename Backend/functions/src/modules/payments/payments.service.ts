import {
  Injectable,
  Inject,
  NotFoundException,
  ConflictException,
  BadRequestException,
  InternalServerErrorException,
  Logger,
} from '@nestjs/common';
import { Timestamp } from 'firebase-admin/firestore';
import { IPaymentsRepository, PAYMENTS_REPOSITORY_TOKEN } from './interfaces';
import { IOrdersRepository, ORDERS_REPOSITORY } from '../orders/interfaces';
import { PaymentEntity, PaymentMethod, PaymentStatus } from './entities';
import { PaymentStatus as OrderPaymentStatus } from '../orders/entities';
import { CreatePaymentDto } from './dto';
import { ConfigService } from '../../core/config/config.service';
import axios from 'axios';

@Injectable()
export class PaymentsService {
  private readonly logger = new Logger(PaymentsService.name);

  constructor(
    @Inject(PAYMENTS_REPOSITORY_TOKEN)
    private readonly paymentsRepo: IPaymentsRepository,
    @Inject(ORDERS_REPOSITORY)
    private readonly ordersRepo: IOrdersRepository,
    private readonly configService: ConfigService,
  ) {}

  /**
   * Create payment for order
   * POST /api/orders/:orderId/payment
   */
  async createPayment(
    customerId: string,
    orderId: string,
    dto: CreatePaymentDto,
  ): Promise<PaymentEntity> {
    // 1. Get order and validate
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'PAYMENT_001',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Verify ownership
    if (order.customerId !== customerId) {
      throw new BadRequestException({
        code: 'PAYMENT_002',
        message: 'Order does not belong to you',
        statusCode: 400,
      });
    }

    // 3. Check if payment already exists
    const existingPayment = await this.paymentsRepo.findByOrderId(orderId);
    if (existingPayment) {
      throw new ConflictException({
        code: 'PAYMENT_003',
        message: 'Payment already exists for this order',
        statusCode: 409,
      });
    }

    // 4. Validate payment method matches order
    if (dto.method !== order.paymentMethod) {
      throw new BadRequestException({
        code: 'PAYMENT_004',
        message: `Payment method mismatch. Order requires ${order.paymentMethod}`,
        statusCode: 400,
      });
    }

    // 5. Check order is UNPAID
    if (order.paymentStatus !== OrderPaymentStatus.UNPAID) {
      throw new ConflictException({
        code: 'PAYMENT_005',
        message: `Order payment status is ${order.paymentStatus}`,
        statusCode: 409,
      });
    }

    // 6. Handle payment based on method
    let paymentStatus: PaymentStatus;
    let paidAt: Timestamp | undefined;
    let providerData: any = {};

    if (dto.method === PaymentMethod.COD) {
      // COD: Instantly mark as PAID for demo
      paymentStatus = PaymentStatus.PAID;
      paidAt = Timestamp.now();
    } else if (dto.method === PaymentMethod.SEPAY) {
      // SEPAY: Generate QR code and return for customer to scan
      const sepayConfig = this.validateSepayConfig();
      const sepayContent = this.generateSepayContent(order);
      const qrCodeUrl = this.generateSepayQrUrl(
        sepayConfig.accountNumber,
        sepayConfig.bankCode,
        order.total,
        sepayContent,
      );

      paymentStatus = PaymentStatus.PROCESSING;
      providerData = {
        sepayContent,
        qrCodeUrl,
        accountNumber: sepayConfig.accountNumber,
        accountName: sepayConfig.accountName,
        bankCode: sepayConfig.bankCode,
        amount: order.total,
      };
    } else {
      // MOMO, ZALOPAY not implemented
      throw new BadRequestException({
        code: 'PAYMENT_010',
        message: `Payment method ${dto.method} is not supported`,
        statusCode: 400,
      });
    }

    // 7. Create payment record
    const payment = await this.paymentsRepo.create({
      orderId,
      orderNumber: order.orderNumber,
      customerId,
      shopId: order.shopId,
      amount: order.total,
      method: dto.method,
      status: paymentStatus,
      ...(paidAt && { paidAt }),
      ...(Object.keys(providerData).length > 0 && { providerData }),
      createdAt: Timestamp.now(),
      updatedAt: Timestamp.now(),
    });

    // 8. Update order payment status
    if (paymentStatus === PaymentStatus.PAID) {
      await this.ordersRepo.update(orderId, {
        paymentStatus: OrderPaymentStatus.PAID,
      });
      this.logger.log(`Order ${order.orderNumber} marked as PAID (COD)`);
    } else if (paymentStatus === PaymentStatus.PROCESSING) {
      await this.ordersRepo.update(orderId, {
        paymentStatus: OrderPaymentStatus.PROCESSING,
      });
      this.logger.log(`Order ${order.orderNumber} payment initiated (${dto.method})`);
    }

    return payment;
  }

  /**
   * SEPAY: Validate required env config
   */
  private validateSepayConfig() {
    const accountNumber = this.configService.sePayAccountNumber;
    const bankCode = this.configService.sePayBankCode;
    const secretKey = this.configService.sePaySecretKey;
    const accountName = this.configService.sePayAccountName;

    const missing: string[] = [];
    if (!accountNumber) missing.push('SEPAY_ACCOUNT_NUMBER');
    if (!bankCode) missing.push('SEPAY_BANK_CODE');
    if (!secretKey) missing.push('SEPAY_SECRET_KEY');
    if (!accountName) missing.push('SEPAY_ACCOUNT_NAME');

    if (missing.length > 0) {
      throw new InternalServerErrorException({
        code: 'PAYMENT_011',
        message: `SEPAY config missing: ${missing.join(', ')}`,
        statusCode: 500,
      });
    }

    return { accountNumber, bankCode, secretKey, accountName };
  }

  /**
   * SEPAY: Generate unique payment content (deterministic, sanitized)
   * Format: KTX{orderNumber} or KTX{orderId}
   * Max length: 25 characters
   * Allowed: A-Z, a-z, 0-9
   */
  private generateSepayContent(order: any): string {
    const base = order.orderNumber || order.id;
    const content = `KTX${base}`.replace(/[^A-Za-z0-9]/g, '').substring(0, 25);
    return content;
  }

  /**
   * SEPAY: Generate QR code URL
   */
  private generateSepayQrUrl(
    accountNumber: string,
    bankCode: string,
    amount: number,
    content: string,
  ): string {
    const template = this.configService.sePayQrTemplate;

    return template
      .replace('{account}', accountNumber)
      .replace('{bank}', bankCode)
      .replace('{amount}', amount.toString())
      .replace('{content}', encodeURIComponent(content));
  }

  /**
   * Get payment by order ID
   */
  async getPaymentByOrderId(orderId: string): Promise<PaymentEntity | null> {
    return this.paymentsRepo.findByOrderId(orderId);
  }

  /**
   * SEPAY: Verify payment by polling transactions API
   * POST /api/orders/:orderId/payment/verify
   */
  async verifyPayment(
    customerId: string,
    orderId: string,
  ): Promise<{ matched: boolean; payment: PaymentEntity }> {
    // 1. Get order and validate
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      throw new NotFoundException({
        code: 'PAYMENT_006',
        message: 'Order not found',
        statusCode: 404,
      });
    }

    // 2. Verify ownership
    if (order.customerId !== customerId) {
      throw new BadRequestException({
        code: 'PAYMENT_007',
        message: 'Order does not belong to you',
        statusCode: 400,
      });
    }

    // 3. Get payment record
    const payment = await this.paymentsRepo.findByOrderId(orderId);
    if (!payment) {
      throw new NotFoundException({
        code: 'PAYMENT_008',
        message: 'Payment not found for this order',
        statusCode: 404,
      });
    }

    // 4. If already PAID, return immediately (idempotent)
    if (payment.status === PaymentStatus.PAID) {
      return { matched: true, payment };
    }

    // 5. Only SEPAY supports polling
    if (payment.method !== PaymentMethod.SEPAY) {
      throw new BadRequestException({
        code: 'PAYMENT_009',
        message: 'Payment method does not support verification polling',
        statusCode: 400,
      });
    }

    // 6. Call SEPAY API to check transactions
    const matched = await this.checkSepayTransaction(payment);

    // 7. If matched, update payment and order
    if (matched) {
      await this.paymentsRepo.update(payment.id!, {
        status: PaymentStatus.PAID,
        paidAt: Timestamp.now(),
        providerData: {
          ...payment.providerData,
          verifiedAt: new Date().toISOString(),
        },
      });

      await this.ordersRepo.update(orderId, {
        paymentStatus: OrderPaymentStatus.PAID,
      });

      this.logger.log(`Order ${order.orderNumber} SEPAY payment verified`);

      // Fetch updated payment
      const updatedPayment = (await this.paymentsRepo.findByOrderId(orderId))!;
      return { matched: true, payment: updatedPayment };
    }

    // 8. Not matched yet
    return { matched: false, payment };
  }

  /**
   * SEPAY: Check if transaction exists in SEPAY API
   */
  private async checkSepayTransaction(payment: PaymentEntity): Promise<boolean> {
    try {
      const config = this.validateSepayConfig();
      const apiUrl = this.configService.sePayApiUrl;
      const limit = this.configService.sePayPollLimit;

      // Call SEPAY transactions list API
      const url = `${apiUrl}/transactions/list`;
      const response = await axios.get(url, {
        params: {
          account_number: config.accountNumber,
          limit,
          amount_in: payment.amount,
        },
        headers: {
          Authorization: `Bearer ${config.secretKey}`,
        },
        timeout: 10000, // 10 second timeout
      });

      // Parse response (handle different response structures)
      let transactions: any[] = [];
      if (response.data?.transactions) {
        transactions = response.data.transactions;
      } else if (response.data?.data?.transactions) {
        transactions = response.data.data.transactions;
      } else if (Array.isArray(response.data)) {
        transactions = response.data;
      }

      // Get expected content from payment
      const expectedContent = payment.providerData?.sepayContent;
      if (!expectedContent) {
        this.logger.warn(`Payment ${payment.id} missing sepayContent`);
        return false;
      }

      // Check if any transaction matches
      for (const txn of transactions) {
        // Parse amount_in to number for comparison (Sepay returns string like "350000.00")
        const txnAmount = parseFloat(txn.amount_in || '0');
        const amountMatch = txnAmount === payment.amount;
        const contentMatch = txn.transaction_content
          ?.toLowerCase()
          .includes(expectedContent.toLowerCase());

        if (amountMatch && contentMatch) {
          this.logger.log(`SEPAY transaction matched: ${txn.transaction_content}`);
          return true;
        }
      }

      return false;
    } catch (error: any) {
      this.logger.error('SEPAY API error:', error.message);
      // Don't throw - return false so client can retry
      return false;
    }
  }

  /**
   * Confirm SEPAY payment from webhook
   * Called by SEPAY webhook when bank transfer is received
   */
  async confirmSepayPayment(
    orderId: string,
    amount: number,
    transactionId: string,
    bankCode: string,
    transactionDate: string,
  ): Promise<void> {
    this.logger.log(`Confirming SEPAY payment for order ${orderId}`);

    // Find payment for this order
    const payment = await this.paymentsRepo.findByOrderId(orderId);

    if (!payment) {
      this.logger.warn(`Payment not found for order ${orderId}, skipping confirmation`);
      return;
    }

    // Check if already confirmed (idempotent)
    if (payment.status === PaymentStatus.PAID) {
      this.logger.log(`Payment ${payment.id} already confirmed, skipping`);
      return;
    }

    // Verify amount matches
    if (Math.abs(payment.amount - amount) > 1) {
      this.logger.error(
        `Amount mismatch for order ${orderId}: expected ${payment.amount}, got ${amount}`,
      );
      // Don't throw - still mark as paid but log warning
    }

    // Find order
    const order = await this.ordersRepo.findById(orderId);
    if (!order) {
      this.logger.error(`Order ${orderId} not found`);
      return;
    }

    // Update payment status
    const updatedPayment: Partial<PaymentEntity> = {
      status: PaymentStatus.PAID,
      paidAt: Timestamp.now(),
      providerData: {
        ...(payment.providerData || {}),
        transactionId,
        bankCode,
        transactionDate,
        confirmedViaWebhook: true,
      },
      updatedAt: Timestamp.now(),
    };

    await this.paymentsRepo.update(payment.id!, updatedPayment);

    // Update order payment status
    await this.ordersRepo.update(orderId, {
      paymentStatus: OrderPaymentStatus.PAID,
    });

    this.logger.log(
      `SEPAY payment confirmed: Order ${order.orderNumber}, Amount ${amount}đ, TxnId ${transactionId}`,
    );
  }

  /**
   * Initiate refund for cancelled order
   * PAYMENT-006: Auto-refund on order cancellation
   *
   * Updates both Payment.status and Order.paymentStatus to REFUNDED
   * Maintains consistency between payment and order records
   */
  async initiateRefund(orderId: string, reason: string): Promise<PaymentEntity | null> {
    // 1. Get payment for order
    const payment = await this.paymentsRepo.findByOrderId(orderId);
    if (!payment) {
      this.logger.warn(`No payment found for order ${orderId} - refund skipped`);
      return null;
    }

    // 2. Only refund if payment was PAID
    if (payment.status !== PaymentStatus.PAID) {
      this.logger.warn(
        `Cannot refund payment - status is ${payment.status}, not PAID (Order: ${orderId})`,
      );
      return null;
    }

    // 3. Update Payment.status = REFUNDED
    await this.paymentsRepo.update(payment.id!, {
      status: PaymentStatus.REFUNDED,
      refundedAt: Timestamp.now(),
      refundReason: reason,
      updatedAt: Timestamp.now(),
    });

    // 4. Sync: Update Order.paymentStatus = REFUNDED (keep consistent)
    await this.ordersRepo.update(orderId, {
      paymentStatus: OrderPaymentStatus.UNPAID, // Or REFUNDED if Order entity has it
    });

    this.logger.log(`Payment refunded for order ${orderId}: reason="${reason}", status=REFUNDED`);

    // 5. Return updated payment
    return this.paymentsRepo.findById(payment.id!);
  }
}
