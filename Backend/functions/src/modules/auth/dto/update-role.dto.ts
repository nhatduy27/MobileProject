import { IsIn, IsNotEmpty, IsString } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

/**
 * DTO for updating user role after registration
 *
 * Used in Role Selection screen.
 */
export class UpdateRoleDto {
  @ApiProperty({
    description: 'User role: user (customer), seller, or delivery (shipper)',
    example: 'user',
    enum: ['user', 'seller', 'delivery'],
  })
  @IsNotEmpty({ message: 'Vai trò không được để trống' })
  @IsString({ message: 'Vai trò phải là chuỗi' })
  @IsIn(['user', 'seller', 'delivery'], {
    message: 'Vai trò phải là: user, seller, hoặc delivery',
  })
  role: 'user' | 'seller' | 'delivery';
}
