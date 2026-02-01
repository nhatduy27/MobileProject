package com.example.foodapp.pages.owner.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.example.foodapp.R
import com.example.foodapp.pages.owner.notifications.NotificationsViewModel
import com.example.foodapp.utils.LanguageManager

@Composable
fun SettingsScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    notificationViewModel: NotificationsViewModel = viewModel()
) {
    val notificationUiState by notificationViewModel.uiState.collectAsState()
    val preferences = notificationUiState.preferences

    // Load preferences on first composition
    androidx.compose.runtime.LaunchedEffect(Unit) {
        notificationViewModel.loadPreferences()
    }

    val accountItems = listOf(
        SettingItem(
            title = stringResource(R.string.settings_personal_info),
            subtitle = stringResource(R.string.settings_personal_info_desc),
            icon = Icons.Outlined.Person,
            onClick = { navController.navigate("personal_info") }
        ),
        SettingItem(
            title = stringResource(R.string.settings_change_password),
            subtitle = stringResource(R.string.settings_change_password_desc),
            icon = Icons.Outlined.Lock,
            onClick = { navController.navigate("change_password") }
        )
    )

    val storeItems = listOf(
        SettingItem(
            title = stringResource(R.string.settings_store_info),
            subtitle = stringResource(R.string.settings_store_info_desc),
            icon = Icons.Outlined.Store,
            onClick = { navController.navigate("store_info") }
        ),
        SettingItem(
            title = stringResource(R.string.settings_payment_method),
            subtitle = stringResource(R.string.settings_payment_method_desc),
            icon = Icons.Outlined.CreditCard,
            onClick = { navController.navigate("payment_method") }
        )
    )

    val securityItems = listOf(
        SettingItem(
            title = stringResource(R.string.settings_2fa),
            subtitle = stringResource(R.string.settings_2fa_desc),
            icon = Icons.Outlined.Security,
            hasSwitch = true,
            isEnabled = false,
            isDisabled = true
        ),
        SettingItem(
            title = stringResource(R.string.settings_login_history),
            subtitle = stringResource(R.string.settings_login_history_desc),
            icon = Icons.Outlined.History,
            onClick = { navController.navigate("login_history") },
            isDisabled = true
        )
    )

    val aboutItems = listOf(
        SettingItem(
            title = stringResource(R.string.settings_terms),
            subtitle = stringResource(R.string.settings_terms_desc),
            icon = Icons.Default.List,
            onClick = { navController.navigate("terms") }
        ),
        SettingItem(
            title = stringResource(R.string.settings_privacy),
            subtitle = stringResource(R.string.settings_privacy_desc),
            icon = Icons.Outlined.PrivacyTip,
            onClick = { navController.navigate("privacy") }
        ),
        SettingItem(
            title = stringResource(R.string.settings_support),
            subtitle = stringResource(R.string.settings_support_desc),
            icon = Icons.Outlined.SupportAgent,
            onClick = { navController.navigate("support") }
        )
    )

    // Language state
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    var showLanguageDialog by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf(LanguageManager.getCurrentLanguage(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Settings List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Account
            SettingSectionCard(title = stringResource(R.string.settings_section_account), items = accountItems)
            
            // Store
            SettingSectionCard(title = stringResource(R.string.settings_section_store), items = storeItems)
            
            // Thông báo - with API integration
            NotificationSettingsSection(
                preferences = preferences,
                onTransactionalChanged = { /* Không thể tắt */ },
                onInformationalChanged = { enabled ->
                    notificationViewModel.updatePreferences(
                        informational = enabled,
                        marketing = preferences?.marketing
                    )
                },
                onMarketingChanged = { enabled ->
                    notificationViewModel.updatePreferences(
                        informational = preferences?.informational,
                        marketing = enabled
                    )
                }
            )
            
            // Ngôn ngữ / Language
            LanguageSettingsSection(
                currentLanguage = currentLanguage,
                onLanguageClick = { showLanguageDialog = true }
            )
            
            // Security
            SettingSectionCard(title = stringResource(R.string.settings_section_security), items = securityItems)
            
            // About
            SettingSectionCard(title = stringResource(R.string.settings_section_about), items = aboutItems)

            // Nút đăng xuất
            Button(
                onClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.settings_logout), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            // Version Info
            Text(
                text = stringResource(R.string.settings_version),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
    
    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { language ->
                currentLanguage = language
                LanguageManager.saveLanguage(context, language)
                showLanguageDialog = false
                // Restart activity to apply language change
                activity?.recreate()
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
private fun SettingSectionCard(
    title: String,
    items: List<SettingItem>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        androidx.compose.material3.Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingItemCard(
                        item = item,
                        onSwitchChanged = { enabled ->
                            println("${item.title} switched to $enabled")
                        }
                    )
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section cho Notification Settings - kết nối với API
 */
@Composable
private fun NotificationSettingsSection(
    preferences: com.example.foodapp.data.model.owner.notification.NotificationPreferences?,
    onTransactionalChanged: (Boolean) -> Unit,
    onInformationalChanged: (Boolean) -> Unit,
    onMarketingChanged: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.settings_section_notification),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        androidx.compose.material3.Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                // Orders & Payments - Always on
                NotificationToggleItem(
                    title = stringResource(R.string.settings_notif_orders),
                    subtitle = stringResource(R.string.settings_notif_orders_desc),
                    icon = Icons.Default.ShoppingCart,
                    checked = true,
                    enabled = false,
                    onCheckedChange = onTransactionalChanged
                )
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // Updates & Summary (informational)
                NotificationToggleItem(
                    title = stringResource(R.string.settings_notif_updates),
                    subtitle = stringResource(R.string.settings_notif_updates_desc),
                    icon = Icons.Default.Update,
                    checked = preferences?.informational ?: true,
                    enabled = preferences != null,
                    onCheckedChange = onInformationalChanged
                )
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // Promotions (marketing)
                NotificationToggleItem(
                    title = stringResource(R.string.settings_notif_promo),
                    subtitle = stringResource(R.string.settings_notif_promo_desc),
                    icon = Icons.Default.LocalOffer,
                    checked = preferences?.marketing ?: true,
                    enabled = preferences != null,
                    onCheckedChange = onMarketingChanged
                )
            }
        }
    }
}

@Composable
private fun NotificationToggleItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    androidx.compose.material3.Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // Icon Background
            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Title and Subtitle
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Switch
            androidx.compose.material3.Switch(
                checked = checked,
                onCheckedChange = if (enabled) onCheckedChange else null,
                enabled = enabled,
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledCheckedThumbColor = Color.White.copy(alpha = 0.6f),
                    disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledUncheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    }
}

/**
 * Language Settings Section
 */
@Composable
private fun LanguageSettingsSection(
    currentLanguage: LanguageManager.Language,
    onLanguageClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "NGÔN NGỮ / LANGUAGE",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        androidx.compose.material3.Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            onClick = onLanguageClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                // Icon Background
                androidx.compose.material3.Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = androidx.compose.ui.Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Title and Current Language
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = "Ngôn ngữ / Language",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentLanguage.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Arrow icon
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Language Selection Dialog
 */
@Composable
private fun LanguageSelectionDialog(
    currentLanguage: LanguageManager.Language,
    onLanguageSelected: (LanguageManager.Language) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chọn ngôn ngữ / Select Language",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                LanguageManager.Language.values().forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == currentLanguage,
                            onClick = { onLanguageSelected(language) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = language.displayName,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = if (language == LanguageManager.Language.VIETNAMESE) 
                                    "Vietnamese" else "Tiếng Anh",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (language == currentLanguage) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Check,
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
                Text("Đóng / Close")
            }
        }
    )
}
