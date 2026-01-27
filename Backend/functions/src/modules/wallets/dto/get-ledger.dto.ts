import { IsNumber, IsPositive, Min } from 'class-validator';

export class GetLedgerDto {
  @IsNumber()
  @Min(1)
  page?: number = 1;

  @IsNumber()
  @IsPositive()
  @Min(1)
  limit?: number = 20;
}
