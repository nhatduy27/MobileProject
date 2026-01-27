import { IsEnum, IsNotEmpty } from 'class-validator';
import { PaymentMethod } from '../entities';

export class CreatePaymentDto {
  @IsEnum(PaymentMethod)
  @IsNotEmpty()
  method: PaymentMethod;
}
