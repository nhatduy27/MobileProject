package com.example.foodapp.pages.client.components.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R
import com.example.foodapp.pages.client.home.CategoryState
import com.example.foodapp.pages.client.home.UserNameState

/**
 * Section header chính của màn hình Home, bao gồm phần chào, bộ lọc và tìm kiếm.
 */
@Composable
fun HomeHeaderSection(
    nameState: UserNameState,
    categoryState: CategoryState,
    categoryMap: Map<String?, String>,
    selectedCategory: String?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (String?) -> Unit,
    searchQuery: String,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF9800), Color(0xFFFFB74D))
                )
            )
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EnhancedUserHeader(nameState = nameState)
            CategoryFilterSpinner(
                categoryState = categoryState,
                categoryMap = categoryMap,
                selectedCategory = selectedCategory,
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                onCategorySelected = onCategorySelected
            )
        }
        EnhancedSearchBar(
            onSearch = onSearch,
            onClearSearch = onClearSearch,
            currentQuery = searchQuery
        )
    }
}

@Composable
private fun EnhancedUserHeader(nameState: UserNameState) {
    val userName = when (nameState) {
        is UserNameState.Success -> nameState.userName
        is UserNameState.Loading -> "..."
        else -> stringResource(R.string.guest)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(R.string.avatar),
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Column {
            Text(
                text = stringResource(R.string.welcome),
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Normal
            )
            Text(
                text = userName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EnhancedSearchBar(
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    currentQuery: String
) {
    var searchText by remember { mutableStateOf(currentQuery) }

    LaunchedEffect(currentQuery) {
        searchText = currentQuery
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        contentColor = Color.Black
    ) {
        TextField(
            value = searchText,
            onValueChange = { newText ->
                searchText = newText
                onSearch(newText)
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.search_favorite_foods),
                    color = Color(0xFF9E9E9E),
                    fontSize = 15.sp
                )
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(R.string.search),
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
            },
            trailingIcon = {
                if (searchText.isNotBlank()) {
                    IconButton(
                        onClick = {
                            searchText = ""
                            onClearSearch()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.clear_search),
                            tint = Color(0xFF757575)
                        )
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFFFF9800),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }
}

@Composable
private fun CategoryFilterSpinner(
    categoryState: CategoryState,
    categoryMap: Map<String?, String>,
    selectedCategory: String?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    Box {
        val buttonContent: @Composable () -> Unit = when (categoryState) {
            is CategoryState.Loading -> {
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.loading),
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            is CategoryState.Error -> {
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = stringResource(R.string.error_icon_desc),
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.error),
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            else -> {
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filter),
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = categoryMap[selectedCategory] ?: stringResource(R.string.all_categories),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Button(
            onClick = { if (categoryState !is CategoryState.Loading) onExpandedChange(true) },
            modifier = Modifier.width(130.dp).height(44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.25f),
                contentColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = categoryState !is CategoryState.Loading && categoryMap.isNotEmpty(),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            buttonContent()
        }

        DropdownMenu(
            expanded = expanded && categoryState !is CategoryState.Loading && categoryState !is CategoryState.Error,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.width(200.dp).background(Color.White, RoundedCornerShape(12.dp))
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.all_categories),
                        fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal
                    )
                },
                onClick = {
                    onCategorySelected(null)
                    onExpandedChange(false)
                },
                leadingIcon = {
                    if (selectedCategory == null) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(R.string.selected_description),
                            tint = Color(0xFFFF9800)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            categoryMap.forEach { (id, name) ->
                if (id != null) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = name,
                                fontWeight = if (selectedCategory == id) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onCategorySelected(id)
                            onExpandedChange(false)
                        },
                        leadingIcon = {
                            if (selectedCategory == id) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.selected_description),
                                    tint = Color(0xFFFF9800)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}