import { IsString, IsNumber, IsOptional, Min } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class RequestPayoutDto {
  @ApiProperty({
    description: 'Số tiền muốn rút (VNĐ)',
    example: 500000,
    minimum: 50000,
  })
  @IsNumber()
  @Min(50000, { message: 'Số tiền rút tối thiểu 50,000đ' })
  amount: number;

  @ApiProperty({
    description: 'Mã ngân hàng',
    example: 'VCB',
  })
  @IsString()
  bankCode: string;

  @ApiProperty({
    description: 'Số tài khoản ngân hàng',
    example: '00012112005000',
  })
  @IsString()
  accountNumber: string;

  @ApiProperty({
    description: 'Tên chủ tài khoản',
    example: 'NGUYEN VAN A',
  })
  @IsString()
  accountName: string;

  @ApiProperty({
    description: 'Ghi chú (optional)',
    example: 'Rút tiền tuần này',
    required: false,
  })
  @IsString()
  @IsOptional()
  note?: string;
}
