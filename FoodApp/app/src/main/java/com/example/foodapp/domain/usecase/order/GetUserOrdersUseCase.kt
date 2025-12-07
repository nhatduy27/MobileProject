package com.example.foodapp.domain.usecase.order

import com.example.foodapp.domain.entities.Order
import com.example.foodapp.domain.repositories.OrderRepository

class GetUserOrdersUseCase(private val orderRepository: OrderRepository) {
    suspend operator fun invoke(): List<Order> {
        return orderRepository.getOrdersForCurrentUser()
    }
}
