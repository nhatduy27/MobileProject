package com.example.foodapp.pages.shipper.home

import com.example.foodapp.data.model.shipper.DeliveryTask
import com.example.foodapp.data.model.shipper.ShipperStats

// UI state cho màn hình Home của Shipper

data class ShipperHomeUiState(
    val stats: ShipperStats? = null,
    val tasks: List<DeliveryTask> = emptyList()
)
