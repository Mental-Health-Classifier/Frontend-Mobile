package com.example.mindcare.platform

expect class AudioRecorder() {
    fun start(): Boolean
    fun stop(): ByteArray?
    fun cancel()
    fun getAmplitude(): Int
}
