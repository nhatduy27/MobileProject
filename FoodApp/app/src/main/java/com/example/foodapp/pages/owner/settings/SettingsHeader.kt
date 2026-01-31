package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R

@Composable
fun SettingsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFF6B35))
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_settings),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = stringResource(R.string.settings_subtitle),
            fontSize = 14.sp,
            color = Color(0xFFFFE5D9),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
