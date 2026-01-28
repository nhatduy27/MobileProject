import { Injectable } from '@nestjs/common';
import { DeliveryPointsRepository } from '../../gps/repositories/delivery-points.repository';
import { DeliveryPoint } from '../../gps/entities/delivery-point.entity';

/**
 * Delivery Points Service
 *
 * Quản lý điểm giao hàng (tòa nhà KTX).
 * Shared service dùng cho Customer (chọn tòa), Shipper (routing), Admin (quản lý).
 */
@Injectable()
export class DeliveryPointsService {
  constructor(private readonly deliveryPointsRepository: DeliveryPointsRepository) {}

  /**
   * Lấy danh sách các delivery points đang active
   * Sắp xếp theo block (A-E) và số (1-9)
   *
   * @returns Mảng các delivery points đã được sắp xếp
   */
  async listActiveDeliveryPoints(): Promise<DeliveryPoint[]> {
    const deliveryPoints = await this.deliveryPointsRepository.listActiveDeliveryPoints();

    // Sắp xếp: Primary = block letter (A-E), Secondary = building number (1-9)
    return deliveryPoints.sort((a, b) => {
      // Lấy block letter (ký tự đầu) và số (các chữ số còn lại)
      const blockA = a.buildingCode.charAt(0);
      const blockB = b.buildingCode.charAt(0);
      const numA = parseInt(a.buildingCode.slice(1)) || 0;
      const numB = parseInt(b.buildingCode.slice(1)) || 0;

      // So sánh block trước
      if (blockA !== blockB) {
        return blockA.localeCompare(blockB);
      }

      // Nếu cùng block, so sánh số
      return numA - numB;
    });
  }
}
