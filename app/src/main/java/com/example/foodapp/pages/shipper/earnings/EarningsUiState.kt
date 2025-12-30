package com.example.foodapp.pages.shipper.earnings

import com.example.foodapp.data.model.shipper.EarningsData
import com.example.foodapp.data.model.shipper.EarningsPeriod

// UI state cho màn Thu nhập của Shipper

data class EarningsUiState(
    val selectedPeriod: EarningsPeriod = EarningsPeriod.MONTH,
    val allHistory: List<EarningsData> = emptyList()
)
