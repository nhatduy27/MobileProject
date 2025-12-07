package com.example.foodapp.data.mapper

import com.example.foodapp.domain.entities.MenuItem
import com.example.foodapp.data.remote.model.MenuItemRemote

object MenuItemMapper {
    
    fun fromRemote(menuItemRemote: MenuItemRemote): MenuItem {
        return MenuItem(
            id = menuItemRemote.id ?: "",
            restaurantId = menuItemRemote.restaurantId ?: "",
            name = menuItemRemote.name ?: "",
            description = menuItemRemote.description,
            price = menuItemRemote.price ?: 0L,
            isAvailable = menuItemRemote.isAvailable ?: true,
            imageUrl = menuItemRemote.imageUrl
            // Note: category, preparationTime, rating, totalOrders, createdAt are stored in Firestore but not in domain
        )
    }
    
    fun toRemote(menuItem: MenuItem): MenuItemRemote {
        return MenuItemRemote(
            id = menuItem.id,
            restaurantId = menuItem.restaurantId,
            name = menuItem.name,
            description = menuItem.description,
            price = menuItem.price,
            category = "", // Default category, can be set when creating
            imageUrl = menuItem.imageUrl,
            isAvailable = menuItem.isAvailable,
            preparationTime = 15, // Default 15 minutes
            rating = 0.0,
            totalOrders = 0,
            createdAt = System.currentTimeMillis()
        )
    }
}
