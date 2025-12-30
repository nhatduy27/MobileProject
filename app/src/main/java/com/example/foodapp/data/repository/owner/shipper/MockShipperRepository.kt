package com.example.foodapp.data.repository.owner.shipper

import com.example.foodapp.data.model.owner.Shipper
import com.example.foodapp.data.model.owner.ShipperStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MockShipperRepository {

    private val _internalShippersFlow = MutableStateFlow<List<Shipper>>(emptyList())

    init {
        _internalShippersFlow.value = listOf(
            Shipper(
                id = "SH001",
                name = "Nguyễn Văn A",
                phone = "0912345678",
                rating = 4.8,
                totalDeliveries = 245,
                todayDeliveries = 8,
                status = ShipperStatus.DELIVERING,
                avatarUrl = "https://images.pexels.com/photos/4391470/pexels-photo-4391470.jpeg?auto=compress&cs=tinysrgb&w=400"
            ),
            Shipper(
                id = "SH002",
                name = "Trần Thị B",
                phone = "0987654321",
                rating = 4.9,
                totalDeliveries = 312,
                todayDeliveries = 12,
                status = ShipperStatus.AVAILABLE,
                avatarUrl = "https://images.pexels.com/photos/4391476/pexels-photo-4391476.jpeg?auto=compress&cs=tinysrgb&w=400"
            ),
            Shipper(
                id = "SH003",
                name = "Lê Văn C",
                phone = "0901234567",
                rating = 4.7,
                totalDeliveries = 198,
                todayDeliveries = 6,
                status = ShipperStatus.DELIVERING,
                avatarUrl = "https://images.pexels.com/photos/4391477/pexels-photo-4391477.jpeg?auto=compress&cs=tinysrgb&w=400"
            ),
            Shipper(
                id = "SH004",
                name = "Phạm Thị D",
                phone = "0923456789",
                rating = 4.6,
                totalDeliveries = 156,
                todayDeliveries = 0,
                status = ShipperStatus.OFFLINE,
                avatarUrl = "https://images.pexels.com/photos/4391479/pexels-photo-4391479.jpeg?auto=compress&cs=tinysrgb&w=400"
            ),
            Shipper(
                id = "SH005",
                name = "Hoàng Văn E",
                phone = "0934567890",
                rating = 4.9,
                totalDeliveries = 289,
                todayDeliveries = 10,
                status = ShipperStatus.AVAILABLE,
                avatarUrl = "https://images.pexels.com/photos/4391480/pexels-photo-4391480.jpeg?auto=compress&cs=tinysrgb&w=400"
            ),
            Shipper(
                id = "SH006",
                name = "Võ Thị F",
                phone = "0945678901",
                rating = 4.5,
                totalDeliveries = 134,
                todayDeliveries = 5,
                status = ShipperStatus.DELIVERING,
                avatarUrl = "https://images.pexels.com/photos/4391482/pexels-photo-4391482.jpeg?auto=compress&cs=tinysrgb&w=400"
            )
        )
    }

    fun getShippers(): Flow<List<Shipper>> = _internalShippersFlow.asStateFlow()

    fun addShipper(shipper: Shipper) {
        _internalShippersFlow.update { current -> current + shipper }
    }

    fun updateShipper(updated: Shipper) {
        _internalShippersFlow.update { current ->
            current.map { if (it.id == updated.id) updated else it }
        }
    }

    fun deleteShipper(id: String) {
        _internalShippersFlow.update { current ->
            current.filterNot { it.id == id }
        }
    }
}
