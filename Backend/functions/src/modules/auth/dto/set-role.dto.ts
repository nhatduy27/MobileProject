import { IsEnum, IsNotEmpty } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';
import { UserRole } from '../../../core/interfaces/user.interface';

/**
 * DTO for setting user role
 */
export class SetRoleDto {
  @ApiProperty({
    description: 'User role',
    enum: UserRole,
    example: UserRole.CUSTOMER,
  })
  @IsEnum(UserRole, { message: 'Role must be a valid UserRole' })
  @IsNotEmpty({ message: 'Role is required' })
  role: UserRole;
}

/**
 * Response DTO for set role
 */
export class SetRoleResponseDto {
  @ApiProperty({ example: 'Role updated successfully' })
  message: string;

  @ApiProperty({ example: 'CUSTOMER' })
  role: UserRole;
}
