package com.example.foodapp.pages.owner.shippers

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.pages.owner.notifications.NotificationBell
import com.example.foodapp.pages.owner.theme.OwnerColors

/**
 * Header cho màn hình Shippers với search và refresh
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippersHeader(
    onMenuClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
        else focusManager.clearFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(OwnerColors.Surface)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Normal header
        AnimatedVisibility(
            visible = !isSearchActive,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally { -it / 2 }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = OwnerColors.TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quản lý Shipper",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OwnerColors.TextPrimary
                    )
                }
                
                // Notification Bell Icon
                NotificationBell()

                Row {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = OwnerColors.TextPrimary)
                    }
                    IconButton(
                        onClick = { isSearchActive = true },
                        modifier = Modifier
                            .background(OwnerColors.SurfaceVariant, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = OwnerColors.TextPrimary)
                    }
                }
            }
        }

        // Search mode
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        isSearchActive = false
                        onSearchQueryChange("")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OwnerColors.TextPrimary)
                    }
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .focusRequester(focusRequester),
                        placeholder = { Text("Tìm shipper...", color = OwnerColors.TextTertiary, fontSize = 14.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = OwnerColors.Primary
                        ),
                        shape = CircleShape,
                        keyboardOptions = KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = OwnerColors.TextTertiary)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
