package com.example.foodapp.domain.usecase.order

import com.example.foodapp.domain.entities.Order
import com.example.foodapp.domain.repositories.OrderRepository

class PlaceOrderUseCase(private val orderRepository: OrderRepository) {
    suspend operator fun invoke(order: Order): Order {
        return orderRepository.placeOrder(order)
    }
}
