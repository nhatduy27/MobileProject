import { IsNumber, IsPositive, Min, IsOptional } from 'class-validator';
import { Type } from 'class-transformer';

export class GetLedgerDto {
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(1)
  page?: number = 1;

  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @IsPositive()
  @Min(1)
  limit?: number = 20;
}
