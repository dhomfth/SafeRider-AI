package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HazardMarker
import com.example.model.HazardType
import com.example.ui.components.RouteMapCanvas
import com.example.ui.theme.SafeRiderAmber
import com.example.ui.theme.SafeRiderCyan
import com.example.ui.theme.SafeRiderDarkBg
import com.example.ui.theme.SafeRiderGreen
import com.example.ui.theme.SafeRiderOrange
import com.example.ui.theme.SafeRiderRed
import com.example.viewmodel.SafeRiderUiState

@Composable
fun HazardsMapScreen(
    state: SafeRiderUiState,
    onFilterChange: (HazardType?) -> Unit,
    onUpvoteHazard: (String) -> Unit,
    onCrossCheckLocation: (HazardMarker) -> Unit,
    onOpenReportDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHazardDetail by remember { mutableStateOf<HazardMarker?>(null) }

    val filteredHazards = if (state.selectedHazardFilter == null) {
        state.hazards
    } else {
        state.hazards.filter { it.type == state.selectedHazardFilter }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SafeRiderDarkBg)
            .testTag("hazards_map_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Peta Bahaya Komunitas",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Laporan real-time titik gelap & rawan di sekitar Anda",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SafeRiderOrange.copy(alpha = 0.2f))
                            .border(1.dp, SafeRiderOrange, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${filteredHazards.size} Titik Aktif",
                            color = SafeRiderOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Map View of Hazards
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, Color(0xFF26354D), RoundedCornerShape(18.dp))
                ) {
                    RouteMapCanvas(
                        route = null,
                        hazards = filteredHazards,
                        pois = state.realWorldPois,
                        mapStyle = state.mapStyleMode,
                        progressRatio = 0f,
                        isNavigating = false,
                        showInteractiveControls = true,
                        onHazardSelected = { hazard ->
                            selectedHazardDetail = hazard
                        },
                        onUpvoteHazard = onUpvoteHazard
                    )
                }
            }

            // Filter Tabs
            item {
                val filterItems = listOf(
                    null to "Semua",
                    HazardType.PJU_MATI to "Lampu Mati",
                    HazardType.JALAN_BERLUBANG to "Berlubang",
                    HazardType.RAWAN_BEGAL to "Rawan Begal",
                    HazardType.BLIND_SPOT to "Blind Spot",
                    HazardType.GENANGAN_AIR to "Genangan"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterItems) { (type, label) ->
                        val isSelected = state.selectedHazardFilter == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) SafeRiderCyan else Color(0xFF1E293B))
                                .clickable { onFilterChange(type) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("filter_tab_${label}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color(0xFF090D16) else Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // List of Hazards
            items(filteredHazards) { hazard ->
                HazardCard(
                    hazard = hazard,
                    onUpvote = { onUpvoteHazard(hazard.id) },
                    onCrossCheck = { onCrossCheckLocation(hazard) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // Floating Action Button to Report
        FloatingActionButton(
            onClick = onOpenReportDialog,
            containerColor = SafeRiderCyan,
            contentColor = Color(0xFF090D16),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_report_hazard")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Lapor Bahaya")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lapor Bahaya", fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun HazardCard(
    hazard: HazardMarker,
    onUpvote: () -> Unit,
    onCrossCheck: () -> Unit
) {
    val (typeColor, typeIcon) = when (hazard.type) {
        HazardType.PJU_MATI -> SafeRiderAmber to Icons.Default.FlashlightOff
        HazardType.JALAN_BERLUBANG -> SafeRiderOrange to Icons.Default.Warning
        HazardType.RAWAN_BEGAL -> SafeRiderRed to Icons.Default.Shield
        HazardType.BLIND_SPOT -> Color(0xFFE040FB) to Icons.Default.VisibilityOff
        HazardType.GENANGAN_AIR -> Color(0xFF00B0FF) to Icons.Default.WaterDrop
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121A2A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, typeColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .testTag("hazard_item_${hazard.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(typeColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = hazard.type.label,
                        color = typeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Verified Badge
                if (hazard.verified) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SafeRiderGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Diverifikasi",
                            color = SafeRiderGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = hazard.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${hazard.locationName} • ${hazard.reportedAgo}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recommended Action Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E283D))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Tindakan: ${hazard.recommendedAction}",
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skor Terang: ${hazard.illuminationScore}/10",
                    color = if (hazard.illuminationScore < 4f) SafeRiderRed else SafeRiderAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Google Cross-Check button
                    Button(
                        onClick = onCrossCheck,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2438)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .border(1.dp, SafeRiderCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .testTag("cross_check_btn_${hazard.id}")
                    ) {
                        Text(
                            text = "Cross-Check",
                            color = SafeRiderCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Upvote button
                    Button(
                        onClick = onUpvote,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp).testTag("upvote_btn_${hazard.id}")
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = SafeRiderCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Valid (${hazard.upvotes})",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
