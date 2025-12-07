package com.example.foodapp.data.mapper

import com.example.foodapp.domain.entities.MenuItem

object MenuItemMapper {
    
    fun toMap(menuItem: MenuItem): Map<String, Any?> {
        return mapOf(
            "id" to menuItem.id,
            "restaurantId" to menuItem.restaurantId,
            "name" to menuItem.name,
            "description" to menuItem.description,
            "price" to menuItem.price,
            "isAvailable" to menuItem.isAvailable,
            "imageUrl" to menuItem.imageUrl
        )
    }
    
    fun fromMap(map: Map<String, Any?>): MenuItem {
        return MenuItem(
            id = map["id"] as? String ?: "",
            restaurantId = map["restaurantId"] as? String ?: "",
            name = map["name"] as? String ?: "",
            description = map["description"] as? String,
            price = (map["price"] as? Number)?.toLong() ?: 0L,
            isAvailable = map["isAvailable"] as? Boolean ?: true,
            imageUrl = map["imageUrl"] as? String
        )
    }
}
