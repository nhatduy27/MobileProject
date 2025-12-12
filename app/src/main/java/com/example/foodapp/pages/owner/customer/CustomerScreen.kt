package com.example.foodapp.pages.owner.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foodapp.pages.owner.customer.CustomerHeader
import com.example.foodapp.pages.owner.customer.CustomerFilterTabs
import com.example.foodapp.pages.owner.customer.CustomerStats
import com.example.foodapp.pages.owner.customer.CustomerList

@Composable
fun CustomerScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        // Header
        CustomerHeader()

        // Filter Tabs
        CustomerFilterTabs()

        // Statistics cards
        CustomerStats()

        // Customer list
        CustomerList()
    }
}
