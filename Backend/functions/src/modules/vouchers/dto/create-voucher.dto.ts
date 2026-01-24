import {
  IsString,
  IsNotEmpty,
  IsNumber,
  Min,
  IsEnum,
  IsOptional,
  Length,
  Matches,
  IsISO8601,
  ValidateIf,
  Max,
} from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Transform } from 'class-transformer';
import { VoucherType } from '../entities';

export class CreateVoucherDto {
  @ApiProperty({
    example: 'FREESHIP50',
    description: 'Voucher code (6-10 uppercase alphanumeric)',
  })
  @IsString()
  @IsNotEmpty({ message: 'Mã voucher không được để trống' })
  @Length(6, 10, { message: 'Mã voucher phải từ 6-10 ký tự' })
  @Matches(/^[A-Z0-9]+$/, { message: 'Mã voucher chỉ chứa chữ hoa và số' })
  @Transform(({ value }) => value?.toUpperCase())
  code: string;

  @ApiProperty({
    enum: VoucherType,
    example: VoucherType.PERCENTAGE,
    description: 'Voucher type (PERCENTAGE | FIXED_AMOUNT | FREE_SHIP)',
  })
  @IsEnum(VoucherType, { message: 'Loại voucher không hợp lệ' })
  type: VoucherType;

  @ApiProperty({
    example: 50,
    description: 'Discount value (% for PERCENTAGE/FREE_SHIP, amount for FIXED_AMOUNT)',
  })
  @IsNumber()
  @Min(0.01, { message: 'Giá trị phải lớn hơn 0' })
  @ValidateIf((o) => o.type === VoucherType.PERCENTAGE || o.type === VoucherType.FREE_SHIP)
  @Max(100, { message: 'Giá trị phần trăm không được vượt quá 100' })
  value: number;

  @ApiProperty({
    example: 20000,
    description: 'Maximum discount amount (REQUIRED for PERCENTAGE, optional for FIXED_AMOUNT/FREE_SHIP)',
  })
  @ValidateIf((o) => o.type === VoucherType.PERCENTAGE)
  @IsNotEmpty({ message: 'maxDiscount là bắt buộc cho loại voucher PERCENTAGE' })
  @IsNumber()
  @Min(0, { message: 'Giá trị giảm tối đa phải >= 0' })
  @ValidateIf((o) => o.type !== VoucherType.PERCENTAGE)
  @IsOptional()
  maxDiscount?: number;

  @ApiPropertyOptional({
    example: 30000,
    description: 'Minimum order amount to apply voucher',
  })
  @IsNumber()
  @Min(0, { message: 'Giá trị đơn tối thiểu phải >= 0' })
  @IsOptional()
  minOrderAmount?: number;

  @ApiProperty({
    example: 100,
    description: 'Total usage limit',
  })
  @IsNumber()
  @Min(1, { message: 'Số lượt sử dụng phải >= 1' })
  usageLimit: number;

  @ApiProperty({
    example: 1,
    description: 'Usage limit per user',
  })
  @IsNumber()
  @Min(1, { message: 'Số lượt mỗi người phải >= 1' })
  usageLimitPerUser: number;

  @ApiProperty({
    example: '2026-01-21T00:00:00Z',
    description: 'Valid from date (ISO 8601)',
  })
  @IsISO8601({}, { message: 'Ngày bắt đầu không đúng định dạng ISO 8601' })
  validFrom: string;

  @ApiProperty({
    example: '2026-12-31T23:59:59Z',
    description: 'Valid to date (ISO 8601)',
  })
  @IsISO8601({}, { message: 'Ngày kết thúc không đúng định dạng ISO 8601' })
  validTo: string;

  @ApiPropertyOptional({
    example: 'Miễn phí ship 50%',
    description: 'Display name',
  })
  @IsString()
  @IsOptional()
  @Length(1, 100, { message: 'Tên tối đa 100 ký tự' })
  name?: string;

  @ApiPropertyOptional({
    example: 'Giảm 50% phí vận chuyển cho đơn từ 30k',
    description: 'Description',
  })
  @IsString()
  @IsOptional()
  @Length(1, 500, { message: 'Mô tả tối đa 500 ký tự' })
  description?: string;
}
