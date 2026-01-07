import {
  Controller,
  Post,
  Put,
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
import { AuthGuard } from '../../core/guards';
import { CurrentUser } from '../../core/decorators';
import {
  RegisterDto,
  RegisterResponseDto,
  LoginDto,
  LoginResponseDto,
  GoogleAuthDto,
  GoogleAuthResponseDto,
  SendOTPDto,
  VerifyOTPDto,
  ForgotPasswordDto,
  ResetPasswordDto,
  ChangePasswordDto,
  LogoutDto,
} from './dto';

/**
 * Auth Controller
 * 
 * Handles all authentication endpoints:
 * - Registration
 * - Login
 * - Google Sign-In
 * - OTP verification
 * - Password reset
 * - Logout
 */
@ApiTags('Auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  /**
   * POST /auth/register
   * Register new user with email/password
   * 
   * AUTH-003
   */
  @Post('register')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ 
    summary: 'Register new user',
    description: 'Create a new user account with email and password. Returns custom token for client-side Firebase sign-in.',
  })
  @ApiResponse({ 
    status: 201, 
    description: 'User registered successfully',
    type: RegisterResponseDto,
  })
  @ApiResponse({ status: 409, description: 'Email or phone already exists' })
  @ApiResponse({ status: 400, description: 'Invalid input data' })
  async register(@Body() dto: RegisterDto) {
    const result = await this.authService.register(dto);
    return {
      success: true,
      data: result,
    };
  }

  /**
   * POST /auth/login
   * Login with email/password
   */
  @Post('login')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ 
    summary: 'Login with email/password',
    description: 'Authenticate user with email and password. Returns custom token for client-side Firebase sign-in.',
  })
  @ApiResponse({ 
    status: 200, 
    description: 'Login successful',
    type: LoginResponseDto,
    example: {
      success: true,
      data: {
        user: {
          id: 'firebase-user-uid',
          email: 'user@example.com',
          displayName: 'John Doe',
          role: 'CUSTOMER',
          status: 'ACTIVE',
          emailVerified: true,
        },
        customToken: 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...',
        message: 'Đăng nhập thành công',
      },
    },
  })
  @ApiResponse({ status: 401, description: 'Invalid credentials or account banned' })
  async login(@Body() dto: LoginDto) {
    const result = await this.authService.login(dto);
    return {
      success: true,
      data: result,
    };
  }

  /**
   * POST /auth/google
   * Sign in with Google
   * 
   * AUTH-004
   */
  @Post('google')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Google Sign-In',
    description: 'Authenticate with Google ID token. Creates new user if first time, otherwise returns existing user.',
  })
  @ApiResponse({
    status: 200,
    description: 'Google sign-in successful',
    type: GoogleAuthResponseDto,
  })
  @ApiResponse({ status: 401, description: 'Invalid Google token' })
  async googleSignIn(@Body() dto: GoogleAuthDto) {
    const result = await this.authService.googleSignIn(dto);
    return {
      success: true,
      data: result,
    };
  }

  /**
   * POST /auth/send-otp
   * Send OTP to email for verification
   * 
   * AUTH-005
   */
  @Post('send-otp')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Send OTP to email',
    description: 'Generate and send 6-digit OTP code to email. Valid for 5 minutes. Rate limited to 1 request per 60 seconds.',
  })
  @ApiResponse({ status: 200, description: 'OTP sent successfully' })
  @ApiResponse({ status: 429, description: 'Too many requests. Please wait before requesting again.' })
  async sendOTP(@Body() dto: SendOTPDto) {
    const result = await this.authService.sendOTP(dto);
    return {
      success: true,
      ...result,
    };
  }

  /**
   * POST /auth/verify-otp
   * Verify OTP code
   * 
   * AUTH-005
   */
  @Post('verify-otp')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Verify OTP code',
    description: 'Verify 6-digit OTP code sent to email. Max 3 attempts.',
  })
  @ApiResponse({ status: 200, description: 'Email verified successfully' })
  @ApiResponse({ status: 400, description: 'Invalid OTP or expired' })
  @ApiResponse({ status: 404, description: 'OTP not found' })
  async verifyOTP(@Body() dto: VerifyOTPDto) {
    const result = await this.authService.verifyOTP(dto);
    return {
      success: true,
      ...result,
    };
  }

  /**
   * POST /auth/forgot-password
   * Request password reset OTP
   * 
   * AUTH-006
   */
  @Post('forgot-password')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Request password reset',
    description: 'Send OTP code to email for password reset.',
  })
  @ApiResponse({ status: 200, description: 'OTP sent successfully' })
  @ApiResponse({ status: 404, description: 'Email not found' })
  @ApiResponse({ status: 429, description: 'Too many requests' })
  async forgotPassword(@Body() dto: ForgotPasswordDto) {
    const result = await this.authService.forgotPassword(dto);
    return {
      success: true,
      ...result,
    };
  }

  /**
   * POST /auth/reset-password
   * Reset password with OTP
   * 
   * AUTH-006
   */
  @Post('reset-password')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Reset password',
    description: 'Reset password using OTP code sent to email.',
  })
  @ApiResponse({ status: 200, description: 'Password reset successfully' })
  @ApiResponse({ status: 400, description: 'Invalid OTP' })
  @ApiResponse({ status: 404, description: 'OTP not found or user not found' })
  async resetPassword(@Body() dto: ResetPasswordDto) {
    const result = await this.authService.resetPassword(dto);
    return {
      success: true,
      ...result,
    };
  }

  /**
   * PUT /auth/change-password
   * Change password (authenticated)
   * 
   * AUTH-007
   */
  @Put('change-password')
  @UseGuards(AuthGuard)
  @ApiBearerAuth('firebase-auth')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Change password',
    description: 'Change password for authenticated user. Requires current password.',
  })
  @ApiResponse({ status: 200, description: 'Password changed successfully' })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  @ApiResponse({ status: 400, description: 'Invalid old password' })
  async changePassword(
    @CurrentUser() user: any,
    @Body() dto: ChangePasswordDto,
  ) {
    const result = await this.authService.changePassword(user.uid, dto);
    return {
      success: true,
      ...result,
    };
  }

  /**
   * POST /auth/logout
   * Logout - remove FCM token
   * 
   * AUTH-008
   */
  @Post('logout')
  @UseGuards(AuthGuard)
  @ApiBearerAuth('firebase-auth')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({
    summary: 'Logout',
    description: 'Remove FCM token from user devices. Note: Firebase Auth tokens cannot be invalidated server-side.',
  })
  @ApiResponse({ status: 200, description: 'Logged out successfully' })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  async logout(
    @CurrentUser() user: any,
    @Body() dto: LogoutDto,
  ) {
    const result = await this.authService.logout(user.uid, dto);
    return {
      success: true,
      ...result,
    };
  }
}
