package com.example.foodapp.data.repositories

import com.example.foodapp.domain.entities.Order
import com.example.foodapp.domain.repositories.OrderRepository
import com.example.foodapp.domain.repositories.AuthRepository
import com.example.foodapp.data.remote.sdk.FirestoreOrderDataSource
import com.example.foodapp.data.mapper.OrderMapper

class OrderRepositoryImpl(
    private val firestoreOrderDataSource: FirestoreOrderDataSource,
    private val authRepository: AuthRepository
) : OrderRepository {
    
    override suspend fun placeOrder(order: Order): Order {
        val orderRemote = OrderMapper.toRemote(order)
        val createdOrderRemote = firestoreOrderDataSource.createOrder(orderRemote)
        return OrderMapper.fromRemote(createdOrderRemote)
    }
    
    override suspend fun getOrdersForCurrentUser(): List<Order> {
        val currentUser = authRepository.getCurrentUser() 
            ?: throw Exception("User not logged in")
        val orderRemoteList = firestoreOrderDataSource.getOrdersForUser(currentUser.id)
        return orderRemoteList.map { OrderMapper.fromRemote(it) }
    }
    
    override suspend fun getOrderDetail(orderId: String): Order {
        val orderRemote = firestoreOrderDataSource.getOrderDetail(orderId)
        return OrderMapper.fromRemote(orderRemote)
    }
}
