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
        )
    }
    
    fun toRemote(menuItem: MenuItem): MenuItemRemote {
        return MenuItemRemote(
            id = menuItem.id,
            restaurantId = menuItem.restaurantId,
            name = menuItem.name,
            description = menuItem.description,
            price = menuItem.price,
            isAvailable = menuItem.isAvailable,
            imageUrl = menuItem.imageUrl
        )
    }
}
