/**
 * Address Entity
 *
 * User delivery address for customers
 */

export class AddressEntity {
  id: string;
  userId: string;
  label: string; // "Nhà", "Phòng ký túc xá"
  fullAddress: string; // "Tòa A, Phòng 101, KTX Khu B"
  building?: string; // "A", "B", "C"
  room?: string; // "101"
  note?: string; // "Gọi trước khi đến"
  isDefault: boolean;
  createdAt: Date;
  updatedAt: Date;

  constructor(partial: Partial<AddressEntity>) {
    Object.assign(this, partial);
  }
}
