package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.HazardType
import com.example.ui.theme.SafeRiderAmber
import com.example.ui.theme.SafeRiderCyan
import com.example.ui.theme.SafeRiderOrange
import com.example.ui.theme.SafeRiderRed

@Composable
fun ReportHazardDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (HazardType, String, String, String) -> Unit
) {
    if (!isOpen) return

    var selectedType by remember { mutableStateOf(HazardType.PJU_MATI) }
    var locationText by remember { mutableStateOf("Jl. Thamrin KM 3.2 (Posisi Terkini)") }
    var notesText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF131C2E),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, SafeRiderCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .testTag("report_hazard_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Lapor Bahaya Jalan",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bantu sesama pengendara tetap aman",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PILIH JENIS BAHAYA:",
                    color = SafeRiderCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val hazardOptions = listOf(
                    HazardType.PJU_MATI to (Icons.Default.FlashlightOff to SafeRiderAmber),
                    HazardType.JALAN_BERLUBANG to (Icons.Default.Warning to SafeRiderOrange),
                    HazardType.RAWAN_BEGAL to (Icons.Default.Shield to SafeRiderRed),
                    HazardType.BLIND_SPOT to (Icons.Default.VisibilityOff to Color(0xFFE040FB)),
                    HazardType.GENANGAN_AIR to (Icons.Default.WaterDrop to Color(0xFF00B0FF))
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(hazardOptions) { (type, iconColor) ->
                        val isSelected = selectedType == type
                        HazardTypeChip(
                            type = type,
                            icon = iconColor.first,
                            accentColor = iconColor.second,
                            isSelected = isSelected,
                            onSelect = { selectedType = type }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Location Field
                OutlinedTextField(
                    value = locationText,
                    onValueChange = { locationText = it },
                    label = { Text("Lokasi Kejadian", color = Color(0xFF94A3B8)) },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SafeRiderCyan)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hazard_location_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SafeRiderCyan,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes Field
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Keterangan Tambahan (opsional)", color = Color(0xFF94A3B8)) },
                    placeholder = { Text("Contoh: Lubang dalam di lajur kiri setelah halte", color = Color(0xFF64748B)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("hazard_notes_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SafeRiderCyan,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSubmit(
                            selectedType,
                            selectedType.label,
                            locationText,
                            notesText
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeRiderCyan),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_hazard_report_button")
                ) {
                    Text(
                        text = "KIRIM LAPORAN SEKARANG",
                        color = Color(0xFF090D16),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun HazardTypeChip(
    type: HazardType,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color(0xFF1E293B))
            .border(
                1.5.dp,
                if (isSelected) accentColor else Color(0xFF334155),
                RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = type.label,
                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
