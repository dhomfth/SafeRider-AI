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

import com.example.model.RealWorldPoi

object SafeRiderRepository {

    val sampleEmergencyContacts = listOf(
        EmergencyContact("c1", "Call Center Polri", "Layanan Darurat Polisi", "110", isPrimary = true),
        EmergencyContact("c2", "Ambulans Gawat Darurat", "Kesehatan & P3K", "118", isPrimary = false),
        EmergencyContact("c3", "SafeRider Patrol Guard", "Bantuan Derek & Kawal Motor", "0800-1-SAFERIDE", isPrimary = true),
        EmergencyContact("c4", "Ibu (Kontak Keluarga)", "Keluarga Inti", "0812-9876-5432", isPrimary = false)
    )

    val sampleRealWorldPois = listOf(
        RealWorldPoi(
            id = "poi_spbu_sudirman",
            name = "SPBU Pertamina 31.129.02 Sudirman (24 Jam)",
            category = "SPBU_24H",
            x = 0.52f,
            y = 0.45f,
            latitude = -6.2105,
            longitude = 106.8220,
            description = "Penerangan sangat terang, fasilitas toilet, ATM, mini market 24 jam dan CCTV aktif.",
            isOpen24Hours = true,
            googleMapsUrl = "https://maps.google.com/?q=-6.2105,106.8220"
        ),
        RealWorldPoi(
            id = "poi_pos_polisi_hi",
            name = "Pos Polisi Lalu Lintas Bundaran HI",
            category = "POS_POLISI",
            x = 0.40f,
            y = 0.55f,
            latitude = -6.1950,
            longitude = 106.8231,
            description = "Petugas kepolisian siaga 24 jam, area aman untuk berhenti dan meminta pertolongan.",
            isOpen24Hours = true,
            googleMapsUrl = "https://maps.google.com/?q=-6.1950,106.8231"
        ),
        RealWorldPoi(
            id = "poi_rs_siloam",
            name = "IGD 24 Jam RS Siloam MRCCC Semanggi",
            category = "RS_IGD",
            x = 0.68f,
            y = 0.32f,
            latitude = -6.2195,
            longitude = 106.8152,
            description = "Layanan gawat darurat medis trauma & kecelakaan 24 jam.",
            isOpen24Hours = true,
            googleMapsUrl = "https://maps.google.com/?q=-6.2195,106.8152"
        ),
        RealWorldPoi(
            id = "poi_spbu_gatsu",
            name = "SPBU Shell Gatot Subroto 24 Jam",
            category = "SPBU_24H",
            x = 0.82f,
            y = 0.18f,
            latitude = -6.2305,
            longitude = 106.8195,
            description = "Lampu LED terang, musholla, angin nitrogen, dan petugas ramah.",
            isOpen24Hours = true,
            googleMapsUrl = "https://maps.google.com/?q=-6.2305,106.8195"
        ),
        RealWorldPoi(
            id = "poi_pos_polisi_kuningan",
            name = "Pos Polisi Subsektor Kuningan Timur",
            category = "POS_POLISI",
            x = 0.65f,
            y = 0.25f,
            latitude = -6.2280,
            longitude = 106.8320,
            description = "Pos pantau lalu lintas & patroli malam polsek.",
            isOpen24Hours = true,
            googleMapsUrl = "https://maps.google.com/?q=-6.2280,106.8320"
        )
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
                recommendedAction = "Nyalakan lampu jauh seperlunya dan ambil lajur kanan yang lebih terang.",
                latitude = -6.2105,
                longitude = 106.8450,
                googleMapsUrl = "https://maps.google.com/?q=-6.2105,106.8450"
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
                recommendedAction = "Kurangi kecepatan ke 25 km/jam dan hindari lajur kiri.",
                latitude = -6.2260,
                longitude = 106.8580,
                googleMapsUrl = "https://maps.google.com/?q=-6.2260,106.8580"
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
                recommendedAction = "Disarankan belok kanan ke Jl. Protokol Raya yang ramai dan terang.",
                latitude = -6.2410,
                longitude = 106.8900,
                googleMapsUrl = "https://maps.google.com/?q=-6.2410,106.8900"
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
                recommendedAction = "Klakson perlahan dan kurangi kecepatan sebelum masuk tikungan.",
                latitude = -6.2050,
                longitude = 106.8520,
                googleMapsUrl = "https://maps.google.com/?q=-6.2050,106.8520"
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
                recommendedAction = "Hati-hati rem mendadak, pertahankan jarak aman dengan kendaraan depan.",
                latitude = -6.1550,
                longitude = 106.8950,
                googleMapsUrl = "https://maps.google.com/?q=-6.1550,106.8950"
            )
        )
    }

    fun getSampleRoutes(): List<RouteOption> {
        val safeLightPoints = listOf(
            MapPoint(0.15f, 0.85f, 8.8f, "Jl. Medan Merdeka", -6.1754, 106.8272),
            MapPoint(0.25f, 0.70f, 9.4f, "Jl. MH Thamrin", -6.1915, 106.8233),
            MapPoint(0.40f, 0.55f, 9.2f, "Bundaran HI", -6.1950, 106.8231),
            MapPoint(0.58f, 0.40f, 9.5f, "Jl. Jendral Sudirman", -6.2146, 106.8214),
            MapPoint(0.72f, 0.28f, 9.0f, "Semanggi Flyover (Lampu LED Terang)", -6.2195, 106.8125),
            MapPoint(0.85f, 0.15f, 8.9f, "Jl. Gatot Subroto (Tujuan)", -6.2301, 106.8190)
        )

        val fastestPoints = listOf(
            MapPoint(0.15f, 0.85f, 8.0f, "Jl. Medan Merdeka", -6.1754, 106.8272),
            MapPoint(0.30f, 0.75f, 5.0f, "Jl. Kebon Sirih", -6.1830, 106.8310),
            MapPoint(0.42f, 0.50f, 2.2f, "Gang Swadaya Lorong 4 (PJU Mati)", -6.2105, 106.8450),
            MapPoint(0.50f, 0.45f, 2.8f, "Belakang Rel Kereta (Minim Lampu)", -6.2180, 106.8420),
            MapPoint(0.65f, 0.30f, 3.5f, "Jalan Tikus Pemukiman", -6.2230, 106.8350),
            MapPoint(0.85f, 0.15f, 8.5f, "Jl. Gatot Subroto (Tujuan)", -6.2301, 106.8190)
        )

        val alternativePoints = listOf(
            MapPoint(0.15f, 0.85f, 8.5f, "Jl. Medan Merdeka", -6.1754, 106.8272),
            MapPoint(0.20f, 0.50f, 8.2f, "Jl. Boulevard Barat Niaga", -6.1880, 106.8400),
            MapPoint(0.45f, 0.30f, 8.6f, "Kawasan Rasuna Said", -6.2120, 106.8310),
            MapPoint(0.70f, 0.22f, 8.4f, "Kuningan Protokol", -6.2250, 106.8280),
            MapPoint(0.85f, 0.15f, 8.9f, "Jl. Gatot Subroto (Tujuan)", -6.2301, 106.8190)
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
        "📍 Cari SPBU 24 Jam & Pos Polisi terdekat",
        "🗺️ Cek rute Google Maps: Sudirman ke Gatsu",
        "Bagaimana kondisi jalur ini malam hari?",
        "Kenapa Rute SafeLight lebih aman?",
        "Laporkan lampu PJU mati di depan",
        "Tips berkendara motor malam aman"
    )

    fun openNavigationInGoogleMaps(
        context: android.content.Context,
        destLat: Double,
        destLng: Double,
        originLat: Double? = null,
        originLng: Double? = null
    ) {
        try {
            val navUri = android.net.Uri.parse("google.navigation:q=$destLat,$destLng&mode=l")
            val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, navUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val originParam = if (originLat != null && originLng != null) "&origin=$originLat,$originLng" else ""
                val webUri = android.net.Uri.parse("https://www.google.com/maps/dir/?api=1$originParam&destination=$destLat,$destLng&travelmode=two_wheeler")
                val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, webUri).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            val webUri = android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng")
            val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, webUri).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    fun openPlaceInGoogleMaps(
        context: android.content.Context,
        latitude: Double,
        longitude: Double,
        title: String? = null
    ) {
        try {
            val label = title?.let { android.net.Uri.encode(it) } ?: "Titik Lokasi"
            val geoUri = android.net.Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
            val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, geoUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val webUri = android.net.Uri.parse("https://maps.google.com/?q=$latitude,$longitude")
                val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, webUri).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            val webUri = android.net.Uri.parse("https://maps.google.com/?q=$latitude,$longitude")
            val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, webUri).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    fun generateAiResponse(query: String, selectedRoute: RouteOption?): AiMessage {
        val lower = query.lowercase()
        val responseText = when {
            lower.contains("spbu") || lower.contains("polisi") || lower.contains("pos") || lower.contains("dekat") -> {
                "Hasil Pencarian Google Maps (Grounding Real-Time):\n" +
                "1. SPBU Pertamina 31.129.02 Sudirman (24 Jam) - Berjarak 800 meter di lajur kiri. Sangat terang, ATM, CCTV aktif.\n" +
                "2. Pos Polisi Lalu Lintas Bundaran HI - Berjarak 1.4 km. Petugas piket malam siaga.\n" +
                "3. SPBU Shell Gatot Subroto (24 Jam) - Berjarak 2.8 km di rute destinasi Anda."
            }
            lower.contains("safelight") || lower.contains("kenapa") || lower.contains("alasan") -> {
                "Analisis SafeRider AI & Data Google Maps:\n" +
                "• Rute Tercepat: 15 Menit (Melintasi Gang Swadaya - Minim Lampu Jalan, Skor Terang: 3.2/10, 3 titik bahaya).\n" +
                "• Rute SafeLight: 18 Menit (Melintasi Jl. Utama Protokol Sudirman - Terang & Ramai, Skor Terang: 9.2/10, 0 bahaya).\n" +
                "• Rekomendasi: Sangat disarankan memilih Rute SafeLight karena kondisi malam hari. Selisih 3 menit memberikan perlindungan maksimal dari jalan berlubang dan area rawan kejahatan."
            }
            lower.contains("kondisi") || lower.contains("jalur") || lower.contains("malam") -> {
                val routeName = selectedRoute?.title ?: "Rute Aktif"
                "Kondisi Jalur $routeName (Tervalidasi Google Maps):\n" +
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

        val groundedPlaces = if (lower.contains("spbu") || lower.contains("polisi") || lower.contains("pos") || lower.contains("dekat")) {
            listOf(
                com.example.model.GroundedPlace("SPBU Pertamina 24 Jam Sudirman", "https://maps.google.com/?q=-6.2105,106.8220"),
                com.example.model.GroundedPlace("Pos Polisi Bundaran HI", "https://maps.google.com/?q=-6.1950,106.8231")
            )
        } else {
            emptyList()
        }

        return AiMessage(
            id = "ai_${System.currentTimeMillis()}",
            text = responseText,
            isFromUser = false,
            timestamp = "Sekarang",
            modeBadge = "Google Maps Grounded",
            groundedPlaces = groundedPlaces
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
