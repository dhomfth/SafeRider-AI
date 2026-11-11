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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmergencyContact
import com.example.ui.theme.SafeRiderCyan
import com.example.ui.theme.SafeRiderDarkBg
import com.example.ui.theme.SafeRiderGreen
import com.example.ui.theme.SafeRiderOrange
import com.example.ui.theme.SafeRiderRed
import com.example.viewmodel.SafeRiderUiState

@Composable
fun SettingsScreen(
    state: SafeRiderUiState,
    onSelectVehicle: (String) -> Unit,
    onTestVoice: () -> Unit,
    onTriggerSos: () -> Unit,
    onOpenEditContact: (EmergencyContact?) -> Unit,
    onSignInGoogle: () -> Unit,
    onSignOutGoogle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var autoStopAnomalyEnabled by remember { mutableStateOf(true) }
    var highContrastNightEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SafeRiderDarkBg)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = "Pengaturan & Keamanan",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Akun SafeRider, database cloud, & kontak darurat",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }

        // Google Sign-In & Profile Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SafeRiderCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .testTag("auth_profile_card")
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SafeRiderCyan.copy(alpha = 0.2f))
                                    .border(1.5.dp, SafeRiderCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = SafeRiderCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (state.isSignedIn) state.userName ?: "Pengendara SafeRider" else "Belum Masuk Akun",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (state.isSignedIn) state.userEmail ?: "Tersinkronisasi Cloud" else "Masuk untuk sinkronisasi kontak & laporan",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (state.isSignedIn) {
                            IconButton(
                                onClick = onSignOutGoogle,
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = "Keluar",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    if (!state.isSignedIn) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onSignInGoogle,
                            colors = ButtonDefaults.buttonColors(containerColor = SafeRiderCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("sign_in_google_button")
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFF090D16))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign in with Google",
                                color = Color(0xFF090D16),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Vehicle Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF26354D), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "JENIS KENDARAAN",
                        color = SafeRiderCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VehicleButton(
                            title = "Sepeda Motor",
                            subtitle = "Prioritas rute terang & hindari begal",
                            icon = Icons.Default.DirectionsBike,
                            isSelected = state.vehicleType == "Sepeda Motor",
                            onClick = { onSelectVehicle("Sepeda Motor") },
                            modifier = Modifier.weight(1f)
                        )
                        VehicleButton(
                            title = "Mobil Pribadi",
                            subtitle = "Jalan lebar & aspal mulus",
                            icon = Icons.Default.DirectionsCar,
                            isSelected = state.vehicleType == "Mobil",
                            onClick = { onSelectVehicle("Mobil") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Emergency & Anomaly Detection Settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF26354D), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "DETEKSI KEAMANAN OTOMATIS",
                        color = SafeRiderRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Deteksi Berhenti di Area Gelap (>3 Mnt)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Memicu konfirmasi keamanan 5 detik jika kendaraan berhenti di lokasi ber-Illumination Score < 3.0",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = autoStopAnomalyEnabled,
                            onCheckedChange = { autoStopAnomalyEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SafeRiderRed
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mode Kontras Tinggi Malam (Night HUD)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Meminimalkan silau layar untuk menjaga adaptasi mata pengendara malam",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = highContrastNightEnabled,
                            onCheckedChange = { highContrastNightEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SafeRiderCyan
                            )
                        )
                    }
                }
            }
        }

        // Voice Assistant & TTS Settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF26354D), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SUARA NAVIGASI (TEXT-TO-SPEECH)",
                        color = SafeRiderCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Format Suara: [KONDISI] + [JARAK] + [TINDAKAN REKOMENDASI]",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onTestVoice,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SafeRiderCyan, RoundedCornerShape(12.dp))
                            .testTag("test_voice_tts_button")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = SafeRiderCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Uji Suara Peringatan Sekarang",
                            color = SafeRiderCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Emergency Contacts Card with Editing Options
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF26354D), RoundedCornerShape(16.dp))
                    .testTag("emergency_contacts_settings_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "KONTAK DARURAT SOS",
                                color = SafeRiderRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ketuk kontak untuk mengubah nomor / nama",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }

                        // Add contact button
                        Button(
                            onClick = { onOpenEditContact(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeRiderCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp).testTag("add_contact_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF090D16), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah", color = Color(0xFF090D16), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    state.emergencyContacts.forEach { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E283D))
                                .clickable { onOpenEditContact(contact) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("contact_item_${contact.id}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(contact.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    if (contact.isPrimary) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SafeRiderGreen.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Utama", color = SafeRiderGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text("${contact.role} • ${contact.phoneNumber}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }

                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Ubah Kontak",
                                tint = SafeRiderCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // About
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SafeRider AI v1.0.0",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Didukung Gemini 3.8 Flash, Firestore Cloud, & SafeLight Engine",
                    color = Color(0xFF475569),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun VehicleButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SafeRiderCyan.copy(alpha = 0.15f) else Color(0xFF0F1726))
            .border(
                1.5.dp,
                if (isSelected) SafeRiderCyan else Color(0xFF26354D),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) SafeRiderCyan else Color(0xFF94A3B8),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                lineHeight = 12.sp
            )
        }
    }
}
