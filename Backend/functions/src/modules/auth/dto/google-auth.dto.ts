import { IsNotEmpty, IsString } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

/**
 * DTO for Google Sign-In authentication
 *
 * Client sends the Google ID Token received from Google Sign-In SDK.
 * Backend verifies this token with Firebase Auth.
 */
export class GoogleAuthDto {
  @ApiProperty({
    description: 'Google ID Token from Google Sign-In SDK',
    example: 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...',
  })
  @IsNotEmpty({ message: 'ID Token không được để trống' })
  @IsString({ message: 'ID Token phải là chuỗi' })
  idToken: string;
}
