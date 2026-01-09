import { IsBoolean } from 'class-validator';

export class ToggleShopStatusDto {
  @IsBoolean({ message: 'isOpen phải là boolean' })
  isOpen: boolean;
}
