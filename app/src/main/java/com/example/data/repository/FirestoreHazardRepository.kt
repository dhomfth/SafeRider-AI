package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.EmergencyContact
import com.example.model.HazardMarker
import com.example.model.HazardType
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreHazardRepository(private val context: Context) {

    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val auth by lazy { Firebase.auth }

    fun currentUserId(): String? = auth.currentUser?.uid
    fun currentUserName(): String? = auth.currentUser?.displayName ?: "Pengendara SafeRider"

    // Real-time observation of community hazards
    fun observeHazards(): Flow<List<HazardMarker>> {
        return db.collection("hazards")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val typeStr = doc.getString("type") ?: "PJU_MATI"
                        val type = try {
                            HazardType.valueOf(typeStr)
                        } catch (e: Exception) {
                            HazardType.PJU_MATI
                        }
                        HazardMarker(
                            id = doc.id,
                            type = type,
                            title = doc.getString("title") ?: "Bahaya Jalan",
                            locationName = doc.getString("locationName") ?: "Lokasi",
                            x = (doc.getDouble("x") ?: 0.5).toFloat(),
                            y = (doc.getDouble("y") ?: 0.5).toFloat(),
                            illuminationScore = (doc.getDouble("illuminationScore") ?: 3.0).toFloat(),
                            reportedAgo = "Komunitas SafeRider",
                            upvotes = (doc.getLong("upvotes") ?: 1L).toInt(),
                            verified = doc.getBoolean("verified") ?: false,
                            distanceMeters = (doc.getLong("distanceMeters") ?: 250L).toInt(),
                            recommendedAction = doc.getString("recommendedAction") ?: "Kurangi kecepatan dan waspada.",
                            notes = doc.getString("notes") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.w("FirestoreHazard", "Failed to parse hazard doc ${doc.id}", e)
                        null
                    }
                }
            }
            .catch { e ->
                if (e is CancellationException) throw e
                Log.e("FirestoreHazard", "Error observing hazards from Firestore", e)
                emit(emptyList())
            }
    }

    suspend fun addHazard(hazard: HazardMarker): Boolean {
        val uid = auth.currentUser?.uid ?: "anonymous_user"
        val docRef = db.collection("hazards").document()
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "userName" to currentUserName()!!,
            "type" to hazard.type.name,
            "title" to hazard.title,
            "locationName" to hazard.locationName,
            "notes" to hazard.notes,
            "x" to hazard.x.toDouble(),
            "y" to hazard.y.toDouble(),
            "illuminationScore" to hazard.illuminationScore.toDouble(),
            "upvotes" to hazard.upvotes,
            "verified" to hazard.verified,
            "distanceMeters" to hazard.distanceMeters,
            "recommendedAction" to hazard.recommendedAction,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreHazard", "Failed to save hazard to Firestore", e)
            false
        }
    }

    suspend fun upvoteHazard(hazardId: String) {
        val docRef = db.collection("hazards").document(hazardId)
        try {
            docRef.update(
                mapOf(
                    "upvotes" to FieldValue.increment(1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreHazard", "Failed to upvote hazard $hazardId", e)
        }
    }

    // Emergency Contacts per user
    fun observeUserContacts(userId: String): Flow<List<EmergencyContact>> {
        return db.collection("users").document(userId).collection("contacts")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        EmergencyContact(
                            id = doc.id,
                            name = doc.getString("name") ?: "Kontak Darurat",
                            role = doc.getString("role") ?: "Keluarga",
                            phoneNumber = doc.getString("phoneNumber") ?: "",
                            isPrimary = doc.getBoolean("isPrimary") ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .catch { e ->
                if (e is CancellationException) throw e
                Log.e("FirestoreHazard", "Error observing contacts", e)
                emit(emptyList())
            }
    }

    suspend fun saveContact(userId: String, contact: EmergencyContact) {
        val docRef = if (contact.id.isBlank() || contact.id.startsWith("c")) {
            db.collection("users").document(userId).collection("contacts").document()
        } else {
            db.collection("users").document(userId).collection("contacts").document(contact.id)
        }

        val payload = hashMapOf<String, Any>(
            "userId" to userId,
            "name" to contact.name,
            "role" to contact.role,
            "phoneNumber" to contact.phoneNumber,
            "isPrimary" to contact.isPrimary,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreHazard", "Failed to save contact", e)
            throw e
        }
    }

    suspend fun deleteContact(userId: String, contactId: String) {
        try {
            db.collection("users").document(userId).collection("contacts").document(contactId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreHazard", "Failed to delete contact $contactId", e)
        }
    }
}
