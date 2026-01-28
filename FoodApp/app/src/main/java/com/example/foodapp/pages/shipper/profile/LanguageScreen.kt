package com.example.foodapp.pages.shipper.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun LanguageScreen(
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    val languages = listOf(
        "Tiếng Việt" to "🇻🇳",
        "English" to "🇺🇸",
        "日本語" to "🇯🇵",
        "한국어" to "🇰🇷"
    )
    var selected by remember { mutableStateOf("Tiếng Việt") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                languages.forEachIndexed { index, (lang, flag) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = lang }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(flag, fontSize = 24.sp)
                        Text(
                            lang, 
                            fontSize = 15.sp, 
                            fontWeight = if (selected == lang) FontWeight.SemiBold else FontWeight.Normal, 
                            color = if (selected == lang) ShipperColors.Primary else ShipperColors.TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        RadioButton(
                            selected = selected == lang,
                            onClick = { selected = lang },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = ShipperColors.Primary,
                                unselectedColor = ShipperColors.TextTertiary
                            )
                        )
                    }
                    if (index < languages.lastIndex) {
                        HorizontalDivider(
                            color = ShipperColors.Divider,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ShipperColors.TextSecondary
                )
            ) {
                Text("Hủy")
            }
            Button(
                onClick = {
                    Toast.makeText(context, "Đã chọn: $selected", Toast.LENGTH_SHORT).show()
                    onSave()
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary)
            ) {
                Text("Lưu", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
