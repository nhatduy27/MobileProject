package com.example.foodapp.data.mapper

import com.example.foodapp.domain.entities.Restaurant

object RestaurantMapper {
    
    fun toMap(restaurant: Restaurant): Map<String, Any?> {
        return mapOf(
            "id" to restaurant.id,
            "ownerId" to restaurant.ownerId,
            "name" to restaurant.name,
            "description" to restaurant.description,
            "category" to restaurant.category,
            "phoneNumber" to restaurant.phoneNumber,
            "email" to restaurant.email,
            "address" to restaurant.address,
            "averageRating" to restaurant.averageRating,
            "totalReviews" to restaurant.totalReviews,
            "isOpen" to restaurant.isOpen,
            "deliveryFee" to restaurant.deliveryFee,
            "minOrderAmount" to restaurant.minOrderAmount
        )
    }
    
    fun fromMap(map: Map<String, Any?>): Restaurant {
        return Restaurant(
            id = map["id"] as? String ?: "",
            ownerId = map["ownerId"] as? String ?: "",
            name = map["name"] as? String ?: "",
            description = map["description"] as? String,
            category = map["category"] as? String ?: "",
            phoneNumber = map["phoneNumber"] as? String,
            email = map["email"] as? String,
            address = map["address"] as? String ?: "",
            averageRating = (map["averageRating"] as? Number)?.toDouble() ?: 0.0,
            totalReviews = (map["totalReviews"] as? Number)?.toInt() ?: 0,
            isOpen = map["isOpen"] as? Boolean ?: false,
            deliveryFee = (map["deliveryFee"] as? Number)?.toLong() ?: 0L,
            minOrderAmount = (map["minOrderAmount"] as? Number)?.toLong() ?: 0L
        )
    }
}
