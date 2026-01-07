import {
  Controller,
  Post,
  Get,
  Put,
  Delete,
  Body,
  UseGuards,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
} from '@nestjs/swagger';
import { AuthService } from './auth.service';
import {
  RegisterDto,
  LoginDto,
  GoogleAuthDto,
  UpdateRoleDto,
  UpdateProfileDto,
} from './dto';
import { AuthGuard } from '../../core/guards/auth.guard';
import { CurrentUser } from '../../core/decorators/current-user.decorator';
import { Public } from '../../core/decorators/public.decorator';
import { IUser } from '../../core/interfaces/user.interface';

/**
 * Auth Controller
 *
 * Handles authentication endpoints:
 * - POST /auth/register - Register new user
 * - POST /auth/login - Verify login token
 * - POST /auth/google - Google Sign-In
 * - GET /auth/profile - Get current user profile
 * - PUT /auth/profile - Update profile
 * - PUT /auth/role - Update user role
 * - DELETE /auth/account - Delete account
 */
@ApiTags('Auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  /**
   * Register new user with email/password
   *
   * Creates Firebase Auth user and Firestore profile.
   * After registration, user should verify email/OTP then select role.
   */
  @Post('register')
  @Public()
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Đăng ký tài khoản mới' })
  @ApiResponse({
    status: 201,
    description: 'Đăng ký thành công',
    schema: {
      example: {
        success: true,
        data: {
          user: {
            id: 'abc123',
            fullName: 'Nguyễn Văn A',
            email: 'user@example.com',
            isVerify: false,
            phone: '',
            role: 'user',
            imageAvatar: '',
            createdAt: 1704700000000,
            updatedAt: 1704700000000,
          },
          uid: 'abc123',
        },
      },
    },
  })
  @ApiResponse({ status: 400, description: 'Dữ liệu không hợp lệ' })
  @ApiResponse({ status: 409, description: 'Email đã được sử dụng' })
  async register(@Body() dto: RegisterDto) {
    const result = await this.authService.register(dto);
    return {
      success: true,
      message: 'Đăng ký thành công',
      data: result,
    };
  }

  /**
   * Verify login token
   *
   * Client logs in with Firebase Auth SDK, then calls this endpoint
   * with the ID token to verify and get user profile.
   */
  @Post('login')
  @Public()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Xác thực đăng nhập' })
  @ApiResponse({
    status: 200,
    description: 'Đăng nhập thành công',
    schema: {
      example: {
        success: true,
        data: {
          user: {
            id: 'abc123',
            fullName: 'Nguyễn Văn A',
            email: 'user@example.com',
            isVerify: true,
            phone: '0901234567',
            role: 'user',
            imageAvatar: '',
            createdAt: 1704700000000,
            updatedAt: 1704700000000,
          },
        },
      },
    },
  })
  @ApiResponse({ status: 401, description: 'Token không hợp lệ' })
  @ApiResponse({ status: 404, description: 'Không tìm thấy người dùng' })
  async login(@Body() _dto: LoginDto) {
    // Note: Client should use Firebase Auth SDK to login first,
    // then send the ID token here. This endpoint verifies the token
    // and returns the user profile.
    //
    // For now, we'll treat this as a "verify token" endpoint.
    // The actual email/password login happens on client with Firebase SDK.
    //
    // If you want backend-only login, you'd need Firebase Auth REST API.
    return {
      success: true,
      message:
        'Sử dụng Firebase Auth SDK để đăng nhập, sau đó gọi /auth/verify-token với ID token',
      data: null,
    };
  }

  /**
   * Verify Firebase ID Token and get user profile
   *
   * Called after client login with Firebase Auth SDK.
   */
  @Post('verify-token')
  @Public()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Xác thực ID Token và lấy thông tin user' })
  @ApiResponse({ status: 200, description: 'Xác thực thành công' })
  @ApiResponse({ status: 401, description: 'Token không hợp lệ' })
  async verifyToken(@Body() dto: GoogleAuthDto) {
    // Reuse GoogleAuthDto since it has idToken field
    const result = await this.authService.verifyToken(dto.idToken);
    return {
      success: true,
      message: 'Xác thực thành công',
      data: result,
    };
  }

  /**
   * Google Sign-In
   *
   * Client signs in with Google SDK, then sends the ID token here.
   * Backend verifies and creates user profile if new.
   */
  @Post('google')
  @Public()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Đăng nhập bằng Google' })
  @ApiResponse({
    status: 200,
    description: 'Đăng nhập Google thành công',
    schema: {
      example: {
        success: true,
        data: {
          user: {
            id: 'abc123',
            fullName: 'Google User',
            email: 'user@gmail.com',
            isVerify: true,
            phone: '',
            role: 'user',
            imageAvatar: 'https://...',
            createdAt: 1704700000000,
            updatedAt: 1704700000000,
          },
          isNewUser: true,
        },
      },
    },
  })
  @ApiResponse({ status: 401, description: 'Token không hợp lệ' })
  async googleSignIn(@Body() dto: GoogleAuthDto) {
    const result = await this.authService.googleSignIn(dto.idToken);
    return {
      success: true,
      message: result.isNewUser
        ? 'Đăng ký mới thành công, vui lòng chọn vai trò'
        : 'Đăng nhập thành công',
      data: result,
    };
  }

  /**
   * Get current user profile
   *
   * Requires authentication.
   */
  @Get('profile')
  @UseGuards(AuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Lấy thông tin profile' })
  @ApiResponse({ status: 200, description: 'Thành công' })
  @ApiResponse({ status: 401, description: 'Chưa đăng nhập' })
  async getProfile(@CurrentUser() user: IUser) {
    const profile = await this.authService.getProfile(user.uid);
    return {
      success: true,
      data: profile,
    };
  }

  /**
   * Update user profile
   *
   * Requires authentication.
   */
  @Put('profile')
  @UseGuards(AuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Cập nhật thông tin profile' })
  @ApiResponse({ status: 200, description: 'Cập nhật thành công' })
  @ApiResponse({ status: 401, description: 'Chưa đăng nhập' })
  async updateProfile(
    @CurrentUser() user: IUser,
    @Body() dto: UpdateProfileDto,
  ) {
    const profile = await this.authService.updateProfile(user.uid, dto);
    return {
      success: true,
      message: 'Cập nhật thành công',
      data: profile,
    };
  }

  /**
   * Update user role (Role Selection screen)
   *
   * Called after registration/Google sign-in to set user role.
   */
  @Put('role')
  @UseGuards(AuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Cập nhật vai trò (user/seller/delivery)' })
  @ApiResponse({ status: 200, description: 'Cập nhật thành công' })
  @ApiResponse({ status: 401, description: 'Chưa đăng nhập' })
  async updateRole(@CurrentUser() user: IUser, @Body() dto: UpdateRoleDto) {
    const profile = await this.authService.updateRole(user.uid, dto.role);
    return {
      success: true,
      message: 'Cập nhật vai trò thành công',
      data: profile,
    };
  }

  /**
   * Delete user account
   *
   * Deletes both Firebase Auth and Firestore data.
   */
  @Delete('account')
  @UseGuards(AuthGuard)
  @ApiBearerAuth()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Xóa tài khoản' })
  @ApiResponse({ status: 200, description: 'Xóa thành công' })
  @ApiResponse({ status: 401, description: 'Chưa đăng nhập' })
  async deleteAccount(@CurrentUser() user: IUser) {
    await this.authService.deleteAccount(user.uid);
    return {
      success: true,
      message: 'Xóa tài khoản thành công',
    };
  }
}
