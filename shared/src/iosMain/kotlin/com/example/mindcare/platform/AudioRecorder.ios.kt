package com.example.mindcare.platform

// Stub — rekam audio belum diimplementasikan di iOS (lihat plan migrasi).
actual class AudioRecorder actual constructor() {
    actual fun start(): Boolean = false
    actual fun stop(): ByteArray? = null
    actual fun cancel() {}
    actual fun getAmplitude(): Int = 0
}
