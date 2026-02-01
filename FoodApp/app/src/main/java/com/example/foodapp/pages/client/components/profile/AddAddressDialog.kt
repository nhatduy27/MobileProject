package com.example.foodapp.pages.client.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import com.example.foodapp.R
import com.example.foodapp.pages.client.profile.ProfileViewModel
import com.example.foodapp.pages.client.profile.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressDialog(
    viewModel: ProfileViewModel,
    createAddressState: CreateAddressState?,
    onDismiss: () -> Unit
) {
    // State cho form địa chỉ
    var label by remember { mutableStateOf("") }
    var selectedBuilding by remember { mutableStateOf<String?>(null) }
    var room by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Observe pickup points từ ViewModel
    val pickupPointsState by viewModel.pickupPointsState.observeAsState()
    val pickupPoints by viewModel.pickupPoints.observeAsState(emptyList())

    val isLoading = createAddressState is CreateAddressState.Loading

    // Tìm pickup point được chọn
    val selectedPickupPoint = selectedBuilding?.let { buildingCode ->
        pickupPoints.find { it.buildingCode == buildingCode }
    }

    // Tự động refresh pickup points khi mở dialog
    LaunchedEffect(Unit) {
        viewModel.fetchPickupPoints()
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = {
            Text(
                text = stringResource(id = R.string.add_address_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hiển thị lỗi nếu có
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                // Label field
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(id = R.string.address_label_field)) },
                    placeholder = { Text(stringResource(id = R.string.address_label_placeholder)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Label,
                            contentDescription = stringResource(id = R.string.label_icon)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    isError = label.isBlank(),
                    supportingText = {
                        if (label.isBlank()) {
                            Text(
                                text = stringResource(id = R.string.address_label_required),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                // Building field - DROPDOWN thay vì EditText
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.building_field_label),
                        fontSize = 14.sp,
                        color = if (selectedBuilding == null && !isLoading) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )

                    // Loading state
                    if (pickupPointsState is PickupPointsState.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.small
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(stringResource(id = R.string.loading_buildings))
                            }
                        }
                    }
                    // Error state
                    else if (pickupPointsState is PickupPointsState.Error) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable(enabled = !isLoading) {
                                    viewModel.fetchPickupPoints()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(
                                    id = R.string.error_generic,
                                    (pickupPointsState as PickupPointsState.Error).message
                                ),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                        }
                    }
                    // Success state - Dropdown menu
                    else if (pickupPoints.isNotEmpty()) {
                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it && !isLoading },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedPickupPoint?.name ?: "",
                                onValueChange = { },
                                label = { Text(stringResource(id = R.string.select_building_placeholder)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Apartment,
                                        contentDescription = stringResource(id = R.string.apartment_icon)
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expanded
                                    )
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                enabled = !isLoading,
                                isError = selectedBuilding == null && !isLoading,
                                supportingText = {
                                    if (selectedBuilding == null && !isLoading) {
                                        Text(
                                            text = stringResource(id = R.string.building_required),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                pickupPoints.forEach { point ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = point.name,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                point.note?.let { noteText ->
                                                    Text(
                                                        text = noteText,
                                                        fontSize = 12.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedBuilding = point.buildingCode
                                            expanded = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    // Empty state
                    else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.small
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.no_buildings_available),
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Thông tin chi tiết của tòa nhà đã chọn
                    selectedPickupPoint?.let { point ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.building_info_title),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(id = R.string.building_code_label, point.buildingCode),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = stringResource(id = R.string.building_name_label, point.name),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                point.note?.let { noteText ->
                                    Text(
                                        text = stringResource(id = R.string.building_note_label, noteText),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Room field
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text(stringResource(id = R.string.room_field_label)) },
                    placeholder = { Text(stringResource(id = R.string.room_placeholder)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.DoorFront,
                            contentDescription = stringResource(id = R.string.door_icon)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && selectedBuilding != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = room.isBlank(),
                    supportingText = {
                        if (room.isBlank()) {
                            Text(
                                text = stringResource(id = R.string.room_required),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                // Note field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(id = R.string.note_field_label)) },
                    placeholder = { Text(stringResource(id = R.string.note_placeholder)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Note,
                            contentDescription = stringResource(id = R.string.note_icon)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3
                )

                // Checkbox for default address
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.set_default_address),
                        fontSize = 14.sp
                    )
                }

                // Hiển thị lỗi từ ViewModel
                if (createAddressState is CreateAddressState.Error) {
                    Text(
                        text = createAddressState.message,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Validate
                    if (label.isBlank()) {
                        errorMessage = R.string.address_label_required.toString()
                        return@TextButton
                    }

                    if (selectedBuilding == null) {
                        errorMessage = R.string.building_required.toString()
                        return@TextButton
                    }

                    if (room.isBlank()) {
                        errorMessage = R.string.room_required.toString()
                        return@TextButton
                    }

                    errorMessage = null

                    // Gọi tạo địa chỉ
                    viewModel.createAddress(
                        label = label,
                        buildingCode = selectedBuilding!!,
                        room = room,
                        note = note.takeIf { it.isNotBlank() },
                        isDefault = isDefault
                    )
                },
                enabled = !isLoading &&
                        label.isNotBlank() &&
                        selectedBuilding != null &&
                        room.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.adding_address))
                } else {
                    Text(stringResource(id = R.string.add_address_button))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}