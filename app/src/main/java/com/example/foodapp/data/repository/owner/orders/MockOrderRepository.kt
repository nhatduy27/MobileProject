package com.example.foodapp.data.repository.owner.orders

import com.example.foodapp.data.model.owner.Order
import com.example.foodapp.data.model.owner.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MockOrderRepository {

    private val _internalOrdersFlow = MutableStateFlow<List<Order>>(emptyList())

    init {
        _internalOrdersFlow.value = listOf(
            Order(
                id = "#ORD10245",
                customerName = "Nguyễn Văn A",
                location = "KTX Khu A, Phòng 201",
                items = "• Cơm gà xối mỡ x2\n• Trà sữa trân châu x1",
                time = "10:25 AM",
                price = 125_000,
                status = OrderStatus.DELIVERING
            ),
            Order(
                id = "#ORD10244",
                customerName = "Trần Thị B",
                location = "KTX Khu B, Phòng 305",
                items = "• Phở bò x1\n• Chả giò x3",
                time = "10:18 AM",
                price = 95_000,
                status = OrderStatus.PENDING
            ),
            Order(
                id = "#ORD10243",
                customerName = "Lê Văn C",
                location = "KTX Khu A, Phòng 108",
                items = "• Bún chả Hà Nội x2\n• Nước chanh x2",
                time = "10:05 AM",
                price = 150_000,
                status = OrderStatus.PROCESSING
            ),
            Order(
                id = "#ORD10242",
                customerName = "Phạm Thị D",
                location = "KTX Khu C, Phòng 401",
                items = "• Cà ri gà x1\n• Bánh mì x2",
                time = "09:50 AM",
                price = 85_000,
                status = OrderStatus.COMPLETED
            ),
            Order(
                id = "#ORD10241",
                customerName = "Hoàng Văn E",
                location = "KTX Khu D, Phòng 105",
                items = "• Pizza Pepperoni x1\n• Nước ngọt x2",
                time = "09:30 AM",
                price = 200_000,
                status = OrderStatus.CANCELLED
            )
        )
    }

    fun getOrders(): Flow<List<Order>> = _internalOrdersFlow.asStateFlow()

    fun addOrder(order: Order) {
        _internalOrdersFlow.update { current -> current + order }
    }

    fun updateOrder(updated: Order) {
        _internalOrdersFlow.update { current ->
            current.map { if (it.id == updated.id) updated else it }
        }
    }

    fun deleteOrder(id: String) {
        _internalOrdersFlow.update { current -> current.filterNot { it.id == id } }
    }
}
