# MindCare

Aplikasi mobile untuk analisis kesehatan mental — pengguna menulis atau merekam suara tentang perasaannya, lalu backend (model ML) menganalisis dan mengembalikan kategori dominan (stres/depresi/kecemasan) beserta tingkat kepercayaannya. Project ini adalah hasil migrasi dari app Android native (`Frontend-Mobile`, Jetpack Compose) ke **Kotlin Multiplatform + Compose Multiplatform**, supaya UI dan business logic-nya bisa dipakai bersama di Android maupun iOS.

## Tech Stack

| Area | Library |
|---|---|
| UI | Compose Multiplatform |
| Networking | Ktor Client (`ktor-client-okhttp` di Android, `ktor-client-darwin` di iOS) |
| Serialization | kotlinx.serialization |
| Tanggal/waktu | kotlinx-datetime |
| Storage lokal (token, preferensi) | multiplatform-settings |
| Navigasi | androidx.navigation (versi multiplatform) |
| Resource gambar | Compose Multiplatform Resources (`Res.drawable.*`) |

## Struktur Project

```
mindcare/
├── shared/                 # Kode bersama (UI + business logic)
│   └── src/
│       ├── commonMain/      # Kode yang sama untuk semua platform
│       │   ├── kotlin/com/example/mindcare/
│       │   │   ├── data/         # Model, API service (Ktor), AppSettings
│       │   │   ├── navigation/   # Routes & AppNavigation
│       │   │   ├── platform/     # expect class/fun (AudioRecorder, ReminderScheduler, dll)
│       │   │   └── ui/           # screens/, components/, theme/
│       │   └── composeResources/drawable/  # Icon & gambar (Res.drawable.*)
│       ├── androidMain/      # Implementasi actual khusus Android (native, fitur penuh)
│       └── iosMain/          # Implementasi actual khusus iOS (saat ini stub/no-op)
├── androidApp/              # Entry point Android (MainActivity, Manifest)
├── iosApp/                  # Entry point iOS (SwiftUI shell)
└── assets/icon-source/      # Source asset icon app (resolusi tinggi, bukan dibundle ke app)
```

## Fitur

- **Auth** — Login, Sign Up. ("Lupa kata sandi" untuk sementara dinonaktifkan di Login screen.)
- **Chat** — kirim teks atau rekam audio untuk dianalisis; ada visualisasi waveform saat merekam, shimmer loading saat menunggu hasil analisis, dan riwayat sesi (drawer).
- **Dashboard** — statistik 7 hari terakhir (total analisis, kondisi dominan, tren mingguan per kategori), dihitung di backend lewat endpoint `GET /analysis/dashboard-stats`.
- **Settings** — edit profil, ganti password, atur reminder notifikasi harian, logout.

## Dukungan Platform

| Fitur native | Android | iOS |
|---|---|---|
| Rekam audio (mic) | ✅ `MediaRecorder` | ⛔ stub (belum diimplementasi) |
| Reminder/notifikasi | ✅ `AlarmManager` + `BroadcastReceiver` | ⛔ stub |
| Permission request | ✅ Activity Result API | ⛔ stub (selalu granted) |
| Dynamic color (Material You) | ✅ API 31+ | ⛔ static color scheme |

iOS belum punya implementasi native karena development environment saat ini tidak punya Xcode/Mac untuk build & test iOS. UI tetap compile dan tampil di iOS, tapi fitur-fitur di atas belum benar-benar berfungsi.

## Setup & Menjalankan

Butuh JDK 17+ dan Android SDK (lewat Android Studio atau `local.properties` yang menunjuk ke SDK path).

```bash
# Build & install APK debug Android
./gradlew :androidApp:assembleDebug

# Cek compile shared module saja (cepat, tanpa build APK)
./gradlew :shared:compileCommonMainKotlinMetadata

# Cek compile target iOS (tanpa run, karena perlu Mac/Xcode untuk run sungguhan)
./gradlew :shared:compileKotlinIosSimulatorArm64
```

APK debug hasil build ada di dua tempat (sama persis, beda nama file):
```
androidApp/build/outputs/apk/debug/androidApp-debug.apk        # nama default AGP
androidApp/build/outputs/apk-named/debug/MindCare-<versi>-debug.apk  # salinan dengan nama rapi, ikut MINDCARE_VERSION_NAME
```

Untuk iOS: buka folder [`/iosApp`](./iosApp) di Xcode dan run dari sana (perlu macOS).

### Menjalankan test

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```

## Backend

App ini mengonsumsi REST API (FastAPI, terpisah dari repo ini). Base URL diatur di `shared/src/commonMain/kotlin/com/example/mindcare/data/api/KtorClient.kt`.

## Versioning

Versi app disimpan di **satu sumber**: `gradle.properties` (`MINDCARE_VERSION_NAME` & `MINDCARE_VERSION_CODE`), dipakai otomatis oleh `androidApp/build.gradle.kts`.

Untuk rilis baru:
1. Bump `MINDCARE_VERSION_NAME` (semantic version, misal `1.0.0` → `1.1.0`) dan `MINDCARE_VERSION_CODE` (`+1`, harus selalu naik — dipakai Play Store untuk menentukan update mana yang lebih baru) di `gradle.properties`.
2. Cerminkan nilai yang sama secara manual ke `iosApp/Configuration/Config.xcconfig` (`MARKETING_VERSION` = `MINDCARE_VERSION_NAME`, `CURRENT_PROJECT_VERSION` = `MINDCARE_VERSION_CODE`) — Xcode tidak baca file Gradle, jadi ini belum bisa otomatis tersinkron tanpa build-phase script tambahan di Xcode.

## App Icon

Source asset icon launcher (resolusi tinggi, persegi) disimpan di `assets/icon-source/app-icon.png` — file ini bukan untuk dibundle ke app, cuma master untuk regenerasi ulang mipmap Android / AppIcon iOS kalau logo berubah.

---

Dibuat dengan [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html) & [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/).
