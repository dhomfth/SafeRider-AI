package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.EmergencyContact
import com.example.ui.theme.SafeRiderCyan
import com.example.ui.theme.SafeRiderGreen
import com.example.ui.theme.SafeRiderRed

@Composable
fun EditContactDialog(
    contact: EmergencyContact?,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSaveContact: (EmergencyContact) -> Unit,
    onDeleteContact: ((String) -> Unit)? = null
) {
    if (!isOpen) return

    var name by remember(contact) { mutableStateOf(contact?.name ?: "") }
    var role by remember(contact) { mutableStateOf(contact?.role ?: "Keluarga") }
    var phone by remember(contact) { mutableStateOf(contact?.phoneNumber ?: "") }
    var isPrimary by remember(contact) { mutableStateOf(contact?.isPrimary ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF131D2E),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, SafeRiderCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .testTag("edit_contact_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (contact == null) "Tambah Kontak Darurat" else "Ubah Info Kontak",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Akan dihubungi saat pemicu SOS SafeRider aktif",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kontak / Instansi", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SafeRiderCyan) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SafeRiderCyan,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Role field
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Hubungan (Keluarga, Pasangan, Satpam, dll)", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = SafeRiderCyan) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_role_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SafeRiderCyan,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor Telepon / WhatsApp", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = SafeRiderCyan) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_phone_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SafeRiderCyan,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Primary toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Jadikan Kontak Utama",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Prioritas pertama panggilan otomatis dan SMS darurat",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SafeRiderGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Save button
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            onSaveContact(
                                EmergencyContact(
                                    id = contact?.id ?: "c_${System.currentTimeMillis()}",
                                    name = name,
                                    role = role,
                                    phoneNumber = phone,
                                    isPrimary = isPrimary
                                )
                            )
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeRiderCyan),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_contact_button")
                ) {
                    Text(
                        text = "SIMPAN KONTAK",
                        color = Color(0xFF090D16),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                if (contact != null && onDeleteContact != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onDeleteContact(contact.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261214)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("delete_contact_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = SafeRiderRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hapus Kontak", color = SafeRiderRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
