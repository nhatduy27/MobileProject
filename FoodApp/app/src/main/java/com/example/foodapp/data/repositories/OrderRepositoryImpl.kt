package com.example.foodapp.data.repositories

import com.example.foodapp.domain.entities.Order
import com.example.foodapp.domain.repositories.OrderRepository
import com.example.foodapp.domain.repositories.AuthRepository
import com.example.foodapp.data.remote.FirestoreOrderDataSource
import com.example.foodapp.data.mapper.OrderMapper

class OrderRepositoryImpl(
    private val firestoreOrderDataSource: FirestoreOrderDataSource,
    private val authRepository: AuthRepository
) : OrderRepository {
    
    override suspend fun placeOrder(order: Order): Order {
        return firestoreOrderDataSource.createOrder(order)
    }
    
    override suspend fun getOrdersForCurrentUser(): List<Order> {
        val currentUser = authRepository.getCurrentUser() 
            ?: throw Exception("User not logged in")
        return firestoreOrderDataSource.getOrdersForUser(currentUser.id)
    }
    
    override suspend fun getOrderDetail(orderId: String): Order {
        return firestoreOrderDataSource.getOrderDetail(orderId)
    }
}
