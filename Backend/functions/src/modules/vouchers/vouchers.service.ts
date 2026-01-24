import {
  Injectable,
  Inject,
  NotFoundException,
  BadRequestException,
  ConflictException,
} from '@nestjs/common';
import { IVouchersRepository } from './interfaces';
import { VoucherEntity, VoucherType } from './entities';
import {
  CreateVoucherDto,
  UpdateVoucherDto,
  UpdateVoucherStatusDto,
  ValidateVoucherDto,
} from './dto';
import { ErrorCodes } from '../../shared/constants/error-codes';

@Injectable()
export class VouchersService {
  constructor(
    @Inject('VOUCHERS_REPOSITORY')
    private readonly vouchersRepository: IVouchersRepository,
  ) {}

  // ==================== Owner Operations ====================

  /**
   * Create voucher (OWNER only)
   * Route: POST /owner/vouchers
   */
  async createVoucher(shopId: string, dto: CreateVoucherDto): Promise<VoucherEntity> {
    // Check uniqueness: shopId + code
    const existing = await this.vouchersRepository.findByShopAndCode(shopId, dto.code);
    if (existing) {
      throw new ConflictException({
        code: ErrorCodes.VOUCHER_CODE_EXISTS,
        message: 'Mã voucher đã tồn tại trong shop',
        statusCode: 409,
      });
    }

    // Validate date range
    const validFrom = new Date(dto.validFrom);
    const validTo = new Date(dto.validTo);
    if (validTo <= validFrom) {
      throw new BadRequestException({
        code: ErrorCodes.VOUCHER_INVALID_DATE_RANGE,
        message: 'Ngày kết thúc phải sau ngày bắt đầu',
        statusCode: 400,
      });
    }

    // Validate maxDiscount for PERCENTAGE type
    if (dto.type === VoucherType.PERCENTAGE && dto.maxDiscount && dto.maxDiscount <= 0) {
      throw new BadRequestException('maxDiscount phải > 0 khi type là PERCENTAGE');
    }

    // Normalize payload: remove maxDiscount for FIXED_AMOUNT/FREE_SHIP
    const normalizedDto = this.normalizeVoucherPayload(dto);

    return await this.vouchersRepository.create(shopId, normalizedDto);
  }

  /**
   * Get all vouchers of shop (OWNER only)
   * Route: GET /owner/vouchers
   */
  async getMyVouchers(shopId: string, filters?: { isActive?: string }): Promise<VoucherEntity[]> {
    const isActiveBoolean =
      filters?.isActive === 'true' ? true : filters?.isActive === 'false' ? false : undefined;

    return await this.vouchersRepository.findByShopId(shopId, {
      isActive: isActiveBoolean,
      orderBy: 'createdAt',
      orderDir: 'desc',
    });
  }

  /**
   * Update voucher (OWNER only)
   * Route: PUT /owner/vouchers/:id
   */
  async updateVoucher(shopId: string, voucherId: string, dto: UpdateVoucherDto): Promise<void> {
    const voucher = await this.getVoucherById(voucherId);

    // Check ownership
    if (voucher.shopId !== shopId) {
      throw new NotFoundException({
        code: ErrorCodes.VOUCHER_NOT_FOUND,
        message: 'Voucher không tồn tại hoặc bạn không có quyền',
        statusCode: 404,
      });
    }

    // Validate validTo if provided
    if (dto.validTo) {
      const newValidTo = new Date(dto.validTo);
      const validFrom = new Date(voucher.validFrom);
      if (newValidTo <= validFrom) {
        throw new BadRequestException({
          code: ErrorCodes.VOUCHER_INVALID_DATE_RANGE,
          message: 'Ngày kết thúc phải sau ngày bắt đầu',
          statusCode: 400,
        });
      }
    }

    // Normalize payload (though UpdateVoucherDto doesn't have maxDiscount, be defensive)
    const normalizedDto = this.normalizeVoucherPayload(dto);

    await this.vouchersRepository.update(voucherId, normalizedDto);
  }

  /**
   * Update voucher status (OWNER only)
   * Route: PUT /owner/vouchers/:id/status
   */
  async updateVoucherStatus(
    shopId: string,
    voucherId: string,
    dto: UpdateVoucherStatusDto,
  ): Promise<void> {
    const voucher = await this.getVoucherById(voucherId);

    // Check ownership
    if (voucher.shopId !== shopId) {
      throw new NotFoundException({
        code: ErrorCodes.VOUCHER_NOT_FOUND,
        message: 'Voucher không tồn tại hoặc bạn không có quyền',
        statusCode: 404,
      });
    }

    await this.vouchersRepository.update(voucherId, { isActive: dto.isActive });
  }

  /**
   * Delete voucher (OWNER only)
   * Route: DELETE /owner/vouchers/:id
   */
  async deleteVoucher(shopId: string, voucherId: string): Promise<void> {
    const voucher = await this.getVoucherById(voucherId);

    // Check ownership
    if (voucher.shopId !== shopId) {
      throw new NotFoundException({
        code: ErrorCodes.VOUCHER_NOT_FOUND,
        message: 'Voucher không tồn tại hoặc bạn không có quyền',
        statusCode: 404,
      });
    }

    await this.vouchersRepository.delete(voucherId);
  }

  // ==================== Customer Operations ====================

  /**
   * Get available vouchers for customer
   * Route: GET /vouchers?shopId=xxx
   * @param shopId - Shop ID to fetch vouchers for
   * @param userId - Current user ID for per-user usage tracking (optional, for anonymous requests)
   */
  async getAvailableVouchers(shopId: string, userId?: string): Promise<any[]> {
    const now = new Date().toISOString();

    const allVouchers = await this.vouchersRepository.findByShopId(shopId, {
      isActive: true,
      orderBy: 'validTo',
      orderDir: 'asc',
    });

    // Filter valid time range + not fully used
    const validVouchers = allVouchers.filter((v) => {
      const isInTimeRange = v.validFrom <= now && v.validTo >= now;
      const hasUsageLeft = v.currentUsage < v.usageLimit;
      return isInTimeRange && hasUsageLeft;
    });

    // If no user context, return vouchers without per-user usage info
    if (!userId) {
      return validVouchers;
    }

    // Batch fetch per-user usage counts (avoid N+1)
    const voucherIds = validVouchers.map((v) => v.id);
    const userUsageCounts = await this.vouchersRepository.countUsageByUserBatch(voucherIds, userId);

    // Enrich each voucher with per-user usage information
    return validVouchers.map((voucher) => ({
      ...voucher,
      myUsageCount: userUsageCounts[voucher.id] ?? 0,
      myRemainingUses: Math.max(0, voucher.usageLimitPerUser - (userUsageCounts[voucher.id] ?? 0)),
    }));
  }

  /**
   * Validate voucher for customer (preview discount)
   * Route: POST /vouchers/validate
   */
  async validateVoucher(
    userId: string,
    dto: ValidateVoucherDto,
  ): Promise<{
    valid: boolean;
    voucherId: string;
    discountAmount: number;
    errorCode?: string;
    errorMessage?: string;
  }> {
    // Find voucher by shop + code
    const voucher = await this.vouchersRepository.findByShopAndCode(dto.shopId, dto.code);
    if (!voucher) {
      return {
        valid: false,
        voucherId: '',
        discountAmount: 0,
        errorCode: ErrorCodes.VOUCHER_NOT_FOUND,
        errorMessage: 'Mã voucher không tồn tại',
      };
    }

    // Check isActive
    if (!voucher.isActive) {
      return {
        valid: false,
        voucherId: voucher.id,
        discountAmount: 0,
        errorCode: ErrorCodes.VOUCHER_INACTIVE,
        errorMessage: 'Voucher đã bị vô hiệu hóa',
      };
    }

    // Check time range
    const now = new Date().toISOString();
    if (now < voucher.validFrom) {
      return {
        valid: false,
        voucherId: voucher.id,
        discountAmount: 0,
        errorCode: ErrorCodes.VOUCHER_NOT_STARTED,
        errorMessage: 'Voucher chưa có hiệu lực',
      };
    }
    if (now > voucher.validTo) {
      return {
        valid: false,
        voucherId: voucher.id,
        discountAmount: 0,
        errorCode: ErrorCodes.VOUCHER_EXPIRED,
        errorMessage: 'Voucher đã hết hạn',
      };
    }

    // Check usage limit
    if (voucher.currentUsage >= voucher.usageLimit) {
      return {
        valid: false,
        voucherId: voucher.id,
        discountAmount: 0,
        errorCode: ErrorCodes.VOUCHER_TOTAL_LIMIT_REACHED,
        errorMessage: 'Voucher đã hết lượt sử dụng',
      };
    }

    // Check user-specific limit
    const userUsageCount = await this.vouchersRepository.countUsageByUser(voucher.id, userId);
    if (userUsageCount >= voucher.usageLimitPerUser) {
      return {
        valid: false,
        voucherId: voucher.id,
        discountAmount: 0,
        errorCode: ErrorCodes.VOUCHER_USER_LIMIT_REACHED,
        errorMessage: 'Bạn đã sử dụng hết lượt cho voucher này',
      };
    }

    // Check min order amount
    if (voucher.minOrderAmount && dto.subtotal < voucher.minOrderAmount) {
      return {
        valid: false,
        voucherId: voucher.id,
        discountAmount: 0,
        errorCode: ErrorCodes.VOUCHER_MIN_ORDER_NOT_MET,
        errorMessage: `Đơn hàng tối thiểu ${voucher.minOrderAmount}đ`,
      };
    }

    // Calculate discount
    let discountAmount = 0;
    if (voucher.type === VoucherType.FIXED_AMOUNT) {
      discountAmount = voucher.value;
    } else if (voucher.type === VoucherType.PERCENTAGE) {
      discountAmount = Math.round((dto.subtotal * voucher.value) / 100);
      if (voucher.maxDiscount && discountAmount > voucher.maxDiscount) {
        discountAmount = voucher.maxDiscount;
      }
    } else if (voucher.type === VoucherType.FREE_SHIP) {
      if (!dto.shipFee) {
        return {
          valid: false,
          voucherId: voucher.id,
          discountAmount: 0,
          errorCode: ErrorCodes.VOUCHER_NOT_APPLICABLE,
          errorMessage: 'FREE_SHIP voucher yêu cầu shipFee',
        };
      }
      discountAmount = Math.round((dto.shipFee * voucher.value) / 100);
    }

    return {
      valid: true,
      voucherId: voucher.id,
      discountAmount,
    };
  }

  /**
   * Apply voucher atomically (called from OrdersService in transaction)
   * This is NOT an endpoint - it's a service method for internal use
   */
  async applyVoucherAtomic(
    voucherId: string,
    userId: string,
    orderId: string,
    discountAmount: number,
  ): Promise<VoucherEntity> {
    return await this.vouchersRepository.applyVoucherAtomic(
      voucherId,
      userId,
      orderId,
      discountAmount,
    );
  }

  // ==================== Helper Methods ====================

  /**
   * Normalize voucher payload by type:
   * - PERCENTAGE: keep maxDiscount (required)
   * - FIXED_AMOUNT/FREE_SHIP: remove maxDiscount (optional, ignored)
   *
   * This ensures clean Firestore documents without unnecessary fields
   */
  private normalizeVoucherPayload(
    dto: CreateVoucherDto | Partial<CreateVoucherDto>,
  ): CreateVoucherDto | Partial<CreateVoucherDto> {
    if (dto.type === VoucherType.FIXED_AMOUNT || dto.type === VoucherType.FREE_SHIP) {
      // Remove maxDiscount for non-PERCENTAGE types
      const { maxDiscount, ...rest } = dto;
      return rest;
    }

    // For PERCENTAGE, keep as-is (maxDiscount is required anyway)
    return dto;
  }

  private async getVoucherById(voucherId: string): Promise<VoucherEntity> {
    const voucher = await this.vouchersRepository.findById(voucherId);
    if (!voucher || voucher.isDeleted) {
      throw new NotFoundException({
        code: ErrorCodes.VOUCHER_NOT_FOUND,
        message: 'Voucher không tồn tại',
        statusCode: 404,
      });
    }
    return voucher;
  }
}
