import { 
  Controller, 
  Post, 
  Body, 
  HttpCode, 
  HttpStatus, 
  UsePipes, 
  ValidationPipe,
  Get,
  Query,
  BadRequestException
} from '@nestjs/common';
import { OtpService } from './otp.service';
import { SendOtpDto, VerifyOtpDto } from './dto/otp.dto';
import { FirebaseConfig } from '../../firebase/firebase.config';

interface VerificationDoc {
  id: string;
  email: string;
  verified: boolean;
  verifiedAt?: Date;
  createdAt?: Date;
}

@Controller('otp')
@UsePipes(new ValidationPipe({ transform: true }))
export class OtpController {
  constructor(
    private readonly otpService: OtpService,
    private readonly firebase: FirebaseConfig,
  ) {}

  @Post('send')
  @HttpCode(HttpStatus.OK)
  async sendOtp(@Body() sendOtpDto: SendOtpDto) {
    const result = await this.otpService.sendOtp(sendOtpDto.email);
    
    return {
      success: result.success,
      message: result.message,
      data: {
        email: sendOtpDto.email,
        expiresAt: result.expiresAt,
      },
      timestamp: new Date().toISOString(),
    };
  }

  @Post('verify')
  @HttpCode(HttpStatus.OK)
  async verifyOtp(@Body() verifyOtpDto: VerifyOtpDto) {
    const result = await this.otpService.verifyOtp(
      verifyOtpDto.email,
      verifyOtpDto.otp,
    );

    return {
      success: result.success,
      message: result.message,
      data: {
        email: verifyOtpDto.email,
        verified: result.verified,
      },
      timestamp: new Date().toISOString(),
    };
  }

  @Get('verification-status')
  async getVerificationStatus(@Query('email') email: string) {
    if (!email) {
      throw new BadRequestException('Email is required');
    }
    
    const status = await this.otpService.checkEmailVerification(email);
    
    return {
      success: true,
      data: status,
      timestamp: new Date().toISOString(),
    };
  }

  @Get('all-verifications')
  async getAllVerifications() {
    try {
      const verificationsRef = this.firebase.collection('email_verifications');
      const snapshot = await verificationsRef.orderBy('createdAt', 'desc').get();
      
      const verifications: VerificationDoc[] = [];
      
      snapshot.forEach(doc => {
        const data = doc.data();
        verifications.push({
          id: doc.id,
          email: data.email || '',
          verified: data.verified || false,
          verifiedAt: data.verifiedAt?.toDate?.(),
          createdAt: data.createdAt?.toDate?.(),
        });
      });
      
      return {
        success: true,
        data: {
          count: verifications.length,
          verifications,
        },
        timestamp: new Date().toISOString(),
      };
    } catch (error: any) {
      return {
        success: false,
        error: error.message,
        timestamp: new Date().toISOString(),
      };
    }
  }

  @Post('cleanup')
  async cleanup() {
    const result = await this.otpService.cleanup();
    
    return {
      success: true,
      message: 'Cleanup completed',
      data: result,
      timestamp: new Date().toISOString(),
    };
  }

  @Get('test-firebase')
  async testFirebase() {
    try {
      const testRef = this.firebase.collection('test').doc('test');
      await testRef.set({
        test: true,
        timestamp: new Date(),
      });
      
      return {
        success: true,
        message: 'Firebase connection successful',
        timestamp: new Date().toISOString(),
      };
    } catch (error: any) {
      return {
        success: false,
        message: `Firebase connection failed: ${error.message}`,
        timestamp: new Date().toISOString(),
      };
    }
  }
}