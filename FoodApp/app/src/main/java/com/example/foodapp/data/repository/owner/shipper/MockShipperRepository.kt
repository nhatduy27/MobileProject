package com.example.foodapp.data.repository.owner.shipper

import com.example.foodapp.data.model.owner.Shipper
import com.example.foodapp.data.model.owner.ShipperStatus
import com.example.foodapp.data.repository.owner.base.OwnerShipperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MockShipperRepository : OwnerShipperRepository {

    private val _internalShippersFlow = MutableStateFlow<List<Shipper>>(emptyList())

    init {
        _internalShippersFlow.value = listOf(
            Shipper("SH001", "Nguyễn Thành Long", "0901122334", 5.0, 1250, 15, ShipperStatus.DELIVERING, "https://i.pravatar.cc/150?u=SH001"),
            Shipper("SH002", "Trần Thị Thu Hà", "0902233445", 4.9, 890, 12, ShipperStatus.AVAILABLE, "https://i.pravatar.cc/150?u=SH002"),
            Shipper("SH003", "Lê Quốc Bảo", "0903344556", 4.8, 450, 8, ShipperStatus.DELIVERING, "https://i.pravatar.cc/150?u=SH003"),
            Shipper("SH004", "Phạm Đức Thắng", "0904455667", 4.7, 320, 5, ShipperStatus.AVAILABLE, "https://i.pravatar.cc/150?u=SH004"),
            Shipper("SH005", "Hoàng Minh Tuấn", "0905566778", 4.9, 1500, 18, ShipperStatus.DELIVERING, "https://i.pravatar.cc/150?u=SH005"),
            Shipper("SH006", "Võ Thị Kim Oanh", "0906677889", 4.6, 210, 0, ShipperStatus.OFFLINE, "https://i.pravatar.cc/150?u=SH006"),
            Shipper("SH007", "Đặng Văn Lâm", "0907788990", 4.5, 180, 6, ShipperStatus.DELIVERING, "https://i.pravatar.cc/150?u=SH007"),
            Shipper("SH008", "Bùi Thị Hương", "0908899001", 4.8, 600, 9, ShipperStatus.AVAILABLE, "https://i.pravatar.cc/150?u=SH008"),
            Shipper("SH009", "Đỗ Tuấn Anh", "0909900112", 4.4, 150, 4, ShipperStatus.DELIVERING, "https://i.pravatar.cc/150?u=SH009"),
            Shipper("SH010", "Ngô Văn Đạt", "0910011223", 4.7, 410, 0, ShipperStatus.OFFLINE, "https://i.pravatar.cc/150?u=SH010"),
            Shipper("SH011", "Dương Thị Ngọc", "0911122334", 5.0, 45, 3, ShipperStatus.AVAILABLE, "https://i.pravatar.cc/150?u=SH011"),
            Shipper("SH012", "Lý Văn Phúc", "0912233445", 4.3, 90, 7, ShipperStatus.DELIVERING, "https://i.pravatar.cc/150?u=SH012"),
            Shipper("SH013", "Vũ Thị Hồng", "0913344556", 4.6, 330, 0, ShipperStatus.OFFLINE, "https://i.pravatar.cc/150?u=SH013"),
            Shipper("SH014", "Phan Văn Đức", "0914455667", 4.8, 520, 11, ShipperStatus.DELIVERING, "https://i.pravatar.cc/150?u=SH014"),
            Shipper("SH015", "Trịnh Văn Quyết", "0915566778", 4.2, 80, 2, ShipperStatus.AVAILABLE, "https://i.pravatar.cc/150?u=SH015"),
            Shipper("SH016", "Đinh Thị Lan", "0916677889", 4.9, 950, 14, ShipperStatus.DELIVERING, "https://i.pravatar.cc/150?u=SH016"),
            Shipper("SH017", "Lâm Văn Hậu", "0917788990", 4.5, 240, 5, ShipperStatus.AVAILABLE, "https://i.pravatar.cc/150?u=SH017"),
            Shipper("SH018", "Hà Văn Thắm", "0918899001", 4.7, 380, 0, ShipperStatus.OFFLINE, "https://i.pravatar.cc/150?u=SH018"),
            Shipper("SH019", "Cao Thị Mỹ Duyên", "0919900112", 4.8, 110, 6, ShipperStatus.DELIVERING, "https://i.pravatar.cc/150?u=SH019"),
            Shipper("SH020", "Phùng Quang Thanh", "0920011223", 4.4, 75, 0, ShipperStatus.OFFLINE, "https://i.pravatar.cc/150?u=SH020")
        )
    }

    override fun getShippers(): Flow<List<Shipper>> = _internalShippersFlow.asStateFlow()

    override fun addShipper(shipper: Shipper) {
        _internalShippersFlow.update { current -> current + shipper }
    }

    override fun updateShipper(updated: Shipper) {
        _internalShippersFlow.update { current ->
            current.map { if (it.id == updated.id) updated else it }
        }
    }

    override fun deleteShipper(shipperId: String) {
        _internalShippersFlow.update { current ->
            current.filterNot { it.id == shipperId }
        }
    }
}