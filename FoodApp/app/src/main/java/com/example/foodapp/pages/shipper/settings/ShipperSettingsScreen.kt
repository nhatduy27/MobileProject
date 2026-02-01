package com.example.foodapp.pages.shipper.settings

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.pages.shipper.theme.ShipperColors
import com.example.foodapp.utils.LanguageManager

@Composable
fun ShipperSettingsScreen(
    onNavigate: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ShipperSettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Language state
    var showLanguageDialog by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf(LanguageManager.getCurrentLanguage(context)) }
    
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, "${context.getString(R.string.shipper_error)}: $it", Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Account Section
        SettingSection(
            title = stringResource(R.string.shipper_settings_account),
            items = listOf(
                SettingItemData(
                    title = stringResource(R.string.shipper_settings_personal_info),
                    subtitle = stringResource(R.string.shipper_settings_personal_info_desc),
                    icon = Icons.Outlined.Person,
                    onClick = { onNavigate("edit_profile") }
                ),
                SettingItemData(
                    title = stringResource(R.string.shipper_settings_change_password),
                    subtitle = stringResource(R.string.shipper_settings_change_password_desc),
                    icon = Icons.Outlined.Lock,
                    onClick = { onNavigate("change_password") }
                )
            )
        )
        
        // Shipper Info Section
        SettingSection(
            title = stringResource(R.string.shipper_settings_shipper_info),
            items = listOf(
                SettingItemData(
                    title = stringResource(R.string.shipper_settings_vehicle),
                    subtitle = stringResource(R.string.shipper_settings_vehicle_desc),
                    icon = Icons.Outlined.DirectionsBike,
                    onClick = { onNavigate("vehicle_info") }
                ),
                SettingItemData(
                    title = stringResource(R.string.shipper_settings_payment),
                    subtitle = stringResource(R.string.shipper_settings_payment_desc),
                    icon = Icons.Outlined.AccountBalanceWallet,
                    onClick = { onNavigate("payment_method") }
                )
            )
        )
        
        // Notification Section
        SettingSection(
            title = stringResource(R.string.shipper_settings_notifications),
            items = listOf(
                SettingItemData(
                    title = stringResource(R.string.shipper_settings_notifications_title),
                    subtitle = stringResource(R.string.shipper_settings_notifications_desc),
                    icon = Icons.Outlined.Notifications,
                    onClick = { onNavigate("notification_settings") }
                )
            )
        )
        
        // Language Section
        SettingSection(
            title = stringResource(R.string.shipper_settings_language),
            items = listOf(
                SettingItemData(
                    title = stringResource(R.string.shipper_settings_language_title),
                    subtitle = currentLanguage.displayName,
                    icon = Icons.Outlined.Language,
                    onClick = { showLanguageDialog = true }
                )
            )
        )
        
        // About Section
        SettingSection(
            title = stringResource(R.string.shipper_settings_about),
            items = listOf(
                SettingItemData(
                    title = stringResource(R.string.shipper_settings_terms),
                    subtitle = stringResource(R.string.shipper_settings_terms_desc),
                    icon = Icons.Outlined.Description,
                    onClick = { onNavigate("terms") }
                ),
                SettingItemData(
                    title = stringResource(R.string.shipper_settings_privacy),
                    subtitle = stringResource(R.string.shipper_settings_privacy_desc),
                    icon = Icons.Outlined.PrivacyTip,
                    onClick = { onNavigate("privacy") }
                ),
                SettingItemData(
                    title = stringResource(R.string.shipper_settings_help),
                    subtitle = stringResource(R.string.shipper_settings_help_desc),
                    icon = Icons.Outlined.HelpOutline,
                    onClick = { onNavigate("help_screen") }
                )
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Logout Button
        Button(
            onClick = {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ShipperColors.ErrorLight
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                stringResource(R.string.shipper_settings_logout), 
                color = ShipperColors.Error, 
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
        
        // Version Info
        Text(
            text = stringResource(R.string.shipper_version),
            fontSize = 12.sp,
            color = ShipperColors.TextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
    }
    
    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { language ->
                LanguageManager.saveLanguage(context, language)
                currentLanguage = language
                showLanguageDialog = false
                // Restart activity to apply language
                (context as? Activity)?.recreate()
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: LanguageManager.Language,
    onLanguageSelected: (LanguageManager.Language) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.shipper_language_select)) },
        text = {
            Column {
                LanguageManager.Language.values().forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = language == currentLanguage,
                                onClick = { onLanguageSelected(language) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(language.displayName, style = MaterialTheme.typography.bodyLarge)
                        }
                        if (language == currentLanguage) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.shipper_close))
            }
        }
    )
}

@Composable
private fun SettingSection(
    title: String,
    items: List<SettingItemData>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ShipperColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingItemRow(item = item)
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = ShipperColors.Divider
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingItemRow(item: SettingItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = ShipperColors.PrimaryLight,
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = ShipperColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Title & Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = ShipperColors.TextPrimary
            )
            item.subtitle?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = ShipperColors.TextSecondary
                )
            }
        }
        
        // Arrow
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = ShipperColors.TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}

private data class SettingItemData(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val onClick: () -> Unit = {}
)
