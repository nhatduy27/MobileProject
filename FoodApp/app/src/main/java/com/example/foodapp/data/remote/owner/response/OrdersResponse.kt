package com.example.foodapp.data.remote.owner.response

import com.example.foodapp.data.model.owner.Order
import com.example.foodapp.data.model.owner.OrderStatus

data class OrdersResponse(
    val orders: List<OrderDto>
) {
    data class OrderDto(
        val id: String,
        val customerName: String,
        val location: String,
        val items: String,
        val time: String,
        val price: Int,
        val status: String // PENDING, PROCESSING, DELIVERING, COMPLETED, CANCELLED
    )

    fun toOrderList(): List<Order> = orders.map { dto ->
        Order(
            id = dto.id,
            customerName = dto.customerName,
            location = dto.location,
            items = dto.items,
            time = dto.time,
            price = dto.price,
            status = when (dto.status) {
                "PENDING" -> OrderStatus.PENDING
                "PROCESSING" -> OrderStatus.PROCESSING
                "DELIVERING" -> OrderStatus.DELIVERING
                "COMPLETED" -> OrderStatus.COMPLETED
                "CANCELLED" -> OrderStatus.CANCELLED
                else -> OrderStatus.PENDING
            }
        )
    }
}
