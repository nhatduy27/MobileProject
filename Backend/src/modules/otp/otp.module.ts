import { Module } from '@nestjs/common';
import { OtpService } from './otp.service';
import { OtpController } from './otp.controller';
import { MailModule } from '../mail/mail.module';
import { FirebaseModule } from '../../firebase/firebase.module';

@Module({
  imports: [MailModule, FirebaseModule],
  controllers: [OtpController],
  providers: [OtpService],
  exports: [OtpService],
})
export class OtpModule {}