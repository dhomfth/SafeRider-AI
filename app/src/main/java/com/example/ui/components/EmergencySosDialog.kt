package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.EmergencyContact
import com.example.ui.theme.SafeRiderCyan
import com.example.ui.theme.SafeRiderGreen
import com.example.ui.theme.SafeRiderOrange
import com.example.ui.theme.SafeRiderRed

@Composable
fun EmergencySosDialog(
    isOpen: Boolean,
    countdownSeconds: Int,
    isDispatched: Boolean,
    triggerReason: String?,
    contacts: List<EmergencyContact>,
    isStrobeActive: Boolean,
    onCancel: () -> Unit,
    onDispatchNow: () -> Unit,
    onToggleStrobe: () -> Unit
) {
    if (!isOpen) return

    val infiniteTransition = rememberInfiniteTransition(label = "strobe")
    val strobeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobeAlpha"
    )

    Dialog(
        onDismissRequest = { /* Prevent accidental dismiss */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isStrobeActive) {
                        SafeRiderRed.copy(alpha = strobeAlpha)
                    } else {
                        Color(0xFF090D16).copy(alpha = 0.97f)
                    }
                )
                .testTag("emergency_sos_dialog")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!isDispatched && countdownSeconds > 0) {
                    // Stage 1: 5-Second Confirmation Countdown
                    CountdownConfirmView(
                        countdown = countdownSeconds,
                        triggerReason = triggerReason,
                        onCancel = onCancel,
                        onDispatchNow = onDispatchNow
                    )
                } else {
                    // Stage 2: Dispatched Status & Quick Emergency Actions
                    DispatchedSosView(
                        contacts = contacts,
                        isStrobeActive = isStrobeActive,
                        onCancel = onCancel,
                        onToggleStrobe = onToggleStrobe
                    )
                }
            }
        }
    }
}

@Composable
private fun CountdownConfirmView(
    countdown: Int,
    triggerReason: String?,
    onCancel: () -> Unit,
    onDispatchNow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF190B0D))
            .border(2.dp, SafeRiderRed, RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(SafeRiderRed.copy(alpha = 0.2f))
                .border(2.dp, SafeRiderRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "SOS Alert",
                tint = SafeRiderRed,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "DETEKSI KEAMANAN DARURAT",
            color = SafeRiderRed,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = triggerReason ?: "Pemicu darurat aktif",
            color = Color(0xFFFFB4AB),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Big Countdown Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            CircularProgressIndicator(
                progress = { countdown / 5f },
                modifier = Modifier.size(120.dp),
                color = SafeRiderRed,
                strokeWidth = 8.dp,
                trackColor = Color(0xFF3B1015)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$countdown",
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "DETIK",
                    color = SafeRiderRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Apakah Anda aman?\nSinyal darurat dikirim otomatis saat hitungan habis.",
            color = Color(0xFFE2E8F0),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Big safe button
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = SafeRiderGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("sos_i_am_safe_button")
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SAYA AMAN (BATALKAN)",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onDispatchNow,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SafeRiderRed),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(SafeRiderRed, SafeRiderRed))),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("sos_dispatch_now_button")
        ) {
            Icon(Icons.Default.Send, contentDescription = null, tint = SafeRiderRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "KIRIM SOS SEKARANG",
                color = SafeRiderRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DispatchedSosView(
    contacts: List<EmergencyContact>,
    isStrobeActive: Boolean,
    onCancel: () -> Unit,
    onToggleStrobe: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF141A26))
            .border(2.dp, SafeRiderRed, RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(SafeRiderRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SINYAL SOS AKTIF",
                    color = SafeRiderRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
            
            // Strobe toggle
            Button(
                onClick = onToggleStrobe,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isStrobeActive) SafeRiderOrange else Color(0xFF334155)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(36.dp).testTag("toggle_strobe_button")
            ) {
                Icon(
                    Icons.Default.FlashOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isStrobeActive) Color.Black else Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isStrobeActive) "Strobe ON" else "Strobe",
                    fontSize = 12.sp,
                    color = if (isStrobeActive) Color.Black else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Broadcasted GPS Coordinates Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SafeRiderCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lokasi Live Disiarkan ke Tim Reaksi Cepat",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Koordinat: -6.2088° S, 106.8456° E (Jl. Gatot Subroto KM 4.2)",
                    color = SafeRiderCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Akurasi: ±3 meter • Baterai HP: 84% • Kecepatan: 0 km/h",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "HUBUNGI NOMOR DARURAT:",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        contacts.take(3).forEach { contact ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E283D))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = contact.name,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${contact.role} • ${contact.phoneNumber}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                Button(
                    onClick = { /* simulated dial */ },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeRiderRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Panggil", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Panggil", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("sos_end_button")
        ) {
            Text(
                text = "Akhiri Mode Darurat (Kembali Normal)",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
