package com.example.foodapp.data.repositories

import com.example.foodapp.domain.entities.Restaurant
import com.example.foodapp.domain.entities.MenuItem
import com.example.foodapp.domain.repositories.RestaurantRepository
import com.example.foodapp.data.remote.sdk.FirestoreRestaurantDataSource
import com.example.foodapp.data.mapper.RestaurantMapper
import com.example.foodapp.data.mapper.MenuItemMapper

class RestaurantRepositoryImpl(
    private val firestoreRestaurantDataSource: FirestoreRestaurantDataSource
) : RestaurantRepository {
    
    override suspend fun getRestaurants(): List<Restaurant> {
        val restaurantRemoteList = firestoreRestaurantDataSource.getRestaurants()
        return restaurantRemoteList.map { RestaurantMapper.fromRemote(it) }
    }
    
    override suspend fun getRestaurantDetail(id: String): Restaurant {
        val restaurantRemote = firestoreRestaurantDataSource.getRestaurantDetail(id)
        return RestaurantMapper.fromRemote(restaurantRemote)
    }
    
    override suspend fun getMenuItems(restaurantId: String): List<MenuItem> {
        val menuItemRemoteList = firestoreRestaurantDataSource.getMenuItems(restaurantId)
        return menuItemRemoteList.map { MenuItemMapper.fromRemote(it) }
    }
}
