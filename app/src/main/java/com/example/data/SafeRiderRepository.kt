package com.example.data

import com.example.model.AiMessage
import com.example.model.AlertSeverity
import com.example.model.EmergencyContact
import com.example.model.HazardMarker
import com.example.model.HazardType
import com.example.model.MapPoint
import com.example.model.RouteOption
import com.example.model.RouteStep
import com.example.model.VoiceNavAlert

object SafeRiderRepository {

    val sampleEmergencyContacts = listOf(
        EmergencyContact("c1", "Call Center Polri", "Layanan Darurat Polisi", "110", isPrimary = true),
        EmergencyContact("c2", "Ambulans Gawat Darurat", "Kesehatan & P3K", "118", isPrimary = false),
        EmergencyContact("c3", "SafeRider Patrol Guard", "Bantuan Derek & Kawal Motor", "0800-1-SAFERIDE", isPrimary = true),
        EmergencyContact("c4", "Ibu (Kontak Keluarga)", "Keluarga Inti", "0812-9876-5432", isPrimary = false)
    )

    fun getInitialHazards(): List<HazardMarker> {
        return listOf(
            HazardMarker(
                id = "h1",
                type = HazardType.PJU_MATI,
                title = "Lampu PJU Mati Total (200m)",
                locationName = "Jl. Swadaya Lorong 4, dekat rel",
                x = 0.42f,
                y = 0.48f,
                illuminationScore = 2.1f,
                reportedAgo = "12 mnt lalu",
                upvotes = 18,
                verified = true,
                distanceMeters = 250,
                recommendedAction = "Nyalakan lampu jauh seperlunya dan ambil lajur kanan yang lebih terang."
            ),
            HazardMarker(
                id = "h2",
                type = HazardType.JALAN_BERLUBANG,
                title = "Lubang Dalam di Lajur Kiri",
                locationName = "Underpass Tebet Raya arah timur",
                x = 0.55f,
                y = 0.35f,
                illuminationScore = 4.0f,
                reportedAgo = "25 mnt lalu",
                upvotes = 34,
                verified = true,
                distanceMeters = 500,
                recommendedAction = "Kurangi kecepatan ke 25 km/jam dan hindari lajur kiri."
            ),
            HazardMarker(
                id = "h3",
                type = HazardType.RAWAN_BEGAL,
                title = "Area Rawan Kejahatan Malam",
                locationName = "Tanggul Kali Malang KM 4 (Area Gelap)",
                x = 0.35f,
                y = 0.62f,
                illuminationScore = 2.8f,
                reportedAgo = "1 jam lalu",
                upvotes = 52,
                verified = true,
                distanceMeters = 1200,
                recommendedAction = "Disarankan belok kanan ke Jl. Protokol Raya yang ramai dan terang."
            ),
            HazardMarker(
                id = "h4",
                type = HazardType.BLIND_SPOT,
                title = "Tikungan Tajam Minim Rambu & Gelap",
                locationName = "Tikungan Jembatan Kali Ciliwung",
                x = 0.68f,
                y = 0.70f,
                illuminationScore = 3.8f,
                reportedAgo = "2 jam lalu",
                upvotes = 14,
                verified = false,
                distanceMeters = 850,
                recommendedAction = "Klakson perlahan dan kurangi kecepatan sebelum masuk tikungan."
            ),
            HazardMarker(
                id = "h5",
                type = HazardType.GENANGAN_AIR,
                title = "Genangan Air & Aspal Licin",
                locationName = "Depan SPBU Boulevard Barat",
                x = 0.28f,
                y = 0.22f,
                illuminationScore = 6.5f,
                reportedAgo = "45 mnt lalu",
                upvotes = 9,
                verified = true,
                distanceMeters = 1600,
                recommendedAction = "Hati-hati rem mendadak, pertahankan jarak aman dengan kendaraan depan."
            )
        )
    }

    fun getSampleRoutes(): List<RouteOption> {
        val safeLightPoints = listOf(
            MapPoint(0.15f, 0.85f, 8.8f, "Jl. Medan Merdeka"),
            MapPoint(0.25f, 0.70f, 9.4f, "Jl. MH Thamrin"),
            MapPoint(0.40f, 0.55f, 9.2f, "Bundaran HI"),
            MapPoint(0.58f, 0.40f, 9.5f, "Jl. Jendral Sudirman"),
            MapPoint(0.72f, 0.28f, 9.0f, "Semanggi Flyover (Lampu LED Terang)"),
            MapPoint(0.85f, 0.15f, 8.9f, "Jl. Gatot Subroto (Tujuan)")
        )

        val fastestPoints = listOf(
            MapPoint(0.15f, 0.85f, 8.0f, "Jl. Medan Merdeka"),
            MapPoint(0.30f, 0.75f, 5.0f, "Jl. Kebon Sirih"),
            MapPoint(0.42f, 0.50f, 2.2f, "Gang Swadaya Lorong 4 (PJU Mati)"),
            MapPoint(0.50f, 0.45f, 2.8f, "Belakang Rel Kereta (Minim Lampu)"),
            MapPoint(0.65f, 0.30f, 3.5f, "Jalan Tikus Pemukiman"),
            MapPoint(0.85f, 0.15f, 8.5f, "Jl. Gatot Subroto (Tujuan)")
        )

        val alternativePoints = listOf(
            MapPoint(0.15f, 0.85f, 8.5f, "Jl. Medan Merdeka"),
            MapPoint(0.20f, 0.50f, 8.2f, "Jl. Boulevard Barat Niaga"),
            MapPoint(0.45f, 0.30f, 8.6f, "Kawasan Rasuna Said"),
            MapPoint(0.70f, 0.22f, 8.4f, "Kuningan Protokol"),
            MapPoint(0.85f, 0.15f, 8.9f, "Jl. Gatot Subroto (Tujuan)")
        )

        val safeLightSteps = listOf(
            RouteStep("Mulai dari posisi Anda di Jl. Medan Merdeka", 300, "Jl. Medan Merdeka", 8.8f, "straight"),
            RouteStep("Lurus 1.2 km di Jl. MH Thamrin (Penerangan Sangat Baik)", 1200, "Jl. MH Thamrin", 9.4f, "straight"),
            RouteStep("Lanjutkan melewati Bundaran HI ke Jl. Sudirman", 2400, "Jl. Jend. Sudirman", 9.3f, "straight"),
            RouteStep("Ambil jalur lambat Semanggi, ada pos pantau polisi", 1800, "Semanggi Flyover", 9.5f, "turn_right"),
            RouteStep("Belok kiri ke Jl. Gatot Subroto menuju destinasi", 900, "Jl. Gatot Subroto", 9.0f, "destination")
        )

        val fastestSteps = listOf(
            RouteStep("Mulai dari posisi Anda, masuk ke Jl. Kebon Sirih", 400, "Jl. Kebon Sirih", 6.0f, "straight"),
            RouteStep("Belok kanan masuk Gang Swadaya Lorong 4 (PERINGATAN: Gelap & PJU Mati)", 1500, "Gang Swadaya", 2.2f, "turn_right"),
            RouteStep("Waspada lubang dalam di samping rel kereta", 1100, "Jalur Rel Kereta", 2.8f, "warning"),
            RouteStep("Keluar gang sempit menuju Jl. Gatot Subroto", 800, "Akses Belakang", 4.0f, "turn_left"),
            RouteStep("Tiba di destinasi", 200, "Jl. Gatot Subroto", 8.5f, "destination")
        )

        val alternativeSteps = listOf(
            RouteStep("Arahkan ke Jl. Boulevard Barat Niaga", 800, "Boulevard Barat", 8.2f, "straight"),
            RouteStep("Lurus sepanjang Rasuna Said, penerangan stabil", 3200, "Jl. HR Rasuna Said", 8.6f, "straight"),
            RouteStep("Belok kanan ke Jl. Gatot Subroto", 1400, "Jl. Gatot Subroto", 8.4f, "turn_right"),
            RouteStep("Tiba di destinasi", 400, "Jl. Gatot Subroto", 8.9f, "destination")
        )

        return listOf(
            RouteOption(
                id = "route_safelight",
                title = "Rute SafeLight (Terang & Aman)",
                routeType = "SAFELIGHT",
                subtitle = "Via Jl. Utama Protokol Sudirman - Ramai & Terang",
                durationMinutes = 18,
                distanceKm = 7.4f,
                illuminationScore = 9.2f,
                safetyIndexPercent = 96,
                hazardCount = 0,
                pjuCoveragePercent = 98,
                streetWidth = "Lebar (4-6 Jalur Aspal Mulus)",
                patrolInfo = "Patroli Polisi & Satpam Rutin 24 Jam",
                isRecommended = true,
                aiReasoning = "Disarankan memilih Rute SafeLight karena kondisi malam hari. Selisih hanya +3 menit, namun 100% bebas dari titik rawan begal dan memiliki penerangan lampu jalan 9.2/10.",
                pathPoints = safeLightPoints,
                steps = safeLightSteps
            ),
            RouteOption(
                id = "route_fastest",
                title = "Rute Tercepat (Minim Lampu)",
                routeType = "FASTEST",
                subtitle = "Via Gang Swadaya & Pemukiman Sepi",
                durationMinutes = 15,
                distanceKm = 5.8f,
                illuminationScore = 3.2f,
                safetyIndexPercent = 42,
                hazardCount = 3,
                pjuCoveragePercent = 28,
                streetWidth = "Sempit (Gang 1 Mobil, Aspal Berlubang)",
                patrolInfo = "Tidak ada pos keamanan aktif malam",
                isRecommended = false,
                aiReasoning = "Peringatan: Rute tercepat melintasi Gang Swadaya yang minim lampu (3.2/10), terdapat 2 titik jalan rusak parah, dan memiliki riwayat kerawanan malam.",
                pathPoints = fastestPoints,
                steps = fastestSteps
            ),
            RouteOption(
                id = "route_alternative",
                title = "Rute Boulevard Barat",
                routeType = "ALTERNATIVE",
                subtitle = "Via Kawasan Bisnis Rasuna Said",
                durationMinutes = 21,
                distanceKm = 8.2f,
                illuminationScore = 8.5f,
                safetyIndexPercent = 89,
                hazardCount = 1,
                pjuCoveragePercent = 85,
                streetWidth = "Sedang (3 Jalur)",
                patrolInfo = "Keamanan gedung perkantoran aktif",
                isRecommended = false,
                aiReasoning = "Alternatif yang cukup aman dan terang, namun sedikit memutar dengan waktu tempuh 21 menit.",
                pathPoints = alternativePoints,
                steps = alternativeSteps
            )
        )
    }

    val predefinedAiPrompts = listOf(
        "Bagaimana kondisi jalur ini malam hari?",
        "Kenapa Rute SafeLight lebih aman?",
        "Apa arti Illumination Score 9.2?",
        "Laporkan lampu PJU mati di depan",
        "Tips berkendara motor malam aman"
    )

    fun generateAiResponse(query: String, selectedRoute: RouteOption?): AiMessage {
        val lower = query.lowercase()
        val responseText = when {
            lower.contains("safelight") || lower.contains("kenapa") || lower.contains("alasan") -> {
                "Analisis SafeRider AI:\n" +
                "• Rute Tercepat: 15 Menit (Melintasi Gang Swadaya - Minim Lampu Jalan, Skor Terang: 3.2/10, 3 titik bahaya).\n" +
                "• Rute SafeLight: 18 Menit (Melintasi Jl. Utama Protokol Sudirman - Terang & Ramai, Skor Terang: 9.2/10, 0 bahaya).\n" +
                "• Rekomendasi: Sangat disarankan memilih Rute SafeLight karena kondisi malam hari. Selisih 3 menit memberikan perlindungan maksimal dari jalan berlubang dan area rawan kejahatan."
            }
            lower.contains("kondisi") || lower.contains("jalur") || lower.contains("malam") -> {
                val routeName = selectedRoute?.title ?: "Rute Aktif"
                "Kondisi Jalur $routeName:\n" +
                "Tingkat Penerangan: ${selectedRoute?.illuminationScore ?: 9.2}/10 (Tinggi)\n" +
                "Cakupan Lampu PJU: ${selectedRoute?.pjuCoveragePercent ?: 98}%\n" +
                "Kondisi Jalan: Aspal rata, 4 lajur, jarak pandang jernih hingga 80 meter.\n" +
                "Tetap nyalakan lampu utama motor dan jaga kecepatan aman di bawah 50 km/jam."
            }
            lower.contains("score") || lower.contains("skor") || lower.contains("arti") -> {
                "Indeks Penerangan Jalan (Illumination Score 1 - 10):\n" +
                "• 1.0 - 4.0: Gelap Gulita / Sangat Minim PJU. Risiko tinggi kejahatan dan lubang tak terlihat.\n" +
                "• 4.1 - 7.0: Penerangan Sedang. Lampu jalan selang-seling.\n" +
                "• 7.1 - 10.0: Sangat Terang (SafeLight Standard). Lampu LED merata, CCTV jalan aktif, dan visibilitas optimal."
            }
            lower.contains("lapor") || lower.contains("pju") || lower.contains("mati") -> {
                "Laporan diterima. Titik lampu PJU mati telah ditandai pada koordinat terkini Anda. Data ini disinkronkan ke seluruh komunitas SafeRider dan diteruskan ke dashboard dinas penerangan jalan umum."
            }
            lower.contains("tips") || lower.contains("aman") -> {
                "Tips Berkendara Malam Hari SafeRider:\n" +
                "1. Prioritaskan jalur ber-Illumination Score > 8.0 meskipun selisih beberapa menit.\n" +
                "2. Gunakan jaket atau helm dengan strip reflektif agar mudah terlihat pengendara lain.\n" +
                "3. Jika harus berhenti mendadak, pilih area terang seperti SPBU atau minimarket 24 jam.\n" +
                "4. Aktifkan fitur SOS SafeRider agar kontak darurat dapat melacak Anda jika terjadi anomali berhenti."
            }
            else -> {
                "SafeRider AI siap memandu. Seluruh sensor keamanan aktif: Indeks penerangan terpantau ${selectedRoute?.illuminationScore ?: 9.2}/10. Jika Anda mendeteksi bahaya di depan, katakan atau pilih 'Lapor Bahaya' untuk memperingatkan pengendara lain."
            }
        }

        return AiMessage(
            id = "ai_${System.currentTimeMillis()}",
            text = responseText,
            isFromUser = false,
            timestamp = "Sekarang",
            modeBadge = "SafeRider AI"
        )
    }

    val sampleVoiceAlerts = listOf(
        VoiceNavAlert(
            id = "va1",
            condition = "Rute gelap dan rawan lubang",
            distance = "200 meter di depan",
            action = "Disarankan ambil jalur kanan yang lebih terang",
            fullSpokenText = "Peringatan: 200 meter di depan terdapat rute gelap dan rawan lubang. Disarankan ambil jalur kanan yang lebih terang."
        ),
        VoiceNavAlert(
            id = "va2",
            condition = "Lampu PJU padam sepanjang 150 meter",
            distance = "350 meter di depan",
            action = "Nyalakan lampu jauh dan kurangi kecepatan",
            fullSpokenText = "Peringatan: 350 meter di depan terdapat lampu PJU padam sepanjang 150 meter. Nyalakan lampu jauh dan kurangi kecepatan."
        ),
        VoiceNavAlert(
            id = "va3",
            condition = "Area rawan begal dan minim keramaian",
            distance = "500 meter di depan",
            action = "Tetap berada di rombongan kendaraan atau ambil jalan utama",
            fullSpokenText = "Peringatan: 500 meter di depan terdapat area rawan begal dan minim keramaian. Tetap berada di rombongan kendaraan atau ambil jalan utama."
        )
    )
}
